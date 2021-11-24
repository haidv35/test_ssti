package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.AsyncContext;
import java.util.List;

@ProviderFor(TelemetryApiInterface.class)
public interface TelemetryProvider extends Service, TelemetryTypes {
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure, AsyncContext<Void> paramAsyncContext);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList, AsyncContext<Void> paramAsyncContext);
  
  void getLevel(String paramString1, String paramString2, AsyncContext<String> paramAsyncContext);
}
