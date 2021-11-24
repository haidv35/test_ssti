package com.vmware.cis.data.internal.provider;

public interface DataProviderConnector {
  DataProviderConnection getConnection(AuthenticationTokenSource paramAuthenticationTokenSource);
}
