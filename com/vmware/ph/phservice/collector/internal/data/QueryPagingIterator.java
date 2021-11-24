package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QueryCommand;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.phservice.provider.common.internal.BaseQueryPagingIterator;

public class QueryPagingIterator extends BaseQueryPagingIterator {
  private final NamedQuery _baseNamedQuery;
  
  private final QueryService _queryService;
  
  public QueryPagingIterator(NamedQuery namedQuery, int defaultPageSize, QueryService queryService) {
    super(namedQuery.getQuery(), defaultPageSize, namedQuery.getPageSize());
    this._baseNamedQuery = namedQuery;
    this._queryService = queryService;
  }
  
  protected ResultSet fetchNextResultSet(Query query) {
    QueryCommand nextPageQueryCommand = buildQueryCommandFromQuery(query, this._queryService);
    return nextPageQueryCommand.fetch();
  }
  
  public NamedQuery getBaseNamedQuery() {
    return this._baseNamedQuery;
  }
  
  public static QueryCommand buildQueryCommandFromQuery(Query query, QueryService queryService) {
    QueryCommand.Builder queryCommandBuilder = queryService.select(query.getProperties()).from(query.getResourceModels()).offset(query.getOffset()).limit(query.getLimit()).where(query.getFilter()).orderBy(query.getSortCriteria());
    if (query.getWithTotalCount())
      queryCommandBuilder.withTotalCount(); 
    return queryCommandBuilder.build();
  }
}
