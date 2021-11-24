package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PscCcsLocator implements CcsLocator {
  private static final Logger _log = LoggerFactory.getLogger(PscCcsLocator.class);
  
  private static final String CCS_SDK_PATH = "/ls/ph/sdk";
  
  static final String LICENSING_SDK_PATH = "/ls/sdk";
  
  static final String PH_SERVICE_ENDPOINT_TYPE = "com.vmware.ph.globalconfig.consentservice";
  
  static final String LICENSEING_SERVICE_ENDPOINT_TYPE = "com.vmware.cis.cs.license.sdk";
  
  public URI getSdkUri(CisContext cisContext) {
    try (LookupClient lookupClient = cisContext.getLookupClientBuilder().build()) {
      String serviceNodeId;
      Pair<ServiceRegistration.Endpoint, String> ccsEndpointToNodeId = ServiceLocatorUtil.getEndpointByEndpointType(lookupClient, new ServiceRegistration.EndpointType(null, "com.vmware.ph.globalconfig.consentservice"));
      if (ccsEndpointToNodeId == null) {
        _log.debug("LookupService does not contain endpoints of type {}. Will look for endpoints of type {} and construct the URL as relative to the found one.", "com.vmware.ph.globalconfig.consentservice", "com.vmware.cis.cs.license.sdk");
        Pair<ServiceRegistration.Endpoint, String> licenseEndpointToNodeId = ServiceLocatorUtil.getEndpointByEndpointType(lookupClient, new ServiceRegistration.EndpointType(null, "com.vmware.cis.cs.license.sdk"));
        if (licenseEndpointToNodeId == null)
          throw new IllegalStateException("Cannot initialize ConsentConfigService because LookupService does not contain registration neither of endpoint type com.vmware.ph.globalconfig.consentservicenor of endpoint type com.vmware.cis.cs.license.sdk."); 
        sdkUri = constructSdkUriFromLicenseServiceUri(licenseEndpointToNodeId.getFirst());
        serviceNodeId = licenseEndpointToNodeId.getSecond();
      } else {
        sdkUri = ServiceLocatorUtil.getEndpointUri(ccsEndpointToNodeId.getFirst());
        serviceNodeId = ccsEndpointToNodeId.getSecond();
      } 
      String localNodeId = null;
      ApplianceContext applianceContext = cisContext.getApplianceContext();
      if (applianceContext != null)
        localNodeId = applianceContext.getNodeId(); 
      URI sdkUri = ServiceLocatorUtil.convertUriToEnvoySidecarIfNeeded(sdkUri, serviceNodeId, localNodeId, cisContext


          
          .getShouldUseEnvoySidecar());
      return sdkUri;
    } 
  }
  
  private static URI constructSdkUriFromLicenseServiceUri(ServiceRegistration.Endpoint licenseEndpoint) {
    URI sdkUri, licensingServiceUri = ServiceLocatorUtil.getEndpointUri(licenseEndpoint);
    if ("/ls/sdk".equals(licensingServiceUri.getPath())) {
      try {
        sdkUri = new URI(licensingServiceUri.getScheme(), licensingServiceUri.getUserInfo(), licensingServiceUri.getHost(), licensingServiceUri.getPort(), "/ls/ph/sdk", licensingServiceUri.getQuery(), licensingServiceUri.getFragment());
      } catch (URISyntaxException e) {
        throw new IllegalStateException("Cannot construct URI.", e);
      } 
    } else {
      throw new IllegalStateException("Found URI of the Licensing service: " + licensingServiceUri + "Its path " + licensingServiceUri
          
          .getPath() + " is not equal to the expected " + "/ls/sdk" + ". Changing CEIP status through UI will not work properly because of this fact.");
    } 
    return sdkUri;
  }
}
