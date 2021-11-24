package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.core.ExecutionContext;

public interface SecurityContextProvider {
  ExecutionContext.SecurityContext getSecurityContext(StubFactory paramStubFactory) throws Exception;
  
  void deleteSecurityContext(StubFactory paramStubFactory, ExecutionContext.SecurityContext paramSecurityContext) throws Exception;
}
