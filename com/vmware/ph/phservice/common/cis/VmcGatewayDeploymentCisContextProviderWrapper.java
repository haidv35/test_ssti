package com.vmware.ph.phservice.common.cis;

import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;

public class VmcGatewayDeploymentCisContextProviderWrapper implements CisContextProvider {
  private final CisContextProvider _wrappedCisContextProvider;
  
  public VmcGatewayDeploymentCisContextProviderWrapper(CisContextProvider wrappedCisContextProvider) {
    this._wrappedCisContextProvider = wrappedCisContextProvider;
  }
  
  public CisContext getCisContext() {
    CisContext cisContext = this._wrappedCisContextProvider.getCisContext();
    if (cisContext != null) {
      ApplianceContext applianceContext = cisContext.getApplianceContext();
      if (applianceContext != null && DeploymentNodeTypeReader.DeploymentNodeType.VMC_GATEWAY.equals(applianceContext
          .getDeploymentNodeType()))
        return cisContext; 
    } 
    return null;
  }
}
