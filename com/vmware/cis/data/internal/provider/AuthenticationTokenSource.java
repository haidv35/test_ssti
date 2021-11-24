package com.vmware.cis.data.internal.provider;

import com.vmware.vim.sso.client.SamlToken;
import java.security.PrivateKey;

public interface AuthenticationTokenSource {
  SamlToken getAuthenticationToken();
  
  PrivateKey getConfirmationKey();
}
