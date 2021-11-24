package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.common.cis.CisContext;
import java.net.URI;

public interface CcsLocator {
  URI getSdkUri(CisContext paramCisContext);
}
