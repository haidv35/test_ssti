package com.vmware.ph.phservice.collector.internal;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;

public interface NamedQueryResultSetMapping<OUT> extends Mapping<NamedQueryResultSet, OUT> {
  boolean isQuerySupported(String paramString);
}
