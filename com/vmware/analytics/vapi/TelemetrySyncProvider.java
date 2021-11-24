package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.InvocationContext;
import java.util.List;

@ProviderFor(TelemetrySyncApiInterface.class)
public interface TelemetrySyncProvider extends Service, TelemetryTypes {
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure, InvocationContext paramInvocationContext);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList, InvocationContext paramInvocationContext);
  
  String getLevel(String paramString1, String paramString2, InvocationContext paramInvocationContext);
}
