package com.vmware.ph.phservice.provider.appliance.vmaf;

import com.vmware.af.VmAfClient;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.vmaf.CisContextVmAfClientBuilder;
import com.vmware.ph.phservice.provider.appliance.internal.BaseApplianceDataProviderConnection;
import java.util.Collections;
import java.util.List;

public class VmAfDataProvidersConnection extends BaseApplianceDataProviderConnection {
  private final Builder<VmAfClient> _vmAfClientBuilder;
  
  private VmAfClient _vmAfClient;
  
  public VmAfDataProvidersConnection(CisContext cisContext) {
    super(cisContext);
    this._vmAfClientBuilder = (Builder<VmAfClient>)new CisContextVmAfClientBuilder(cisContext);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    if (this._vmAfClient == null)
      this._vmAfClient = (VmAfClient)this._vmAfClientBuilder.build(); 
    String applianceId = getApplianceId();
    List<DataProvider> dataProviders = Collections.singletonList(new VmAfDataProvider(this._vmAfClient, applianceId));
    return dataProviders;
  }
}
