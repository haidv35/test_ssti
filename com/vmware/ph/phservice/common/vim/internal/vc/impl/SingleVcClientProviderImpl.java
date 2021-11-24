package com.vmware.ph.phservice.common.vim.internal.vc.impl;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;

public class SingleVcClientProviderImpl implements VcClientProvider {
  private final Builder<VcClient> _vcClientBuilder;
  
  private final Object _lock = new Object();
  
  private VcClient _vcClient;
  
  public SingleVcClientProviderImpl(Builder<VcClient> vcClientBuilder) {
    this._vcClientBuilder = vcClientBuilder;
  }
  
  public VcClient getVcClient() {
    synchronized (this._lock) {
      if (this._vcClient == null)
        this._vcClient = this._vcClientBuilder.build(); 
      return this._vcClient;
    } 
  }
  
  public void close() {
    synchronized (this._lock) {
      if (this._vcClient != null) {
        this._vcClient.close();
        this._vcClient = null;
      } 
    } 
  }
  
  VcClient getInnerVcClient() {
    return this._vcClient;
  }
}
