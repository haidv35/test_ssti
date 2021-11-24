package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.merge.DefaultItemComparator;
import com.vmware.cis.data.internal.provider.merge.OrderedSequenceMergePolicy;
import com.vmware.cis.data.internal.provider.merge.ResultMergePolicy;
import com.vmware.cis.data.internal.provider.merge.SequenceMergePolicy;
import com.vmware.cis.data.internal.provider.merge.UnorderedSequenceMergePolicy;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyByName;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FederationConnection implements DataProvider {
  public static final class FederatedProviderInfo {
    private final String _providerName;
    
    private final String _serviceInstanceUuid;
    
    private final String _serviceTypeAndVersion;
    
    private final DataProvider _dataProvider;
    
    public FederatedProviderInfo(String providerName, String serviceInstanceUuid, String serviceTypeAndVersion, DataProvider dataProvider) {
      Validate.notNull(serviceInstanceUuid, "Argument 'serviceInstanceUuid' is required");
      Validate.notNull(serviceTypeAndVersion, "Argument 'serviceTypeAndVersion' is required");
      Validate.notNull(dataProvider, "Argument 'dataProvider' is required");
      this._providerName = providerName;
      this._serviceInstanceUuid = serviceInstanceUuid;
      this._serviceTypeAndVersion = serviceTypeAndVersion;
      this._dataProvider = dataProvider;
    }
    
    public String getProviderName() {
      return this._providerName;
    }
    
    public String getServiceInstanceUuid() {
      return this._serviceInstanceUuid;
    }
    
    public String getServiceTypeAndVersion() {
      return this._serviceTypeAndVersion;
    }
    
    public DataProvider getDataProvider() {
      return this._dataProvider;
    }
    
    public String toString() {
      return this._providerName;
    }
  }
  
  private static final Logger logger = LoggerFactory.getLogger(FederationConnection.class);
  
  private final FederationQueryAdapter _queryAdapter;
  
  private final TaskExecutor _taskExecutor;
  
  private final Map<String, FederatedProviderInfo> _providerByServiceId;
  
  private final Collection<QuerySchema> _schemas;
  
  private FederationConnection(QueryRouter queryRouter, TaskExecutor taskExecutor, QuerySchemaCache schemaCache, Collection<FederatedProviderInfo> providerInfos) {
    assert queryRouter != null;
    assert taskExecutor != null;
    assert schemaCache != null;
    assert providerInfos != null;
    Map<String, QuerySchema> schemaByInstanceUuid = (new FailoverSchemaRetriever(taskExecutor, schemaCache)).getSchemasByInstanceUuid(providerInfos);
    this._queryAdapter = new FederationQueryAdapter(queryRouter, new FederationQuerySchemaLookup(schemaByInstanceUuid));
    this._schemas = schemaByInstanceUuid.values();
    this._taskExecutor = taskExecutor;
    this._providerByServiceId = new HashMap<>(providerInfos.size());
    for (FederatedProviderInfo providerInfo : providerInfos)
      this._providerByServiceId.put(providerInfo.getServiceInstanceUuid(), providerInfo); 
  }
  
  public static DataProvider create(QueryRouter queryRouter, ExecutorService executor, QuerySchemaCache schemaCache, Collection<FederatedProviderInfo> providerInfos) {
    assert queryRouter != null;
    assert executor != null;
    assert schemaCache != null;
    assert providerInfos != null;
    TaskExecutor taskExecutor = new TaskExecutor(executor, TaskExecutor.ErrorHandlingPolicy.LENIENT);
    return create(queryRouter, taskExecutor, schemaCache, providerInfos);
  }
  
  public static DataProvider create(QueryRouter queryRouter, TaskExecutor taskExecutor, QuerySchemaCache schemaCache, Collection<FederatedProviderInfo> providerInfos) {
    assert queryRouter != null;
    assert taskExecutor != null;
    assert schemaCache != null;
    assert providerInfos != null;
    if (providerInfos.size() == 1) {
      FederatedProviderInfo providerInfo = providerInfos.iterator().next();
      return new SingleNodeConnection(queryRouter, schemaCache, providerInfo);
    } 
    return new FederationConnection(queryRouter, taskExecutor, schemaCache, providerInfos);
  }
  
  public ResultSet executeQuery(Query query) {
    Query queryWithSelectedSortProps = adjustQueryForOrderedMerge(query);
    ResultSet resultWithSortProps = executeQueryWithSelectedSortProps(queryWithSelectedSortProps);
    if (queryWithSelectedSortProps == query)
      return resultWithSortProps; 
    return ResultSetUtil.project(resultWithSortProps, query.getProperties());
  }
  
  private ResultSet executeQueryWithSelectedSortProps(Query query) {
    ResultSet result;
    this._queryAdapter.validateQuery(query);
    Map<String, Query> queryByConnectionId = new HashMap<>();
    for (String targetInstanceId : this._providerByServiceId.keySet()) {
      Query routedQuery = this._queryAdapter.adaptQuery(query, targetInstanceId);
      if (routedQuery != null)
        queryByConnectionId.put(targetInstanceId, routedQuery); 
    } 
    boolean targetMultipleInstances = (queryByConnectionId.size() > 1);
    List<Callable<PartialResult>> partialResultTasks = new ArrayList<>(queryByConnectionId.size());
    for (Map.Entry<String, Query> entry : queryByConnectionId.entrySet()) {
      String connId = entry.getKey();
      FederatedProviderInfo provider = this._providerByServiceId.get(connId);
      assert provider != null;
      Query originalQuery = entry.getValue();
      Query federationQuery = adjustQueryForFederation(originalQuery, targetMultipleInstances);
      Callable<PartialResult> task = createQueryTask(originalQuery, federationQuery, provider);
      partialResultTasks.add(task);
    } 
    List<PartialResult> partialResults = this._taskExecutor.invokeTasks(partialResultTasks);
    if (partialResults.isEmpty()) {
      result = emptyResult(query.getProperties(), query.getWithTotalCount());
    } else {
      List<ResultSet> unifiedResults = partialResultsToUnifiedResultSets(partialResults);
      result = mergeAndApplyPaging(query, targetMultipleInstances, unifiedResults);
    } 
    return result;
  }
  
  private static Callable<PartialResult> createQueryTask(final Query originalQuery, final Query routedQuery, final FederatedProviderInfo providerInfo) {
    assert originalQuery != null;
    assert routedQuery != null;
    assert providerInfo != null;
    return new Callable<PartialResult>() {
        public FederationConnection.PartialResult call() throws Exception {
          DataProvider dataProvider = providerInfo.getDataProvider();
          String providerName = providerInfo.getProviderName();
          String instanceId = providerInfo.getServiceInstanceUuid();
          FederationConnection.logger.debug("Routing query to {}: {}", providerName, routedQuery);
          ResultSet partialResultSet = dataProvider.executeQuery(routedQuery);
          FederationConnection.logger.trace("Partial result received from {}: {}", providerName, partialResultSet);
          return new FederationConnection.PartialResult(originalQuery, instanceId, partialResultSet);
        }
        
        public String toString() {
          return String.format("federation query for %s executed on %s", new Object[] { this.val$routedQuery.getResourceModels(), this.val$providerInfo.getProviderName() });
        }
      };
  }
  
  public QuerySchema getSchema() {
    QuerySchema unionSchema = SchemaUtil.union(this._schemas);
    return addFilterableInstanceUuid(unionSchema);
  }
  
  public String toString() {
    return "Federation(" + 
      StringUtils.join(this._providerByServiceId.values(), ", ") + ")";
  }
  
  private static Query adjustQueryForFederation(Query original, boolean targetMultipleInstances) {
    Query modifiedQuery;
    if (original.getLimit() == 0)
      return original; 
    List<SortCriterion> modifiedSort = new ArrayList<>(original.getSortCriteria().size());
    for (SortCriterion sortCriterion : original.getSortCriteria()) {
      if (!PropertyUtil.isInstanceUuid(sortCriterion.getProperty()))
        modifiedSort.add(sortCriterion); 
    } 
    List<String> modifiedProperties = new ArrayList<>(original.getProperties().size());
    for (String property : original.getProperties()) {
      if (!PropertyUtil.isInstanceUuid(property))
        modifiedProperties.add(property); 
    } 
    boolean modifyQuery = (!original.getSortCriteria().equals(modifiedSort) || !original.getProperties().equals(modifiedProperties) || targetMultipleInstances);
    if (modifyQuery) {
      Query.Builder modifiedQueryBuilder = QueryCopy.copyAndSelect(original, modifiedProperties).orderBy(modifiedSort);
      if (modifiedSort.isEmpty()) {
        modifiedQueryBuilder = modifiedQueryBuilder.offset(0).limit(-1);
      } else if (targetMultipleInstances) {
        modifiedQueryBuilder = modifiedQueryBuilder.offset(0).limit(getLimitForFederation(original.getOffset(), original.getLimit()));
      } 
      modifiedQuery = modifiedQueryBuilder.build();
    } else {
      modifiedQuery = original;
    } 
    return modifiedQuery;
  }
  
  private static int getLimitForFederation(int originalOffset, int originalLimit) {
    if (originalLimit == 0)
      return 0; 
    if (originalLimit < 0)
      return -1; 
    long effectiveLimit = Math.min(2147483647L, originalOffset + originalLimit);
    return (int)effectiveLimit;
  }
  
  private static List<ResultSet> partialResultsToUnifiedResultSets(List<PartialResult> partialResults) {
    Set<String> allPropertiesSet = new LinkedHashSet<>();
    Set<String> instanceIdProperties = new LinkedHashSet<>();
    for (PartialResult partialResult : partialResults) {
      allPropertiesSet.addAll(partialResult.resultSet.getProperties());
      for (String originalProperty : partialResult.originalQuery.getProperties()) {
        if (PropertyUtil.isInstanceUuid(originalProperty)) {
          instanceIdProperties.add(originalProperty);
          allPropertiesSet.add(originalProperty);
        } 
      } 
    } 
    List<String> allProperties = new ArrayList<>(allPropertiesSet);
    List<ResultSet> unifiedResults = new ArrayList<>(partialResults.size());
    for (PartialResult partialResult : partialResults) {
      ResultSet partialResultSet = partialResult.resultSet;
      if (allProperties.equals(partialResultSet.getProperties())) {
        unifiedResults.add(partialResultSet);
        continue;
      } 
      ResultSet desiredResult = reorderAndAppendInstanceIdColumns(partialResultSet, allProperties, instanceIdProperties, partialResult.sourceInstanceUuid);
      unifiedResults.add(desiredResult);
    } 
    return unifiedResults;
  }
  
  private static ResultSet reorderAndAppendInstanceIdColumns(ResultSet resultSet, List<String> desiredProperties, Set<String> instanceIdProperties, String sourceInstanceUuid) {
    ResourceItemPropertyByName valueByProperty = new CombinedResourceItemPropertyByName(resultSet.getProperties(), instanceIdProperties, sourceInstanceUuid);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(desiredProperties);
    for (ResourceItem resultItem : resultSet.getItems()) {
      List<Object> reorderedValues = new ArrayList(desiredProperties.size());
      for (String property : desiredProperties) {
        Object value = valueByProperty.getValue(property, resultItem);
        reorderedValues.add(value);
      } 
      resultBuilder.item(resultItem.getKey(), reorderedValues);
    } 
    return resultBuilder.totalCount(resultSet.getTotalCount()).build();
  }
  
  private static ResultSet mergeAndApplyPaging(Query query, boolean targetMultipleInstances, List<ResultSet> partialResults) {
    SequenceMergePolicy<ResourceItem> mergePolicy;
    if (query.getSortCriteria().isEmpty()) {
      mergePolicy = new UnorderedSequenceMergePolicy<>();
    } else {
      List<String> properties = ((ResultSet)partialResults.get(0)).getProperties();
      Comparator<ResourceItem> comparator = new DefaultItemComparator(properties, query.getSortCriteria());
      mergePolicy = new OrderedSequenceMergePolicy<>(comparator);
    } 
    int offsetToApply = targetMultipleInstances ? query.getOffset() : 0;
    ResultMergePolicy resultMergePolicy = new ResultMergePolicy(mergePolicy);
    ResultSet result = resultMergePolicy.merge(partialResults, query.getWithTotalCount(), offsetToApply, query
        .getLimit());
    return result;
  }
  
  private static QuerySchema addFilterableInstanceUuid(QuerySchema baseSchema) {
    Map<String, QuerySchema.ModelInfo> enhancedSchema = new HashMap<>();
    for (Map.Entry<String, QuerySchema.ModelInfo> schemaEntry : baseSchema.getModels().entrySet()) {
      String modelName = schemaEntry.getKey();
      QuerySchema.ModelInfo modelInfo = schemaEntry.getValue();
      Map<String, QuerySchema.PropertyInfo> enhancedProperties = new HashMap<>(modelInfo.getProperties());
      enhancedProperties.put("@instanceUuid", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
      QuerySchema.ModelInfo enhancedModelInfo = new QuerySchema.ModelInfo(enhancedProperties);
      enhancedSchema.put(modelName, enhancedModelInfo);
    } 
    return QuerySchema.forModels(enhancedSchema);
  }
  
  private static Query adjustQueryForOrderedMerge(Query query) {
    if (query.getSortCriteria().isEmpty())
      return query; 
    Set<String> selected = new HashSet<>(query.getProperties());
    List<String> newSelect = new ArrayList<>();
    for (SortCriterion sort : query.getSortCriteria()) {
      if (!selected.contains(sort.getProperty()))
        newSelect.add(sort.getProperty()); 
    } 
    if (newSelect.isEmpty())
      return query; 
    newSelect.addAll(query.getProperties());
    Query modified = QueryCopy.copyAndSelect(query, newSelect).build();
    return modified;
  }
  
  private static ResultSet emptyResult(List<String> properties, boolean withTotalCount) {
    return ResultSet.Builder.properties(properties)
      .totalCount(withTotalCount ? Integer.valueOf(0) : null)
      .build();
  }
  
  private static final class PartialResult {
    public final Query originalQuery;
    
    public final String sourceInstanceUuid;
    
    public final ResultSet resultSet;
    
    public PartialResult(Query originalQuery, String sourceInstanceUuid, ResultSet resultSet) {
      this.originalQuery = originalQuery;
      this.sourceInstanceUuid = sourceInstanceUuid;
      this.resultSet = resultSet;
    }
  }
  
  private static final class CombinedResourceItemPropertyByName implements ResourceItemPropertyByName {
    private final List<String> _nativeProperties;
    
    private final Set<String> _instanceIdProperties;
    
    private final String _sourceInstanceUuid;
    
    public CombinedResourceItemPropertyByName(List<String> nativeProperties, Set<String> instanceIdProperties, String sourceInstanceUuid) {
      this._nativeProperties = nativeProperties;
      this._instanceIdProperties = instanceIdProperties;
      this._sourceInstanceUuid = sourceInstanceUuid;
    }
    
    public Object getValue(String propertyName, ResourceItem item) {
      if (this._instanceIdProperties.contains(propertyName))
        return this._sourceInstanceUuid; 
      int propIdx = this._nativeProperties.indexOf(propertyName);
      if (propIdx >= 0 && propIdx < item.getPropertyValues().size())
        return item.getPropertyValues().get(propIdx); 
      return null;
    }
  }
  
  private static class SingleNodeConnection implements DataProvider {
    private final QueryRouter _queryRouter;
    
    private final DataProvider _provider;
    
    private final String _serviceInstanceUuid;
    
    private final QuerySchemaCache _schemaCache;
    
    private final String _schemaCacheKey;
    
    private final Callable<QuerySchema> _schemaGet;
    
    public SingleNodeConnection(QueryRouter queryRouter, QuerySchemaCache schemaCache, FederationConnection.FederatedProviderInfo providerInfo) {
      this._queryRouter = queryRouter;
      this._provider = providerInfo.getDataProvider();
      this._serviceInstanceUuid = providerInfo.getServiceInstanceUuid();
      this._schemaCache = schemaCache;
      this._schemaCacheKey = providerInfo.getServiceTypeAndVersion();
      this._schemaGet = new Callable<QuerySchema>() {
          public QuerySchema call() {
            QuerySchema baseSchema = FederationConnection.SingleNodeConnection.this._provider.getSchema();
            return FederationConnection.addFilterableInstanceUuid(baseSchema);
          }
        };
    }
    
    public ResultSet executeQuery(Query query) {
      Query routedQuery = this._queryRouter.route(query, this._serviceInstanceUuid);
      if (routedQuery == null)
        return FederationConnection.emptyResult(query.getProperties(), query.getWithTotalCount()); 
      Query adaptedQuery = FederationConnection.adjustQueryForFederation(routedQuery, false);
      ResultSet rawResult = this._provider.executeQuery(adaptedQuery);
      Set<String> instanceIdProperties = gatherInstanceIdProperties(query.getProperties());
      if (instanceIdProperties.isEmpty())
        return rawResult; 
      ResultSet result = FederationConnection.reorderAndAppendInstanceIdColumns(rawResult, query
          .getProperties(), instanceIdProperties, this._serviceInstanceUuid);
      return result;
    }
    
    private static Set<String> gatherInstanceIdProperties(List<String> properties) {
      Set<String> instanceIdProperties = new HashSet<>();
      for (String property : properties) {
        if (PropertyUtil.isInstanceUuid(property))
          instanceIdProperties.add(property); 
      } 
      return instanceIdProperties;
    }
    
    public QuerySchema getSchema() {
      return this._schemaCache.get(this._schemaCacheKey, this._schemaGet);
    }
    
    public String toString() {
      return this._provider.toString();
    }
  }
}
