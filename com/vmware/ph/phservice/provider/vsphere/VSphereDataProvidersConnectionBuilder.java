package com.vmware.ph.phservice.provider.vsphere;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;

public class VSphereDataProvidersConnectionBuilder implements Builder<VSphereDataProvidersConnection> {
  private final VimContextProvider _readOnlyVimContextProvider;
  
  private final VimContextProvider _nonReadOnlyVimContextProvider;
  
  public VSphereDataProvidersConnectionBuilder(VimContextProvider readOnlyVimContextProvider, VimContextProvider nonReadOnlyVimContextProvider) {
    this._readOnlyVimContextProvider = readOnlyVimContextProvider;
    this._nonReadOnlyVimContextProvider = nonReadOnlyVimContextProvider;
  }
  
  public VSphereDataProvidersConnection build() {
    VimContext readOnlyVimContext = this._readOnlyVimContextProvider.getVimContext();
    VimContext nonReadOnlyVimContext = this._nonReadOnlyVimContextProvider.getVimContext();
    if (readOnlyVimContext == null || nonReadOnlyVimContext == null)
      return null; 
    VSphereDataProvidersConnection connection = new VSphereDataProvidersConnection(readOnlyVimContext, nonReadOnlyVimContext);
    return connection;
  }
}
