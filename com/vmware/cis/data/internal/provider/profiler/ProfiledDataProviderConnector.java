package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.provider.DataProvider;

public final class ProfiledDataProviderConnector implements DataProviderConnector {
  private final DataProviderConnector _providerConnector;
  
  public ProfiledDataProviderConnector(DataProviderConnector providerConnector) {
    this._providerConnector = providerConnector;
  }
  
  public DataProviderConnection getConnection(AuthenticationTokenSource authn) {
    final DataProviderConnection profiledConnection = this._providerConnector.getConnection(authn);
    return new DataProviderConnection() {
        public void close() throws Exception {
          profiledConnection.close();
        }
        
        public DataProvider getDataProvider() {
          return ProfiledDataProvider.create(profiledConnection.getDataProvider());
        }
      };
  }
  
  public String toString() {
    return getClass().getSimpleName() + "(" + this._providerConnector.toString() + ")";
  }
}
