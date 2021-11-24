package com.vmware.cis.data.internal.provider.property;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.PropertyRetriever;
import com.vmware.cis.data.internal.provider.ProviderBySchemaLookup;
import com.vmware.cis.data.internal.provider.ProviderRepository;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.provider.QueryExecutor;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelProviderConnection;
import com.vmware.cis.data.internal.provider.ext.aggregated.DefaultAggregatedModels;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyProviders implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PropertyProviders.class);
  
  private static final ModelKeyFilter KEY_FILTER = new ModelKeyFilter() {
      public boolean accept(Object key, String model) {
        if (key instanceof ManagedObjectReference) {
          ManagedObjectReference ref = (ManagedObjectReference)key;
          return model.equals(ref.getType());
        } 
        return true;
      }
    };
  
  private final DataProvider _dataProvider;
  
  private final ProviderBySchemaLookup _providerLookup;
  
  private final PropertyRetriever _propertyRetriever;
  
  private final Set<String> _supportedProperties;
  
  public static DataProvider forBeans(Collection<?> propertyProviderBeans, DataProvider dataProvider, ExecutorService executor, long timeLimitMs) {
    assert propertyProviderBeans != null;
    assert dataProvider != null;
    assert executor != null;
    assert timeLimitMs > 0L;
    if (propertyProviderBeans.isEmpty()) {
      _logger.info("No property providers found");
      return dataProvider;
    } 
    DataProvider raw = PropertyProviderBeansDataProvider.toDataProvider(propertyProviderBeans, executor, timeLimitMs, KEY_FILTER);
    if (raw == null) {
      _logger.info("No property provider methods found within the {} registered property provider beans", 
          
          Integer.valueOf(propertyProviderBeans.size()));
      return dataProvider;
    } 
    DataProvider provider = new AggregatedModelProviderConnection(raw, DefaultAggregatedModels.getModelLookup(), executor);
    ProviderBySchemaLookup providerLookup = ProviderRepository.forProviders(Collections.singleton(provider));
    QueryClauseAnalyzer clauseAnalyzer = new QueryClauseAnalyzer(providerLookup);
    QueryExecutor queryExecutor = new QueryExecutor(executor);
    PropertyRetriever propertyRetriever = new PropertyRetriever(clauseAnalyzer, queryExecutor);
    QuerySchema schema = providerLookup.getSchema();
    Set<String> supportedProperties = new LinkedHashSet<>();
    for (Map.Entry<String, QuerySchema.ModelInfo> e : schema.getModels().entrySet()) {
      for (String simpleProperty : ((QuerySchema.ModelInfo)e.getValue()).getProperties().keySet()) {
        if (PropertyUtil.isSpecialProperty(simpleProperty))
          continue; 
        supportedProperties.add(
            QualifiedProperty.forModelAndSimpleProperty(e.getKey(), simpleProperty)
            .toString());
      } 
    } 
    _logger.debug("Properties supported by property providers: {}", supportedProperties);
    return new PropertyProviders(dataProvider, providerLookup, propertyRetriever, supportedProperties);
  }
  
  private PropertyProviders(DataProvider dataProvider, ProviderBySchemaLookup providerLookup, PropertyRetriever propertyRetriever, Set<String> supportedProperties) {
    assert dataProvider != null;
    assert providerLookup != null;
    assert propertyRetriever != null;
    assert supportedProperties != null;
    this._dataProvider = dataProvider;
    this._providerLookup = providerLookup;
    this._propertyRetriever = propertyRetriever;
    this._supportedProperties = Collections.unmodifiableSet(supportedProperties);
  }
  
  public ResultSet executeQuery(Query query) {
    validateQuery(query);
    if (skip(query)) {
      _logger.trace("Skip query because it contains no properties from property providers: {}", query);
      return this._dataProvider.executeQuery(query);
    } 
    _logger.trace("Query with properties from property providers: {}", query);
    Query rawQuery = QueryCopy.copyAndSelect(query, adaptSelect(query.getProperties())).build();
    _logger.trace("Query without property providers: {}", rawQuery);
    ResultSet rawResult = this._dataProvider.executeQuery(rawQuery);
    _logger.trace("Result without property providers: {}", rawResult);
    if (rawResult.getItems().isEmpty())
      return rawResult; 
    assert rawResult.getProperties().size() == rawQuery.getProperties().size();
    ResultSet result = this._propertyRetriever.gatherRemainingProperties(query, rawResult);
    _logger.trace("Result with properties from property providers: {}", result);
    return result;
  }
  
  public QuerySchema getSchema() {
    return SchemaUtil.merge(this._dataProvider.getSchema(), this._providerLookup
        .getSchema());
  }
  
  public Collection<String> getSupportedProperties() {
    return this._supportedProperties;
  }
  
  private boolean skip(Query query) {
    assert query != null;
    for (String property : query.getProperties()) {
      if (this._supportedProperties.contains(property))
        return false; 
    } 
    return true;
  }
  
  private void validateQuery(Query query) {
    Validate.notNull(query, "Query must not be null");
    for (SortCriterion sort : query.getSortCriteria()) {
      if (this._supportedProperties.contains(sort.getProperty()))
        throw new IllegalArgumentException("Cannot order by property from property provider: " + sort
            
            .getProperty()); 
    } 
    if (query.getFilter() == null)
      return; 
    for (PropertyPredicate predicate : query.getFilter().getCriteria()) {
      if (this._supportedProperties.contains(predicate.getProperty()))
        throw new IllegalArgumentException("Cannot filter by property from property provider: " + predicate
            
            .getProperty()); 
    } 
  }
  
  private List<String> adaptSelect(List<String> properties) {
    assert properties != null;
    assert !properties.isEmpty();
    List<String> executableSelect = new ArrayList<>(properties.size() + 1);
    if (!properties.contains("@modelKey"))
      executableSelect.add("@modelKey"); 
    for (String property : properties) {
      if (!this._supportedProperties.contains(property))
        executableSelect.add(property); 
    } 
    return executableSelect;
  }
}
