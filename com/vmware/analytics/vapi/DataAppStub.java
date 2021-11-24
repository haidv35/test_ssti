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

public class DataAppStub extends Stub implements DataApp {
  public DataAppStub(ApiProvider apiProvider, TypeConverter typeConverter, StubConfigurationBase config) {
    super(apiProvider, typeConverter, new InterfaceIdentifier("com.vmware.analytics.data_app"), config);
  }
  
  public DataAppStub(ApiProvider apiProvider, StubConfigurationBase config) {
    this(apiProvider, (TypeConverter)null, config);
  }
  
  public void send(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String pluginType, String dataType, String objectId, byte[] payload) {
    send(collectorId, collectorInstanceId, collectionId, deploymentSecret, pluginType, dataType, objectId, payload, (InvocationConfig)null);
  }
  
  public void send(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String pluginType, String dataType, String objectId, byte[] payload, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppDefinitions.__sendInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("collection_id", collectionId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("payload", payload);
    invokeMethod(new MethodIdentifier(this.ifaceId, "send"), strBuilder, DataAppDefinitions.__sendInput, DataAppDefinitions.__sendOutput, null, invocationConfig);
  }
  
  public void send(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String pluginType, String dataType, String objectId, byte[] payload, AsyncCallback<Void> asyncCallback) {
    send(collectorId, collectorInstanceId, collectionId, deploymentSecret, pluginType, dataType, objectId, payload, asyncCallback, (InvocationConfig)null);
  }
  
  public void send(String collectorId, String collectorInstanceId, String collectionId, String deploymentSecret, String pluginType, String dataType, String objectId, byte[] payload, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppDefinitions.__sendInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("collection_id", collectionId);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("payload", payload);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "send"), strBuilder, DataAppDefinitions.__sendInput, DataAppDefinitions.__sendOutput, null, invocationConfig, asyncCallback);
  }
  
  public Structure getResults(String collectorId, String collectorInstanceId, String dataType, String objectId, Long sinceTimestamp, String pluginType, String deploymentSecret) {
    return getResults(collectorId, collectorInstanceId, dataType, objectId, sinceTimestamp, pluginType, deploymentSecret, (InvocationConfig)null);
  }
  
  public Structure getResults(String collectorId, String collectorInstanceId, String dataType, String objectId, Long sinceTimestamp, String pluginType, String deploymentSecret, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppDefinitions.__getResultsInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("since_timestamp", sinceTimestamp);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    Structure result = null;
    result = (Structure)invokeMethod(new MethodIdentifier(this.ifaceId, "get_results"), strBuilder, DataAppDefinitions.__getResultsInput, DataAppDefinitions.__getResultsOutput, null, invocationConfig);
    return result;
  }
  
  public void getResults(String collectorId, String collectorInstanceId, String dataType, String objectId, Long sinceTimestamp, String pluginType, String deploymentSecret, AsyncCallback<Structure> asyncCallback) {
    getResults(collectorId, collectorInstanceId, dataType, objectId, sinceTimestamp, pluginType, deploymentSecret, asyncCallback, (InvocationConfig)null);
  }
  
  public void getResults(String collectorId, String collectorInstanceId, String dataType, String objectId, Long sinceTimestamp, String pluginType, String deploymentSecret, AsyncCallback<Structure> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(DataAppDefinitions.__getResultsInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("data_type", dataType);
    strBuilder.addStructField("object_id", objectId);
    strBuilder.addStructField("since_timestamp", sinceTimestamp);
    strBuilder.addStructField("plugin_type", pluginType);
    strBuilder.addStructField("deployment_secret", deploymentSecret);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "get_results"), strBuilder, DataAppDefinitions.__getResultsInput, DataAppDefinitions.__getResultsOutput, null, invocationConfig, asyncCallback);
  }
}
