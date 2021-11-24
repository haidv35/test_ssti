package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.InvocationContext;

@ProviderFor(CeipSyncApiInterface.class)
public interface CeipSyncProvider extends Service, CeipTypes {
  boolean get(InvocationContext paramInvocationContext);
  
  void set(boolean paramBoolean, InvocationContext paramInvocationContext);
}
