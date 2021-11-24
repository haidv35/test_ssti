package com.vmware.cis.data.internal.provider.ext.aggregated;

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
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AggregatedModelProviderConnection implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(AggregatedModelProviderConnection.class);
  
  private final DataProvider _connection;
  
  private final QuerySchema _schema;
  
  private final AggregatedModelLookup _aggregatedModelLookup;
  
  private final AggregatedModelQueryExecutor _queryExecutor;
  
  public AggregatedModelProviderConnection(DataProvider connection, AggregatedModelLookup aggregatedModelLookup, ExecutorService executor) {
    assert connection != null;
    assert aggregatedModelLookup != null;
    assert executor != null;
    this._connection = connection;
    this._schema = connection.getSchema();
    this._aggregatedModelLookup = aggregatedModelLookup;
    this._queryExecutor = new AggregatedModelQueryExecutor(connection, executor);
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query, "Query is null");
    Set<String> aggregatedModels = getAggregatedModels(query.getResourceModels());
    if (aggregatedModels.isEmpty()) {
      _logger.trace("Skipping query because it has no aggregated models: {}", query
          
          .getResourceModels());
      return this._connection.executeQuery(query);
    } 
    validateAtMostOneAggregatedModel(aggregatedModels);
    String aggregatedModel = aggregatedModels.iterator().next();
    Set<String> childModels = getChildModels(aggregatedModel);
    validateAggregatedIsTheOnlyModel(query.getResourceModels(), aggregatedModel);
    _logger.debug("Query for aggregated model '{}' with child models '{}'", aggregatedModel, childModels);
    _logger.trace("Query for aggregated model '{}': {}", aggregatedModel, query);
    validateSelectContainsEverySortProperty(query);
    List<String> orderedChildModels = new ArrayList<>(childModels);
    List<Query> childQueries = createChildQueries(query, aggregatedModel, orderedChildModels);
    assert childQueries.size() == orderedChildModels.size();
    List<ResultSet> childResults = this._queryExecutor.executeQueries(childQueries);
    assert childResults.size() == childQueries.size();
    List<ResultSet> results = convertChildResults(childResults, orderedChildModels, aggregatedModel, query
        .getProperties());
    ResultSet result = mergeResults(results, query);
    _logger.trace("Result for aggregated model '{}': {}", aggregatedModel, result);
    return result;
  }
  
  public QuerySchema getSchema() {
    return AggregatedModelSchema.addAggregatedModels(this._schema, this._aggregatedModelLookup);
  }
  
  public String toString() {
    return this._connection.toString();
  }
  
  private Set<String> getChildModels(String model) {
    assert model != null;
    return this._aggregatedModelLookup.getChildrenOfAggregatedModel(model);
  }
  
  private Set<String> getAggregatedModels(Collection<String> models) {
    assert models != null;
    Set<String> aggregatedModels = new HashSet<>(models.size());
    for (String model : models) {
      Set<String> childModels = getChildModels(model);
      assert childModels != null;
      if (!childModels.isEmpty())
        aggregatedModels.add(model); 
    } 
    return aggregatedModels;
  }
  
  private void validateAtMostOneAggregatedModel(Set<String> aggregatedModels) {
    if (aggregatedModels.size() > 1)
      throw new UnsupportedOperationException(String.format("Found multiple aggregated models in the same query: %s", new Object[] { aggregatedModels })); 
  }
  
  private void validateSelectContainsEverySortProperty(Query query) {
    assert query != null;
    Set<String> selectedProperties = new HashSet<>(query.getProperties());
    for (SortCriterion criterion : query.getSortCriteria()) {
      String property = criterion.getProperty();
      if (!selectedProperties.contains(property))
        throw new UnsupportedOperationException(String.format("Query orders by property '%s' but does not select it: %s", new Object[] { property, query })); 
    } 
  }
  
  private void validateAggregatedIsTheOnlyModel(Collection<String> models, String aggregatedModel) {
    assert models != null;
    assert !models.isEmpty();
    assert aggregatedModel != null;
    if (models.size() > 1 || !((String)models.iterator().next()).equals(aggregatedModel))
      throw new UnsupportedOperationException(String.format("Unexpected models in query for aggregated model '%s': %s", new Object[] { aggregatedModel, models })); 
  }
  
  private List<Query> createChildQueries(Query query, String aggregatedModel, List<String> childModels) {
    assert query != null;
    assert aggregatedModel != null;
    assert childModels != null;
    List<Query> childQueries = new ArrayList<>(childModels.size());
    for (String childModel : childModels) {
      Set<String> childModelProperties = getChildModelProperties(childModel);
      Query childQuery = null;
      if (childModelProperties != null)
        childQuery = (new AggregatedModelQueryConverter(aggregatedModel, childModel, childModelProperties)).toChildQuery(query); 
      childQueries.add(childQuery);
    } 
    return childQueries;
  }
  
  private Set<String> getChildModelProperties(String childModel) {
    QuerySchema.ModelInfo modelInfo = this._schema.getModels().get(childModel);
    if (modelInfo == null)
      return null; 
    return modelInfo.getProperties().keySet();
  }
  
  private List<ResultSet> convertChildResults(List<ResultSet> childResults, List<String> childModels, String aggregatedModel, List<String> aggregatedProperties) {
    assert childResults != null;
    assert childModels != null;
    assert childResults.size() == childModels.size();
    assert aggregatedModel != null;
    assert aggregatedProperties != null;
    List<ResultSet> results = new ArrayList<>(childResults.size());
    Iterator<ResultSet> childResultIterator = childResults.iterator();
    for (String childModel : childModels) {
      ResultSet childResult = childResultIterator.next();
      if (childResult == null)
        continue; 
      Set<String> childModelProperties = getChildModelProperties(childModel);
      if (childModelProperties == null)
        continue; 
      ResultSet result = (new AggregatedModelResultConverter(aggregatedModel, childModel, childModelProperties)).fromChildResult(childResult, aggregatedProperties);
      results.add(result);
    } 
    return results;
  }
  
  private ResultSet mergeResults(List<ResultSet> results, Query query) {
    assert results != null;
    assert query != null;
    SequenceMergePolicy<ResourceItem> itemMergePolicy = getItemSequenceMergePolicy(query);
    ResultMergePolicy resultMergePolicy = new ResultMergePolicy(itemMergePolicy);
    return resultMergePolicy.merge(results, query
        .getWithTotalCount(), query
        .getOffset(), query
        .getLimit());
  }
  
  private SequenceMergePolicy<ResourceItem> getItemSequenceMergePolicy(Query query) {
    assert query != null;
    if (query.getSortCriteria().isEmpty())
      return new UnorderedSequenceMergePolicy<>(); 
    Comparator<ResourceItem> itemComparator = new DefaultItemComparator(query.getProperties(), query.getSortCriteria());
    return new OrderedSequenceMergePolicy<>(itemComparator);
  }
}
