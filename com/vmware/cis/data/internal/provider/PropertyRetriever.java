package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.join.RelationalAlgebra;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PropertyRetriever {
  private final QueryClauseAnalyzer _clauseAnalyzer;
  
  private final QueryExecutor _queryExecutor;
  
  public PropertyRetriever(QueryClauseAnalyzer clauseAnalyzer, QueryExecutor queryExecutor) {
    assert clauseAnalyzer != null;
    assert queryExecutor != null;
    this._clauseAnalyzer = clauseAnalyzer;
    this._queryExecutor = queryExecutor;
  }
  
  public ResultSet gatherRemainingProperties(Query query, ResultSet result) {
    assert query != null;
    assert result != null;
    Collection<String> remainingProperties = determineRemainingProperties(query
        .getProperties(), result.getProperties());
    if (remainingProperties.isEmpty())
      return result; 
    List<Object> modelKeys = ResultSetAnalyzer.gatherModelKeysOrdered(result);
    if (modelKeys.isEmpty())
      return result; 
    Map<DataProvider, Query> queryPerProvider = createQueryPerProvider(remainingProperties, query.getResourceModels(), modelKeys);
    Collection<ResultSet> remainingResults = this._queryExecutor.executeQueries(queryPerProvider).values();
    List<ResultSet> results = new ArrayList<>(remainingResults);
    results.add(result);
    ResultSet mergedResults = RelationalAlgebra.joinSelectAndProject(results, modelKeys, query
        .getProperties());
    return ResultSet.Builder.copy(mergedResults)
      .totalCount(result.getTotalCount())
      .build();
  }
  
  private Collection<String> determineRemainingProperties(List<String> requested, List<String> returned) {
    Set<String> requestedSet = new HashSet<>(requested);
    requestedSet.removeAll(returned);
    return requestedSet;
  }
  
  private Map<DataProvider, Query> createQueryPerProvider(Collection<String> properties, Collection<String> resourceModels, List<Object> modelKeys) {
    Map<DataProvider, List<String>> propertiesByProvider = this._clauseAnalyzer.getPropertiesByProvider(properties);
    Map<DataProvider, Query> queryByProvider = new HashMap<>(propertiesByProvider.size());
    for (Map.Entry<DataProvider, List<String>> providerProperties : propertiesByProvider.entrySet()) {
      Query providerQuery = Query.Builder.select(PropertyUtil.plusModelKey(providerProperties.getValue())).from(extractModels(providerProperties.getValue())).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, modelKeys).build();
      queryByProvider.put(providerProperties.getKey(), providerQuery);
    } 
    return queryByProvider;
  }
  
  private List<String> extractModels(List<String> properties) {
    Set<String> models = new HashSet<>();
    for (String property : properties) {
      if (PropertyUtil.isSpecialProperty(property))
        continue; 
      models.add(QualifiedProperty.forQualifiedName(property).getResourceModel());
    } 
    return new ArrayList<>(models);
  }
}
