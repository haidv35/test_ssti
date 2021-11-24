package com.vmware.cis.data.internal.provider.property;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.PropertyRetriever;
import com.vmware.cis.data.internal.provider.ProviderBySchemaLookup;
import com.vmware.cis.data.internal.provider.ProviderRepository;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.provider.QueryExecutor;
import com.vmware.cis.data.internal.provider.profiler.OperationThresholdDataProvider;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyProviderBeansDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PropertyProviderBeansDataProvider.class);
  
  private final ProviderBySchemaLookup _providerLookup;
  
  private final PropertyRetriever _propertyRetriever;
  
  private final ModelKeyFilter _keyFilter;
  
  public static PropertyProviderBeansDataProvider toDataProvider(Collection<?> propertyProviderBeans, ExecutorService executor, long timeLimitMs, ModelKeyFilter keyFilter) {
    assert propertyProviderBeans != null;
    assert executor != null;
    assert timeLimitMs > 0L;
    assert keyFilter != null;
    List<DataProvider> providers = new ArrayList<>(propertyProviderBeans.size());
    for (Object propertyProviderBean : propertyProviderBeans) {
      DataProvider adapted = PropertyProviderBeanDataProvider.toDataProvider(propertyProviderBean);
      if (adapted == null)
        continue; 
      DataProvider provider = new OperationThresholdDataProvider(adapted, timeLimitMs);
      providers.add(provider);
    } 
    if (providers.isEmpty())
      return null; 
    ProviderBySchemaLookup providerLookup = ProviderRepository.forProviders(providers);
    QueryClauseAnalyzer clauseAnalyzer = new QueryClauseAnalyzer(providerLookup);
    QueryExecutor queryExecutor = new QueryExecutor(executor);
    PropertyRetriever propertyRetriever = new PropertyRetriever(clauseAnalyzer, queryExecutor);
    return new PropertyProviderBeansDataProvider(providerLookup, propertyRetriever, keyFilter);
  }
  
  private PropertyProviderBeansDataProvider(ProviderBySchemaLookup providerLookup, PropertyRetriever propertyRetriever, ModelKeyFilter keyFilter) {
    assert providerLookup != null;
    assert propertyRetriever != null;
    assert keyFilter != null;
    this._providerLookup = providerLookup;
    this._propertyRetriever = propertyRetriever;
    this._keyFilter = keyFilter;
  }
  
  public ResultSet executeQuery(Query query) {
    verifyQuery(query);
    _logger.trace("Query for property providers: {}", query);
    Collection<?> rawKeys = PropertyProviderBeanDataProvider.getKeys(query
        .getFilter());
    String model = query.getResourceModels().iterator().next();
    Collection<?> keys = filterKeys(rawKeys, model);
    _logger.trace("Keys applicable to model '{}': {}", model, keys);
    if (keys.isEmpty())
      return ResultSet.Builder.properties(query.getProperties())
        .totalCount(query.getWithTotalCount() ? Integer.valueOf(0) : null)
        .build(); 
    ResultSet result = this._propertyRetriever.gatherRemainingProperties(query, 
        asResultSet(keys));
    _logger.trace("Result of query for property providers: {}", result);
    return result;
  }
  
  public QuerySchema getSchema() {
    return this._providerLookup.getSchema();
  }
  
  private static void verifyQuery(Query query) {
    assert query != null;
    assert !query.getWithTotalCount();
    assert !query.getProperties().isEmpty();
    assert query.getResourceModels().size() == 1;
    Filter filter = query.getFilter();
    assert filter != null;
    assert filter.getCriteria().size() == 1;
    PropertyPredicate predicate = filter.getCriteria().get(0);
    assert predicate.getProperty().equals("@modelKey");
    assert predicate.getOperator().equals(PropertyPredicate.ComparisonOperator.EQUAL) || predicate
      .getOperator().equals(PropertyPredicate.ComparisonOperator.IN);
  }
  
  private Collection<?> filterKeys(Collection<?> keys, String model) {
    assert keys != null;
    assert model != null;
    if (keys.isEmpty())
      return keys; 
    List<Object> filtered = new ArrayList(keys.size());
    for (Object key : keys) {
      if (this._keyFilter.accept(key, model))
        filtered.add(key); 
    } 
    return filtered;
  }
  
  private static ResultSet asResultSet(Collection<?> keys) {
    assert keys != null;
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(new String[] { "@modelKey" });
    for (Object key : keys) {
      resultBuilder.item(key, new Object[] { key });
    } 
    return resultBuilder.build();
  }
}
