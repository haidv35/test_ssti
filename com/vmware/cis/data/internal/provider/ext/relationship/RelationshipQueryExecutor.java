package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.ResourceItemUtil;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RelationshipQueryExecutor {
  private static Logger _logger = LoggerFactory.getLogger(RelationshipQueryExecutor.class);
  
  public static ResultSet executeRelationshipQueriesForSelect(String relatedPropertyName, List<RelationshipQuery> relationshipQueries, ResultSet previousResultSet, SortCriterion relatedPropertySortCriterion, List<Integer> reorderedItemsIndices, DataProvider connection) {
    assert relatedPropertyName != null;
    assert relationshipQueries != null;
    assert !relationshipQueries.isEmpty();
    assert previousResultSet != null;
    assert reorderedItemsIndices != null;
    assert connection != null;
    RelationshipQuery firstRelationshipQuery = relationshipQueries.get(0);
    String sourceModelJoinProperty = firstRelationshipQuery.getRelationshipJoin().getSourceJoinProperty();
    ResultSet resultSetForJoin = ResultSetUtil.extractPropertyFromResultSet(previousResultSet, sourceModelJoinProperty);
    ResultSet previousSelectResult = resultSetForJoin;
    if (relationshipQueries.size() > 1)
      previousSelectResult = executeRelationshipQueries(relatedPropertyName, relationshipQueries
          .subList(0, relationshipQueries.size() - 1), resultSetForJoin, connection); 
    RelationshipQuery lastRelationshipQuery = relationshipQueries.get(relationshipQueries.size() - 1);
    Query selectRelationshipQuery = lastRelationshipQuery.buildQueryForJoin(previousSelectResult, relatedPropertySortCriterion);
    if (selectRelationshipQuery == null)
      return null; 
    ResultSet selectResult = executeRelationshipQuery(relatedPropertyName, selectRelationshipQuery, connection);
    String lastRightJoinProperty = lastRelationshipQuery.getRelationshipJoin().getTargetJoinProperty();
    ResultSet relatedResult = RelationshipResultJoin.joinResults(previousSelectResult, lastRelationshipQuery
        .getRelationshipJoin().getSourceJoinProperty(), true, selectResult, lastRightJoinProperty, false);
    if (relatedPropertySortCriterion != null)
      reorderedItemsIndices.addAll(ResourceItemUtil.getResourceItemsPermutation(relatedResult, lastRightJoinProperty, selectResult, lastRightJoinProperty, SortCriterion.SortDirection.ASCENDING

            
            .equals(relatedPropertySortCriterion.getSortDirection()))); 
    return relatedResult;
  }
  
  public static ResultSet executeRelationshipQueriesForFilter(String relatedPropertyName, List<RelationshipQuery> relationshipQueries, Filter relatedPropertyPartialFilter, SortCriterion relatedPropertySortCriterion, DataProvider connection) {
    assert relatedPropertyName != null;
    assert relationshipQueries != null;
    assert !relationshipQueries.isEmpty();
    assert relatedPropertyPartialFilter != null;
    assert connection != null;
    RelationshipQuery firstRelationshipQuery = relationshipQueries.get(0);
    Query filterRelationshipQuery = firstRelationshipQuery.buildQueryForFilter(relatedPropertyPartialFilter, relatedPropertySortCriterion);
    ResultSet filterResult = executeRelationshipQuery(relatedPropertyName, filterRelationshipQuery, connection);
    if (relationshipQueries.size() == 1)
      return filterResult; 
    return executeRelationshipQueries(relatedPropertyName, relationshipQueries
        .subList(1, relationshipQueries.size()), filterResult, connection);
  }
  
  private static ResultSet executeRelationshipQueries(String relatedPropertyName, List<RelationshipQuery> relationshipQueries, ResultSet previousResultSet, DataProvider connection) {
    assert relationshipQueries != null;
    assert !relationshipQueries.isEmpty();
    if (previousResultSet == null)
      return null; 
    ResultSet relatedResult = previousResultSet;
    for (RelationshipQuery relationshipQuery : relationshipQueries) {
      Query query = relationshipQuery.buildQueryForJoin(relatedResult, null);
      if (query == null) {
        relatedResult = null;
        break;
      } 
      ResultSet relationshipResult = executeRelationshipQuery(relatedPropertyName, query, connection);
      relatedResult = RelationshipResultJoin.joinResults(relatedResult, relationshipQuery
          .getRelationshipJoin().getSourceJoinProperty(), true, relationshipResult, relationshipQuery
          .getRelationshipJoin().getTargetJoinProperty(), true);
    } 
    return relatedResult;
  }
  
  private static ResultSet executeRelationshipQuery(String relatedProperty, Query query, DataProvider connection) {
    ResultSet result;
    assert connection != null;
    try {
      if (_logger.isTraceEnabled())
        _logger.trace("[QueryModel] Executing a generated query for the related property '{}': {}", relatedProperty, query); 
      if (RelationshipClientSideTypeFilter.mayFilterModelKeysOnClientSide(query)) {
        _logger.debug("[QueryModel] Executing client-side filtering of keys in the query: {}", query);
        result = RelationshipClientSideTypeFilter.filterModelKeysOnClientSide(query);
      } else {
        result = connection.executeQuery(query);
      } 
      if (_logger.isTraceEnabled())
        _logger.trace("[QueryModel] Received response for a generated query for the related property '{}': {}", relatedProperty, result); 
    } catch (Exception e) {
      throw new RuntimeException(String.format("Error while executing a query for the related property '%s': '%s'", new Object[] { relatedProperty, query }), e);
    } 
    return result;
  }
}
