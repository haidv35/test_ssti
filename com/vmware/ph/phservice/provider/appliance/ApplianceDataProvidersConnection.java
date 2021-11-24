package com.vmware.ph.phservice.provider.appliance;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.provider.appliance.domain.DomainDeploymentDataProvidersConnection;
import com.vmware.ph.phservice.provider.appliance.management.ApplianceManagementDataProvidersConnection;
import com.vmware.ph.phservice.provider.appliance.vmaf.VmAfDataProvidersConnection;
import com.vmware.ph.phservice.provider.appliance.vmon.VmonDataProvidersConnection;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.CompositeDataProvidersConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplianceDataProvidersConnection implements DataProvidersConnection {
  private final CompositeDataProvidersConnection _compositeDpc;
  
  public ApplianceDataProvidersConnection(CisContext cisContext) {
    this
      
      ._compositeDpc = new CompositeDataProvidersConnection(Collections.unmodifiableList(
          createDataProvidersConnections(cisContext)), new AutoCloseable[0]);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    return this._compositeDpc.getDataProviders();
  }
  
  public void close() {
    this._compositeDpc.close();
  }
  
  private static List<DataProvidersConnection> createDataProvidersConnections(CisContext cisContext) {
    List<DataProvidersConnection> dpcList = new ArrayList<>();
    if (cisContext.getApplianceContext() == null)
      return dpcList; 
    DataProvidersConnection applmgmtDpc = new ApplianceManagementDataProvidersConnection(cisContext);
    dpcList.add(applmgmtDpc);
    DataProvidersConnection vmonDpc = new VmonDataProvidersConnection(cisContext);
    dpcList.add(vmonDpc);
    DataProvidersConnection domainDeploymentDpc = new DomainDeploymentDataProvidersConnection(cisContext);
    dpcList.add(domainDeploymentDpc);
    DataProvidersConnection vmAfDpc = new VmAfDataProvidersConnection(cisContext);
    dpcList.add(vmAfDpc);
    return dpcList;
  }
}
