package com.vmware.ph.phservice.common.cis.appliance;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;

public class ApplianceIdBuilder implements Builder<String> {
  private final CisContextProvider _cisContextProvider;
  
  public ApplianceIdBuilder(CisContextProvider cisContextProvider) {
    this._cisContextProvider = cisContextProvider;
  }
  
  public String build() {
    CisContext cisContext = this._cisContextProvider.getCisContext();
    if (cisContext != null) {
      ApplianceContext applianceContext = cisContext.getApplianceContext();
      if (applianceContext != null)
        return applianceContext.getId(); 
    } 
    return null;
  }
}
