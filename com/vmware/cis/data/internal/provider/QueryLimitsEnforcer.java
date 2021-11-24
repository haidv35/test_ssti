package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QueryLimitsSpec;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.exception.QueryLimitExceededException;
import com.vmware.cis.data.api.exception.ResultLimitExceededException;
import com.vmware.vapi.bindings.ApiError;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryLimitsEnforcer {
  private static final Logger _logger = LoggerFactory.getLogger(QueryLimitsEnforcer.class);
  
  private final QueryLimitsSpec _limitsSpec;
  
  public QueryLimitsEnforcer(QueryLimitsSpec limitsSpec) {
    this._limitsSpec = limitsSpec;
  }
  
  public void enforceQuerySizeLimits(Query query) {
    assert query != null;
    if (query.getFilter() == null)
      return; 
    List<PropertyPredicate> criteria = query.getFilter().getCriteria();
    if (this._limitsSpec.getMaxCriteriaSize() >= 0)
      enforceMaxCriteriaSize(criteria); 
    if (this._limitsSpec.getMaxComparableListSize() >= 0)
      enforceMaxComparableListSize(criteria); 
  }
  
  public void enforceResultSizeLimits(ResultSet resultSet) throws ApiError {
    if (this._limitsSpec.getMaxResultSize() >= 0 && resultSet
      .getItems().size() > this._limitsSpec.getMaxResultSize()) {
      String errorMsg = String.format("Maximum number of result items exceeded: %s. Please refine your query criteria.", new Object[] { Integer.valueOf(this._limitsSpec.getMaxResultSize()) });
      _logger.info(errorMsg);
      throw new ResultLimitExceededException(this._limitsSpec.getMaxResultSize());
    } 
  }
  
  private void enforceMaxCriteriaSize(List<PropertyPredicate> criteria) {
    int criteriaSize = criteria.size();
    if (criteriaSize > this._limitsSpec.getMaxCriteriaSize()) {
      _logger.info("The query contains too many predicates: {}. The maximum size of the search criteria is {}.", 
          Integer.valueOf(criteriaSize), Integer.valueOf(this._limitsSpec.getMaxCriteriaSize()));
      throw new QueryLimitExceededException("filter.criteria", criteriaSize, this._limitsSpec
          .getMaxCriteriaSize());
    } 
  }
  
  private void enforceMaxComparableListSize(List<PropertyPredicate> criteria) {
    for (PropertyPredicate predicate : criteria) {
      if (predicate.getOperator() == PropertyPredicate.ComparisonOperator.IN || predicate
        .getOperator() == PropertyPredicate.ComparisonOperator.NOT_IN) {
        int comparableListSize = ((Collection)predicate.getComparableValue()).size();
        if (comparableListSize > this._limitsSpec.getMaxComparableListSize()) {
          _logger.info("The query contains comparableList with too many values: {}. The maximum size of a comparableList is {}.", 
              Integer.valueOf(comparableListSize), 
              Integer.valueOf(this._limitsSpec.getMaxComparableListSize()));
          throw new QueryLimitExceededException("filter.criteria.comparableValue list", comparableListSize, this._limitsSpec
              .getMaxComparableListSize());
        } 
      } 
    } 
  }
}
