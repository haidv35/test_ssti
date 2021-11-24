package com.vmware.ph.phservice.common.vim;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;

public class VimVmodlUtil {
  public static final String SERVICE_INSTANCE_MO_REF_TYPE = "ServiceInstance";
  
  public static final String SERVICE_INSTANCE_MO_REF_VALUE = "ServiceInstance";
  
  public static final ManagedObjectReference SERVICE_INSTANCE_MOREF = new ManagedObjectReference("ServiceInstance", "ServiceInstance");
}
