package com.vmware.ph.phservice.common.vapi;

import com.vmware.vapi.bindings.type.StructType;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

public class MapBasedVapiTypeProvider implements VapiTypeProvider {
  private static final String PACKAGE_DELIMITER = ".";
  
  private final Map<String, StructType> _canonicalNameToStructTypeMap;
  
  private final Set<String> _packageNames;
  
  public MapBasedVapiTypeProvider(Map<String, StructType> canonicalNameToStructTypeMap) {
    Validate.notNull(canonicalNameToStructTypeMap);
    this._canonicalNameToStructTypeMap = canonicalNameToStructTypeMap;
    this._packageNames = new HashSet<>();
    for (String canonicalName : this._canonicalNameToStructTypeMap.keySet()) {
      int lastIndexOfPackageDelimiter = canonicalName.lastIndexOf(".");
      if (lastIndexOfPackageDelimiter != -1) {
        String packageName = canonicalName.substring(0, lastIndexOfPackageDelimiter);
        this._packageNames.add(packageName);
      } 
    } 
  }
  
  public StructType getResourceModelType(String canonicalName) {
    StructType structType = this._canonicalNameToStructTypeMap.get(canonicalName);
    return structType;
  }
  
  public Set<String> getPackageNames() {
    return this._packageNames;
  }
}
