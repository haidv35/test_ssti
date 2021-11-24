package com.vmware.ph.phservice.provider.vcenter.license.client;

import com.vmware.vim.vmomi.client.ext.ServerEndpointProvider;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import java.net.URI;

public interface LicenseClientFactory {
  LicenseClient createClient(URI paramURI, HttpConfiguration paramHttpConfiguration);
  
  LicenseClient createClient(ServerEndpointProvider paramServerEndpointProvider, HttpConfiguration paramHttpConfiguration);
  
  public static interface LicenseClientAutomaticAuthenticator {
    void login(LicenseClient param1LicenseClient);
  }
}
