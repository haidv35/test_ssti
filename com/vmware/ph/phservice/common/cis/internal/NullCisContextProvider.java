package com.vmware.ph.phservice.common.cis.internal;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;

public class NullCisContextProvider implements CisContextProvider {
  public CisContext getCisContext() {
    return null;
  }
}
