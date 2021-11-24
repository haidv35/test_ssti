package com.vmware.ph.phservice.provider.vcenter.license.client;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.provider.vcenter.license.client.impl.LicenseClientFactoryImpl;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.HttpConfigurationImpl;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LicenseClientBuilder implements Builder<LicenseClient> {
  private URI _lsSdkUri;
  
  private LicenseClientFactory.LicenseClientAutomaticAuthenticator _licenseClientAutomaticAuthenticator;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private final LicenseClientFactoryImpl _lsClientFactory;
  
  private static final Log _log = LogFactory.getLog(LicenseClientBuilder.class);
  
  private LicenseClientBuilder(Class<?> versionClass, ExecutorService threadPool) {
    this._lsClientFactory = new LicenseClientFactoryImpl(versionClass, threadPool);
  }
  
  public static LicenseClientBuilder forLicenseClient(Class<?> versionClass, ExecutorService threadPool) {
    return new LicenseClientBuilder(versionClass, threadPool);
  }
  
  public LicenseClientBuilder withSdkUri(URI lsSdkUri) {
    this._lsSdkUri = lsSdkUri;
    return this;
  }
  
  public LicenseClientBuilder withTrustStore(KeyStore trustStore) {
    this._trustStore = trustStore;
    return this;
  }
  
  public LicenseClientBuilder withThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
    return this;
  }
  
  public LicenseClientBuilder withSsoTokenProvider(SsoTokenProvider ssoTokenProvider) {
    this._licenseClientAutomaticAuthenticator = new SsoTokenClientAutomaticAuthenticator(ssoTokenProvider);
    return this;
  }
  
  public LicenseClient build() {
    LicenseClient licenseClient = null;
    this._lsClientFactory.setClientAuthenticator(this._licenseClientAutomaticAuthenticator);
    licenseClient = this._lsClientFactory.createClient(this._lsSdkUri, createHttpConfiguration());
    return licenseClient;
  }
  
  private HttpConfiguration createHttpConfiguration() {
    HttpConfigurationImpl httpConfigurationImpl = new HttpConfigurationImpl();
    httpConfigurationImpl.setTrustStore(this._trustStore);
    httpConfigurationImpl.setThumbprintVerifier(this._thumbprintVerifier);
    httpConfigurationImpl.setTimeoutMs(180000);
    return (HttpConfiguration)httpConfigurationImpl;
  }
  
  private static class SsoTokenClientAutomaticAuthenticator implements LicenseClientFactory.LicenseClientAutomaticAuthenticator {
    private final SsoTokenProvider _ssoTokenProvider;
    
    public SsoTokenClientAutomaticAuthenticator(SsoTokenProvider ssoTokenProvider) {
      this._ssoTokenProvider = ssoTokenProvider;
    }
    
    public void login(LicenseClient licenseClient) {
      try {
        SsoTokenProvider.TokenKeyPair tokenKeyPair = this._ssoTokenProvider.getToken();
        licenseClient.login(tokenKeyPair.token, tokenKeyPair.key);
      } catch (Exception e) {
        LicenseClientBuilder._log.error("License Client login failure.", e);
        throw new LicenseClientException(e);
      } 
    }
  }
}
