package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.StubConfigurationBase;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.InterfaceIdentifier;
import com.vmware.vapi.core.MethodIdentifier;
import com.vmware.vapi.internal.bindings.StructValueBuilder;
import com.vmware.vapi.internal.bindings.Stub;
import com.vmware.vapi.internal.bindings.TypeConverter;

public class DataAppAgentStub extends Stub implements DataAppAgent {
  public DataAppAgentStub(ApiProvider apiProvider, TypeConverter typeConverter, StubConfigurationBase config) {
    super(apiProvider, typeConverter, new InterfaceIdentifier("com.vmware.analytics.data_app_agent"), config);
  }
  
  public DataAppAgentStub(ApiProvider apiProvider, StubConfigurationBase config) {
    this(apiProvider, (TypeConverter)null, config);
  }
  
  public void create(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, DataAppAgentTypes.CreateSpec createSpec) {
    create(collectorId, collectorInstanceId, deploymentSecret, pluginType, createSpec, (InvocationConfig)null);
  }
  
  public void create(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, DataAppAgentTypes.CreateSpec createSpec, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__createInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("create_spec", createSpec);
    invokeMethod(new MethodIdentifier(this.ifaceId, "create"), strBuilder, DataAppAgentDefinitions.__createInput, DataAppAgentDefinitions.__createOutput, null, invocationConfig);
  }
  
  public void create(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, DataAppAgentTypes.CreateSpec createSpec, AsyncCallback<Void> asyncCallback) {
    create(collectorId, collectorInstanceId, deploymentSecret, pluginType, createSpec, asyncCallback, (InvocationConfig)null);
  }
  
  public void create(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, DataAppAgentTypes.CreateSpec createSpec, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__createInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("create_spec", createSpec);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "create"), strBuilder, DataAppAgentDefinitions.__createInput, DataAppAgentDefinitions.__createOutput, null, invocationConfig, asyncCallback);
  }
  
  public void delete(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType) {
    delete(collectorId, collectorInstanceId, deploymentSecret, pluginType, (InvocationConfig)null);
  }
  
  public void delete(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__deleteInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    invokeMethod(new MethodIdentifier(this.ifaceId, "delete"), strBuilder, DataAppAgentDefinitions.__deleteInput, DataAppAgentDefinitions.__deleteOutput, null, invocationConfig);
  }
  
  public void delete(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, AsyncCallback<Void> asyncCallback) {
    delete(collectorId, collectorInstanceId, deploymentSecret, pluginType, asyncCallback, (InvocationConfig)null);
  }
  
  public void delete(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__deleteInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "delete"), strBuilder, DataAppAgentDefinitions.__deleteInput, DataAppAgentDefinitions.__deleteOutput, null, invocationConfig, asyncCallback);
  }
  
  public Structure get(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType) {
    return get(collectorId, collectorInstanceId, deploymentSecret, pluginType, (InvocationConfig)null);
  }
  
  public Structure get(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__getInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    Structure result = null;
    result = (Structure)invokeMethod(new MethodIdentifier(this.ifaceId, "get"), strBuilder, DataAppAgentDefinitions.__getInput, DataAppAgentDefinitions.__getOutput, null, invocationConfig);
    return result;
  }
  
  public void get(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, AsyncCallback<Structure> asyncCallback) {
    get(collectorId, collectorInstanceId, deploymentSecret, pluginType, asyncCallback, (InvocationConfig)null);
  }
  
  public void get(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, AsyncCallback<Structure> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__getInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "get"), strBuilder, DataAppAgentDefinitions.__getInput, DataAppAgentDefinitions.__getOutput, null, invocationConfig, asyncCallback);
  }
  
  public Structure execute(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String dataType, String objectId, Boolean useCache, String jsonLdContextData) {
    return execute(collectorId, collectorInstanceId, deploymentSecret, pluginType, dataType, objectId, useCache, jsonLdContextData, (InvocationConfig)null);
  }
  
  public Structure execute(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String dataType, String objectId, Boolean useCache, String jsonLdContextData, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__executeInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("use_cache", useCache);
    strBuilder.addStructField("json_ld_context_data", jsonLdContextData);
    Structure result = null;
    result = (Structure)invokeMethod(new MethodIdentifier(this.ifaceId, "execute"), strBuilder, DataAppAgentDefinitions.__executeInput, DataAppAgentDefinitions.__executeOutput, null, invocationConfig);
    return result;
  }
  
  public void execute(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String dataType, String objectId, Boolean useCache, String jsonLdContextData, AsyncCallback<Structure> asyncCallback) {
    execute(collectorId, collectorInstanceId, deploymentSecret, pluginType, dataType, objectId, useCache, jsonLdContextData, asyncCallback, (InvocationConfig)null);
  }
  
  public void execute(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String dataType, String objectId, Boolean useCache, String jsonLdContextData, AsyncCallback<Structure> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__executeInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("use_cache", useCache);
    strBuilder.addStructField("json_ld_context_data", jsonLdContextData);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "execute"), strBuilder, DataAppAgentDefinitions.__executeInput, DataAppAgentDefinitions.__executeOutput, null, invocationConfig, asyncCallback);
  }
  
  public Structure collect(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, DataAppAgentTypes.CollectRequestSpec collectRequestSpec) {
    return collect(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, collectRequestSpec, (InvocationConfig)null);
  }
  
  public Structure collect(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, DataAppAgentTypes.CollectRequestSpec collectRequestSpec, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__collectInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("collect_request_spec", collectRequestSpec);
    Structure result = null;
    result = (Structure)invokeMethod(new MethodIdentifier(this.ifaceId, "collect"), strBuilder, DataAppAgentDefinitions.__collectInput, DataAppAgentDefinitions.__collectOutput, null, invocationConfig);
    return result;
  }
  
  public void collect(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, DataAppAgentTypes.CollectRequestSpec collectRequestSpec, AsyncCallback<Structure> asyncCallback) {
    collect(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, collectRequestSpec, asyncCallback, (InvocationConfig)null);
  }
  
  public void collect(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, DataAppAgentTypes.CollectRequestSpec collectRequestSpec, AsyncCallback<Structure> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__collectInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("collect_request_spec", collectRequestSpec);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "collect"), strBuilder, DataAppAgentDefinitions.__collectInput, DataAppAgentDefinitions.__collectOutput, null, invocationConfig, asyncCallback);
  }
  
  public DataAppAgentTypes.AuditResult audit() {
    return audit((InvocationConfig)null);
  }
  
  public DataAppAgentTypes.AuditResult audit(InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__auditInput, this.converter);
    DataAppAgentTypes.AuditResult result = null;
    result = (DataAppAgentTypes.AuditResult)invokeMethod(new MethodIdentifier(this.ifaceId, "audit"), strBuilder, DataAppAgentDefinitions.__auditInput, DataAppAgentDefinitions.__auditOutput, null, invocationConfig);
    return result;
  }
  
  public void audit(AsyncCallback<DataAppAgentTypes.AuditResult> asyncCallback) {
    audit(asyncCallback, (InvocationConfig)null);
  }
  
  public void audit(AsyncCallback<DataAppAgentTypes.AuditResult> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__auditInput, this.converter);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "audit"), strBuilder, DataAppAgentDefinitions.__auditInput, DataAppAgentDefinitions.__auditOutput, null, invocationConfig, asyncCallback);
  }
  
  public Structure exportObfuscationMap(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId) {
    return exportObfuscationMap(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, (InvocationConfig)null);
  }
  
  public Structure exportObfuscationMap(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__exportObfuscationMapInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("object_id", objectId);
    Structure result = null;
    result = (Structure)invokeMethod(new MethodIdentifier(this.ifaceId, "export_obfuscation_map"), strBuilder, DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentDefinitions.__exportObfuscationMapOutput, null, invocationConfig);
    return result;
  }
  
  public void exportObfuscationMap(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, AsyncCallback<Structure> asyncCallback) {
    exportObfuscationMap(collectorId, collectorInstanceId, deploymentSecret, pluginType, objectId, asyncCallback, (InvocationConfig)null);
  }
  
  public void exportObfuscationMap(String collectorId, String collectorInstanceId, String deploymentSecret, String pluginType, String objectId, AsyncCallback<Structure> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppAgentDefinitions.__exportObfuscationMapInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("object_id", objectId);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "export_obfuscation_map"), strBuilder, DataAppAgentDefinitions.__exportObfuscationMapInput, DataAppAgentDefinitions.__exportObfuscationMapOutput, null, invocationConfig, asyncCallback);
  }
}
