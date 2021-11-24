package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.InvocationContext;

@ProviderFor(DataAppSyncApiInterface.class)
public interface DataAppSyncProvider extends Service, DataAppTypes {
  void send(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, byte[] paramArrayOfbyte, InvocationContext paramInvocationContext);
  
  Structure getResults(String paramString1, String paramString2, String paramString3, String paramString4, Long paramLong, String paramString5, String paramString6, InvocationContext paramInvocationContext);
}
