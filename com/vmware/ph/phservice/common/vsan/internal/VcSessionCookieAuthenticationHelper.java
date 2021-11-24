package com.vmware.ph.phservice.common.vsan.internal;

import com.vmware.ph.phservice.common.vim.internal.vc.util.VcAuthenticationUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.vmomi.client.common.ProtocolBinding;
import com.vmware.vim.vmomi.client.common.Session;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;

public final class VcSessionCookieAuthenticationHelper implements AuthenticationHelper {
  private static final String VC_SESSION_COOKIE = "vmware_soap_session";
  
  private final VcClient _vcClient;
  
  private String _sessionId;
  
  public VcSessionCookieAuthenticationHelper(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  public void addAuthenticationContext(Stub stub, VmomiClient vmomiClient) {
    if (this._vcClient.getSessionManager().getCurrentSession() == null) {
      this._sessionId = VcAuthenticationUtil.getAuthenticatedSessionId(this._vcClient);
    } else if (this._sessionId == null) {
      this._sessionId = this._vcClient.getVlsiClient().getBinding().getSession().getId();
    } 
    addSessionCookieToRequestContext(stub);
    setSessionInClient(vmomiClient, this._sessionId);
  }
  
  private void addSessionCookieToRequestContext(Stub stub) {
    RequestContextImpl requestContextImpl = new RequestContextImpl();
    requestContextImpl.put("vmware_soap_session", this._sessionId);
    stub._setRequestContext((RequestContext)requestContextImpl);
  }
  
  private static void setSessionInClient(VmomiClient vmomiClient, String sessionId) {
    ProtocolBinding vmomiClientBinding = vmomiClient.getVlsiClient().getBinding();
    Session session = vmomiClientBinding.createSession(sessionId);
    vmomiClientBinding.setSession(session);
  }
}
