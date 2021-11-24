package com.vmware.ph.phservice.common.vim.vc;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.vim.internal.vc.impl.VcClientFactoryImpl;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VcClientBuilder implements Builder<VcClient> {
  private static final Log _logger = LogFactory.getLog(VcClientBuilder.class);
  
  private final VcClientFactoryImpl _vcClientFactory;
  
  private URI _vcSdkEndpoint;
  
  private SsoTokenProvider _ssoTokenProvider;
  
  private String _vcUsername;
  
  private char[] _vcPassword;
  
  public static VcClientBuilder forVcVersion(Class<?> versionClass, ExecutorService threadPool) {
    return new VcClientBuilder(versionClass, threadPool);
  }
  
  private VcClientBuilder(Class<?> versionClass, ExecutorService threadPool) {
    this._vcClientFactory = new VcClientFactoryImpl("vim25", versionClass, threadPool);
    this._vcClientFactory.setTimeoutMs(Integer.valueOf(180000));
  }
  
  public VcClientBuilder withTimeoutMs(int timeoutMs) {
    this._vcClientFactory.setTimeoutMs(Integer.valueOf(timeoutMs));
    return this;
  }
  
  public VcClientBuilder withVcSdkUri(URI vcSdkUri) {
    this._vcSdkEndpoint = vcSdkUri;
    return this;
  }
  
  public VcClientBuilder withTrust(KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._vcClientFactory.setTrustStore(trustStore);
    this._vcClientFactory.setThumbprintVerifier(thumbprintVerifier);
    return this;
  }
  
  public VcClientBuilder withSsoTokenProvider(SsoTokenProvider ssoTokenProvider) {
    this._ssoTokenProvider = ssoTokenProvider;
    return this;
  }
  
  public VcClientBuilder withVcCredentials(String vcUsername, char[] vcPassword) {
    this._vcUsername = vcUsername;
    this._vcPassword = vcPassword;
    return this;
  }
  
  public VcClient build() {
    VcClient vcClient = null;
    if (this._ssoTokenProvider != null) {
      vcClient = this._vcClientFactory.connectVcWithSamlToken(this._vcSdkEndpoint, this._ssoTokenProvider, null);
    } else if (this._vcUsername != null) {
      vcClient = this._vcClientFactory.connectVcAsUser(this._vcSdkEndpoint, this._vcUsername, this._vcPassword, null);
    } else {
      vcClient = this._vcClientFactory.connectVcAnnonymous(this._vcSdkEndpoint, null);
    } 
    if (vcClient != null)
      _logger.info("VC Client created with vmodl version " + vcClient.getVmodlVersion()); 
    return vcClient;
  }
}
