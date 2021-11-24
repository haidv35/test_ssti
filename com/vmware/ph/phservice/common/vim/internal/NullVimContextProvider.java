package com.vmware.ph.phservice.common.vim.internal;

import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;

public final class NullVimContextProvider implements VimContextProvider {
  public VimContext getVimContext() {
    return null;
  }
}
