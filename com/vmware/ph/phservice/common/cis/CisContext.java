package com.vmware.ph.phservice.common.cis;

import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.Executors;

public class CisContext {
  private final KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private final URI _lookupSdkUri;
  
  private boolean _shouldUseEnvoySidecar;
  
  private final SsoTokenProvider _ssoTokenProvider;
  
  private final ApplianceContext _applianceContext;
  
  public CisContext(KeyStore trustStore, URI lookupSdkUri, SsoTokenProvider ssoTokenProvider) {
    this(trustStore, lookupSdkUri, ssoTokenProvider, null);
  }
  
  public CisContext(KeyStore trustStore, URI lookupSdkUri, SsoTokenProvider ssoTokenProvider, ApplianceContext applianceContext) {
    this._trustStore = trustStore;
    this._lookupSdkUri = lookupSdkUri;
    this._ssoTokenProvider = ssoTokenProvider;
    this._applianceContext = applianceContext;
  }
  
  public void setThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
  }
  
  public void setShouldUseEnvoySidecar(boolean shouldUseEnvoySidecar) {
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public SsoTokenProvider getSsoTokenProvider() {
    return this._ssoTokenProvider;
  }
  
  public LookupClientBuilder getLookupClientBuilder() {
    return 
      LookupClientBuilder.forAutoLookupVersion(Executors.newSingleThreadExecutor())
      .withTrust(this._trustStore, this._thumbprintVerifier)
      .withLookupSdkUri(this._lookupSdkUri);
  }
  
  public URI getLookupSdkUri() {
    return this._lookupSdkUri;
  }
  
  public boolean getShouldUseEnvoySidecar() {
    return this._shouldUseEnvoySidecar;
  }
  
  public KeyStore getTrustedStore() {
    return this._trustStore;
  }
  
  public ThumbprintVerifier getThumprintVerifier() {
    return this._thumbprintVerifier;
  }
  
  public ApplianceContext getApplianceContext() {
    return this._applianceContext;
  }
}
