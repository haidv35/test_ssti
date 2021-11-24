package com.vmware.ph.phservice.common.cis.vmaf;

import com.vmware.af.VmAfClient;
import com.vmware.ph.phservice.common.Builder;

public class LocalVmAfClientBuilder implements Builder<VmAfClient> {
  public VmAfClient build() {
    return new VmAfClient("localhost");
  }
}
