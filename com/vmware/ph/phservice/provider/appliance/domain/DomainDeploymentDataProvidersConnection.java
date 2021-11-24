package com.vmware.ph.phservice.provider.appliance.domain;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.provider.appliance.internal.BaseApplianceDataProviderConnection;
import java.util.Collections;
import java.util.List;

public class DomainDeploymentDataProvidersConnection extends BaseApplianceDataProviderConnection {
  private final CisContext _cisContext;
  
  private LookupClient _lookupClient;
  
  public DomainDeploymentDataProvidersConnection(CisContext cisContext) {
    super(cisContext);
    this._cisContext = cisContext;
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    ApplianceContext applianceContext = this._cisContext.getApplianceContext();
    String domainId = applianceContext.getDomainId();
    String hostName = applianceContext.getAbsoluteHostName();
    if (this._lookupClient == null)
      this._lookupClient = this._cisContext.getLookupClientBuilder().build(); 
    return Collections.singletonList(new DomainDeploymentDataProvider(domainId, hostName, this._lookupClient));
  }
  
  public void close() {
    if (this._lookupClient != null)
      this._lookupClient.close(); 
    super.close();
  }
}
