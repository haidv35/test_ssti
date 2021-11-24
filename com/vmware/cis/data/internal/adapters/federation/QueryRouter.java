package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Query;

public interface QueryRouter {
  Query route(Query paramQuery, String paramString);
}
