package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.Ntp;
import com.vmware.appliance.Shutdown;
import com.vmware.appliance.Timesync;
import com.vmware.appliance.system.Storage;
import com.vmware.appliance.system.Time;
import com.vmware.appliance.system.Uptime;
import com.vmware.appliance.system.Version;
import com.vmware.appliance.system.time.Timezone;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SystemDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Shutdown.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Timesync.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Ntp.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Timezone.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Uptime.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Time.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Version.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Storage.class);
  }
  
  public SystemDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<Object, Object> executeService(Service service, Object filter) {
    Object value = null;
    if (service instanceof Storage) {
      value = DataProviderUtil.getMethodInvocationReturnValue(service, "list");
    } else {
      value = DataProviderUtil.getMethodInvocationReturnValue(service, "get");
    } 
    Map<Object, Object> result = new HashMap<>();
    result.put(null, value);
    return result;
  }
}
