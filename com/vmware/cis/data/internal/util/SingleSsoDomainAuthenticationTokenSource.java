package com.vmware.cis.data.internal.util;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.MultiSsoDomainAuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.SsoDomain;
import com.vmware.vim.sso.client.SamlToken;
import java.security.PrivateKey;

public final class SingleSsoDomainAuthenticationTokenSource implements AuthenticationTokenSource {
  private final MultiSsoDomainAuthenticationTokenSource _authn;
  
  private final SsoDomain _ssoDomain;
  
  public SingleSsoDomainAuthenticationTokenSource(MultiSsoDomainAuthenticationTokenSource authn, SsoDomain ssoDomain) {
    assert authn != null;
    this._authn = authn;
    this._ssoDomain = ssoDomain;
  }
  
  public SamlToken getAuthenticationToken() {
    return this._authn.getAuthenticationToken(this._ssoDomain);
  }
  
  public PrivateKey getConfirmationKey() {
    return this._authn.getConfirmationKey();
  }
}
