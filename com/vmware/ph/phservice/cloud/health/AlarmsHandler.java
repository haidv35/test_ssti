package com.vmware.ph.phservice.cloud.health;

import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import java.util.Locale;

public interface AlarmsHandler extends AutoCloseable {
  void handleAlarms(VsanClusterHealthGroup paramVsanClusterHealthGroup, Locale paramLocale);
}
