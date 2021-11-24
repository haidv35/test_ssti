package com.vmware.ph.phservice.provider.common.vapi;

import com.vmware.vapi.bindings.Service;
import java.util.Map;

public class VapiResourceUtil {
  public static String getResourceName(Class<? extends Service> serviceClazz) {
    return serviceClazz.getCanonicalName().toLowerCase();
  }
  
  public static void addToMap(Map<String, Class<? extends Service>> resourceNameToServiceClazz, Class<? extends Service> serviceClazz) {
    resourceNameToServiceClazz.put(getResourceName(serviceClazz), serviceClazz);
  }
}
