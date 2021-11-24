package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.Services;
import com.vmware.appliance.ServicesTypes;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServicesDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Services.class);
  }
  
  public ServicesDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<String, ServicesTypes.Info> executeService(Service service, Object filter) {
    Map<String, ServicesTypes.Info> serviceIdToServiceInfo = ((Services)service).list();
    return serviceIdToServiceInfo;
  }
}
