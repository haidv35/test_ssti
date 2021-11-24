package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.server.InvocationContext;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.MethodDefinition;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.data.VoidValue;
import com.vmware.vapi.internal.bindings.ApiInterfaceSkeleton;
import com.vmware.vapi.internal.bindings.ApiMethodSkeleton;
import com.vmware.vapi.internal.bindings.StructValueExtractor;
import com.vmware.vapi.internal.bindings.TypeConverter;
import com.vmware.vapi.internal.bindings.TypeConverterImpl;
import com.vmware.vapi.internal.util.Validate;
import com.vmware.vapi.provider.ApiMethod;

public class DataAppAgentSyncApiInterface extends ApiInterfaceSkeleton {
  private DataAppAgentSyncProvider impl;
  
  private class CreateApiMethod extends ApiMethodSkeleton {
    public CreateApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "create", DataAppAgentDefinitions.__createInput, DataAppAgentDefinitions.__createOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__createInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      DataAppAgentTypes.CreateSpec createSpec = (DataAppAgentTypes.CreateSpec)extr.valueForField("create_spec");
      try {
        DataAppAgentSyncApiInterface.this.impl.create(collectorId, collectorInstanceId, deploymentSecret, pluginType, createSpec, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class DeleteApiMethod extends ApiMethodSkeleton {
    public DeleteApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "delete", DataAppAgentDefinitions.__deleteInput, DataAppAgentDefinitions.__deleteOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__deleteInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      try {
        DataAppAgentSyncApiInterface.this.impl.delete(collectorId, collectorInstanceId, deploymentSecret, pluginType, invocationContext);
        asyncHandle.setResult(MethodResult.newResult((DataValue)VoidValue.getInstance()));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class GetApiMethod extends ApiMethodSkeleton {
    public GetApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "get", DataAppAgentDefinitions.__getInput, DataAppAgentDefinitions.__getOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__getInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppAgentSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppAgentSyncApiInterface.this.impl.get(collectorId, collectorInstanceId, deploymentSecret, pluginType, invocationContext), DataAppAgentDefinitions.__getOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class ExecuteApiMethod extends ApiMethodSkeleton {
    public ExecuteApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "execute", DataAppAgentDefinitions.__executeInput, DataAppAgentDefinitions.__executeOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__executeInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      Boolean useCache = (Boolean)extr.valueForField("use_cache");
      String jsonLdContextData = (String)extr.valueForField("json_ld_context_data");
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppAgentSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppAgentSyncApiInterface.this.impl.execute(collectorId, collectorInstanceId, deploymentSecret, pluginType, dataType, objectId, useCache, jsonLdContextData, invocationContext), DataAppAgentDefinitions.__executeOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class CollectApiMethod extends ApiMethodSkeleton {
    public CollectApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "collect", DataAppAgentDefinitions.__collectInput, DataAppAgentDefinitions.__collectOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__collectInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String objectId = (String)extr.valueForField("object_id");
      DataAppAgentTypes.CollectRequestSpec collectRequestSpec = (DataAppAgentTypes.CollectRequestSpec)extr.valueForField("collect_request_spec");
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppAgentSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppAgentSyncApiInterface.this.impl.collect(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, collectRequestSpec, invocationContext), DataAppAgentDefinitions.__collectOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class AuditApiMethod extends ApiMethodSkeleton {
    public AuditApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "audit", DataAppAgentDefinitions.__auditInput, DataAppAgentDefinitions.__auditOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppAgentSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppAgentSyncApiInterface.this.impl.audit(invocationContext), DataAppAgentDefinitions.__auditOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  private class ExportObfuscationMapApiMethod extends ApiMethodSkeleton {
    public ExportObfuscationMapApiMethod() {
      super(DataAppAgentSyncApiInterface.this.getIdentifier(), "export_obfuscation_map", DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentDefinitions.__exportObfuscationMapOutput, DataAppAgentSyncApiInterface.this

          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentSyncApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String objectId = (String)extr.valueForField("object_id");
      try {
        asyncHandle.setResult(MethodResult.newResult(DataAppAgentSyncApiInterface.this.getTypeConverter().convertToVapi(DataAppAgentSyncApiInterface.this.impl.exportObfuscationMap(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, invocationContext), DataAppAgentDefinitions.__exportObfuscationMapOutput, new TypeConverter.ConversionContext(invocationContext
                  
                  .getExecutionContext()))));
      } catch (RuntimeException ex) {
        asyncHandle.setResult(MethodResult.newErrorResult(toErrorValue(ex, invocationContext.getExecutionContext())));
      } 
    }
  }
  
  public DataAppAgentSyncApiInterface() {
    this((Class<? extends DataAppAgentSyncProvider>)null);
  }
  
  public DataAppAgentSyncApiInterface(Class<? extends DataAppAgentSyncProvider> implClass) {
    this((DataAppAgentSyncProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.DataAppAgentImpl", DataAppAgentSyncProvider.class));
  }
  
  public DataAppAgentSyncApiInterface(DataAppAgentSyncProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public DataAppAgentSyncApiInterface(DataAppAgentSyncProvider impl, TypeConverter converter) {
    super("com.vmware.analytics.data_app_agent", converter);
    Validate.notNull(impl);
    this.impl = impl;
    registerMethod((ApiMethod)new CreateApiMethod());
    registerMethod((ApiMethod)new DeleteApiMethod());
    registerMethod((ApiMethod)new GetApiMethod());
    registerMethod((ApiMethod)new ExecuteApiMethod());
    registerMethod((ApiMethod)new CollectApiMethod());
    registerMethod((ApiMethod)new AuditApiMethod());
    registerMethod((ApiMethod)new ExportObfuscationMapApiMethod());
  }
}
