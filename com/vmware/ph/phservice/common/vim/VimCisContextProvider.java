package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;

public class VimCisContextProvider implements CisContextProvider {
  private final VimContextProvider _vimContextProvider;
  
  public VimCisContextProvider(VimContextProvider vimContextProvider) {
    this._vimContextProvider = vimContextProvider;
  }
  
  public CisContext getCisContext() {
    VimContext vimContext = this._vimContextProvider.getVimContext();
    if (vimContext != null)
      return vimContext.getCisContext(); 
    return null;
  }
}
