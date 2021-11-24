package com.vmware.ph.phservice.common.cis;

import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.LookupSsoEndpointProviderImpl;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderBuilder;
import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;

public class CisContextBuilder {
  private final KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private boolean _isLegacyVim;
  
  private final URI _lookupSdkUri;
  
  private Boolean _shouldUseEnvoySidecar = Boolean.valueOf(true);
  
  private String _ssoUsername;
  
  private char[] _ssoPassword;
  
  private X509Certificate _solutionCertificate;
  
  private Key _solutionPrivateKey;
  
  private SsoTokenProvider.TokenKeyPair _tokenKeyPair;
  
  private KeyStore _solutionKeyStore;
  
  private String _solutionKeyAlias;
  
  private char[] _solutionKeyPassword;
  
  private ApplianceContext _applianceContext;
  
  private CisContextBuilder(URI lookupSdkUri, KeyStore trustStore) {
    this._lookupSdkUri = lookupSdkUri;
    this._trustStore = trustStore;
    this._isLegacyVim = false;
  }
  
  public static CisContextBuilder forCis(URI lookupSdkUri, KeyStore trustStore) {
    return new CisContextBuilder(lookupSdkUri, trustStore);
  }
  
  public CisContextBuilder withTrust(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
    return this;
  }
  
  public CisContextBuilder shouldUseEnvoySidecar(Boolean shouldUseEnvoySidecar) {
    if (shouldUseEnvoySidecar != null)
      this._shouldUseEnvoySidecar = shouldUseEnvoySidecar; 
    return this;
  }
  
  public CisContextBuilder withSsoUser(String ssoUsername, char[] ssoPassword) {
    this._ssoUsername = ssoUsername;
    this._ssoPassword = ssoPassword;
    return this;
  }
  
  public CisContextBuilder withSamlTokenAndPrivateKey(SamlToken samlToken, PrivateKey privateKey) {
    this._tokenKeyPair = new SsoTokenProvider.TokenKeyPair();
    this._tokenKeyPair.token = samlToken;
    this._tokenKeyPair.key = privateKey;
    return this;
  }
  
  public CisContextBuilder withSsoSolutionUser(X509Certificate solutionCertificate, Key solutionPrivateKey) {
    this._solutionCertificate = solutionCertificate;
    this._solutionPrivateKey = solutionPrivateKey;
    return this;
  }
  
  public CisContextBuilder withSsoSolutionUser(KeyStore solutionKeyStore, String solutionKeyAlias, char[] solutionKeyPassword) {
    this._solutionKeyStore = solutionKeyStore;
    this._solutionKeyAlias = solutionKeyAlias;
    this._solutionKeyPassword = solutionKeyPassword;
    return this;
  }
  
  public CisContextBuilder withApplianceContext(ApplianceContext applianceContext) {
    this._applianceContext = applianceContext;
    return this;
  }
  
  public CisContext build() {
    CisContext cisContext = null;
    LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forAutoLookupVersion(Executors.newSingleThreadExecutor()).withTrust(this._trustStore, this._thumbprintVerifier).withLookupSdkUri(this._lookupSdkUri);
    SsoTokenProvider ssoTokenProvider = createSsoTokenProvider(lookupClientBuilder);
    cisContext = new CisContext(this._trustStore, this._lookupSdkUri, ssoTokenProvider, this._applianceContext);
    cisContext.setThumbprintVerifier(this._thumbprintVerifier);
    cisContext.setShouldUseEnvoySidecar(this._shouldUseEnvoySidecar.booleanValue());
    return cisContext;
  }
  
  private SsoTokenProvider createSsoTokenProvider(LookupClientBuilder lookupClientBuilder) {
    if (this._isLegacyVim)
      return null; 
    LookupSsoEndpointProviderImpl ssoEndpointProvider = new LookupSsoEndpointProviderImpl(lookupClientBuilder, this._shouldUseEnvoySidecar.booleanValue());
    SsoTokenProviderBuilder ssoTokenProviderBuilder = SsoTokenProviderBuilder.forSsoService(ssoEndpointProvider).withTrust(this._trustStore, this._thumbprintVerifier);
    if (this._ssoUsername != null) {
      ssoTokenProviderBuilder.withSsoUser(this._ssoUsername, this._ssoPassword);
    } else if (this._tokenKeyPair != null) {
      ssoTokenProviderBuilder.withTokenKeyPair(this._tokenKeyPair);
    } else if (this._solutionCertificate != null) {
      ssoTokenProviderBuilder.withSsoSolutionUser(this._solutionCertificate, this._solutionPrivateKey);
    } else if (this._solutionKeyStore != null) {
      ssoTokenProviderBuilder.withSsoSolutionUser(this._solutionKeyStore, this._solutionKeyAlias, this._solutionKeyPassword);
    } else {
      throw new IllegalArgumentException("Missing SSO user");
    } 
    return ssoTokenProviderBuilder.build();
  }
}
