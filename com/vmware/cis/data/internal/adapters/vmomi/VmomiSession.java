package com.vmware.cis.data.internal.adapters.vmomi;

public interface VmomiSession {
  String getSessionCookie();
  
  String renewSessionCookie(String paramString);
  
  void logout();
}
