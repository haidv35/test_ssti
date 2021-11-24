package com.vmware.ph.phservice.provider.common.vim;

import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import com.vmware.ph.phservice.provider.common.vim.internal.VcContextFactory;

public abstract class VimDataProvidersConnection implements DataProvidersConnection, ContextFactory {
  protected final VimContext _vimContext;
  
  private final VcClientProvider _vcClientProvider;
  
  private final boolean _shouldCloseVcClientProvider;
  
  private final VcContextFactory _vcContextFactory;
  
  public VimDataProvidersConnection(VimContext vimContext) {
    this._vimContext = vimContext;
    this._vcClientProvider = new VimContextVcClientProviderImpl(vimContext);
    this._shouldCloseVcClientProvider = true;
    this._vcContextFactory = new VcContextFactory(this._vcClientProvider);
  }
  
  public VimDataProvidersConnection(VimContext vimContext, VcClientProvider vcClientProvider) {
    this._vimContext = vimContext;
    this._vcClientProvider = vcClientProvider;
    this._shouldCloseVcClientProvider = false;
    this._vcContextFactory = new VcContextFactory(this._vcClientProvider);
  }
  
  public Context createContext(String collectorId, String collectorInstanceId, String collectionId) {
    Context context = this._vcContextFactory.createContext(collectorId, collectorInstanceId, collectionId);
    return context;
  }
  
  public void close() {
    if (this._shouldCloseVcClientProvider)
      this._vcClientProvider.close(); 
  }
  
  public VcClient getVcClient() {
    return this._vcClientProvider.getVcClient();
  }
}
