package com.vmware.cis.data.internal.adapters.vmomi;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;

public interface VmomiAuthenticator {
  VmomiSession login(AuthenticationTokenSource paramAuthenticationTokenSource);
}
