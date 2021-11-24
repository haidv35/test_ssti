package com.vmware.ph.phservice.common.vim.internal.vc.util;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcServiceLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class VcAuthenticationUtil {
  private static final Log _log = LogFactory.getLog(VcServiceLocator.class);
  
  public static String getAuthenticatedSessionId(VcClient vcClient) {
    try {
      if (vcClient.getSessionManager().getCurrentSession() == null)
        vcClient.login(); 
    } catch (SsoTokenProviderException|com.vmware.vim.binding.vim.fault.InvalidLogin|com.vmware.vim.binding.vim.fault.InvalidLocale e) {
      _log.error("Could not get authenticated sessionId: ", e);
    } 
    return vcClient.getVlsiClient().getBinding().getSession().getId();
  }
}
