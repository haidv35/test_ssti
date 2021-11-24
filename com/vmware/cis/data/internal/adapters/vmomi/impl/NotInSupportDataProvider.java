package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.provider.util.filter.KeyPredicateMerger;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class NotInSupportDataProvider implements DataProvider {
  private final DataProvider _provider;
  
  public NotInSupportDataProvider(DataProvider provider) {
    assert provider != null;
    this._provider = provider;
  }
  
  public QuerySchema getSchema() {
    return this._provider.getSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    assert query != null;
    query = KeyPredicateMerger.mergeKeyPredicates(query);
    if (query == null)
      return ResultSetUtil.emptyResult(query); 
    Collection<Object> keysToExclude = getKeysToExclude(query);
    if (keysToExclude.isEmpty())
      return this._provider.executeQuery(query); 
    if (query.getLimit() == 0) {
      Filter modifiedFilter = getFilterWithoutNotIn(query.getFilter());
      Query query1 = QueryCopy.copy(query).where(modifiedFilter).build();
      ResultSet resultSet1 = this._provider.executeQuery(query1);
      return modifyResultTotalCountPerExcludedKeys(this._provider, resultSet1, query, keysToExclude);
    } 
    Query modifiedQuery = extendQueryPage(query, keysToExclude);
    ResultSet resultSet = this._provider.executeQuery(modifiedQuery);
    ResultSetRange resultRange = new ResultSetRange(resultSet, modifiedQuery.getOffset(), modifiedQuery.getLimit());
    ResultSet processedResult = processResult(this._provider, resultRange, query, keysToExclude);
    return ResultSetUtil.project(processedResult, query.getProperties());
  }
  
  public String toString() {
    return this._provider.toString();
  }
  
  private static Collection<Object> getKeysToExclude(Query query) {
    assert query != null;
    Set<Object> keysToExclude = new LinkedHashSet();
    Filter filter = query.getFilter();
    if (filter == null)
      return keysToExclude; 
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (PropertyPredicate.ComparisonOperator.NOT_IN.equals(predicate.getOperator())) {
        if (!"@modelKey".equals(predicate.getProperty()))
          throw new IllegalArgumentException("NOT_IN operator is supported only for @modelKey properties."); 
        Collection<?> comparableKeys = (Collection)predicate.getComparableValue();
        keysToExclude.addAll(comparableKeys);
      } 
    } 
    if (keysToExclude.isEmpty())
      return keysToExclude; 
    if (filter.getCriteria().size() > 1 && filter
      .getOperator().equals(LogicalOperator.OR))
      throw new IllegalArgumentException("NOT_IN operator is supported only for filters with logical operator AND."); 
    return keysToExclude;
  }
  
  private static ResultSet processResult(DataProvider provider, ResultSetRange resultRange, Query query, Collection<Object> keysToExclude) {
    if (resultRange.isEmptyPage(query.getOffset(), query.getLimit())) {
      ResultSet resultSet = resultRange.page(query.getOffset(), query.getLimit());
      if (query.getWithTotalCount())
        return modifyResultTotalCountPerExcludedKeys(provider, resultSet, query, keysToExclude); 
      return resultSet;
    } 
    Collection<?> remainingKeysToExclude = resultRange.getAbsent(keysToExclude);
    resultRange = resultRange.exclude(keysToExclude);
    if (remainingKeysToExclude.isEmpty())
      return resultRange.page(query.getOffset(), query.getLimit()); 
    if (query.getOffset() == 0 && query.getLimit() < 0)
      return resultRange.page(query.getOffset(), query.getLimit()); 
    if (resultRange.isEmptyPage(query.getOffset(), query.getLimit()) || resultRange
      .getOffset() == 0) {
      ResultSet resultSet = resultRange.page(query.getOffset(), query.getLimit());
      if (query.getWithTotalCount())
        return modifyResultTotalCountPerExcludedKeys(provider, resultSet, query, remainingKeysToExclude); 
      return resultSet;
    } 
    ResultSet keysToExcludeResult = fetchSortPropertiesForExcludedKeys(provider, query, remainingKeysToExclude);
    if (keysToExcludeResult.getItems().isEmpty())
      return resultRange.page(query.getOffset(), query.getLimit()); 
    resultRange = resultRange.exclude(keysToExcludeResult.getItems(), query
        .getSortCriteria());
    return resultRange.page(query.getOffset(), query.getLimit());
  }
  
  private static ResultSet modifyResultTotalCountPerExcludedKeys(DataProvider provider, ResultSet resultSet, Query query, Collection<?> keysToExclude) {
    Filter filterForExcludedKeys = filterForExcludedKeys(query.getFilter(), keysToExclude);
    Query queryForTotalCount = Query.Builder.select(new String[0]).from(query.getResourceModels()).where(filterForExcludedKeys).limit(0).withTotalCount().build();
    ResultSet totalCountResultSet = provider.executeQuery(queryForTotalCount);
    int totalCount = resultSet.getTotalCount().intValue() - totalCountResultSet.getTotalCount().intValue();
    totalCount = (totalCount < 0) ? 0 : totalCount;
    return ResultSet.Builder.copy(resultSet).totalCount(Integer.valueOf(totalCount)).build();
  }
  
  private static ResultSet fetchSortPropertiesForExcludedKeys(DataProvider provider, Query query, Collection<?> keysToExclude) {
    Filter filterForExcludedKeys = filterForExcludedKeys(query.getFilter(), keysToExclude);
    List<String> selectedProperties = new ArrayList<>();
    selectedProperties.add("@modelKey");
    assert !query.getSortCriteria().isEmpty();
    for (SortCriterion sort : query.getSortCriteria())
      selectedProperties.add(sort.getProperty()); 
    Query queryForOrderedKeys = Query.Builder.select(selectedProperties).from(query.getResourceModels()).where(filterForExcludedKeys).orderBy(query.getSortCriteria()).build();
    ResultSet orderedKeysResultSet = provider.executeQuery(queryForOrderedKeys);
    return orderedKeysResultSet;
  }
  
  private static Filter filterForExcludedKeys(Filter filter, Collection<?> keysToExclude) {
    assert filter != null;
    assert keysToExclude != null;
    List<PropertyPredicate> predicates = new ArrayList<>();
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (PropertyPredicate.ComparisonOperator.NOT_IN.equals(predicate.getOperator()))
        continue; 
      predicates.add(predicate);
    } 
    if (keysToExclude.size() == 1) {
      predicates.add(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, keysToExclude
            .iterator().next()));
    } else {
      predicates
        .add(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.IN, keysToExclude));
    } 
    return new Filter(predicates, LogicalOperator.AND);
  }
  
  private static Query extendQueryPage(Query query, Collection<?> keysToExclude) {
    List<String> selectedProperties = new ArrayList<>(query.getProperties());
    if (!selectedProperties.contains("@modelKey"))
      selectedProperties.add("@modelKey"); 
    for (SortCriterion sort : query.getSortCriteria()) {
      if (!selectedProperties.contains(sort.getProperty()))
        selectedProperties.add(sort.getProperty()); 
    } 
    int effectiveOffset = getEffectiveOffset(query, keysToExclude);
    int effectiveLimit = getEffectiveLimit(query, keysToExclude);
    Filter modifiedFilter = getFilterWithoutNotIn(query.getFilter());
    return QueryCopy.copyAndSelect(query, selectedProperties).where(modifiedFilter)
      .offset(effectiveOffset).limit(effectiveLimit).build();
  }
  
  private static Filter getFilterWithoutNotIn(Filter filter) {
    assert filter != null;
    List<PropertyPredicate> predicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (PropertyPredicate.ComparisonOperator.NOT_IN.equals(predicate.getOperator()))
        continue; 
      predicates.add(predicate);
    } 
    return predicates.isEmpty() ? null : new Filter(predicates, filter.getOperator());
  }
  
  private static int getEffectiveLimit(Query query, Collection<?> keysToExclude) {
    if (query.getLimit() > 0) {
      long effectiveLimit = Math.min(2147483647L, query.getLimit() + (2 * keysToExclude
          .size()));
      return (int)effectiveLimit;
    } 
    return query.getLimit();
  }
  
  private static int getEffectiveOffset(Query query, Collection<?> keysToExclude) {
    if (query.getOffset() > 0) {
      long effectiveOffset = (query.getOffset() - keysToExclude.size());
      return (effectiveOffset < 0L) ? 0 : (int)effectiveOffset;
    } 
    return query.getOffset();
  }
}
