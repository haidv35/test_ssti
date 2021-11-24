package com.vmware.cis.data.internal.adapters.customfield;

import com.vmware.cis.data.internal.adapters.vmomi.VmomiAuthenticator;
import com.vmware.cis.data.internal.adapters.vmomi.impl.HttpConfigurationFactory;
import com.vmware.cis.data.internal.adapters.vmomi.impl.VlsiClientUtil;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vim.version.internal.version11;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.KeyStore;
import org.apache.commons.lang.Validate;

public final class CustomFieldDataProviderConnector implements DataProviderConnector {
  private static Class<?> REQUIRED_VIM_VERSION = version11.class;
  
  private final VmodlContext vmodlContext;
  
  private final HttpConfigurationFactory httpConfigFactory;
  
  private final URI endpointAddress;
  
  private final KeyStore trustStore;
  
  private final String serverGuid;
  
  private final VmomiAuthenticator authenticator;
  
  public CustomFieldDataProviderConnector(VmodlContext vmodlContext, HttpConfigurationFactory httpConfigFactory, URI endpointAddress, KeyStore trustStore, String serverGuid, VmomiAuthenticator authenticator) {
    Validate.notNull(vmodlContext);
    Validate.notNull(httpConfigFactory);
    Validate.notNull(endpointAddress);
    Validate.notNull(serverGuid);
    Validate.notNull(authenticator);
    this.vmodlContext = vmodlContext;
    this.httpConfigFactory = httpConfigFactory;
    this.endpointAddress = endpointAddress;
    this.trustStore = trustStore;
    this.serverGuid = serverGuid;
    this.authenticator = authenticator;
  }
  
  public DataProviderConnection getConnection(AuthenticationTokenSource authn) {
    Validate.notNull(authn);
    HttpConfiguration httpConfig = this.httpConfigFactory.createConfiguration(this.endpointAddress, this.trustStore);
    final Client vlsiClient = VlsiClientUtil.createAuthenticatedVlsiClient(this.endpointAddress, httpConfig, this.vmodlContext, this.authenticator, authn, REQUIRED_VIM_VERSION);
    final DataProvider customFieldProvider = new CustomFieldDataProviderImpl(vlsiClient, this.serverGuid);
    return new DataProviderConnection() {
        public void close() throws Exception {
          vlsiClient.shutdown();
        }
        
        public DataProvider getDataProvider() {
          return customFieldProvider;
        }
      };
  }
  
  public String toString() {
    return getClass().getSimpleName() + "(url=" + this.endpointAddress + ")";
  }
}
