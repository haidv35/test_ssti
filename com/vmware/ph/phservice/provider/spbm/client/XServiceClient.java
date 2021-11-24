package com.vmware.ph.phservice.provider.spbm.client;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;

public interface XServiceClient extends AutoCloseable {
  URI getServiceUri();
  
  Client getVmomiClient();
  
  VmodlContext getVmodlContext();
  
  VmodlVersion getVmodlVersion();
  
  VcClient getVcClient();
  
  <T extends com.vmware.vim.binding.vmodl.ManagedObject> T createStub(ManagedObjectReference paramManagedObjectReference);
  
  XServiceClientContext getXServiceClientContext();
  
  void close();
}
