package com.vmware.ph.phservice.provider.vcenter.license;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClientBuilder;
import com.vmware.vim.binding.cis.license.License;
import com.vmware.vim.binding.cis.license.version.internal.version2;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LicenseDataProvidersConnection extends VimDataProvidersConnection {
  private static final Class<?> LICENSE_VMODL_BINDINGS_VERSION = version2.class;
  
  static final String LICENSE_VMODL_PACKAGE_NAME = "com.vmware.vim.binding.cis.license";
  
  private LicenseClient _licenseClient;
  
  private DataProvider _licenseDataProvider;
  
  private ExecutorService _executorService;
  
  public LicenseDataProvidersConnection(VimContext vimContext) {
    super(vimContext);
  }
  
  public LicenseDataProvidersConnection(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
  }
  
  public synchronized List<DataProvider> getDataProviders() {
    if (this._licenseDataProvider == null)
      createLicenseDataProvider(); 
    return Collections.singletonList(this._licenseDataProvider);
  }
  
  public synchronized void close() {
    if (this._licenseClient != null) {
      this._licenseClient.logout();
      this._licenseClient.close();
      this._licenseClient = null;
    } 
    if (this._executorService != null) {
      this._executorService.shutdown();
      this._executorService = null;
    } 
    this._licenseDataProvider = null;
    super.close();
  }
  
  private void createLicenseDataProvider() {
    VmodlContext vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass("com.vmware.vim.binding.cis.license", License.class, false);
    this._executorService = Executors.newSingleThreadExecutor();
    LicenseClientBuilder licenseClientBuilder = LicenseClientBuilder.forLicenseClient(LICENSE_VMODL_BINDINGS_VERSION, this._executorService);
    URI licenseServiceSdkUri = discoverLicenseServiceSdkUri();
    if (licenseServiceSdkUri == null)
      throw new IllegalStateException("The license service SDK URI was not discovered!"); 
    this



      
      ._licenseClient = licenseClientBuilder.withSdkUri(licenseServiceSdkUri).withTrustStore(this._vimContext.getVimTrustedStore()).withThumbprintVerifier(this._vimContext.getThumprintVerifier()).withSsoTokenProvider(this._vimContext.getSsoTokenProvider()).build();
    this

      
      ._licenseDataProvider = new LicenseDataProviderImpl(this._licenseClient, getVcClient(), vmodlContext, LICENSE_VMODL_BINDINGS_VERSION);
  }
  
  private URI discoverLicenseServiceSdkUri() {
    LookupClientBuilder lookupClientBuilder = this._vimContext.getLookupClientBuilder(false);
    String localNodeId = this._vimContext.getVcNodeId();
    LicenseServiceLocator licenseServiceLocator = new LicenseServiceLocator(lookupClientBuilder, localNodeId, this._vimContext.getShouldUseEnvoySidecar());
    return licenseServiceLocator.getSdkUri();
  }
}
