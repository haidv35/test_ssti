package com.vmware.ph.phservice.collector.internal;

import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.provider.common.internal.Context;

public final class NoOpNamedQueryResultSetMapping<OUT> implements NamedQueryResultSetMapping<OUT> {
  public OUT map(NamedQueryResultSet input, Context context) {
    return null;
  }
  
  public boolean isQuerySupported(String queryName) {
    return false;
  }
}
