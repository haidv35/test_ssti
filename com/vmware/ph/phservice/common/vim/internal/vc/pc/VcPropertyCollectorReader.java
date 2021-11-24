package com.vmware.ph.phservice.common.vim.internal.vc.pc;

import com.vmware.ph.phservice.common.vim.pc.VimPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;

public class VcPropertyCollectorReader extends VimPropertyCollectorReader {
  public VcPropertyCollectorReader(VcClient vcClient) {
    super(vcClient.getVlsiClient());
  }
}
