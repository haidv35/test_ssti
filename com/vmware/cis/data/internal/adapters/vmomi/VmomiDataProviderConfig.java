package com.vmware.cis.data.internal.adapters.vmomi;

import com.vmware.cis.data.internal.adapters.vmomi.impl.HttpConfigurationFactory;
import com.vmware.cis.data.internal.adapters.vmomi.util.VmomiVersion;
import com.vmware.vim.vmomi.core.types.VmodlContext;

public final class VmomiDataProviderConfig {
  private final VmodlContext _vmodlContext;
  
  private final VmomiVersion _vmomiVersion;
  
  private final HttpConfigurationFactory _vlsiHttpConfigFactory;
  
  private final VmomiAuthenticatorFactory _authFactory;
  
  private VmomiDataProviderConfig(VmodlContext vmodlContext, VmomiVersion vmomiVersion, HttpConfigurationFactory vlsiHttpConfigFactory, VmomiAuthenticatorFactory authFactory) {
    this._vmodlContext = vmodlContext;
    this._vmomiVersion = vmomiVersion;
    this._vlsiHttpConfigFactory = vlsiHttpConfigFactory;
    this._authFactory = authFactory;
  }
  
  public VmodlContext getVmodlContext() {
    return this._vmodlContext;
  }
  
  public VmomiVersion getVmomiVersion() {
    return this._vmomiVersion;
  }
  
  public HttpConfigurationFactory getVlsiHttpConfigFactory() {
    return this._vlsiHttpConfigFactory;
  }
  
  public VmomiAuthenticatorFactory getAuthenticatorFactory() {
    return this._authFactory;
  }
}
