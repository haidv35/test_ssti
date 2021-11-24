package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.InvocationContext;

@ProviderFor(DataAppAgentSyncApiInterface.class)
public interface DataAppAgentSyncProvider extends Service, DataAppAgentTypes {
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec, InvocationContext paramInvocationContext);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4, InvocationContext paramInvocationContext);
  
  Structure get(String paramString1, String paramString2, String paramString3, String paramString4, InvocationContext paramInvocationContext);
  
  Structure execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7, InvocationContext paramInvocationContext);
  
  Structure collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec, InvocationContext paramInvocationContext);
  
  DataAppAgentTypes.AuditResult audit(InvocationContext paramInvocationContext);
  
  Structure exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, InvocationContext paramInvocationContext);
}
