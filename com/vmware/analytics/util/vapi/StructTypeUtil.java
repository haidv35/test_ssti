package com.vmware.analytics.util.vapi;

import com.vmware.analytics.vapi.DataAppAgentDefinitions;
import com.vmware.analytics.vapi.TelemetryDefinitions;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.internal.util.Validate;
import java.util.Map;

public final class StructTypeUtil {
  public static void populateCanonicalNameToStructTypeMap(Map<String, StructType> mapping) {
    Validate.notNull(mapping);
    add(mapping, "com.vmware.analytics.data_app_agent.manifest_spec", DataAppAgentDefinitions.manifestSpec);
    add(mapping, "com.vmware.analytics.data_app_agent.create_spec", DataAppAgentDefinitions.createSpec);
    add(mapping, "com.vmware.analytics.data_app_agent.collect_request_spec", DataAppAgentDefinitions.collectRequestSpec);
    add(mapping, "com.vmware.analytics.data_app_agent.audit_result", DataAppAgentDefinitions.auditResult);
    add(mapping, "com.vmware.analytics.telemetry.telemetry_record", TelemetryDefinitions.telemetryRecord);
    add(mapping, "com.vmware.analytics.telemetry.nested_telemetry_record", TelemetryDefinitions.nestedTelemetryRecord);
  }
  
  private static void add(Map<String, StructType> mapping, String key, StructType structType) {
    if (mapping.containsKey(key))
      throw new IllegalArgumentException("Two structures with the same canonical name detected: " + key + ". Unable to populate the map."); 
    mapping.put(key, structType);
  }
}
