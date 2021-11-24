package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;

public interface MappingBuilder<IN, OUT> {
  Mapping<IN, OUT> build();
}
