package com.vmware.ph.phservice.common.vapi.util;

import java.net.URI;

public class VapiResultUtil {
  private static final String VAPI_ID_SEPARATOR = ":";
  
  public static URI createModelKey(String resourceName, Object resourceEntityId, String serverGuid) {
    URI modelKey = null;
    if (resourceEntityId == null) {
      modelKey = VapiUriSchemeUtil.createUri(resourceName, serverGuid);
    } else {
      String entityIdStr = resourceEntityId.toString();
      entityIdStr = convertToValidModelKey(entityIdStr);
      modelKey = VapiUriSchemeUtil.createUri(resourceName, serverGuid + ":" + entityIdStr);
    } 
    return modelKey;
  }
  
  public static String convertToValidModelKey(String modelKey) {
    return modelKey.replaceAll("[^a-zA-Z0-9-]+", ":");
  }
}
