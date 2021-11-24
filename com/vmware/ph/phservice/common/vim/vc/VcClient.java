package com.vmware.ph.phservice.common.vim.vc;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.vim.binding.vim.InternalServiceInstanceContent;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.UserSession;
import com.vmware.vim.binding.vim.fault.InvalidLocale;
import com.vmware.vim.binding.vim.fault.InvalidLogin;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;

public interface VcClient extends AutoCloseable {
  URI getServiceUri();
  
  VmodlVersion getVmodlVersion();
  
  VmodlContext getVmodlContext();
  
  ServiceInstanceContent getServiceInstanceContent();
  
  InternalServiceInstanceContent getInternalServiceInstanceContent();
  
  SessionManager getSessionManager();
  
  <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createMo(ManagedObjectReference paramManagedObjectReference);
  
  void close();
  
  Client getVlsiClient();
  
  default Optional<UserSession> login() throws InvalidLogin, InvalidLocale, SsoTokenProviderException {
    SessionManager sessionManager = getSessionManager();
    if (sessionManager != null)
      return Optional.of(sessionManager.loginByToken(Locale.getDefault().toString())); 
    return Optional.empty();
  }
}
