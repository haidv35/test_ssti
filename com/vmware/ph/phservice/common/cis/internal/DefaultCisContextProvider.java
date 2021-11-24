package com.vmware.ph.phservice.common.cis.internal;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextBuilder;
import com.vmware.ph.phservice.common.cis.CisContextProvider;
import com.vmware.ph.phservice.common.internal.security.TrustStoreHack;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.AllowAllThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;

public class DefaultCisContextProvider implements CisContextProvider {
  private final URI _lookupServiceUri;
  
  private final String _ssoUserName;
  
  private final String _ssoUserPassword;
  
  private final Boolean _shouldUseEnvoySidecar;
  
  public DefaultCisContextProvider(URI lookupServiceUri, String ssoUserName, String ssoUserPassword, Boolean shouldUseEnvoySidecar) {
    this._lookupServiceUri = lookupServiceUri;
    this._ssoUserName = ssoUserName;
    this._ssoUserPassword = ssoUserPassword;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public CisContext getCisContext() {
    KeyStore trustStore = TrustStoreHack.getTrustStore(new URI[] { this._lookupServiceUri });
    CisContext cisContext = CisContextBuilder.forCis(this._lookupServiceUri, trustStore).withTrust((ThumbprintVerifier)new AllowAllThumbprintVerifier()).withSsoUser(this._ssoUserName, this._ssoUserPassword.toCharArray()).shouldUseEnvoySidecar(this._shouldUseEnvoySidecar).build();
    return cisContext;
  }
}
