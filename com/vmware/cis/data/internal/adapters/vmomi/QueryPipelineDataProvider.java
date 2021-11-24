package com.vmware.cis.data.internal.adapters.vmomi;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;

public interface QueryPipelineDataProvider extends DataProvider {
  Collection<ResultSet> executeQueryBatch(Collection<Query> paramCollection);
}
