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
import java.util.List;

public class TelemetryStub extends Stub implements Telemetry {
  public TelemetryStub(ApiProvider apiProvider, TypeConverter typeConverter, StubConfigurationBase config) {
    super(apiProvider, typeConverter, new InterfaceIdentifier("com.vmware.analytics.telemetry"), config);
  }
  
  public TelemetryStub(ApiProvider apiProvider, StubConfigurationBase config) {
    this(apiProvider, (TypeConverter)null, config);
  }
  
  public void send(String collectorId, String collectorInstanceId, String version, Structure record) {
    send(collectorId, collectorInstanceId, version, record, (InvocationConfig)null);
  }
  
  public void send(String collectorId, String collectorInstanceId, String version, Structure record, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__sendInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("version", version);
    strBuilder.addStructField("record", record);
    invokeMethod(new MethodIdentifier(this.ifaceId, "send"), strBuilder, TelemetryDefinitions.__sendInput, TelemetryDefinitions.__sendOutput, null, invocationConfig);
  }
  
  public void send(String collectorId, String collectorInstanceId, String version, Structure record, AsyncCallback<Void> asyncCallback) {
    send(collectorId, collectorInstanceId, version, record, asyncCallback, (InvocationConfig)null);
  }
  
  public void send(String collectorId, String collectorInstanceId, String version, Structure record, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__sendInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("version", version);
    strBuilder.addStructField("record", record);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "send"), strBuilder, TelemetryDefinitions.__sendInput, TelemetryDefinitions.__sendOutput, null, invocationConfig, asyncCallback);
  }
  
  public void sendMultiple(String collectorId, String collectorInstanceId, String version, List<Structure> records) {
    sendMultiple(collectorId, collectorInstanceId, version, records, (InvocationConfig)null);
  }
  
  public void sendMultiple(String collectorId, String collectorInstanceId, String version, List<Structure> records, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__sendMultipleInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("version", version);
    strBuilder.addStructField("records", records);
    invokeMethod(new MethodIdentifier(this.ifaceId, "send_multiple"), strBuilder, TelemetryDefinitions.__sendMultipleInput, TelemetryDefinitions.__sendMultipleOutput, null, invocationConfig);
  }
  
  public void sendMultiple(String collectorId, String collectorInstanceId, String version, List<Structure> records, AsyncCallback<Void> asyncCallback) {
    sendMultiple(collectorId, collectorInstanceId, version, records, asyncCallback, (InvocationConfig)null);
  }
  
  public void sendMultiple(String collectorId, String collectorInstanceId, String version, List<Structure> records, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__sendMultipleInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    strBuilder.addStructField("version", version);
    strBuilder.addStructField("records", records);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "send_multiple"), strBuilder, TelemetryDefinitions.__sendMultipleInput, TelemetryDefinitions.__sendMultipleOutput, null, invocationConfig, asyncCallback);
  }
  
  public String getLevel(String collectorId, String collectorInstanceId) {
    return getLevel(collectorId, collectorInstanceId, (InvocationConfig)null);
  }
  
  public String getLevel(String collectorId, String collectorInstanceId, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__getLevelInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    String result = null;
    result = (String)invokeMethod(new MethodIdentifier(this.ifaceId, "get_level"), strBuilder, TelemetryDefinitions.__getLevelInput, TelemetryDefinitions.__getLevelOutput, null, invocationConfig);
    return result;
  }
  
  public void getLevel(String collectorId, String collectorInstanceId, AsyncCallback<String> asyncCallback) {
    getLevel(collectorId, collectorInstanceId, asyncCallback, (InvocationConfig)null);
  }
  
  public void getLevel(String collectorId, String collectorInstanceId, AsyncCallback<String> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(TelemetryDefinitions.__getLevelInput, this.converter);
    strBuilder.addStructField("collector_id", collectorId);
    strBuilder.addStructField("collector_instance_id", collectorInstanceId);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "get_level"), strBuilder, TelemetryDefinitions.__getLevelInput, TelemetryDefinitions.__getLevelOutput, null, invocationConfig, asyncCallback);
  }
}
