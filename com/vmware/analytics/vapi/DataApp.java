package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;

public interface DataApp extends Service, DataAppTypes {
  void send(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, byte[] paramArrayOfbyte);
  
  void send(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, byte[] paramArrayOfbyte, InvocationConfig paramInvocationConfig);
  
  void send(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, byte[] paramArrayOfbyte, AsyncCallback<Void> paramAsyncCallback);
  
  void send(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, byte[] paramArrayOfbyte, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  Structure getResults(String paramString1, String paramString2, String paramString3, String paramString4, Long paramLong, String paramString5, String paramString6);
  
  Structure getResults(String paramString1, String paramString2, String paramString3, String paramString4, Long paramLong, String paramString5, String paramString6, InvocationConfig paramInvocationConfig);
  
  void getResults(String paramString1, String paramString2, String paramString3, String paramString4, Long paramLong, String paramString5, String paramString6, AsyncCallback<Structure> paramAsyncCallback);
  
  void getResults(String paramString1, String paramString2, String paramString3, String paramString4, Long paramLong, String paramString5, String paramString6, AsyncCallback<Structure> paramAsyncCallback, InvocationConfig paramInvocationConfig);
}
