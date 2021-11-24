package com.vmware.ph.phservice.collector.internal.manifest.xml.query;

import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.RequestSpec;
import org.w3c.dom.Node;

public interface RequestSpecParser {
  RequestSpec parse(Node paramNode) throws InvalidManifestException;
}
