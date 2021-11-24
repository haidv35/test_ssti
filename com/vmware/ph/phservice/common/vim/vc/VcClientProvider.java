package com.vmware.ph.phservice.common.vim.vc;

public interface VcClientProvider extends AutoCloseable {
  VcClient getVcClient();
  
  void close();
}
