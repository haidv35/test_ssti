package com.vmware.cis.data.internal.provider.util;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QueryCommand;
import com.vmware.cis.data.api.QueryService;

public final class QueryCommandUtil {
  public static QueryCommand toCommand(QueryService queryService, Query query) {
    assert queryService != null;
    assert query != null;
    QueryCommand.Builder builder = queryService.select(query.getProperties()).from(query.getResourceModels()).where(query.getFilter()).orderBy(query.getSortCriteria()).limit(query.getLimit()).offset(query.getOffset());
    if (query.getWithTotalCount())
      builder.withTotalCount(); 
    return builder.build();
  }
}
