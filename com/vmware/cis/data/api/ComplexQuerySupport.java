package com.vmware.cis.data.api;

import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import com.vmware.cis.data.internal.provider.join.InnerJoinOperator;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.ContentLibraryQualifier;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ComplexQuerySupport {
  private static final Logger _logger = LoggerFactory.getLogger(ComplexQuerySupport.class);
  
  private static final String COMPLEX_QUERY_PREFIX = "cq-";
  
  private static final String PARAM_SUFFIX = "/@param";
  
  private static final List<PropertyPredicate> EMPTY_PARAMETERS = Collections.emptyList();
  
  private static final InnerJoinOperator INNER_JOIN = new InnerJoinOperator();
  
  public static ResultSet joinFilter(QueryCommand command, Filter filter) {
    Validate.notNull(command);
    Validate.notNull(filter);
    Query query = command.getQuery();
    QueryService queryService = command.getQueryService();
    String opId = command.getOpId();
    QueryIdLogConfigurator logConfigurator = QueryIdLogConfigurator.onQueryStart(opId, "q-");
    try {
      if (_logger.isTraceEnabled())
        _logger.trace("Joining query with filter: {} \n {}", query, filter); 
      ResultSet result = executeComplexQuery(queryService, query, filter);
      if (_logger.isTraceEnabled())
        _logger.trace("Received response for complex query: {}", result); 
      return result;
    } finally {
      logConfigurator.close();
    } 
  }
  
  private static ResultSet executeComplexQuery(QueryService queryService, Query query, Filter filter) {
    if (query.getResourceModels().size() != 1 && 
      !ContentLibraryQualifier.isClFromClause(query.getResourceModels()))
      throw new UnsupportedOperationException("Unsupported query for multiple models: " + query
          .getResourceModels()); 
    Collection<Filter> mergedFilters = mergeConjunctiveFilters(new Filter[] { query.getFilter(), filter });
    if (mergedFilters.size() == 0)
      return queryService.executeQueryImpl(query); 
    if (mergedFilters.size() == 1) {
      Filter mergedFilter = mergedFilters.iterator().next();
      Query mergedQuery = QueryCopy.copy(query).where(mergedFilter).build();
      return queryService.executeQueryImpl(mergedQuery);
    } 
    return executeMultiFilterQuery(queryService, query, mergedFilters);
  }
  
  private static ResultSet executeMultiFilterQuery(QueryService queryService, Query query, Collection<Filter> filters) {
    assert queryService != null;
    assert query != null;
    assert filters != null && filters.size() > 1;
    if (!hasParameters(filters))
      return executeMultiFilterQuery(queryService, query, filters, EMPTY_PARAMETERS); 
    List<PropertyPredicate> parameters = new ArrayList<>();
    Collection<Filter> filtersToExecute = removeParameters(filters, parameters);
    if (filtersToExecute.size() == 0) {
      Filter filterWithParameters = new Filter(parameters);
      Query queryWithParameters = QueryCopy.copy(query).where(filterWithParameters).build();
      return queryService.executeQueryImpl(queryWithParameters);
    } 
    if (filtersToExecute.size() == 1) {
      Filter filter = filtersToExecute.iterator().next();
      List<PropertyPredicate> predicatesWithParameters = new ArrayList<>(parameters);
      predicatesWithParameters.addAll(filter.getCriteria());
      Filter filterWithParameters = new Filter(predicatesWithParameters, filter.getOperator());
      Query queryWithParameters = QueryCopy.copy(query).where(filterWithParameters).build();
      return queryService.executeQueryImpl(queryWithParameters);
    } 
    return executeMultiFilterQuery(queryService, query, filtersToExecute, parameters);
  }
  
  private static ResultSet executeMultiFilterQuery(QueryService queryService, Query query, Collection<Filter> filters, List<PropertyPredicate> parameters) {
    QueryIdLogConfigurator.QueryCounter queryCounter = QueryIdLogConfigurator.newQueryCounter("cq-");
    Collection<ResultSet> filterResults = executeFilters(queryService, query, filters, queryCounter);
    ResultSet joinedResult = innerJoin(filterResults);
    ResultSet pagedResult = ResultSetUtil.applyLimitAndOffset(joinedResult, query
        .getLimit(), query.getOffset());
    ResultSet resultWithProperties = gatherProperties(queryService, query, pagedResult, parameters, queryCounter);
    Integer totalCount = null;
    if (query.getWithTotalCount())
      totalCount = joinedResult.getTotalCount(); 
    ResultSet finalResult = ResultSet.Builder.copy(resultWithProperties).totalCount(totalCount).build();
    return ResultSetUtil.project(finalResult, query.getProperties());
  }
  
  private static ResultSet innerJoin(Collection<ResultSet> results) {
    assert results != null;
    assert results.size() == 2;
    Iterator<ResultSet> resultIterator = results.iterator();
    ResultSet orderedResult = resultIterator.next();
    ResultSet unorderedResult = resultIterator.next();
    return INNER_JOIN.joinOrderedResult(unorderedResult, orderedResult);
  }
  
  private static Collection<ResultSet> executeFilters(QueryService queryService, Query query, Collection<Filter> filters, QueryIdLogConfigurator.QueryCounter queryCounter) {
    assert queryService != null;
    assert query != null;
    assert !CollectionUtils.isEmpty(filters);
    assert queryCounter != null;
    List<ResultSet> results = new ArrayList<>(filters.size());
    Iterator<Filter> filterIterator = filters.iterator();
    Filter firstFilter = filterIterator.next();
    Collection<String> firstAdaptedModels = adaptFromClause(firstFilter, query
        .getSortCriteria(), query.getResourceModels());
    List<String> sortProperties = getSortProperties(query.getSortCriteria());
    Query orderedQuery = Query.Builder.select(withModelKey(sortProperties)).from(firstAdaptedModels).where(firstFilter).orderBy(query.getSortCriteria()).build();
    ResultSet orderedResult = executeQuery(queryService, orderedQuery, queryCounter);
    results.add(orderedResult);
    while (filterIterator.hasNext()) {
      Filter filter = filterIterator.next();
      Collection<String> adaptedModels = adaptFromClause(filter, null, query
          .getResourceModels());
      Query unorderedQuery = Query.Builder.select(new String[] { "@modelKey" }).from(adaptedModels).where(filter).build();
      ResultSet unorderedResult = executeQuery(queryService, unorderedQuery, queryCounter);
      results.add(unorderedResult);
    } 
    return results;
  }
  
  private static Collection<String> adaptFromClause(Filter filter, List<SortCriterion> sortCriteria, Collection<String> resourceModels) {
    assert filter != null;
    assert resourceModels != null;
    if (resourceModels.size() == 1)
      return resourceModels; 
    Set<String> properties = new HashSet<>();
    for (PropertyPredicate predicate : filter.getCriteria())
      properties.add(predicate.getProperty()); 
    if (sortCriteria != null)
      for (SortCriterion sortCriterion : sortCriteria)
        properties.add(sortCriterion.getProperty());  
    return adaptFromClause(properties, resourceModels);
  }
  
  private static Collection<String> adaptFromClause(Collection<String> properties, Collection<String> resourceModels) {
    assert properties != null;
    assert resourceModels != null;
    if (resourceModels.size() == 1)
      return resourceModels; 
    assert ContentLibraryQualifier.isClFromClause(resourceModels);
    Set<String> selectedModels = new HashSet<>();
    for (String property : properties) {
      String clPropertyModelType = ContentLibraryQualifier.getClModel(property, resourceModels);
      if (clPropertyModelType == null)
        continue; 
      selectedModels.add(clPropertyModelType);
    } 
    return selectedModels.isEmpty() ? resourceModels : selectedModels;
  }
  
  private static Collection<Filter> removeParameters(Collection<Filter> filters, List<PropertyPredicate> parameters) {
    assert filters != null;
    assert parameters != null;
    List<Filter> modifiedFilters = new ArrayList<>(filters.size());
    for (Filter filter : filters) {
      if (!hasParameters(filter)) {
        modifiedFilters.add(filter);
        continue;
      } 
      List<PropertyPredicate> modifiedPredicates = removeParameters(filter
          .getCriteria(), parameters);
      if (!modifiedPredicates.isEmpty()) {
        Filter modifiedFilter = new Filter(modifiedPredicates, filter.getOperator());
        modifiedFilters.add(modifiedFilter);
      } 
    } 
    return modifiedFilters;
  }
  
  private static List<PropertyPredicate> removeParameters(List<PropertyPredicate> predicates, List<PropertyPredicate> parameters) {
    assert predicates != null;
    assert parameters != null;
    List<PropertyPredicate> modifiedPredicates = new ArrayList<>();
    for (PropertyPredicate predicate : predicates) {
      if (isParameter(predicate)) {
        parameters.add(predicate);
        continue;
      } 
      modifiedPredicates.add(predicate);
    } 
    return modifiedPredicates;
  }
  
  private static boolean hasParameters(Collection<Filter> filters) {
    assert filters != null;
    for (Filter filter : filters) {
      if (hasParameters(filter))
        return true; 
    } 
    return false;
  }
  
  private static boolean hasParameters(Filter filter) {
    assert filter != null;
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (isParameter(predicate))
        return true; 
    } 
    return false;
  }
  
  private static boolean isParameter(PropertyPredicate predicate) {
    assert predicate != null;
    return predicate.getProperty().endsWith("/@param");
  }
  
  private static ResultSet gatherProperties(QueryService queryService, Query query, ResultSet result, List<PropertyPredicate> parameters, QueryIdLogConfigurator.QueryCounter queryCounter) {
    assert queryService != null;
    assert query != null;
    assert result != null;
    assert queryCounter != null;
    if (!hasRemainingProperties(query, result))
      return result; 
    List<Object> modelKeys = ResultSetAnalyzer.gatherModelKeysOrdered(result);
    if (modelKeys.isEmpty())
      return result; 
    List<PropertyPredicate> predicates = new ArrayList<>(parameters);
    if (modelKeys.size() == 1) {
      predicates.add(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, modelKeys.get(0)));
    } else {
      predicates.add(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.IN, modelKeys));
    } 
    List<String> propertiesToSelect = withModelKey(query.getProperties());
    Query remainingPropertiesQuery = Query.Builder.select(propertiesToSelect).from(adaptFromClause(propertiesToSelect, query.getResourceModels())).where(LogicalOperator.AND, predicates).build();
    ResultSet remainingPropertiesResult = executeQuery(queryService, remainingPropertiesQuery, queryCounter);
    ResultSet joinResult = INNER_JOIN.joinOrderedResult(remainingPropertiesResult, result);
    return joinResult;
  }
  
  private static Collection<Filter> mergeConjunctiveFilters(Filter... filters) {
    List<Filter> mergedFilters = new ArrayList<>(filters.length);
    List<PropertyPredicate> intersectCriteria = new ArrayList<>();
    for (Filter filter : filters) {
      if (filter != null)
        switch (filter.getOperator()) {
          case AND:
            intersectCriteria.addAll(filter.getCriteria());
            break;
          case OR:
            if (filter.getCriteria().size() == 1) {
              intersectCriteria.addAll(filter.getCriteria());
              break;
            } 
            mergedFilters.add(filter);
            break;
          default:
            throw new UnsupportedOperationException("Unsupported logical operator: " + filter
                .getOperator());
        }  
    } 
    if (!intersectCriteria.isEmpty())
      mergedFilters.add(new Filter(intersectCriteria, LogicalOperator.AND)); 
    return mergedFilters;
  }
  
  private static boolean hasRemainingProperties(Query query, ResultSet resultSet) {
    Set<String> remainingProperties = new HashSet<>(query.getProperties());
    remainingProperties.removeAll(resultSet.getProperties());
    return !remainingProperties.isEmpty();
  }
  
  private static List<String> withModelKey(List<String> properties) {
    if (properties.contains("@modelKey"))
      return properties; 
    List<String> newProperties = new ArrayList<>(properties);
    newProperties.add("@modelKey");
    return newProperties;
  }
  
  private static List<String> getSortProperties(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    if (sortCriteria.isEmpty())
      return Collections.emptyList(); 
    List<String> properties = new ArrayList<>();
    for (SortCriterion sortCriterion : sortCriteria)
      properties.add(sortCriterion.getProperty()); 
    return properties;
  }
  
  private static ResultSet executeQuery(QueryService queryService, Query query, QueryIdLogConfigurator.QueryCounter queryCounter) {
    QueryIdLogConfigurator logConfigurator = queryCounter.onQueryStart();
    try {
      return queryService.executeQueryImpl(query);
    } finally {
      logConfigurator.close();
    } 
  }
}
