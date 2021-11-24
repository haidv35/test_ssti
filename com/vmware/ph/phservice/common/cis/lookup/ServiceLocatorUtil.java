package com.vmware.ph.phservice.common.cis.lookup;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.security.EnvoySidecarUtil;
import com.vmware.vim.binding.lookup.LookupService;
import com.vmware.vim.binding.lookup.SearchCriteria;
import com.vmware.vim.binding.lookup.Service;
import com.vmware.vim.binding.lookup.ServiceEndpoint;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.fault.ServiceFault;
import com.vmware.vim.featurestateswitch.FeatureState;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServiceLocatorUtil {
  private static final Log _log = LogFactory.getLog(ServiceLocatorUtil.class);
  
  static final String LOCALURL_ENDPOINT_ATTRIBUTE_KEY = "cis.common.ep.localurl";
  
  public static ServiceRegistration.Info[] findServiceByServiceAndEndpointType(LookupClient lookupClient, ServiceRegistration.ServiceType serviceTypeFilter, ServiceRegistration.EndpointType endpointTypeFilter, String nodeId) {
    Objects.requireNonNull(lookupClient);
    ServiceRegistration.Info[] foundServices = null;
    ServiceRegistration serviceRegistration = lookupClient.getServiceRegistration();
    if (serviceRegistration == null)
      return null; 
    ServiceRegistration.Filter filter = new ServiceRegistration.Filter();
    filter.setNodeId(nodeId);
    filter.setServiceType(serviceTypeFilter);
    filter.setEndpointType(endpointTypeFilter);
    foundServices = serviceRegistration.list(filter);
    return foundServices;
  }
  
  public static Service[] findServiceByServiceTypeUriViaLegacyLookup(LookupClient lookupClient, URI serviceTypeUri) throws ServiceFault {
    LookupService lookupService = lookupClient.getLookupService();
    SearchCriteria searchCriteria = new SearchCriteria();
    searchCriteria.setViSite(lookupService.getViSite());
    searchCriteria.setServiceType(serviceTypeUri);
    Service[] services = lookupService.find(searchCriteria);
    return services;
  }
  
  public static ServiceEndpoint[] getFirstServiceEndpoints(Service[] services) {
    if (services == null || services.length == 0)
      return null; 
    ServiceEndpoint[] endpoints = services[0].getEndpoints();
    return endpoints;
  }
  
  public static ServiceRegistration.Endpoint[] getFirstServiceRegistrationEndpoints(ServiceRegistration.Info[] serviceInfos) {
    if (serviceInfos == null || serviceInfos.length == 0)
      return null; 
    ServiceRegistration.Endpoint[] endpoints = serviceInfos[0].getServiceEndpoints();
    return endpoints;
  }
  
  public static Pair<ServiceRegistration.Endpoint, String> getEndpointByEndpointType(LookupClient lookupClient, ServiceRegistration.EndpointType endpointTypeFilter) {
    return getEndpointByEndpointTypeAndNodeId(lookupClient, endpointTypeFilter, null);
  }
  
  public static Pair<ServiceRegistration.Endpoint, String> getEndpointByEndpointTypeAndNodeId(LookupClient lookupClient, ServiceRegistration.EndpointType endpointTypeFilter, String nodeId) {
    Objects.requireNonNull(lookupClient);
    Objects.requireNonNull(endpointTypeFilter);
    ServiceRegistration.Info[] serviceList = findServiceByServiceAndEndpointType(lookupClient, null, endpointTypeFilter, nodeId);
    String endpointType = endpointTypeFilter.getType();
    Pair<ServiceRegistration.Endpoint, String> serviceEndpointToNodeId = null;
    if (serviceList != null && serviceList.length > 0)
      for (ServiceRegistration.Info service : serviceList) {
        for (ServiceRegistration.Endpoint endpoint : service.getServiceEndpoints()) {
          if (endpointType.equals(endpoint.getEndpointType().getType())) {
            serviceEndpointToNodeId = new Pair(endpoint, service.getNodeId());
            break;
          } 
        } 
        if (serviceEndpointToNodeId != null)
          break; 
      }  
    return serviceEndpointToNodeId;
  }
  
  public static URI getEndpointUri(ServiceRegistration.Endpoint endpoint) {
    return getEndpointUri(endpoint, false);
  }
  
  public static URI getEndpointUri(ServiceRegistration.Endpoint endpoint, boolean tryGetLocalUri) {
    if (endpoint == null)
      return null; 
    URI serviceUri = endpoint.getUrl();
    if (tryGetLocalUri) {
      ServiceRegistration.Attribute[] endpointAttributes = endpoint.getEndpointAttributes();
      for (ServiceRegistration.Attribute endpointAttribute : endpointAttributes) {
        if (endpointAttribute.getKey().equals("cis.common.ep.localurl")) {
          serviceUri = URI.create(endpointAttribute.getValue());
          break;
        } 
      } 
    } 
    return serviceUri;
  }
  
  public static URI convertUriToLocalEnvoySidecarIfNeeded(URI serviceUri, boolean shouldUseEnvoySidecar) {
    URI convertedUri = serviceUri;
    if (checkShouldUseEnvoySidecar(shouldUseEnvoySidecar)) {
      convertedUri = EnvoySidecarUtil.toEnvoyLocalUri(serviceUri);
      if (_log.isDebugEnabled())
        _log.debug(String.format("Converted LS service URI '%s' to local Envoy sidecar URI '%s'", new Object[] { serviceUri, convertedUri })); 
    } 
    return convertedUri;
  }
  
  public static URI convertUriToEnvoySidecarIfNeeded(URI serviceUri, String serviceNodeId, String localNodeId, boolean shouldUseEnvoySidecar) {
    URI convertedUri = serviceUri;
    if (checkShouldUseEnvoySidecar(shouldUseEnvoySidecar))
      if (localNodeId.equals(serviceNodeId) || serviceUri.getHost().contains("localhost")) {
        convertedUri = EnvoySidecarUtil.toEnvoyLocalUri(serviceUri);
        if (_log.isDebugEnabled())
          _log.debug(String.format("Converted LS service URI '%s' to local Envoy sidecar URI '%s'", new Object[] { serviceUri, convertedUri })); 
      } else {
        convertedUri = EnvoySidecarUtil.toEnvoyRemoteUri(serviceUri);
        if (_log.isDebugEnabled())
          _log.debug(String.format("Converted LS service URI '%s' to remote Envoy sidecar URI '%s'", new Object[] { serviceUri, convertedUri })); 
      }  
    return convertedUri;
  }
  
  public static String[] getSslTrustForServiceAndEndpointType(LookupClient lookupClient, ServiceRegistration.ServiceType serviceTypeFilter, ServiceRegistration.EndpointType endpointTypeFilter, String nodeId) {
    ServiceRegistration.Info[] serviceList = findServiceByServiceAndEndpointType(lookupClient, serviceTypeFilter, endpointTypeFilter, nodeId);
    if (serviceList == null || serviceList.length == 0)
      return null; 
    List<String> sslTrustList = new ArrayList<>();
    for (ServiceRegistration.Info serviceInfo : serviceList) {
      ServiceRegistration.Endpoint[] serviceEndpoints = serviceInfo.getServiceEndpoints();
      if (serviceEndpoints != null && serviceEndpoints.length != 0)
        for (ServiceRegistration.Endpoint serviceEndpoint : serviceEndpoints)
          sslTrustList.addAll(Arrays.asList(serviceEndpoint.getSslTrust()));  
    } 
    String[] sslTrust = sslTrustList.<String>toArray(new String[sslTrustList.size()]);
    return sslTrust;
  }
  
  public static URI getLookupServiceSdkUri(String lsLocation, boolean shouldUseEnvoySidecar) {
    URI lookupServiceSdkUri = URI.create(lsLocation);
    _log.debug("shouldUseEnvoySidecar: " + shouldUseEnvoySidecar);
    if (shouldUseEnvoySidecar) {
      _log.debug("lookupServiceUrl before envoy conversion is " + lookupServiceSdkUri);
      lookupServiceSdkUri = convertUriToLocalEnvoySidecarIfNeeded(lookupServiceSdkUri, true);
      _log.debug("lookupServiceUrl after envoy conversion is " + lookupServiceSdkUri);
    } 
    _log.debug("returning lookupServiceUrl: " + lookupServiceSdkUri);
    return lookupServiceSdkUri;
  }
  
  private static boolean checkShouldUseEnvoySidecar(boolean shouldUseEnvoySidecar) {
    boolean sidecarFssEnabled = FeatureState.getVC_FIPS_SIDECAR();
    if (_log.isDebugEnabled())
      _log.debug(String.format("Envoy sidecar FSS enabled: %s; Should use Envoy sidecar: %s", new Object[] { Boolean.valueOf(sidecarFssEnabled), Boolean.valueOf(shouldUseEnvoySidecar) })); 
    return (sidecarFssEnabled && shouldUseEnvoySidecar);
  }
}
