package com.vmware.ph.phservice.common.cis;

import java.util.List;

public class CompositeCisContextProvider implements CisContextProvider {
  private final List<CisContextProvider> _cisContextProviders;
  
  public CompositeCisContextProvider(List<CisContextProvider> cisContextProviders) {
    this._cisContextProviders = cisContextProviders;
  }
  
  public CisContext getCisContext() {
    CisContext cisContext = null;
    for (CisContextProvider cisContextProvider : this._cisContextProviders) {
      cisContext = cisContextProvider.getCisContext();
      if (cisContext != null)
        break; 
    } 
    return cisContext;
  }
}
