package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.vim.internal.vc.impl.SingleVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;

public class VimContextVcClientProviderImpl implements VcClientProvider {
  private final VimContext _vimContext;
  
  private final boolean _autoDetermineVcVersion;
  
  private final Object _lock = new Object();
  
  private VcClientProvider _innerVcClientProvider;
  
  private int _timeoutMs;
  
  public VimContextVcClientProviderImpl(VimContext vimContext) {
    this(vimContext, true);
  }
  
  public VimContextVcClientProviderImpl(VimContext vimContext, boolean autoDetermineVcVersion) {
    this._vimContext = vimContext;
    this._autoDetermineVcVersion = autoDetermineVcVersion;
  }
  
  public VimContext getVimContext() {
    return this._vimContext;
  }
  
  public void setTimeoutMs(int timeoutMs) {
    this._timeoutMs = timeoutMs;
  }
  
  public VcClient getVcClient() {
    synchronized (this._lock) {
      if (this._innerVcClientProvider == null) {
        VcClientBuilder vcClientBuilder = this._vimContext.getVcClientBuilder(this._autoDetermineVcVersion);
        vcClientBuilder.withTimeoutMs(this._timeoutMs);
        this._innerVcClientProvider = new SingleVcClientProviderImpl(vcClientBuilder);
      } 
      return this._innerVcClientProvider.getVcClient();
    } 
  }
  
  public void close() {
    synchronized (this._lock) {
      if (this._innerVcClientProvider != null) {
        this._innerVcClientProvider.close();
        this._innerVcClientProvider = null;
      } 
    } 
  }
}
