package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.access.Consolecli;
import com.vmware.appliance.access.Dcui;
import com.vmware.appliance.access.Shell;
import com.vmware.appliance.access.Ssh;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccessDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Consolecli.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Dcui.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Ssh.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Shell.class);
  }
  
  public AccessDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<Object, Object> executeService(Service service, Object filter) {
    Object value = DataProviderUtil.getMethodInvocationReturnValue(service, "get");
    HashMap<Object, Object> result = new HashMap<>();
    result.put(null, value);
    return result;
  }
}
