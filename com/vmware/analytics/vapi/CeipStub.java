package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.StubConfigurationBase;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.core.InterfaceIdentifier;
import com.vmware.vapi.core.MethodIdentifier;
import com.vmware.vapi.internal.bindings.StructValueBuilder;
import com.vmware.vapi.internal.bindings.Stub;
import com.vmware.vapi.internal.bindings.TypeConverter;

public class CeipStub extends Stub implements Ceip {
  public CeipStub(ApiProvider apiProvider, TypeConverter typeConverter, StubConfigurationBase config) {
    super(apiProvider, typeConverter, new InterfaceIdentifier("com.vmware.analytics.ceip"), config);
  }
  
  public CeipStub(ApiProvider apiProvider, StubConfigurationBase config) {
    this(apiProvider, (TypeConverter)null, config);
  }
  
  public boolean get() {
    return get((InvocationConfig)null);
  }
  
  public boolean get(InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(CeipDefinitions.__getInput, this.converter);
    Boolean result = null;
    result = (Boolean)invokeMethod(new MethodIdentifier(this.ifaceId, "get"), strBuilder, CeipDefinitions.__getInput, CeipDefinitions.__getOutput, null, invocationConfig);
    return result.booleanValue();
  }
  
  public void get(AsyncCallback<Boolean> asyncCallback) {
    get(asyncCallback, (InvocationConfig)null);
  }
  
  public void get(AsyncCallback<Boolean> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(CeipDefinitions.__getInput, this.converter);
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "get"), strBuilder, CeipDefinitions.__getInput, CeipDefinitions.__getOutput, null, invocationConfig, asyncCallback);
  }
  
  public void set(boolean status) {
    set(status, (InvocationConfig)null);
  }
  
  public void set(boolean status, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(CeipDefinitions.__setInput, this.converter);
    strBuilder.addStructField("status", Boolean.valueOf(status));
    invokeMethod(new MethodIdentifier(this.ifaceId, "set"), strBuilder, CeipDefinitions.__setInput, CeipDefinitions.__setOutput, null, invocationConfig);
  }
  
  public void set(boolean status, AsyncCallback<Void> asyncCallback) {
    set(status, asyncCallback, (InvocationConfig)null);
  }
  
  public void set(boolean status, AsyncCallback<Void> asyncCallback, InvocationConfig invocationConfig) {
    StructValueBuilder strBuilder = new StructValueBuilder(CeipDefinitions.__setInput, this.converter);
    strBuilder.addStructField("status", Boolean.valueOf(status));
    invokeMethodAsync(new MethodIdentifier(this.ifaceId, "set"), strBuilder, CeipDefinitions.__setInput, CeipDefinitions.__setOutput, null, invocationConfig, asyncCallback);
  }
}
