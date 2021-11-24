package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.phservice.provider.common.internal.Context;

public interface Mapping<IN, OUT> {
  OUT map(IN paramIN, Context paramContext);
}
