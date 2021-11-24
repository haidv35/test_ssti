package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import org.w3c.dom.Node;

public interface MappingParser {
  <I, O> Mapping<I, O> parse(Node paramNode);
}
