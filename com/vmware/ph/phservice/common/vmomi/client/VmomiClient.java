package com.vmware.ph.phservice.common.vmomi.client;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;

public interface VmomiClient extends AutoCloseable {
  URI getServiceUri();
  
  VmodlVersion getVmodlVersion();
  
  VmodlContext getVmodlContext();
  
  <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(ManagedObjectReference paramManagedObjectReference) throws Exception;
  
  void close();
  
  Client getVlsiClient();
}
