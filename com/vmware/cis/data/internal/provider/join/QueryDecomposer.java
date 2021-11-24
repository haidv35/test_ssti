package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Map;

interface QueryDecomposer {
  Map<DataProvider, Query> decomposeByProvider(Query paramQuery);
}
