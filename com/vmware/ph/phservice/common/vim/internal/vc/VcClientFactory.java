package com.vmware.ph.phservice.common.vim.internal.vc;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import java.net.URI;

public interface VcClientFactory {
  VcClient connectVcAsUser(URI paramURI, String paramString1, char[] paramArrayOfchar, String paramString2);
  
  VcClient connectVcWithSamlToken(URI paramURI, SsoTokenProvider paramSsoTokenProvider, String paramString);
  
  VcClient connectVcAnnonymous(URI paramURI, String paramString);
}
