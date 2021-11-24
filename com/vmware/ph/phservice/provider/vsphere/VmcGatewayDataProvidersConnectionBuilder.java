package com.vmware.ph.phservice.provider.vsphere;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;
import com.vmware.ph.phservice.provider.appliance.ApplianceDataProvidersConnection;
import com.vmware.ph.phservice.provider.appliance.healthstatus.HealthStatusDataProvidersConnection;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.CompositeDataProvidersConnection;
import com.vmware.ph.phservice.provider.vcenter.lookup.LookupDataProvidersConnection;
import java.util.ArrayList;
import java.util.List;

public class VmcGatewayDataProvidersConnectionBuilder implements Builder<DataProvidersConnection> {
  private final CisContextProvider _applianceContextProvider;
  
  public VmcGatewayDataProvidersConnectionBuilder(CisContextProvider applianceContextProvider) {
    this._applianceContextProvider = applianceContextProvider;
  }
  
  public DataProvidersConnection build() {
    CisContext applianceContext = this._applianceContextProvider.getCisContext();
    if (applianceContext == null)
      return null; 
    List<DataProvidersConnection> vmcGatewayDataProvidersConnections = new ArrayList<>();
    vmcGatewayDataProvidersConnections.add(new ApplianceDataProvidersConnection(applianceContext));
    vmcGatewayDataProvidersConnections.add(new LookupDataProvidersConnection(applianceContext));
    vmcGatewayDataProvidersConnections.add(new HealthStatusDataProvidersConnection(applianceContext));
    return (DataProvidersConnection)new CompositeDataProvidersConnection(vmcGatewayDataProvidersConnections, new AutoCloseable[0]);
  }
}
