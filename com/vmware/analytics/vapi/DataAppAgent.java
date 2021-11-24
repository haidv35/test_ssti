package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;

public interface DataAppAgent extends Service, DataAppAgentTypes {
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec);
  
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec, InvocationConfig paramInvocationConfig);
  
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec, AsyncCallback<Void> paramAsyncCallback);
  
  void create(String paramString1, String paramString2, String paramString3, String paramString4, DataAppAgentTypes.CreateSpec paramCreateSpec, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4, InvocationConfig paramInvocationConfig);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4, AsyncCallback<Void> paramAsyncCallback);
  
  void delete(String paramString1, String paramString2, String paramString3, String paramString4, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  Structure get(String paramString1, String paramString2, String paramString3, String paramString4);
  
  Structure get(String paramString1, String paramString2, String paramString3, String paramString4, InvocationConfig paramInvocationConfig);
  
  void get(String paramString1, String paramString2, String paramString3, String paramString4, AsyncCallback<Structure> paramAsyncCallback);
  
  void get(String paramString1, String paramString2, String paramString3, String paramString4, AsyncCallback<Structure> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  Structure execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7);
  
  Structure execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7, InvocationConfig paramInvocationConfig);
  
  void execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7, AsyncCallback<Structure> paramAsyncCallback);
  
  void execute(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, Boolean paramBoolean, String paramString7, AsyncCallback<Structure> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  Structure collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec);
  
  Structure collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec, InvocationConfig paramInvocationConfig);
  
  void collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec, AsyncCallback<Structure> paramAsyncCallback);
  
  void collect(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, DataAppAgentTypes.CollectRequestSpec paramCollectRequestSpec, AsyncCallback<Structure> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  DataAppAgentTypes.AuditResult audit();
  
  DataAppAgentTypes.AuditResult audit(InvocationConfig paramInvocationConfig);
  
  void audit(AsyncCallback<DataAppAgentTypes.AuditResult> paramAsyncCallback);
  
  void audit(AsyncCallback<DataAppAgentTypes.AuditResult> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  Structure exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5);
  
  Structure exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, InvocationConfig paramInvocationConfig);
  
  void exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, AsyncCallback<Structure> paramAsyncCallback);
  
  void exportObfuscationMap(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, AsyncCallback<Structure> paramAsyncCallback, InvocationConfig paramInvocationConfig);
}
