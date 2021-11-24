package com.vmware.ph.phservice.provider.appliance.healthstatus;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.Collections;
import java.util.List;

public class HealthStatusDataProvidersConnection implements DataProvidersConnection {
  private final CisContext _cisContext;
  
  private LookupClient _lookupClient;
  
  public HealthStatusDataProvidersConnection(CisContext cisContext) {
    this._cisContext = cisContext;
  }
  
  public List<DataProvider> getDataProviders() {
    if (this._lookupClient == null)
      this._lookupClient = this._cisContext.getLookupClientBuilder().build(); 
    String nodeId = null;
    ApplianceContext applianceContext = this._cisContext.getApplianceContext();
    if (applianceContext != null)
      nodeId = applianceContext.getNodeId(); 
    DataProvider healthStatusDataProvider = new HealthStatusDataProvider(this._lookupClient, this._cisContext.getTrustedStore(), nodeId, this._cisContext.getShouldUseEnvoySidecar());
    return Collections.singletonList(healthStatusDataProvider);
  }
  
  public void close() {
    if (this._lookupClient != null)
      this._lookupClient.close(); 
  }
}
