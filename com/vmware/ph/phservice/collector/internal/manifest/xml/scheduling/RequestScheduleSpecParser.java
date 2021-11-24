package com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling;

import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.RequestScheduleSpec;
import org.w3c.dom.Node;

public interface RequestScheduleSpecParser {
  RequestScheduleSpec parse(Node paramNode) throws InvalidManifestException;
}
