package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.type.BinaryType;
import com.vmware.vapi.bindings.type.DynamicStructType;
import com.vmware.vapi.bindings.type.IntegerType;
import com.vmware.vapi.bindings.type.OptionalType;
import com.vmware.vapi.bindings.type.StringType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.bindings.type.VoidType;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.OperationDef;
import com.vmware.vapi.internal.data.ConstraintValidator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAppDefinitions {
  public static final StructType __sendInput = __sendInputInit();
  
  public static final Type __sendOutput = (Type)new VoidType();
  
  public static final OperationDef __sendDef = __sendDefInit();
  
  public static final StructType __getResultsInput = __getResultsInputInit();
  
  public static final Type __getResultsOutput = (Type)new DynamicStructType();
  
  public static final OperationDef __getResultsDef = __getResultsDefInit();
  
  private static StructType __sendInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("collection_id", new OptionalType((Type)new StringType()));
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("data_type", new OptionalType((Type)new StringType()));
    fields.put("object_id", new OptionalType((Type)new StringType()));
    fields.put("payload", new BinaryType());
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __sendDefInit() {
    OperationDef operationDef = new OperationDef("send", "/dataapp/send", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestParam("_n", "collection_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestHeader("X-Data-Type", "data_type");
    operationDef.registerRequestHeader("X-Object-Id", "object_id");
    operationDef.registerRequestBody("payload");
    return operationDef;
  }
  
  private static StructType __getResultsInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("data_type", new OptionalType((Type)new StringType()));
    fields.put("object_id", new OptionalType((Type)new StringType()));
    fields.put("since_timestamp", new OptionalType((Type)new IntegerType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __getResultsDefInit() {
    OperationDef operationDef = new OperationDef("get_results", "/v1/results", "GET", null, null);
    operationDef.registerRequestParam("collectorId", "collector_id");
    operationDef.registerRequestParam("deploymentId", "collector_instance_id");
    operationDef.registerRequestParam("type", "data_type");
    operationDef.registerRequestParam("objectId", "object_id");
    operationDef.registerRequestParam("since", "since_timestamp");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    return operationDef;
  }
  
  public static final List<OperationDef> __operationDefs = Arrays.asList(new OperationDef[] { __sendDef, __getResultsDef });
}
