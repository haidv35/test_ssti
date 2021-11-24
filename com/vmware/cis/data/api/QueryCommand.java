package com.vmware.cis.data.api;

import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.Validate;

public final class QueryCommand {
  static final String QUERY_INVOCATION_ID_PREFIX = "q-";
  
  private final QueryService _queryService;
  
  private final Query _query;
  
  private final String _opId;
  
  private QueryCommand(QueryService queryService, Query query, String opId) {
    assert queryService != null;
    assert query != null;
    assert opId != null;
    this._queryService = queryService;
    this._query = query;
    this._opId = opId;
  }
  
  String getOpId() {
    return this._opId;
  }
  
  Query getQuery() {
    return this._query;
  }
  
  QueryService getQueryService() {
    return this._queryService;
  }
  
  public ResultSet fetch() {
    QueryIdLogConfigurator logConfigurator = QueryIdLogConfigurator.onQueryStart(this._opId, "q-");
    try {
      return this._queryService.executeQueryImpl(this._query);
    } finally {
      logConfigurator.close();
    } 
  }
  
  public String toString() {
    return this._query.toString();
  }
  
  public static final class Builder {
    private final QueryService _queryService;
    
    private final Query.Builder _queryBuilder;
    
    private String _opId;
    
    Builder(QueryService queryService, Query.Builder queryBuilder) {
      assert queryService != null;
      assert queryBuilder != null;
      this._queryService = queryService;
      this._queryBuilder = queryBuilder;
      this._opId = "";
    }
    
    public Builder from(Collection<String> resourceModels) {
      this._queryBuilder.from(resourceModels);
      return this;
    }
    
    public Builder from(String... resourceModels) {
      this._queryBuilder.from(resourceModels);
      return this;
    }
    
    public Builder where(Filter filter) {
      this._queryBuilder.where(filter);
      return this;
    }
    
    public Builder where(PropertyPredicate... criteria) {
      this._queryBuilder.where(criteria);
      return this;
    }
    
    public Builder where(LogicalOperator operator, PropertyPredicate... criteria) {
      this._queryBuilder.where(operator, criteria);
      return this;
    }
    
    public Builder where(LogicalOperator operator, List<PropertyPredicate> criteria) {
      this._queryBuilder.where(operator, criteria);
      return this;
    }
    
    public Builder where(String property, PropertyPredicate.ComparisonOperator operator, Object comparableValue) {
      this._queryBuilder.where(property, operator, comparableValue);
      return this;
    }
    
    public Builder where(String property, PropertyPredicate.ComparisonOperator operator, Object comparableValue, boolean ignoreCase) {
      this._queryBuilder.where(property, operator, comparableValue, ignoreCase);
      return this;
    }
    
    public Builder orderBy(String property) {
      this._queryBuilder.orderBy(property);
      return this;
    }
    
    public Builder orderBy(String property, SortCriterion.SortDirection sortDirection, boolean ignoreCase) {
      this._queryBuilder.orderBy(property, sortDirection, ignoreCase);
      return this;
    }
    
    public Builder orderBy(List<SortCriterion> sortCriteria) {
      this._queryBuilder.orderBy(sortCriteria);
      return this;
    }
    
    public Builder offset(int offset) {
      this._queryBuilder.offset(offset);
      return this;
    }
    
    public Builder limit(int limit) {
      this._queryBuilder.limit(limit);
      return this;
    }
    
    public Builder withTotalCount() {
      this._queryBuilder.withTotalCount();
      return this;
    }
    
    public Builder opId(String opId) {
      Validate.notEmpty(opId, "OpId must not be empty.");
      this._opId = opId;
      return this;
    }
    
    public QueryCommand build() {
      Query query = this._queryBuilder.build();
      return new QueryCommand(this._queryService, query, this._opId);
    }
    
    public ResultSet fetch() {
      QueryCommand cmd = build();
      return cmd.fetch();
    }
  }
}
