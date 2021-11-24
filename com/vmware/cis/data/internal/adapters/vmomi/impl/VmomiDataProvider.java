package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.adapters.property.BackCompatPropertyProviders;
import com.vmware.cis.data.internal.adapters.vmomi.VmomiAuthenticator;
import com.vmware.cis.data.internal.adapters.vmomi.VmomiDataProviderConfig;
import com.vmware.cis.data.internal.adapters.vmomi.util.VmomiVersion;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCacheFactory;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vim.version.internal.versions;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VmomiDataProvider implements DataProviderConnector {
  private static final Logger _logger = LoggerFactory.getLogger(VmomiDataProvider.class);
  
  private final VmomiDataProviderConfig _providerCfg;
  
  private final URI _endpointAddress;
  
  private final KeyStore _trustStore;
  
  private final VmomiAuthenticator _authenticator;
  
  private final QuerySchemaCache _schemaCache;
  
  private final String _serviceTypeAndVersion;
  
  private final ExecutorService _executor;
  
  private final Class<?> _vmomiVersion;
  
  public VmomiDataProvider(VmomiDataProviderConfig providerCfg, URI endpointAddress, KeyStore trustStore, VmomiAuthenticator authenticator) {
    this(providerCfg, endpointAddress, trustStore, authenticator, 
        QuerySchemaCacheFactory.createNoOpCache(), "", null, null);
  }
  
  public VmomiDataProvider(VmomiDataProviderConfig providerCfg, URI endpointAddress, KeyStore trustStore, VmomiAuthenticator authenticator, QuerySchemaCache schemaCache, String serviceTypeAndVersion, ExecutorService executor, Class<?> vcVmomiVersion) {
    assert providerCfg != null;
    assert endpointAddress != null;
    assert authenticator != null;
    assert schemaCache != null;
    assert serviceTypeAndVersion != null;
    this._providerCfg = providerCfg;
    this._endpointAddress = endpointAddress;
    this._trustStore = trustStore;
    this._authenticator = authenticator;
    this._schemaCache = schemaCache;
    this._executor = executor;
    this._serviceTypeAndVersion = serviceTypeAndVersion;
    if (vcVmomiVersion == null)
      vcVmomiVersion = versions.VIM_VERSION_LTS; 
    this._vmomiVersion = VmomiVersionMapper.getVersion(vcVmomiVersion, this._providerCfg
        .getVmomiVersion().equals(VmomiVersion.NEWEST));
    if (_logger.isDebugEnabled()) {
      VmodlVersion vmodlVersion = this._providerCfg.getVmodlContext().getVmodlVersionMap().getVersion(this._vmomiVersion);
      String versionId = (vmodlVersion == null) ? null : vmodlVersion.getVersionId();
      _logger.debug("Using {} version for endpoint {}", versionId, this._endpointAddress);
    } 
  }
  
  public DataProviderConnection getConnection(AuthenticationTokenSource credentials) {
    Validate.notNull(credentials);
    HttpConfiguration httpConfig = this._providerCfg.getVlsiHttpConfigFactory().createConfiguration(this._endpointAddress, this._trustStore);
    final Client vlsiClient = VlsiClientUtil.createAuthenticatedVlsiClient(this._endpointAddress, httpConfig, this._providerCfg

        
        .getVmodlContext(), this._authenticator, credentials, this._vmomiVersion);
    DataProvider adapter = new VmomiDataProviderConnection(vlsiClient, this._schemaCache, this._serviceTypeAndVersion);
    DataProvider withNotInSupport = new NotInSupportDataProvider(adapter);
    DataProvider withBackCompat = withBackCompat(withNotInSupport, vlsiClient, this._executor);
    final DataProvider vmomiProvider = withBackCompat;
    return new DataProviderConnection() {
        public void close() throws Exception {
          vlsiClient.shutdown();
        }
        
        public DataProvider getDataProvider() {
          return vmomiProvider;
        }
      };
  }
  
  public String toString() {
    return getClass().getSimpleName() + "(url=" + this._endpointAddress + ")";
  }
  
  private static DataProvider withBackCompat(DataProvider provider, Client vlsiClient, ExecutorService executor) {
    if (executor == null)
      return provider; 
    return BackCompatPropertyProviders.withVmomiBackCompat(provider, vlsiClient, executor);
  }
}
