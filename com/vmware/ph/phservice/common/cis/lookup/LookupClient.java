package com.vmware.ph.phservice.common.cis.lookup;

import com.vmware.vim.binding.lookup.LookupService;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;

public interface LookupClient extends AutoCloseable {
  VmodlContext getVmodlContext();
  
  VmodlVersion getVmodlVersion();
  
  LookupService getLookupService();
  
  ServiceRegistration getServiceRegistration();
  
  boolean isServiceRegistrationSupported();
  
  void close();
}
