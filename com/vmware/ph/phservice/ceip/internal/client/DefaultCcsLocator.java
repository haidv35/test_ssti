package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCcsLocator implements CcsLocator {
  private static final String CCS_MO_SDK_ENDPOINT_TYPE = "com.vmware.ph.ceip.sdk";
  
  private static final Logger _log = LoggerFactory.getLogger(DefaultCcsLocator.class);
  
  public URI getSdkUri(CisContext cisContext) {
    URI ccsSdkUri;
    if (cisContext == null)
      return null; 
    try (LookupClient lookupClient = cisContext.getLookupClientBuilder().build()) {
      String nodeId = null;
      ApplianceContext applianceContext = cisContext.getApplianceContext();
      if (applianceContext != null)
        nodeId = applianceContext.getNodeId(); 
      Pair<ServiceRegistration.Endpoint, String> ccsEndpointToNodeId = ServiceLocatorUtil.getEndpointByEndpointTypeAndNodeId(lookupClient, new ServiceRegistration.EndpointType(null, "com.vmware.ph.ceip.sdk"), nodeId);
      ccsSdkUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(
          ServiceLocatorUtil.getEndpointUri(ccsEndpointToNodeId.getFirst()), cisContext
          .getShouldUseEnvoySidecar());
      _log.debug("Consent Configuration Service MO SDK URI: {}", ccsSdkUri);
    } 
    return ccsSdkUri;
  }
}
