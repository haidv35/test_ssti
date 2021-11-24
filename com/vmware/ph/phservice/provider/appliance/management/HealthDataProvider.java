package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.health.Applmgmt;
import com.vmware.appliance.health.Databasestorage;
import com.vmware.appliance.health.Load;
import com.vmware.appliance.health.Mem;
import com.vmware.appliance.health.Softwarepackages;
import com.vmware.appliance.health.Storage;
import com.vmware.appliance.health.Swap;
import com.vmware.appliance.health.System;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class HealthDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Applmgmt.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Databasestorage.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Load.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Mem.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Storage.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Swap.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Softwarepackages.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, System.class);
  }
  
  public HealthDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<Object, Object> executeService(Service service, Object filter) {
    Object value = DataProviderUtil.getMethodInvocationReturnValue(service, "get");
    HashMap<Object, Object> result = new HashMap<>();
    result.put(null, value);
    return result;
  }
}
