package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.AdminClient;
import com.vmware.ph.phservice.common.cis.internal.sso.AdminClientFactory;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpoint;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.vim.binding.sso.SessionManager;
import com.vmware.vim.binding.sso.version.version2;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.security.KeyStore;

public class AdminClientFactoryImpl implements AdminClientFactory {
  private static final String SSO_VMODL_PACKAGE_NAME = "com.vmware.vim.binding.sso";
  
  private static final Class<?> SSO_VERSION_CLASS = version2.class;
  
  private final SsoEndpoint _ssoAdminEndpoint;
  
  private final KeyStore _trustStore;
  
  private final ThumbprintVerifier _thumbprintVerifier;
  
  private VmodlContext _vmodlContext;
  
  public AdminClientFactoryImpl(SsoEndpoint ssoAdminEndpoint, KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._ssoAdminEndpoint = ssoAdminEndpoint;
    this._trustStore = trustStore;
    this._thumbprintVerifier = thumbprintVerifier;
    this._vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass("com.vmware.vim.binding.sso", SessionManager.class, false);
  }
  
  public AdminClient createAnonymousAdminClient() {
    return new AdminClientImpl(this._vmodlContext, SSO_VERSION_CLASS, this._ssoAdminEndpoint, this._trustStore, this._thumbprintVerifier);
  }
}
