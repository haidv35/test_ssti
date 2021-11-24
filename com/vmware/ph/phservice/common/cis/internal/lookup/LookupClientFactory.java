package com.vmware.ph.phservice.common.cis.internal.lookup;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import java.net.URI;

public interface LookupClientFactory {
  LookupClient connectLookup(URI paramURI);
}
