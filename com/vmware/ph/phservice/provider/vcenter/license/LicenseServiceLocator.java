package com.vmware.ph.phservice.provider.vcenter.license;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.Service;
import com.vmware.vim.binding.lookup.ServiceEndpoint;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.fault.ServiceFault;
import java.net.URI;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LicenseServiceLocator {
  private static final Log _log = LogFactory.getLog(LicenseServiceLocator.class);
  
  private static final URI LS1_LICENSE_SERVICE_TYPE = URI.create("urn:cs.license");
  
  private static final ServiceRegistration.ServiceType LS2_LICENSE_SERVICE_TYPE = new ServiceRegistration.ServiceType("com.vmware.cis", "cs.license");
  
  private static final ServiceRegistration.EndpointType LS2_LICENSE_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vmomi", "com.vmware.cis.cs.license.sdk");
  
  private static final String LICENSE_SERVICE_SDK_PATH = "/ls/sdk";
  
  private final LookupClientBuilder _lookupClientBuilder;
  
  private URI _licenseSdkEndpointUri;
  
  private final String _localNodeId;
  
  private final boolean _shouldUseEnvoySidecar;
  
  public LicenseServiceLocator(LookupClientBuilder lookupClientBuilder, String localNodeId, boolean shouldUseEnvoySidecar) {
    this._lookupClientBuilder = lookupClientBuilder;
    this._localNodeId = localNodeId;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public URI getSdkUri() {
    if (this._licenseSdkEndpointUri == null)
      this._licenseSdkEndpointUri = retrieveSdkEndpointUri(); 
    return this._licenseSdkEndpointUri;
  }
  
  private URI retrieveSdkEndpointUri() {
    URI licenseSdkEndpointUri = null;
    try (LookupClient lookupClient = this._lookupClientBuilder.build()) {
      if (lookupClient.isServiceRegistrationSupported()) {
        licenseSdkEndpointUri = retrieveSdkEndpointUriViaServiceRegistration(lookupClient);
      } else {
        licenseSdkEndpointUri = retrieveSdkEndpointUriViaLookupService(lookupClient);
      } 
    } 
    return licenseSdkEndpointUri;
  }
  
  private URI retrieveSdkEndpointUriViaLookupService(LookupClient lookupClient) {
    URI licenseSdkEndpointUri = null;
    try {
      Service[] services = ServiceLocatorUtil.findServiceByServiceTypeUriViaLegacyLookup(lookupClient, LS1_LICENSE_SERVICE_TYPE);
      ServiceEndpoint[] lookupServiceEndpoints = ServiceLocatorUtil.getFirstServiceEndpoints(services);
      if (lookupServiceEndpoints == null)
        return null; 
      for (ServiceEndpoint endpoint : lookupServiceEndpoints) {
        if ("/ls/sdk".equals(endpoint.getUrl().getPath())) {
          licenseSdkEndpointUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(endpoint
              .getUrl(), this._shouldUseEnvoySidecar);
          break;
        } 
      } 
    } catch (ServiceFault e) {
      if (_log.isDebugEnabled())
        _log.debug("Looking for License service via LSv1 API failed.", (Throwable)e); 
    } 
    return licenseSdkEndpointUri;
  }
  
  private URI retrieveSdkEndpointUriViaServiceRegistration(LookupClient lookupClient) {
    ServiceRegistration.Info[] serviceInfos = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, LS2_LICENSE_SERVICE_TYPE, LS2_LICENSE_ENDPOINT_TYPE, null);
    ServiceRegistration.Endpoint[] serviceRegistrationEndpoints = ServiceLocatorUtil.getFirstServiceRegistrationEndpoints(serviceInfos);
    if (serviceRegistrationEndpoints == null || serviceRegistrationEndpoints.length != 1) {
      _log.error("There must be exactly 1 endpoint for license service with type " + LS2_LICENSE_ENDPOINT_TYPE
          .getType() + ". Actual: " + 
          
          Arrays.toString((Object[])serviceRegistrationEndpoints));
      return null;
    } 
    URI serviceUri = ServiceLocatorUtil.convertUriToEnvoySidecarIfNeeded(serviceRegistrationEndpoints[0]
        .getUrl(), serviceInfos[0]
        .getNodeId(), this._localNodeId, this._shouldUseEnvoySidecar);
    return serviceUri;
  }
}
