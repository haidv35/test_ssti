package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.AsyncContext;

@ProviderFor(CeipApiInterface.class)
public interface CeipProvider extends Service, CeipTypes {
  void get(AsyncContext<Boolean> paramAsyncContext);
  
  void set(boolean paramBoolean, AsyncContext<Void> paramAsyncContext);
}
