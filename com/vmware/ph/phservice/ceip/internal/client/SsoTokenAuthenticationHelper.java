package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import com.vmware.vim.vmomi.core.security.SignInfo;
import com.vmware.vim.vmomi.core.security.impl.SignInfoImpl;
import java.util.Objects;

public class SsoTokenAuthenticationHelper implements AuthenticationHelper {
  private final SsoTokenProvider _ssoTokenProvider;
  
  public SsoTokenAuthenticationHelper(SsoTokenProvider ssoTokenProvider) {
    this._ssoTokenProvider = Objects.<SsoTokenProvider>requireNonNull(ssoTokenProvider);
  }
  
  public void addAuthenticationContext(Stub stub, VmomiClient vmomiClient) {
    SsoTokenProvider.TokenKeyPair tokenKeyPair = null;
    try {
      tokenKeyPair = this._ssoTokenProvider.getToken();
    } catch (SsoTokenProviderException e) {
      throw new RuntimeException(e);
    } 
    RequestContextImpl requestContext = new RequestContextImpl();
    SignInfoImpl signInfo = new SignInfoImpl(tokenKeyPair.key, tokenKeyPair.token);
    requestContext.setSignInfo((SignInfo)signInfo);
    stub._setRequestContext((RequestContext)requestContext);
  }
}
