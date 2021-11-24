package com.vmware.ph.phservice.cloud.health.vmomi;

import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.internal.server.VmomiUtil;
import com.vmware.vim.binding.vmodl.fault.SecurityError;
import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.ActivationValidator;

public class AuthenticationActivationValidator implements ActivationValidator {
  private final VimContextProvider _vimContextProvider;
  
  public AuthenticationActivationValidator(VimContextProvider vimContextProvider) {
    this._vimContextProvider = vimContextProvider;
  }
  
  public void validate(Activation activation, ActivationValidator.Future validationResult) {
    try {
      String sessionCookie = getSessionCookie(activation);
      boolean isSessionCookieValid = false;
      if (sessionCookie != null)
        isSessionCookieValid = VcVmomiUtil.validateSessionCookie(sessionCookie, 
            
            getVcClientProvider()); 
      if (isSessionCookieValid) {
        validationResult.setValid();
      } else {
        SecurityError fault = new SecurityError();
        fault.setMessage("Caller doesn't have a valid VC session.");
        validationResult.setFault((Exception)fault);
      } 
    } catch (Exception e) {
      validationResult.setFault(e);
    } 
  }
  
  VcClientProvider getVcClientProvider() {
    return VcVmomiUtil.getVcClientProvider(this._vimContextProvider);
  }
  
  String getSessionCookie(Activation activation) {
    return VmomiUtil.getCookieFromRequest("Cookie", activation);
  }
}
