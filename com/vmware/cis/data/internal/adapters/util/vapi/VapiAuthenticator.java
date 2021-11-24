package com.vmware.cis.data.internal.adapters.util.vapi;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;

public interface VapiAuthenticator {
  VapiSession login(AuthenticationTokenSource paramAuthenticationTokenSource);
}
