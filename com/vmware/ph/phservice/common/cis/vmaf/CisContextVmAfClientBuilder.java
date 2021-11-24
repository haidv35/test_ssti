package com.vmware.ph.phservice.common.cis.vmaf;

import com.vmware.af.VmAfClient;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;

public class CisContextVmAfClientBuilder implements Builder<VmAfClient> {
  private final CisContext _applianceContext;
  
  public CisContextVmAfClientBuilder(CisContext applianceContext) {
    this._applianceContext = applianceContext;
  }
  
  public VmAfClient build() {
    ApplianceContext applianceContext = this._applianceContext.getApplianceContext();
    if (applianceContext == null)
      return null; 
    return new VmAfClient(applianceContext.getHostName());
  }
}
