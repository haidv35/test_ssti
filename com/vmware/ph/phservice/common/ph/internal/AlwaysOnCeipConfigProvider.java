package com.vmware.ph.phservice.common.ph.internal;

import com.vmware.ph.config.ceip.CeipConfigProvider;

public class AlwaysOnCeipConfigProvider implements CeipConfigProvider {
  public boolean isCeipEnabled() {
    return true;
  }
}
