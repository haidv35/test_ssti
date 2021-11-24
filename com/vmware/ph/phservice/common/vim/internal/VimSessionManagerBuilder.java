package com.vmware.ph.phservice.common.vim.internal;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.vmomi.client.Client;

public class VimSessionManagerBuilder implements Builder<SessionManager> {
  private final Client _vimVlsiClient;
  
  public VimSessionManagerBuilder(Client vimVlsiClient) {
    this._vimVlsiClient = vimVlsiClient;
  }
  
  public SessionManager build() {
    ServiceInstanceContent serviceInstanceContent = (new VimServiceInstanceContentBuilder(this._vimVlsiClient)).build();
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      SessionManager sessionManager = (SessionManager)this._vimVlsiClient.createStub(SessionManager.class, serviceInstanceContent
          
          .getSessionManager());
      return sessionManager;
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
  }
}
