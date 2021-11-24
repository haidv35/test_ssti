package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import java.util.List;

public class QueryContextUtil {
  public static final String CONTEXT_PROPERTY = "@type";
  
  public static Query addContextToQueryFilter(Query query, Object contextData) {
    if (contextData == null)
      return query; 
    Filter contextQueryFilter = QueryUtil.createFilterForPropertyAndValue("@type", contextData);
    Query queryWithContextFilter = QueryUtil.createQueryWithAdditionalFilter(query, contextQueryFilter);
    return queryWithContextFilter;
  }
  
  public static Query removeContextFromQueryFilter(Query query) {
    Query queryWithoutContextFilter = QueryUtil.removePredicateFromQueryFilter(query, "@type");
    return queryWithoutContextFilter;
  }
  
  public static QueryContext getQueryContextFromQueryFilter(Query query) {
    return getQueryContextFromQueryFilter(query, new QueryContextParser());
  }
  
  public static QueryContext getQueryContextFromQueryFilter(Query query, QueryContextParser parser) {
    List<Object> queryContextObjects = QueryUtil.getFilterPropertyComparableValues(query, "@type");
    Object queryContextObject = !queryContextObjects.isEmpty() ? queryContextObjects.get(0) : null;
    QueryContext queryContext = parser.parse(queryContextObject);
    return queryContext;
  }
}
