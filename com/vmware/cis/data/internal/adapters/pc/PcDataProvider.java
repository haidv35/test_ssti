package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.filter.FilterEvaluator;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.impl.vmodl.TypeNameImpl;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vmodl.DynamicProperty;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.TypeName;
import com.vmware.vim.binding.vmodl.fault.ManagedObjectNotFound;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PcDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PcDataProvider.class);
  
  private static final String SERVICE_INSTANCE = "ServiceInstance";
  
  private static final Integer MAX_OBJECTS = Integer.valueOf(250);
  
  private static final PropertyCollector.RetrieveOptions OPTIONS = new PropertyCollector.RetrieveOptions();
  
  private static final String TYPE_FOLDER = "Folder";
  
  private static final String ROOT_FOLDER_PROPERTY = "@rootFolder";
  
  private final PropertyCollector _pc;
  
  private final QuerySchema _pcSchema;
  
  private final ManagedObjectReference _rootFolder;
  
  static {
    OPTIONS.maxObjects = MAX_OBJECTS;
  }
  
  public PcDataProvider(Client vlsiClient) {
    Validate.notNull(vlsiClient, "VLSI client");
    ManagedObjectReference siMor = new ManagedObjectReference("ServiceInstance", "ServiceInstance");
    ServiceInstance siStub = createStub(vlsiClient, ServiceInstance.class, siMor);
    ServiceInstanceContent sic = siStub.getContent();
    this._rootFolder = sic.getRootFolder();
    this._pc = createPropertyCollector(vlsiClient, siStub.getContent());
    this._pcSchema = PropertyFileQuerySchemaParser.parseSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    QueryAnalyzer.QueryAnalysis analysis = QueryAnalyzer.analyzeQuery(query);
    ResultWrapper allEntitiesResult = retrieveEntities(analysis);
    allEntitiesResult = filterModels(allEntitiesResult, query.getResourceModels());
    addForeignKeys(allEntitiesResult, analysis);
    List<ResultWrapper.ItemValueMap> items = extractItemValues(allEntitiesResult);
    orderItems(items, analysis);
    ResultSet resultSet = createResponse(items, query, analysis);
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    return this._pcSchema;
  }
  
  private ResultWrapper filterModels(ResultWrapper originalResults, Collection<String> models) {
    ResultWrapper resultWrapper = new ResultWrapper();
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : originalResults.entrySet()) {
      ManagedObjectReference key = entry.getKey();
      if (models.contains(key.getType()))
        resultWrapper.add(key, entry.getValue()); 
    } 
    return resultWrapper;
  }
  
  private ResultSet createResponse(List<ResultWrapper.ItemValueMap> results, Query query, QueryAnalyzer.QueryAnalysis analysis) {
    int limit = query.getLimit();
    if (limit == 0)
      return ResultSet.Builder.properties(new String[0]).totalCount(Integer.valueOf(results.size())).build(); 
    if (limit < 0)
      limit = results.size(); 
    int offset = query.getOffset();
    int last = offset + limit;
    if (last > results.size())
      last = results.size(); 
    Map<String, String> originalByUnqualifiedProperties = analysis.getOriginalByUnqualifiedProperties();
    ResultSet.Builder builder = ResultSet.Builder.properties(query.getProperties());
    for (int i = offset; i < last; i++) {
      List<Object> values = new ArrayList();
      ResultWrapper.ItemValueMap itemValueMap = results.get(i);
      for (String property : query.getProperties()) {
        String unqualifiedProperty = originalByUnqualifiedProperties.get(property);
        values.add(itemValueMap.getValue(unqualifiedProperty));
      } 
      Object key = itemValueMap.getValue("@modelKey");
      builder.item(key, values);
    } 
    if (query.getWithTotalCount())
      builder.totalCount(Integer.valueOf(results.size())); 
    return builder.build();
  }
  
  private List<ResultWrapper.ItemValueMap> extractItemValues(ResultWrapper resultWrapper) {
    List<ResultWrapper.ItemValueMap> results = new ArrayList<>();
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : resultWrapper.entrySet())
      results.add(entry.getValue()); 
    return results;
  }
  
  private void orderItems(List<ResultWrapper.ItemValueMap> itemValues, QueryAnalyzer.QueryAnalysis analysis) {
    if (analysis.getSortDirection() == null)
      return; 
    final int multiplier = analysis.getSortDirection().equals(SortCriterion.SortDirection.ASCENDING) ? 1 : -1;
    Collections.sort(itemValues, new Comparator<ResultWrapper.ItemValueMap>() {
          public int compare(ResultWrapper.ItemValueMap o1, ResultWrapper.ItemValueMap o2) {
            ManagedObjectReference mor1 = o1.<ManagedObjectReference>get("@modelKey");
            ManagedObjectReference mor2 = o2.<ManagedObjectReference>get("@modelKey");
            return multiplier * mor1.getValue().compareTo(mor2.getValue());
          }
        });
  }
  
  private ResultWrapper retrieveEntities(QueryAnalyzer.QueryAnalysis analysis) {
    if (analysis.getClientSidePredicates().isEmpty() && analysis
      .getMorPredicates().isEmpty())
      return retrieveEntitiesByModel(analysis); 
    if (analysis.getClientSidePredicates().isEmpty())
      return retrieveEntitiesByKey(analysis); 
    if (analysis.getMorPredicates().isEmpty()) {
      ResultWrapper allEntities = retrieveEntitiesByModel(analysis);
      return filter(allEntities, analysis);
    } 
    ResultWrapper traversalResults = retrieveEntitiesByKey(analysis);
    if (analysis.isIntersection())
      return filter(traversalResults, analysis); 
    ResultWrapper allEntitiesOfModel = retrieveEntitiesByModel(analysis);
    ResultWrapper filteredEntities = filter(allEntitiesOfModel, analysis);
    traversalResults.union(filteredEntities);
    return traversalResults;
  }
  
  private ResultWrapper retrieveEntitiesByKey(QueryAnalyzer.QueryAnalysis analysis) {
    ResultWrapper resultWrapper = null;
    PropertyCollector.PropertySpec propSepc = createPropertySpec(analysis.getModel(), analysis.getNativeProperties());
    for (PropertyPredicate predicate : analysis.getMorPredicates()) {
      String property = predicate.getProperty();
      PropertyRegistry.PropertyDefinition definition = PropertyRegistry.getPropertyDefinition(analysis.getModel(), property);
      PropertyCollector.FilterSpec filterSpec = new PropertyCollector.FilterSpec();
      filterSpec.setPropSet(new PropertyCollector.PropertySpec[] { propSepc });
      List<PropertyCollector.ObjectSpec> objectSpecs = new ArrayList<>();
      if (predicate.getOperator().equals(PropertyPredicate.ComparisonOperator.EQUAL)) {
        ManagedObjectReference mor = (ManagedObjectReference)predicate.getComparableValue();
        objectSpecs.add(createObjectSpec(mor, definition
              .getFilterSpecForPredicate(), true));
      } else {
        Collection<ManagedObjectReference> mors = (Collection<ManagedObjectReference>)predicate.getComparableValue();
        for (ManagedObjectReference mor : mors)
          objectSpecs.add(createObjectSpec(mor, definition
                .getFilterSpecForPredicate(), true)); 
      } 
      filterSpec.setObjectSet(objectSpecs.<PropertyCollector.ObjectSpec>toArray(new PropertyCollector.ObjectSpec[0]));
      filterSpec.setReportMissingObjectsInResults(Boolean.valueOf(true));
      Collection<PropertyCollector.RetrieveResult> retrieveResults = fetchFromPC(filterSpec);
      ResultWrapper partialResult = convertToWrapper(retrieveResults, analysis);
      if (resultWrapper == null) {
        resultWrapper = partialResult;
        continue;
      } 
      if (analysis.isIntersection()) {
        resultWrapper.intersection(partialResult);
        continue;
      } 
      resultWrapper.union(partialResult);
    } 
    return resultWrapper;
  }
  
  private ResultWrapper retrieveEntitiesByModel(QueryAnalyzer.QueryAnalysis analysis) {
    PropertyCollector.PropertySpec propSepc = createPropertySpec(analysis.getModel(), analysis.getNativeProperties());
    PropertyRegistry.PropertyDefinition definition = PropertyRegistry.getPropertyDefinition(analysis
        .getModel(), "@rootFolder");
    if (definition.getFilterSpecForPredicate() == null)
      throw new IllegalArgumentException("Cannot select all '" + analysis
          .getModel() + "' entities"); 
    PropertyCollector.FilterSpec filterSpec = new PropertyCollector.FilterSpec();
    filterSpec.setPropSet(new PropertyCollector.PropertySpec[] { propSepc });
    PropertyCollector.ObjectSpec objectSpec = createObjectSpec(this._rootFolder, definition
        .getFilterSpecForPredicate(), true);
    objectSpec.setSkip(Boolean.valueOf(!analysis.getModel().equals("Folder")));
    filterSpec.setObjectSet(new PropertyCollector.ObjectSpec[] { objectSpec });
    filterSpec.setReportMissingObjectsInResults(Boolean.valueOf(true));
    Collection<PropertyCollector.RetrieveResult> retrieveResults = fetchFromPC(filterSpec);
    return convertToWrapper(retrieveResults, analysis);
  }
  
  private ResultWrapper filter(ResultWrapper filterOnEntities, QueryAnalyzer.QueryAnalysis analysis) {
    ResultWrapper result = new ResultWrapper();
    Filter filter = new Filter(analysis.getClientSidePredicates(), analysis.isIntersection() ? LogicalOperator.AND : LogicalOperator.OR);
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : filterOnEntities.entrySet()) {
      boolean valid = FilterEvaluator.eval(filter, entry.getValue());
      if (valid)
        result.add(entry.getKey(), entry.getValue()); 
    } 
    return result;
  }
  
  private PropertyCollector.ObjectSpec createObjectSpec(ManagedObjectReference mor, PropertyCollector.SelectionSpec[] traversals, boolean skipTraversalInResult) {
    PropertyCollector.ObjectSpec objectSpec = new PropertyCollector.ObjectSpec();
    objectSpec.setDynamicType(mor.getType());
    objectSpec.setObj(mor);
    if (traversals != null) {
      objectSpec.setSelectSet(traversals);
      objectSpec.setSkip(Boolean.valueOf(skipTraversalInResult));
    } 
    return objectSpec;
  }
  
  private PropertyCollector.PropertySpec createPropertySpec(String model, Collection<String> properties) {
    List<String> convertedProperties = new ArrayList<>(properties.size());
    for (String property : properties) {
      if (PropertyUtil.isModelKey(property))
        continue; 
      convertedProperties.add(convertPropertyToPCFormat(property));
    } 
    PropertyCollector.PropertySpec propSpec = new PropertyCollector.PropertySpec();
    propSpec.setType((TypeName)new TypeNameImpl(model));
    propSpec.setAll(Boolean.valueOf(false));
    propSpec.setPathSet(convertedProperties.<String>toArray(new String[0]));
    return propSpec;
  }
  
  private ResultWrapper convertToWrapper(Collection<PropertyCollector.RetrieveResult> retrieveResults, QueryAnalyzer.QueryAnalysis analysis) {
    ResultWrapper resultWrapper = new ResultWrapper();
    for (PropertyCollector.RetrieveResult retrieveResult : retrieveResults)
      convertToWrapper(resultWrapper, retrieveResult); 
    if (analysis.getFormulas().isEmpty())
      return resultWrapper; 
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : resultWrapper.entrySet())
      addVirtualProperties(entry.getValue(), analysis); 
    return resultWrapper;
  }
  
  private void convertToWrapper(ResultWrapper resultWrapper, PropertyCollector.RetrieveResult retrieveResult) {
    if (retrieveResult.objects == null)
      return; 
    for (PropertyCollector.ObjectContent content : retrieveResult.getObjects()) {
      ResultWrapper.ItemValueMap itemValueMap = new ResultWrapper.ItemValueMap();
      ManagedObjectReference mor = content.obj;
      itemValueMap.add("@modelKey", mor);
      itemValueMap.add("@type", mor.getType());
      resultWrapper.add(mor, itemValueMap);
      if (content.getPropSet() != null)
        for (DynamicProperty property : content.getPropSet())
          itemValueMap.add(convertPropertyToDSFormat(property.getName()), property
              .getVal());  
    } 
  }
  
  private void addVirtualProperties(ResultWrapper.ItemValueMap item, QueryAnalyzer.QueryAnalysis analysis) {
    for (Map.Entry<String, FormulaRegistry.Formula> entry : analysis.getFormulas().entrySet()) {
      Object virtualValue = ((FormulaRegistry.Formula)entry.getValue()).computeValue(item);
      item.add(entry.getKey(), virtualValue);
    } 
  }
  
  private void addForeignKeys(ResultWrapper resultWrapper, QueryAnalyzer.QueryAnalysis analysis) {
    if (analysis.getForeignKeyProperties().isEmpty())
      return; 
    String model = analysis.getModel();
    for (String property : analysis.getForeignKeyProperties())
      addForeignKeys(resultWrapper, model, property); 
  }
  
  private void addForeignKeys(ResultWrapper resultWrapper, String model, String property) {
    PropertyRegistry.PropertyDefinition definition = PropertyRegistry.getPropertyDefinition(model, property);
    PropertyCollector.SelectionSpec[] selectionSpec = definition.getFilterSpecForSelect();
    PropertyCollector.PropertySpec[] pSpecs = definition.getPropertySpecForSelect();
    PropertyCollector.FilterSpec filterSpec = new PropertyCollector.FilterSpec();
    filterSpec.setPropSet(pSpecs);
    Collection<PropertyCollector.ObjectSpec> objectSpecs = new ArrayList<>();
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : resultWrapper.entrySet()) {
      PropertyCollector.ObjectSpec objectSpec = createObjectSpec(entry.getKey(), selectionSpec, false);
      objectSpecs.add(objectSpec);
    } 
    filterSpec.setObjectSet(objectSpecs.<PropertyCollector.ObjectSpec>toArray(new PropertyCollector.ObjectSpec[0]));
    filterSpec.setReportMissingObjectsInResults(Boolean.valueOf(true));
    Collection<PropertyCollector.RetrieveResult> retrieveResults = fetchFromPC(filterSpec);
    addForeignKeys(resultWrapper, property, retrieveResults, definition
        .getForeignKeyModels());
  }
  
  private void addForeignKeys(ResultWrapper resultWrapper, String property, Collection<PropertyCollector.RetrieveResult> retrieveResults, Collection<String> models) {
    Map<ManagedObjectReference, ManagedObjectReference> traversalMap = getTraversalMap(retrieveResults);
    for (Map.Entry<ManagedObjectReference, ResultWrapper.ItemValueMap> entry : resultWrapper.entrySet()) {
      ManagedObjectReference entity = entry.getKey();
      ManagedObjectReference foreignKey = getForeignKey(traversalMap, entity, models);
      ((ResultWrapper.ItemValueMap)entry.getValue()).add(property, foreignKey);
    } 
  }
  
  private ManagedObjectReference getForeignKey(Map<ManagedObjectReference, ManagedObjectReference> traversalMap, ManagedObjectReference key, Collection<String> foreignModels) {
    ManagedObjectReference startingKey = key;
    int allowedTraversals = traversalMap.size();
    while (traversalMap.containsKey(key)) {
      key = traversalMap.get(key);
      if (allowedTraversals-- < 0) {
        String message = String.format("Traversal hierarchy %s contains a loop starting from %s in search of models %s", new Object[] { traversalMap, startingKey, foreignModels });
        throw new RuntimeException(message);
      } 
    } 
    if (!foreignModels.contains(key.getType()))
      return null; 
    return key;
  }
  
  private Map<ManagedObjectReference, ManagedObjectReference> getTraversalMap(Collection<PropertyCollector.RetrieveResult> retrieveResults) {
    Map<ManagedObjectReference, ManagedObjectReference> traversalMap = new LinkedHashMap<>();
    for (PropertyCollector.RetrieveResult retrieveResult : retrieveResults) {
      if (retrieveResult == null || retrieveResult.getObjects() == null)
        continue; 
      for (PropertyCollector.ObjectContent objectContent : retrieveResult.getObjects()) {
        ManagedObjectReference mor = objectContent.getObj();
        ManagedObjectReference next = getNextInChain(objectContent);
        if (next != null)
          traversalMap.put(mor, next); 
      } 
    } 
    return traversalMap;
  }
  
  private ManagedObjectReference getNextInChain(PropertyCollector.ObjectContent objectContent) {
    if (objectContent.getPropSet() == null)
      return null; 
    if ((objectContent.getPropSet()).length == 0)
      return null; 
    if ((objectContent.getPropSet()).length >= 2)
      throw new RuntimeException("Too many properties retrieved by traversal"); 
    DynamicProperty property = objectContent.getPropSet()[0];
    Object value = property.getVal();
    if (value instanceof ManagedObjectReference)
      return (ManagedObjectReference)value; 
    String message = String.format("Expected foreign key '%s' of entity %s is of type '%s'", new Object[] { property
          .getName(), objectContent.getObj().toString(), value
          .getClass().getName() });
    throw new RuntimeException(message);
  }
  
  private Collection<PropertyCollector.RetrieveResult> fetchFromPC(PropertyCollector.FilterSpec filterSpec) {
    List<PropertyCollector.RetrieveResult> results = new ArrayList<>();
    String currentResultToken = null;
    try {
      _logger.debug("Querying property collector with filterSpec {}", filterSpec);
      PropertyCollector.RetrieveResult currentResult = this._pc.retrievePropertiesEx(new PropertyCollector.FilterSpec[] { filterSpec }, OPTIONS);
      if (currentResult == null)
        return results; 
      results.add(currentResult);
      currentResultToken = currentResult.getToken();
      while (currentResultToken != null) {
        currentResult = this._pc.continueRetrievePropertiesEx(currentResultToken);
        if (currentResult == null)
          break; 
        results.add(currentResult);
        currentResultToken = currentResult.getToken();
      } 
      return results;
    } catch (ManagedObjectNotFound e) {
      _logger.debug("A query requested a missing object", (Throwable)e);
      return results;
    } catch (InvalidProperty e) {
      throw new IllegalArgumentException("Invalid property " + e.getName(), e);
    } finally {
      if (currentResultToken != null)
        try {
          this._pc.cancelRetrievePropertiesEx(currentResultToken);
        } catch (InvalidProperty e) {
          _logger.warn("Call to cancelRetrievePropertiesEx failed.", (Throwable)e);
        }  
    } 
  }
  
  private String convertPropertyToPCFormat(String property) {
    return property.replace('/', '.');
  }
  
  private String convertPropertyToDSFormat(String property) {
    return property.replace('.', '/');
  }
  
  private static PropertyCollector createPropertyCollector(Client vlsiClient, ServiceInstanceContent serviceInstanceContent) {
    assert vlsiClient != null;
    assert serviceInstanceContent != null;
    try {
      PropertyCollector rootPC = createStub(vlsiClient, PropertyCollector.class, serviceInstanceContent
          .getPropertyCollector());
      return rootPC;
    } catch (Exception e) {
      throw new IllegalStateException("Error has occurred while preparing PropertyCollector.", e);
    } 
  }
  
  private static <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(@Nonnull Client vlsiClient, @Nonnull Class<T> stubBindingClass, @Nonnull ManagedObjectReference ref) {
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(PcDataProvider.class
        .getClassLoader());
    try {
      return (T)vlsiClient.createStub(stubBindingClass, ref);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
}
