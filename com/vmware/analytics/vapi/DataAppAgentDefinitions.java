package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.type.BooleanType;
import com.vmware.vapi.bindings.type.DynamicStructType;
import com.vmware.vapi.bindings.type.EnumType;
import com.vmware.vapi.bindings.type.OptionalType;
import com.vmware.vapi.bindings.type.StringType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.bindings.type.TypeReference;
import com.vmware.vapi.bindings.type.VoidType;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.OperationDef;
import com.vmware.vapi.internal.data.ConstraintValidator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataAppAgentDefinitions {
  public static final StructType manifestSpec = manifestSpecInit();
  
  public static final StructType createSpec = createSpecInit();
  
  public static final StructType collectRequestSpec = collectRequestSpecInit();
  
  public static final StructType auditResult = auditResultInit();
  
  public static final StructType __createInput = __createInputInit();
  
  public static final Type __createOutput = (Type)new VoidType();
  
  public static final OperationDef __createDef = __createDefInit();
  
  public static final StructType __deleteInput = __deleteInputInit();
  
  public static final Type __deleteOutput = (Type)new VoidType();
  
  public static final OperationDef __deleteDef = __deleteDefInit();
  
  public static final StructType __getInput = __getInputInit();
  
  public static final Type __getOutput = (Type)new DynamicStructType();
  
  public static final OperationDef __getDef = __getDefInit();
  
  public static final StructType __executeInput = __executeInputInit();
  
  public static final Type __executeOutput = (Type)new DynamicStructType();
  
  public static final OperationDef __executeDef = __executeDefInit();
  
  public static final StructType __collectInput = __collectInputInit();
  
  public static final Type __collectOutput = (Type)new DynamicStructType();
  
  public static final OperationDef __collectDef = __collectDefInit();
  
  public static final StructType __auditInput = __auditInputInit();
  
  public static final Type __auditOutput = (Type)new TypeReference<StructType>() {
      public StructType resolve() {
        return DataAppAgentDefinitions.auditResult;
      }
    };
  
  public static final OperationDef __auditDef = __auditDefInit();
  
  public static final StructType __exportObfuscationMapInput = __exportObfuscationMapInputInit();
  
  public static final Type __exportObfuscationMapOutput = (Type)new DynamicStructType();
  
  public static final OperationDef __exportObfuscationMapDef = __exportObfuscationMapDefInit();
  
  private static StructType manifestSpecInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("resourceId", new StringType());
    details = new StructType.FieldNameDetails("resourceId", "resourceId", "getResourceId", "setResourceId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("dataType", new StringType());
    details = new StructType.FieldNameDetails("dataType", "dataType", "getDataType", "setDataType");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("objectId", new StringType());
    details = new StructType.FieldNameDetails("objectId", "objectId", "getObjectId", "setObjectId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("versionDataType", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("versionDataType", "versionDataType", "getVersionDataType", "setVersionDataType");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("versionObjectId", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("versionObjectId", "versionObjectId", "getVersionObjectId", "setVersionObjectId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.data_app_agent.manifest_spec", fields, DataAppAgentTypes.ManifestSpec.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType createSpecInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("manifestSpec", new OptionalType((Type)new TypeReference<StructType>() {
            public StructType resolve() {
              return DataAppAgentDefinitions.manifestSpec;
            }
          }));
    details = new StructType.FieldNameDetails("manifestSpec", "manifestSpec", "getManifestSpec", "setManifestSpec");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("objectType", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("objectType", "objectType", "getObjectType", "setObjectType");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("collectionTriggerDataNeeded", new OptionalType((Type)new BooleanType()));
    details = new StructType.FieldNameDetails("collectionTriggerDataNeeded", "collectionTriggerDataNeeded", "getCollectionTriggerDataNeeded", "setCollectionTriggerDataNeeded");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("deploymentDataNeeded", new OptionalType((Type)new BooleanType()));
    details = new StructType.FieldNameDetails("deploymentDataNeeded", "deploymentDataNeeded", "getDeploymentDataNeeded", "setDeploymentDataNeeded");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("resultNeeded", new OptionalType((Type)new BooleanType()));
    details = new StructType.FieldNameDetails("resultNeeded", "resultNeeded", "getResultNeeded", "setResultNeeded");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("signalCollectionCompleted", new OptionalType((Type)new BooleanType()));
    details = new StructType.FieldNameDetails("signalCollectionCompleted", "signalCollectionCompleted", "getSignalCollectionCompleted", "setSignalCollectionCompleted");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("localManifestPath", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("localManifestPath", "localManifestPath", "getLocalManifestPath", "setLocalManifestPath");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("localPayloadPath", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("localPayloadPath", "localPayloadPath", "getLocalPayloadPath", "setLocalPayloadPath");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("localObfuscationMapPath", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("localObfuscationMapPath", "localObfuscationMapPath", "getLocalObfuscationMapPath", "setLocalObfuscationMapPath");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.data_app_agent.create_spec", fields, DataAppAgentTypes.CreateSpec.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType collectRequestSpecInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("manifestContent", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("manifestContent", "manifestContent", "getManifestContent", "setManifestContent");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("objectId", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("objectId", "objectId", "getObjectId", "setObjectId");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("contextData", new OptionalType((Type)new StringType()));
    details = new StructType.FieldNameDetails("contextData", "contextData", "getContextData", "setContextData");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.data_app_agent.collect_request_spec", fields, DataAppAgentTypes.CollectRequestSpec.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType auditResultInit() {
    StructType.FieldNameDetails details = null;
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new LinkedHashMap<>();
    Map<String, StructType.FieldNameDetails> fieldNameDetails = new HashMap<>();
    fields.put("status", new EnumType("com.vmware.analytics.data_app_agent.audit_status", DataAppAgentTypes.AuditStatus.class));
    details = new StructType.FieldNameDetails("status", "status", "getStatus", "setStatus");
    fieldNameDetails.put(details.getCanonicalName(), details);
    fields.put("result", new StringType());
    details = new StructType.FieldNameDetails("result", "result", "getResult", "setResult");
    fieldNameDetails.put(details.getCanonicalName(), details);
    return new StructType("com.vmware.analytics.data_app_agent.audit_result", fields, DataAppAgentTypes.AuditResult.class, validators, false, null, fieldNameDetails, null, null);
  }
  
  private static StructType __createInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("create_spec", new OptionalType((Type)new TypeReference<StructType>() {
            public StructType resolve() {
              return DataAppAgentDefinitions.createSpec;
            }
          }));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __createDefInit() {
    OperationDef operationDef = new OperationDef("create", "/dataapp/agent", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestBody("create_spec");
    return operationDef;
  }
  
  private static StructType __deleteInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __deleteDefInit() {
    OperationDef operationDef = new OperationDef("delete", "/dataapp/agent", "DELETE", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    return operationDef;
  }
  
  private static StructType __getInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __getDefInit() {
    OperationDef operationDef = new OperationDef("get", "/dataapp/agent", "GET", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    return operationDef;
  }
  
  private static StructType __executeInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("data_type", new OptionalType((Type)new StringType()));
    fields.put("object_id", new OptionalType((Type)new StringType()));
    fields.put("use_cache", new OptionalType((Type)new BooleanType()));
    fields.put("json_ld_context_data", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __executeDefInit() {
    OperationDef operationDef = new OperationDef("execute", "/dataapp/agent?action=execute", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestHeader("X-Data-Type", "data_type");
    operationDef.registerRequestHeader("X-Object-Id", "object_id");
    operationDef.registerRequestHeader("X-Use-Cache", "use_cache");
    operationDef.registerRequestBody("json_ld_context_data");
    return operationDef;
  }
  
  private static StructType __collectInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("object_id", new OptionalType((Type)new StringType()));
    fields.put("collect_request_spec", new TypeReference<StructType>() {
          public StructType resolve() {
            return DataAppAgentDefinitions.collectRequestSpec;
          }
        });
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __collectDefInit() {
    OperationDef operationDef = new OperationDef("collect", "/dataapp/agent?action=collect", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestHeader("X-Object-Id", "object_id");
    operationDef.registerRequestBody("collect_request_spec");
    return operationDef;
  }
  
  private static StructType __auditInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __auditDefInit() {
    OperationDef operationDef = new OperationDef("audit", "/dataapp/agent?action=audit", "PUT", null, null);
    return operationDef;
  }
  
  private static StructType __exportObfuscationMapInputInit() {
    List<ConstraintValidator> validators = null;
    Map<String, Type> fields = new HashMap<>();
    fields.put("collector_id", new StringType());
    fields.put("collector_instance_id", new StringType());
    fields.put("deployment_secret", new OptionalType((Type)new StringType()));
    fields.put("plugin_type", new OptionalType((Type)new StringType()));
    fields.put("object_id", new OptionalType((Type)new StringType()));
    return new StructType("operation-input", fields, StructValue.class, validators, false, null, null, null, null);
  }
  
  private static OperationDef __exportObfuscationMapDefInit() {
    OperationDef operationDef = new OperationDef("export_obfuscation_map", "/dataapp/agent?action=exportObfuscationMap", "POST", null, null);
    operationDef.registerRequestParam("_c", "collector_id");
    operationDef.registerRequestParam("_i", "collector_instance_id");
    operationDef.registerRequestHeader("X-Deployment-Secret", "deployment_secret");
    operationDef.registerRequestHeader("X-Plugin-Type", "plugin_type");
    operationDef.registerRequestHeader("X-Object-Id", "object_id");
    return operationDef;
  }
  
  public static final List<OperationDef> __operationDefs = Arrays.asList(new OperationDef[] { __createDef, __deleteDef, __getDef, __executeDef, __collectDef, __auditDef, __exportObfuscationMapDef });
}
