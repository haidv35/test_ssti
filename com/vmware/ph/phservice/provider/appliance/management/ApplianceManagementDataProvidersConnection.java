package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.appliance.internal.BaseApplianceDataProviderConnection;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.util.LinkedList;
import java.util.List;

public class ApplianceManagementDataProvidersConnection extends BaseApplianceDataProviderConnection {
  private static final ServiceRegistration.EndpointType APPLMGMT_HTTPS_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vapi.json.https.public", "com.vmware.applmgmt");
  
  private static final ServiceRegistration.EndpointType APPLMGMT_HTTP_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vapi.json.http", "com.vmware.applmgmt");
  
  public ApplianceManagementDataProvidersConnection(CisContext cisContext) {
    super(cisContext);
    setEndpointType(APPLMGMT_HTTPS_ENDPOINT_TYPE);
    setExplicitLocalEndpointType(APPLMGMT_HTTP_ENDPOINT_TYPE);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    String applianceId = getApplianceId();
    VapiClient applMgmtVapiClient = getVapiClient();
    List<DataProvider> dataProviders = new LinkedList<>();
    dataProviders.add(new AccessDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new HealthDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new NetworkingDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new ServicesDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new SystemDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new MonitoringDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new MonitoringQueryDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new SystemLoadDataProvider(applianceId, applMgmtVapiClient));
    dataProviders.add(new StatusDataProvider(applianceId, applMgmtVapiClient));
    return dataProviders;
  }
}
