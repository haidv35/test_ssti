package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;
import com.vmware.ph.phservice.common.vapi.client.CisContextVapiClientBuilder;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DsVapiClientBuilder implements Builder<VapiClient> {
  private static final Log _log = LogFactory.getLog(DsVapiClientBuilder.class);
  
  private static final ServiceRegistration.EndpointType DATA_SERVICE_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vapi.json.https", "com.vmware.cis.ds.https");
  
  private final CisContextProvider _cisContextProvider;
  
  public DsVapiClientBuilder(CisContextProvider cisContextProvider) {
    this._cisContextProvider = cisContextProvider;
  }
  
  public VapiClient build() {
    ServiceRegistration.EndpointType dataServiceEndpointType = DATA_SERVICE_ENDPOINT_TYPE;
    CisContext cisContext = this._cisContextProvider.getCisContext();
    try {
      CisContextVapiClientBuilder vapiClientBuilder = new CisContextVapiClientBuilder(cisContext, dataServiceEndpointType);
      return vapiClientBuilder
        .withSessionAuthentication()
        .build();
    } catch (Exception e) {
      _log.error(String.format("Could not build vapi client using _cisContext=%s, endpointType=%s  due to: %s", new Object[] { cisContext, dataServiceEndpointType, e }));
      return null;
    } 
  }
}
