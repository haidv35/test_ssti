package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.internal.adapters.vmomi.VmomiAuthenticator;
import com.vmware.cis.data.internal.adapters.vmomi.VmomiSession;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import org.apache.commons.lang.Validate;

public final class VcVmomiAuthenticator implements VmomiAuthenticator {
  private final URI _vmomiUri;
  
  private final HttpConfiguration _vlsiHttpConfig;
  
  private final VmodlContext _vmodlContext;
  
  public VcVmomiAuthenticator(VmodlContext vmodlContext, URI vmomiUri, HttpConfiguration vlsiHttpConfig) {
    Validate.notNull(vmodlContext, "Argument `vmodlContext' is required.");
    Validate.notNull(vmomiUri, "Argument `vmomiUri' is required.");
    Validate.notNull(vlsiHttpConfig, "Argument `vlsiHttpConfig' is required.");
    this._vmomiUri = vmomiUri;
    this._vlsiHttpConfig = vlsiHttpConfig;
    this._vmodlContext = vmodlContext;
  }
  
  public VmomiSession login(AuthenticationTokenSource authn) {
    Validate.notNull(authn);
    return new RenewableVcVmomiSession(this._vmodlContext, this._vmomiUri, this._vlsiHttpConfig, authn);
  }
}
