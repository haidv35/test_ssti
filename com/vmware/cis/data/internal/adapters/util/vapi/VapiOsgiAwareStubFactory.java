package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.core.ApiProvider;

public final class VapiOsgiAwareStubFactory {
  private final StubFactory _stubFactory;
  
  public VapiOsgiAwareStubFactory(ApiProvider api) {
    assert api != null;
    this._stubFactory = new StubFactory(api);
  }
  
  public <T extends com.vmware.vapi.bindings.Service> T createStub(Class<T> vapiIface) {
    assert vapiIface != null;
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(
        getClass().getClassLoader());
    try {
      return (T)this._stubFactory.createStub(vapiIface);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
}
