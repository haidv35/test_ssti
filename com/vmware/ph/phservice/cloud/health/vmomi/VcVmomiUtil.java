package com.vmware.ph.phservice.cloud.health.vmomi;

import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.internal.server.VmomiUtil;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.UserSession;
import com.vmware.vim.vmomi.client.common.ProtocolBinding;
import com.vmware.vim.vmomi.client.common.Session;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.ServerFuture;
import java.util.Locale;

public class VcVmomiUtil {
  public static final String VC_SESSION_ID_KEY = "Cookie";
  
  public static VcClientProvider getVcClientProvider(VimContextProvider vimContextProvider) {
    VimContextVcClientProviderImpl vimContextVcClientProviderImpl;
    VcClientProvider vcClientProvider = null;
    if (vimContextProvider != null) {
      VimContext vimContext = vimContextProvider.getVimContext();
      if (vimContext != null && vimContext.getVcClientBuilder() != null)
        vimContextVcClientProviderImpl = new VimContextVcClientProviderImpl(vimContext); 
    } 
    return (VcClientProvider)vimContextVcClientProviderImpl;
  }
  
  public static String getMessageLocaleForVcSession(String vcSessionCookie, VcClientProvider vcClientProvider) {
    String resultLocale = Locale.ENGLISH.toLanguageTag();
    try {
      if (vcSessionCookie != null && vcClientProvider != null) {
        VcClient vcClient = vcClientProvider.getVcClient();
        if (vcClient != null) {
          UserSession currentSession = getUserSessionForCookie(vcSessionCookie, vcClient);
          if (currentSession != null)
            resultLocale = currentSession.getMessageLocale(); 
        } 
      } 
    } finally {
      if (vcClientProvider != null)
        vcClientProvider.close(); 
    } 
    return resultLocale;
  }
  
  public static String getUserNameForVcSession(String vcSessionCookie, VcClientProvider vcClientProvider) {
    String sessionUser = null;
    try {
      if (vcSessionCookie != null && vcClientProvider != null) {
        VcClient vcClient = vcClientProvider.getVcClient();
        if (vcClient != null) {
          UserSession currentSession = getUserSessionForCookie(vcSessionCookie, vcClient);
          if (currentSession != null)
            sessionUser = currentSession.getUserName(); 
        } 
      } 
    } finally {
      if (vcClientProvider != null)
        vcClientProvider.close(); 
    } 
    return sessionUser;
  }
  
  public static boolean validateSessionCookie(String vcSessionCookie, VcClientProvider vcClientProvider) {
    if (vcClientProvider == null)
      return false; 
    boolean isSessionCookieValid = false;
    try {
      VcClient vcClient = vcClientProvider.getVcClient();
      UserSession userSession = getUserSessionForCookie(vcSessionCookie, vcClient);
      isSessionCookieValid = (userSession != null);
    } finally {
      vcClientProvider.close();
    } 
    return isSessionCookieValid;
  }
  
  private static UserSession getUserSessionForCookie(String vcSessionCookie, VcClient vcClient) {
    UserSession currentSession;
    ProtocolBinding protocolBinding = vcClient.getVlsiClient().getBinding();
    Session session = protocolBinding.createSession(vcSessionCookie);
    protocolBinding.setSession(session);
    try {
      SessionManager sessionManager = vcClient.getSessionManager();
      currentSession = sessionManager.getCurrentSession();
    } finally {
      protocolBinding.clearSession();
    } 
    return currentSession;
  }
  
  public static String getSessionUser(Future<?> serverFuture, VimContextProvider _vimContextProvider) {
    String sessionUser = null;
    if (serverFuture instanceof ServerFuture) {
      Activation serverActivation = ((ServerFuture)serverFuture).getActivation();
      String callerSessionCookie = VmomiUtil.getCookieFromRequest("Cookie", serverActivation);
      VcClientProvider vcClientProvider = getVcClientProvider(_vimContextProvider);
      sessionUser = getUserNameForVcSession(callerSessionCookie, vcClientProvider);
    } 
    return sessionUser;
  }
}
