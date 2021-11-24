package com.vmware.ph.phservice.common.vmomi.client;

import java.net.URI;

public interface VmomiClientFactory {
  VmomiClient create(URI paramURI);
  
  VmomiClient create(URI paramURI, AuthenticationHelper paramAuthenticationHelper);
}
