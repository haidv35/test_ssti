package com.vmware.cis.data.internal.adapters.util.vapi;

import java.util.concurrent.Future;

public interface VapiSessionSource {
  Future<char[]> createSession();
  
  void deleteSession(char[] paramArrayOfchar);
}
