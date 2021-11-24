package com.vmware.ph.phservice.provider.appliance.vmon;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.appliance.internal.BaseApplianceDataProviderConnection;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.util.LinkedList;
import java.util.List;

public class VmonDataProvidersConnection extends BaseApplianceDataProviderConnection {
  private static final ServiceRegistration.EndpointType VMON_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vapi.json.https", "com.vmware.appliance.vmon");
  
  public VmonDataProvidersConnection(CisContext cisContext) {
    super(cisContext);
    setEndpointType(VMON_ENDPOINT_TYPE);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    String applianceId = getApplianceId();
    VapiClient vmonVapiClient = getVapiClient();
    List<DataProvider> dataProviders = new LinkedList<>();
    dataProviders.add(new VmonDataProvider(applianceId, vmonVapiClient));
    return dataProviders;
  }
}
