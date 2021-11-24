package com.vmware.cis.data.internal.adapters.vmomi;

import com.vmware.cis.data.internal.adapters.lookup.ServiceEndpointInfo;

public interface VmomiAuthenticatorFactory {
  VmomiAuthenticator getAuthenticator(ServiceEndpointInfo paramServiceEndpointInfo);
}
