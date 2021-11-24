package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.server.AsyncContext;
import com.vmware.vapi.bindings.server.InvocationContext;
import com.vmware.vapi.core.AsyncHandle;
import com.vmware.vapi.core.MethodDefinition;
import com.vmware.vapi.core.MethodResult;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.ApiInterfaceSkeleton;
import com.vmware.vapi.internal.bindings.ApiMethodSkeleton;
import com.vmware.vapi.internal.bindings.StructValueExtractor;
import com.vmware.vapi.internal.bindings.TypeConverter;
import com.vmware.vapi.internal.bindings.TypeConverterImpl;
import com.vmware.vapi.internal.bindings.server.impl.AsyncContextImpl;
import com.vmware.vapi.internal.util.Validate;
import com.vmware.vapi.provider.ApiMethod;

public class DataAppAgentApiInterface extends ApiInterfaceSkeleton {
  private DataAppAgentProvider impl;
  
  private class CreateApiMethod extends ApiMethodSkeleton {
    public CreateApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "create", DataAppAgentDefinitions.__createInput, DataAppAgentDefinitions.__createOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__createInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      DataAppAgentTypes.CreateSpec createSpec = (DataAppAgentTypes.CreateSpec)extr.valueForField("create_spec");
      DataAppAgentApiInterface.this.impl.create(collectorId, collectorInstanceId, deploymentSecret, pluginType, createSpec, (AsyncContext<Void>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__createOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class DeleteApiMethod extends ApiMethodSkeleton {
    public DeleteApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "delete", DataAppAgentDefinitions.__deleteInput, DataAppAgentDefinitions.__deleteOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__deleteInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      DataAppAgentApiInterface.this.impl.delete(collectorId, collectorInstanceId, deploymentSecret, pluginType, (AsyncContext<Void>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__deleteOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class GetApiMethod extends ApiMethodSkeleton {
    public GetApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "get", DataAppAgentDefinitions.__getInput, DataAppAgentDefinitions.__getOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__getInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      DataAppAgentApiInterface.this.impl.get(collectorId, collectorInstanceId, deploymentSecret, pluginType, (AsyncContext<Structure>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__getOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class ExecuteApiMethod extends ApiMethodSkeleton {
    public ExecuteApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "execute", DataAppAgentDefinitions.__executeInput, DataAppAgentDefinitions.__executeOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__executeInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String dataType = (String)extr.valueForField("data_type");
      String objectId = (String)extr.valueForField("object_id");
      Boolean useCache = (Boolean)extr.valueForField("use_cache");
      String jsonLdContextData = (String)extr.valueForField("json_ld_context_data");
      DataAppAgentApiInterface.this.impl.execute(collectorId, collectorInstanceId, deploymentSecret, pluginType, dataType, objectId, useCache, jsonLdContextData, (AsyncContext<Structure>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__executeOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class CollectApiMethod extends ApiMethodSkeleton {
    public CollectApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "collect", DataAppAgentDefinitions.__collectInput, DataAppAgentDefinitions.__collectOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__collectInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String objectId = (String)extr.valueForField("object_id");
      DataAppAgentTypes.CollectRequestSpec collectRequestSpec = (DataAppAgentTypes.CollectRequestSpec)extr.valueForField("collect_request_spec");
      DataAppAgentApiInterface.this.impl.collect(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, collectRequestSpec, (AsyncContext<Structure>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__collectOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class AuditApiMethod extends ApiMethodSkeleton {
    public AuditApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "audit", DataAppAgentDefinitions.__auditInput, DataAppAgentDefinitions.__auditOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      DataAppAgentApiInterface.this.impl.audit((AsyncContext<DataAppAgentTypes.AuditResult>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__auditOutput, invocationContext, asyncHandle, this));
    }
  }
  
  private class ExportObfuscationMapApiMethod extends ApiMethodSkeleton {
    public ExportObfuscationMapApiMethod() {
      super(DataAppAgentApiInterface.this.getIdentifier(), "export_obfuscation_map", DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentDefinitions.__exportObfuscationMapOutput, DataAppAgentApiInterface.this


          
          .getTypeConverter(), null, MethodDefinition.TaskSupport.NONE);
    }
    
    public void doInvoke(InvocationContext invocationContext, StructValue inStruct, AsyncHandle<MethodResult> asyncHandle) {
      StructValueExtractor extr = new StructValueExtractor(inStruct, DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentApiInterface.this.getTypeConverter());
      String collectorId = (String)extr.valueForField("collector_id");
      String collectorInstanceId = (String)extr.valueForField("collector_instance_id");
      String deploymentSecret = (String)extr.valueForField("deployment_secret");
      String pluginType = (String)extr.valueForField("plugin_type");
      String objectId = (String)extr.valueForField("object_id");
      DataAppAgentApiInterface.this.impl.exportObfuscationMap(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, (AsyncContext<Structure>)new AsyncContextImpl(DataAppAgentApiInterface.this.getTypeConverter(), DataAppAgentDefinitions.__exportObfuscationMapOutput, invocationContext, asyncHandle, this));
    }
  }
  
  public DataAppAgentApiInterface() {
    this((Class<? extends DataAppAgentProvider>)null);
  }
  
  public DataAppAgentApiInterface(Class<? extends DataAppAgentProvider> implClass) {
    this((DataAppAgentProvider)createImplInstance(implClass, "com.vmware.analytics.vapi.impl.DataAppAgentImpl", DataAppAgentProvider.class));
  }
  
  public DataAppAgentApiInterface(DataAppAgentProvider impl) {
    this(impl, (TypeConverter)new TypeConverterImpl());
  }
  
  public DataAppAgentApiInterface(DataAppAgentProvider impl, TypeConverter converter) {
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
