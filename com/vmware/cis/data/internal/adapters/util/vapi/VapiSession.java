package com.vmware.cis.data.internal.adapters.util.vapi;

public interface VapiSession {
  char[] get();
  
  char[] renew(char[] paramArrayOfchar);
  
  void logout();
}
