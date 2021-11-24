package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.type.DynamicStructType;
import com.vmware.vapi.bindings.type.ListType;
import com.vmware.vapi.bindings.type.OptionalType;
import com.vmware.vapi.bindings.type.StringType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.bindings.type.TypeReference;
import com.vmware.vapi.bindings.type.VoidType;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.HasFieldsOfValidator;
import com.vmware.vapi.internal.bindings.OperationDef;
import com.vmware.vapi.internal.data.ConstraintValidator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TelemetryDefinitions {
  public static final StructType telemetryRecord = telemetryRecordInit();
  
  public static final StructType nestedTelemetryRecord = nestedTelemetryRecordInit();
  
  public static final StructType __sendInput = __sendInputInit();
  
  public static final Type __sendOutput = (Type)new VoidType();
  
  public static final OperationDef __sendDef = __sendDefInit();
  
  public static final StructType __sendMultipleInput = __sendMultipleInputInit();
  
  public static final Type __sendMultipleOutput = (Type)new VoidType();
  
  public static final OperationDef __sendMultipleDef = __sendMultipleDefInit();
  
  public static final StructType __getLevelInput = __getLevelInputInit();
  
  public static final Type __getLevelOutput = (Type)new StringType();
  
  public static final OperationDef __getLevelDef = __getLevelDefInit();
  
  private static StructType telemetryRecordInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("@type", new StringType());
    details = new StructType.FieldNameDetails("@type", "type", "getType", "setType");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("@id", new StringType());
    details = new StructType.FieldNameDetails("@id", "id", "getId", "setId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.telemetry.telemetry_record", fields, TelemetryTypes.TelemetryRecord.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType nestedTelemetryRecordInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("@type", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("@type", "type", "getType", "setType");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("@id", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("@id", "id", "getId", "setId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.telemetry.nested_telemetry_record", fields, TelemetryTypes.NestedTelemetryRecord.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType __sendInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new OptionalType((Type)new StringType()));
    fields.put("version", new OptionalType((Type)new StringType()));
    fields.put("record", new DynamicStructType(Arrays.asList(new ConstraintValidator[] { (ConstraintValidator)new HasFieldsOfValidator(new TypeReference<StructType>() {
                  public StructType resolve() {
                    return TelemetryDefinitions.telemetryRecord;
                  }
                }) })));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __sendDefInit() {
    OperationDef operationDef = new OperationDef("send", "/hyper/send", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestParam("_v", "version");
    operationDef.registerRequestBody("record");
    return operationDef;
  }
  
  private static StructType __sendMultipleInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new OptionalType((Type)new StringType()));
    fields.put("version", new OptionalType((Type)new StringType()));
    fields.put("records", new ListType((Type)new DynamicStructType(Arrays.asList(new ConstraintValidator[] { (ConstraintValidator)new HasFieldsOfValidator(new TypeReference<StructType>() {
                    public StructType resolve() {
                      return TelemetryDefinitions.telemetryRecord;
                    }
                  }) }))));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __sendMultipleDefInit() {
    OperationDef operationDef = new OperationDef("send_multiple", "/hyper/send", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestParam("_v", "version");
    operationDef.registerRequestBody("records");
    return operationDef;
  }
  
  private static StructType __getLevelInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __getLevelDefInit() {
    OperationDef operationDef = new OperationDef("get_level", "/level", "GET", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    return operationDef;
  }
  
  public static final List<OperationDef> __operationDefs = Arrays.asList(new OperationDef[] { __sendDef, __sendMultipleDef, __getLevelDef });
}
