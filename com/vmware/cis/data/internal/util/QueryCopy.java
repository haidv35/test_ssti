package com.vmware.cis.data.internal.util;

import com.vmware.cis.data.api.Query;
import java.util.List;
import org.apache.commons.lang.Validate;

public final class QueryCopy {
  public static Query.Builder copyAndSelect(Query query, List<String> properties) {
    Validate.notNull(query);
    Validate.notNull(properties);
    return copyAndSelect(query, properties, query.getWithTotalCount());
  }
  
  public static Query.Builder copyAndSelect(Query query, List<String> properties, boolean withTotalCount) {
    Validate.notNull(query);
    Validate.notNull(properties);
    Query.Builder builder = Query.Builder.select(properties).from(query.getResourceModels()).where(query.getFilter()).orderBy(query.getSortCriteria()).limit(query.getLimit()).offset(query.getOffset());
    if (withTotalCount)
      builder.withTotalCount(); 
    return builder;
  }
  
  public static Query.Builder copy(Query query) {
    return copyAndSelect(query, query.getProperties());
  }
}
