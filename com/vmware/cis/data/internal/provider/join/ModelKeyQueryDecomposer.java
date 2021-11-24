package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.ProviderBySchemaLookup;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
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

final class ModelKeyQueryDecomposer implements QueryDecomposer {
  private final QueryClauseAnalyzer _clauseAnalyzer;
  
  private final ProviderBySchemaLookup _providerLookup;
  
  public ModelKeyQueryDecomposer(ProviderBySchemaLookup providerLookup) {
    assert providerLookup != null;
    this._clauseAnalyzer = new QueryClauseAnalyzer(providerLookup);
    this._providerLookup = providerLookup;
  }
  
  public Map<DataProvider, Query> decomposeByProvider(Query query) {
    if (query.getFilter() == null)
      return decomposeByProviderFilterlessQuery(query); 
    LogicalOperator operator = query.getFilter().getOperator();
    Map<DataProvider, List<PropertyPredicate>> criteriaByProvider = this._clauseAnalyzer.getPredicatesByProvider(query);
    Map<DataProvider, Query> queryByProvider = new HashMap<>(criteriaByProvider.size());
    DataProvider sortProvider = getSortProvider(query, criteriaByProvider);
    boolean sortProviderUsed = false;
    for (Map.Entry<DataProvider, List<PropertyPredicate>> providerCriteria : criteriaByProvider.entrySet()) {
      DataProvider provider = providerCriteria.getKey();
      List<SortCriterion> sortCriteria = null;
      if (sortProvider == provider) {
        sortCriteria = query.getSortCriteria();
        sortProviderUsed = true;
      } 
      List<String> models = extractModels(sortCriteria, providerCriteria.getValue(), query
          .getResourceModels(), provider);
      Query providerQuery = Query.Builder.select(new String[] { "@modelKey" }).from(models).where(operator, providerCriteria.getValue()).orderBy(sortCriteria).build();
      queryByProvider.put(provider, providerQuery);
    } 
    if (sortProvider != null && !sortProviderUsed) {
      List<String> models = extractModels(query.getSortCriteria(), null, query
          .getResourceModels(), sortProvider);
      Query providerQuery = Query.Builder.select(new String[] { "@modelKey" }).from(models).orderBy(query.getSortCriteria()).build();
      queryByProvider.put(sortProvider, providerQuery);
    } 
    return queryByProvider;
  }
  
  private Map<DataProvider, Query> decomposeByProviderFilterlessQuery(Query query) {
    DataProvider fromClauseProvider = this._providerLookup.getProviderForModels(query.getResourceModels());
    if (fromClauseProvider == null)
      throw new UnsupportedOperationException("Query without WHERE clause and with multiple data providers satisfying the FROM clause is not supported."); 
    Map<DataProvider, Query> queryByProvider = new HashMap<>();
    Query providerQuery = Query.Builder.select(new String[] { "@modelKey" }).from(query.getResourceModels()).build();
    queryByProvider.put(fromClauseProvider, providerQuery);
    DataProvider sortClauseProvider = this._clauseAnalyzer.getSortProvider(query.getSortCriteria());
    assert sortClauseProvider != null && sortClauseProvider != fromClauseProvider;
    List<String> sortModels = extractModels(query.getSortCriteria(), null, query
        .getResourceModels(), sortClauseProvider);
    Query sortProviderQuery = Query.Builder.select(new String[] { "@modelKey" }).from(sortModels).orderBy(query.getSortCriteria()).build();
    queryByProvider.put(sortClauseProvider, sortProviderQuery);
    return queryByProvider;
  }
  
  private List<String> extractModels(List<SortCriterion> sortCriteria, List<PropertyPredicate> propertyPredicates, Collection<String> resourceModels, DataProvider provider) {
    Set<String> extractedModels = new HashSet<>();
    if (sortCriteria != null)
      for (SortCriterion criterion : sortCriteria)
        addModel(criterion.getProperty(), extractedModels);  
    if (propertyPredicates != null)
      for (PropertyPredicate propertyPredicate : propertyPredicates)
        addModel(propertyPredicate.getProperty(), extractedModels);  
    if (!extractedModels.isEmpty())
      return new ArrayList<>(extractedModels); 
    return this._clauseAnalyzer.filterModelsForProvider(resourceModels, provider);
  }
  
  private void addModel(String property, Set<String> models) {
    if (PropertyUtil.isSpecialProperty(property))
      return; 
    models.add(QualifiedProperty.forQualifiedName(property).getResourceModel());
  }
  
  private DataProvider getSortProvider(Query query, Map<DataProvider, List<PropertyPredicate>> criteriaByProvider) {
    if (query.getSortCriteria().isEmpty())
      return null; 
    DataProvider sortProvider = this._clauseAnalyzer.getSortProvider(query.getSortCriteria());
    if (sortProvider != null || criteriaByProvider.keySet().isEmpty())
      return sortProvider; 
    if (query.getFilter().getOperator() != LogicalOperator.AND)
      throw new UnsupportedOperationException("Sorting by @modelKey is supported only for filter with operator AND."); 
    return criteriaByProvider.keySet().iterator().next();
  }
}
