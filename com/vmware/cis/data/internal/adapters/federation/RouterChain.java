package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.Validate;

public final class RouterChain implements QueryRouter {
  private final List<QueryRouter> _routerChain;
  
  public RouterChain(QueryRouter... chain) {
    this._routerChain = new ArrayList<>(Arrays.asList(chain));
  }
  
  public Query route(Query query, String targetInstanceId) {
    QueryRouter router;
    Validate.notNull(query);
    Iterator<QueryRouter> iterator = this._routerChain.iterator();
    do {
      router = iterator.next();
    } while (iterator.hasNext() && (
      query = router.route(query, targetInstanceId)) != null);
    return query;
  }
}
