package com.vmware.ph.phservice.provider.vcenter.contentlibrary;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.vim.sso.client.SamlToken;
import java.security.PrivateKey;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SsoTokenProviderAuthenticationTokenSource implements AuthenticationTokenSource {
  private static final Log _log = LogFactory.getLog(SsoTokenProviderAuthenticationTokenSource.class);
  
  private final SsoTokenProvider _ssoTokenProvider;
  
  public SsoTokenProviderAuthenticationTokenSource(SsoTokenProvider ssoTokenProvider) {
    this
      ._ssoTokenProvider = Objects.<SsoTokenProvider>requireNonNull(ssoTokenProvider, "The SSO token provider must not be null.");
  }
  
  public SamlToken getAuthenticationToken() {
    SamlToken token = null;
    try {
      token = (this._ssoTokenProvider.getToken()).token;
    } catch (SsoTokenProviderException e) {
      _log.warn("SAML token could not be acuqired.");
    } 
    return token;
  }
  
  public PrivateKey getConfirmationKey() {
    PrivateKey privateKey = null;
    try {
      privateKey = (this._ssoTokenProvider.getToken()).key;
    } catch (SsoTokenProviderException e) {
      _log.warn("Provate Key could not be acuqired.");
    } 
    return privateKey;
  }
}
