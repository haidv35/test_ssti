package com.vmware.ph.phservice.provider.vcenter;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import java.util.List;

public class VcVimDataProvidersConnectionImpl extends VimDataProvidersConnection {
  private DataProvidersConnection _vcDataProvidersConnection;
  
  private final Object _lock = new Object();
  
  public VcVimDataProvidersConnectionImpl(VimContext vimContext) {
    super(vimContext);
  }
  
  public VcVimDataProvidersConnectionImpl(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    synchronized (this._lock) {
      if (this._vcDataProvidersConnection == null) {
        VcClient vcClient = getVcClient();
        this._vcDataProvidersConnection = new VcDataProvidersConnectionImpl(vcClient);
      } 
      return this._vcDataProvidersConnection.getDataProviders();
    } 
  }
  
  public void close() {
    synchronized (this._lock) {
      if (this._vcDataProvidersConnection != null)
        this._vcDataProvidersConnection.close(); 
    } 
    super.close();
  }
}
