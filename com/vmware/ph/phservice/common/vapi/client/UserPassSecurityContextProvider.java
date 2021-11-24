package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.cis.authn.SecurityContextFactory;
import com.vmware.vapi.core.ExecutionContext;

public class UserPassSecurityContextProvider implements SecurityContextProvider {
  private final String _user;
  
  private final char[] _password;
  
  public UserPassSecurityContextProvider(String user, char[] password) {
    this._user = user;
    this._password = password;
  }
  
  public ExecutionContext.SecurityContext getSecurityContext(StubFactory stubFactory) throws Exception {
    return SecurityContextFactory.createUserPassSecurityContext(this._user, this._password);
  }
  
  public void deleteSecurityContext(StubFactory stubFactory, ExecutionContext.SecurityContext securityContext) throws Exception {}
}
