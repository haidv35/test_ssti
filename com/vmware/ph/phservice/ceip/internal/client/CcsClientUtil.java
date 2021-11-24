package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextBuilder;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.common.vmomi.client.AuthenticationHelper;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.client.impl.VmomiClientFactoryImpl;
import com.vmware.vim.binding.phonehome.service.ConsentConfigurationService;
import com.vmware.vim.binding.phonehome.version.version1;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.PrivateKey;
import java.util.concurrent.Executors;

public final class CcsClientUtil {
  private static final String CONSENT_CONFIG_SERVICE_MO_ID = "ccService";
  
  private static final String PH_VMODL_PACAKGE_NAME = "com.vmware.vim.binding.phonehome";
  
  private static final Class<?> CCS_VERSION_CLASS = version1.class;
  
  public static VmomiClient createCcsVmomiClient(CisContext cisContext, CcsLocator ccsLocator) {
    VmodlContext vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass("com.vmware.vim.binding.phonehome", ConsentConfigurationService.class, false);
    VmomiClientFactoryImpl vmomiClientFactory = new VmomiClientFactoryImpl(CCS_VERSION_CLASS, vmodlContext, Executors.newSingleThreadExecutor());
    vmomiClientFactory.setTrustStore(cisContext.getTrustedStore());
    vmomiClientFactory.setThumbprintVerifier(cisContext.getThumprintVerifier());
    vmomiClientFactory.setTimeoutMs(Integer.valueOf(180000));
    URI sdkUri = ccsLocator.getSdkUri(cisContext);
    AuthenticationHelper authenticationHelper = new SsoTokenAuthenticationHelper(cisContext.getSsoTokenProvider());
    VmomiClient vmomiClient = vmomiClientFactory.create(sdkUri, authenticationHelper);
    return vmomiClient;
  }
  
  public static VmomiClient createCcsVmomiClient(CisContext cisContext, CcsLocator ccsLocator, SamlToken samlToken, PrivateKey privateKey) {
    CisContext cisContextWithModifiedAuthentication = CisContextBuilder.forCis(cisContext.getLookupSdkUri(), cisContext.getTrustedStore()).withSamlTokenAndPrivateKey(samlToken, privateKey).withTrust(cisContext.getThumprintVerifier()).shouldUseEnvoySidecar(Boolean.valueOf(cisContext.getShouldUseEnvoySidecar())).build();
    return createCcsVmomiClient(cisContextWithModifiedAuthentication, ccsLocator);
  }
  
  public static ConsentConfigurationService getConsentConfigurationServiceMo(VmomiClient ccsClient) throws Exception {
    String ccsWsdlName = ccsClient.getVmodlContext().getVmodlTypeMap().getVmodlType(ConsentConfigurationService.class).getWsdlName();
    ManagedObjectReference ccsMoRef = new ManagedObjectReference(ccsWsdlName, "ccService");
    ConsentConfigurationService ccsStub = ccsClient.<ConsentConfigurationService>createStub(ccsMoRef);
    return ccsStub;
  }
}
