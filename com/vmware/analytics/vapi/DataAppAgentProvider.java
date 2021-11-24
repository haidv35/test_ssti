package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.annotation.ProviderFor;
import com.vmware.vapi.bindings.server.AsyncContext;

@ProviderFor(DataAppAgentApiInterface.class)
public interface DataAppAgentProvider extends Service, DataAppAgentTypes {
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec, AsyncContext<Void> paramAsyncContext);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4, AsyncContext<Void> paramAsyncContext);
  
  void get(String paramString1, String paramString2, String paramString3, String paramString4, AsyncContext<Structure> paramAsyncContext);
  
  void execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7, AsyncContext<Structure> paramAsyncContext);
  
  void collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec, AsyncContext<Structure> paramAsyncContext);
  
  void audit(AsyncContext<DataAppAgentTypes.AuditResult> paramAsyncContext);
  
  void exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, AsyncContext<Structure> paramAsyncContext);
}
