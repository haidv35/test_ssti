package com.vmware.ph.phservice.common.cdf.internal;

import com.vmware.ph.phservice.common.Pair;
import java.util.HashMap;
import java.util.Map;

public class VmodlDynamicTypeUtil {
  public static final String KEY_ANY_VALUE_TYPE = "vmodl.KeyAnyValue";
  
  public static final String KEY_ANY_VALUE_TYPE_KEY = "key";
  
  public static final String KEY_ANY_VALUE_TYPE_VALUE = "value";
  
  public static final String DYNAMIC_PROPERTY_TYPE = "vmodl.DynamicProperty";
  
  public static final String DYNAMIC_PROPERTY_TYPE_KEY = "name";
  
  public static final String DYNAMIC_PROPERTY_TYPE_VALUE = "val";
  
  private static final Map<String, Pair<String, String>> VMODL_TYPE_TO_DYNAMIC_PROPERTY = new HashMap<>();
  
  static {
    VMODL_TYPE_TO_DYNAMIC_PROPERTY.put("vmodl.DynamicProperty", new Pair("name", "val"));
    VMODL_TYPE_TO_DYNAMIC_PROPERTY.put("vmodl.KeyAnyValue", new Pair("key", "value"));
  }
  
  public static boolean isDynamicType(String vmodlType) {
    return VMODL_TYPE_TO_DYNAMIC_PROPERTY.containsKey(vmodlType);
  }
  
  public static String getDynamicPropertyKeyForVmodlType(String vmodlType) {
    Pair<String, String> dynamicProperty = getDynamicPropertyForVmodlType(vmodlType);
    return (dynamicProperty != null) ? (String)dynamicProperty.getFirst() : null;
  }
  
  public static String getDynamicPropertyValueForVmodlType(String vmodlType) {
    Pair<String, String> dynamicProperty = getDynamicPropertyForVmodlType(vmodlType);
    return (dynamicProperty != null) ? (String)dynamicProperty.getSecond() : null;
  }
  
  private static Pair<String, String> getDynamicPropertyForVmodlType(String vmodlType) {
    Pair<String, String> dynamicProperty = VMODL_TYPE_TO_DYNAMIC_PROPERTY.get(vmodlType);
    return dynamicProperty;
  }
}
