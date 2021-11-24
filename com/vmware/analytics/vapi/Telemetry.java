package com.vmware.analytics.vapi;

import com.vmware.vapi.bindings.Service;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.bindings.client.AsyncCallback;
import com.vmware.vapi.bindings.client.InvocationConfig;
import java.util.List;

public interface Telemetry extends Service, TelemetryTypes {
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure);
  
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure, InvocationConfig paramInvocationConfig);
  
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure, AsyncCallback<Void> paramAsyncCallback);
  
  void send(String paramString1, String paramString2, String paramString3, Structure paramStructure, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList, InvocationConfig paramInvocationConfig);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList, AsyncCallback<Void> paramAsyncCallback);
  
  void sendMultiple(String paramString1, String paramString2, String paramString3, List<Structure> paramList, AsyncCallback<Void> paramAsyncCallback, InvocationConfig paramInvocationConfig);
  
  String getLevel(String paramString1, String paramString2);
  
  String getLevel(String paramString1, String paramString2, InvocationConfig paramInvocationConfig);
  
  void getLevel(String paramString1, String paramString2, AsyncCallback<String> paramAsyncCallback);
  
  void getLevel(String paramString1, String paramString2, AsyncCallback<String> paramAsyncCallback, InvocationConfig paramInvocationConfig);
}
