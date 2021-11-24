package com.vmware.ph.phservice.common.cis.sso;

import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpointProvider;
import com.vmware.ph.phservice.common.cis.internal.sso.StsClientFactory;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.SsoBearerTokenProviderImpl;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.SsoFixedTokenProviderImpl;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.SsoHoKTokenProviderImpl;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.StsClientFactoryImpl;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class SsoTokenProviderBuilder {
  private final SsoEndpointProvider _ssoEndpointProvider;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private String _ssoUsername;
  
  private char[] _ssoPassword;
  
  private X509Certificate _solutionCertificate;
  
  private Key _solutionPrivateKey;
  
  private SsoTokenProvider.TokenKeyPair _tokenKeyPair;
  
  private KeyStore _solutionKeyStore;
  
  private String _solutionKeyAlias;
  
  private char[] _solutionKeyPassword;
  
  public static SsoTokenProviderBuilder forSsoService(SsoEndpointProvider ssoEndpointProvider) {
    return new SsoTokenProviderBuilder(ssoEndpointProvider);
  }
  
  public SsoTokenProviderBuilder(SsoEndpointProvider ssoEndpointProvider) {
    this._ssoEndpointProvider = ssoEndpointProvider;
  }
  
  public SsoTokenProviderBuilder withTrust(KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._trustStore = trustStore;
    this._thumbprintVerifier = thumbprintVerifier;
    return this;
  }
  
  public SsoTokenProviderBuilder withTokenKeyPair(SsoTokenProvider.TokenKeyPair tokenKeyPair) {
    this._tokenKeyPair = tokenKeyPair;
    return this;
  }
  
  public SsoTokenProviderBuilder withSsoUser(String ssoUsername, char[] ssoPassword) {
    this._ssoUsername = ssoUsername;
    this._ssoPassword = ssoPassword;
    return this;
  }
  
  public SsoTokenProviderBuilder withSsoSolutionUser(X509Certificate solutionCertificate, Key solutionPrivateKey) {
    this._solutionCertificate = solutionCertificate;
    this._solutionPrivateKey = solutionPrivateKey;
    return this;
  }
  
  public SsoTokenProviderBuilder withSsoSolutionUser(KeyStore solutionKeyStore, String solutionKeyAlias, char[] solutionKeyPassword) {
    this._solutionKeyStore = solutionKeyStore;
    this._solutionKeyAlias = solutionKeyAlias;
    this._solutionKeyPassword = solutionKeyPassword;
    return this;
  }
  
  public SsoTokenProvider build() {
    StsClientFactory stsClientFactory = new StsClientFactoryImpl(this._ssoEndpointProvider, this._trustStore, this._thumbprintVerifier, null);
    SsoTokenProvider ssoTokenProvider = null;
    if (this._ssoUsername != null) {
      ssoTokenProvider = new SsoBearerTokenProviderImpl(stsClientFactory, this._ssoUsername, this._ssoPassword);
    } else if (this._tokenKeyPair != null) {
      ssoTokenProvider = new SsoFixedTokenProviderImpl(this._tokenKeyPair);
    } else if (this._solutionCertificate != null) {
      ssoTokenProvider = new SsoHoKTokenProviderImpl(stsClientFactory, this._solutionCertificate, this._solutionPrivateKey);
    } else {
      ssoTokenProvider = new SsoHoKTokenProviderImpl(stsClientFactory, this._solutionKeyStore, this._solutionKeyAlias, this._solutionKeyPassword);
    } 
    return ssoTokenProvider;
  }
}
