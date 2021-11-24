package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.client.impl.VmomiClientFactoryImpl;
import com.vmware.ph.phservice.common.vsan.internal.VcSessionCookieAuthenticationHelper;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vsan.binding.vim.VsanMassCollector;
import com.vmware.vim.vsan.binding.vsan.version.version10;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class VsanHealthClientBuilder implements Builder<VmomiClient> {
  private static final ServiceRegistration.EndpointType VSAN_HEALTH_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("https", "com.vmware.vsan-health");
  
  public static final List<String> VMODL_PACKAGES = Collections.unmodifiableList(
      Arrays.asList(new String[] { "com.vmware.vim.binding.vim", "com.vmware.vim.vsan.binding.vim" }));
  
  public static final VmodlContext VMODL_CONTEXT = createVmodlContext();
  
  private final VimContext _vimContext;
  
  private final VcClient _vcClient;
  
  private final VmomiClientFactoryImpl _vsanVmomiClientFactory;
  
  private VsanHealthClientBuilder(VimContext vimContext, VcClient vcClient) {
    this._vimContext = vimContext;
    this._vcClient = vcClient;
    this


      
      ._vsanVmomiClientFactory = new VmomiClientFactoryImpl(version10.class, VMODL_CONTEXT, Executors.newFixedThreadPool(1));
  }
  
  public static VsanHealthClientBuilder forVc(VimContext vimContext, VcClient vcClient) {
    VsanHealthClientBuilder builder = new VsanHealthClientBuilder(vimContext, vcClient);
    builder.withTrust(vimContext
        .getVimTrustedStore(), vimContext
        .getThumprintVerifier());
    return builder;
  }
  
  public VsanHealthClientBuilder withTrust(KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._vsanVmomiClientFactory.setTrustStore(trustStore);
    this._vsanVmomiClientFactory.setThumbprintVerifier(thumbprintVerifier);
    return this;
  }
  
  public VsanHealthClientBuilder withTimeoutMs(int timeoutMs) {
    this._vsanVmomiClientFactory.setTimeoutMs(Integer.valueOf(timeoutMs));
    return this;
  }
  
  public VmomiClient build() {
    AuthenticationHelper authenticationHelper = new VcSessionCookieAuthenticationHelper(this._vcClient);
    URI vsanHealthUri = getVsanHealthUri(this._vimContext);
    VmomiClient vmomiClient = this._vsanVmomiClientFactory.create(vsanHealthUri, authenticationHelper);
    return vmomiClient;
  }
  
  private static VmodlContext createVmodlContext() {
    VmodlContext vmodlContext = null;
    Map<String, Class<?>> vmodlPackageToClass = new LinkedHashMap<>();
    vmodlPackageToClass.put("com.vmware.vim.binding.vim", AboutInfo.class);
    vmodlPackageToClass.put("com.vmware.vim.vsan.binding.vim", VsanMassCollector.class);
    vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass(vmodlPackageToClass, false);
    return vmodlContext;
  }
  
  private static URI getVsanHealthUri(VimContext vimContext) {
    ServiceRegistration.Endpoint vsanHealthEndpoint = null;
    LookupClientBuilder lookupClientBuilder = vimContext.getLookupClientBuilder(true);
    try (LookupClient lookupClient = lookupClientBuilder.build()) {
      vsanHealthEndpoint = ServiceLocatorUtil.getEndpointByEndpointTypeAndNodeId(lookupClient, VSAN_HEALTH_ENDPOINT_TYPE, vimContext.getVcNodeId()).getFirst();
    } 
    URI vsanHealthUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(
        ServiceLocatorUtil.getEndpointUri(vsanHealthEndpoint, vimContext
          
          .getApplianceContext().isLocal()), vimContext
        .getShouldUseEnvoySidecar());
    return vsanHealthUri;
  }
}
