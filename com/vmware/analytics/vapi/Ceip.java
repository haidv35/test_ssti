package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;

public interface Ceip extends Service, CeipTypes {
  boolean get();
  
  boolean get(InvocationConfig paramInvocationConfig);
  
  void get(AsyncCallback<Boolean> paramAsyncCallback);
  
  void get(AsyncCallback<Boolean> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  void set(boolean paramBoolean);
  
  void set(boolean paramBoolean, InvocationConfig paramInvocationConfig);
  
  void set(boolean paramBoolean, AsyncCallback<Void> paramAsyncCallback);
  
  void set(boolean paramBoolean, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
}
