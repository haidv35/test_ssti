package com.vmware.ph.phservice.common.cis.appliance;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;

public class ApplianceIdAndTypeBuilder implements Builder<Pair<String, String>> {
  private final CisContextProvider _cisContextProvider;
  
  public ApplianceIdAndTypeBuilder(CisContextProvider cisContextProvider) {
    this._cisContextProvider = cisContextProvider;
  }
  
  public Pair<String, String> build() {
    CisContext cisContext = this._cisContextProvider.getCisContext();
    if (cisContext != null) {
      ApplianceContext applianceContext = cisContext.getApplianceContext();
      if (applianceContext != null) {
        String applianceId = applianceContext.getId();
        String nodeType = applianceContext.getDeploymentNodeType().toString();
        return new Pair(applianceId, nodeType);
      } 
    } 
    return null;
  }
}
