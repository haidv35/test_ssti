package com.vmware.ph.phservice.common.cis.internal.sso;

public interface SsoEndpointProvider {
  SsoEndpoint getAdminEndpoint() throws SsoException;
  
  SsoEndpoint getStsEndpoint() throws SsoException;
}
