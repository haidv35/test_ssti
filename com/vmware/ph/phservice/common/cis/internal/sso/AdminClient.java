package com.vmware.ph.phservice.common.cis.internal.sso;

import com.vmware.vim.binding.sso.admin.ConfigurationManagementService;

public interface AdminClient {
  ConfigurationManagementService getConfigurationManagementService();
  
  void close();
}
