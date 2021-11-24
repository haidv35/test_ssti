package com.vmware.ph.phservice.common.vim.vc;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.Service;
import com.vmware.vim.binding.lookup.ServiceEndpoint;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.fault.ServiceFault;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VcServiceLocator {
  private static final Log _log = LogFactory.getLog(VcServiceLocator.class);
  
  private static final URI LS1_VC_SERVICE_TYPE = URI.create("urn:vc");
  
  private static final ServiceRegistration.ServiceType LS2_VC_SERVICE_TYPE = new ServiceRegistration.ServiceType("com.vmware.cis", "vcenterserver");
  
  private static final ServiceRegistration.EndpointType LS2_VC_SDK_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vmomi", "com.vmware.vim");
  
  private final LookupClientBuilder _lookupClientBuilder;
  
  private final boolean _shouldUseEnvoySidecar;
  
  public VcServiceLocator(LookupClientBuilder lookupClientBuilder, boolean shouldUseEnvoySidecar) {
    this._lookupClientBuilder = lookupClientBuilder;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public String getServiceIdByNodeId(String vcNodeId) {
    String serviceId = null;
    ServiceRegistration.Info vcServiceInfo = getServiceByNodeIdViaServiceRegistration(vcNodeId);
    if (vcServiceInfo != null)
      serviceId = vcServiceInfo.getServiceId(); 
    return serviceId;
  }
  
  public URI getSdkUriByServiceId(String vcServiceId, boolean tryGetLocalUri) {
    return getSdkUriByServiceId(vcServiceId, tryGetLocalUri, false);
  }
  
  public URI getSdkUriByServiceId(String vcServiceId, boolean tryGetLocalUri, boolean getAbsoluteUri) {
    URI vcSdkUri = null;
    try (LookupClient lookupClient = this._lookupClientBuilder.build()) {
      if (lookupClient.isServiceRegistrationSupported()) {
        vcSdkUri = retrieveSdkUriByServiceIdViaServiceRegistration(vcServiceId, lookupClient, tryGetLocalUri, getAbsoluteUri);
      } else {
        vcSdkUri = retrieveSdkUriByServiceIdViaLookupService(vcServiceId, lookupClient, getAbsoluteUri);
      } 
    } 
    return vcSdkUri;
  }
  
  private URI retrieveSdkUriByServiceIdViaServiceRegistration(String vcServiceId, LookupClient lookupClient, boolean tryGetLocalUri, boolean getAbsoluteUri) {
    ServiceRegistration.Info vcServiceInfo = null;
    ServiceRegistration.Info[] serviceInfos = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, LS2_VC_SERVICE_TYPE, LS2_VC_SDK_ENDPOINT_TYPE, null);
    for (ServiceRegistration.Info serviceInfo : serviceInfos) {
      if (serviceInfo.getServiceId().endsWith(vcServiceId))
        vcServiceInfo = serviceInfo; 
    } 
    if (vcServiceInfo == null)
      return null; 
    ServiceRegistration.Endpoint endpoint = vcServiceInfo.getServiceEndpoints()[0];
    URI endpointUri = ServiceLocatorUtil.getEndpointUri(endpoint, tryGetLocalUri);
    if (!getAbsoluteUri)
      endpointUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(endpointUri, this._shouldUseEnvoySidecar); 
    return endpointUri;
  }
  
  private URI retrieveSdkUriByServiceIdViaLookupService(String vcServiceId, LookupClient lookupClient, boolean getAbsoluteUri) {
    URI vcSdkUri = null;
    Service vcService = getServiceByServiceIdViaLookupService(vcServiceId, lookupClient);
    if (vcService != null) {
      ServiceEndpoint[] endpoints = vcService.getEndpoints();
      for (ServiceEndpoint serviceEndpoint : endpoints) {
        if (serviceEndpoint.getUrl().toString().endsWith("/sdk") && serviceEndpoint
          .getProtocol().equals("vmomi")) {
          vcSdkUri = serviceEndpoint.getUrl();
          if (!getAbsoluteUri)
            vcSdkUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(vcSdkUri, this._shouldUseEnvoySidecar); 
          break;
        } 
      } 
    } 
    return vcSdkUri;
  }
  
  private Service getServiceByServiceIdViaLookupService(String vcServiceId, LookupClient lookupClient) {
    Service vcService = null;
    try {
      Service[] services = ServiceLocatorUtil.findServiceByServiceTypeUriViaLegacyLookup(lookupClient, LS1_VC_SERVICE_TYPE);
      for (Service service : services) {
        if (service.getServiceId().endsWith(vcServiceId))
          vcService = service; 
      } 
    } catch (ServiceFault e) {
      if (_log.isDebugEnabled())
        _log.debug("Looking for VC service by its UUID via LSv1 API failed.", (Throwable)e); 
    } 
    return vcService;
  }
  
  private ServiceRegistration.Info getServiceByNodeIdViaServiceRegistration(String nodeId) {
    ServiceRegistration.Info vcServiceInfo = null;
    try (LookupClient lookupClient = this._lookupClientBuilder.build()) {
      ServiceRegistration.Info[] serviceInfos = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, LS2_VC_SERVICE_TYPE, LS2_VC_SDK_ENDPOINT_TYPE, nodeId);
      if (serviceInfos != null && serviceInfos.length == 1)
        vcServiceInfo = serviceInfos[0]; 
    } 
    return vcServiceInfo;
  }
}
