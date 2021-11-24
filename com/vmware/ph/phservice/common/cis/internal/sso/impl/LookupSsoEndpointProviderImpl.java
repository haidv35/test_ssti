package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpoint;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoEndpointProvider;
import com.vmware.ph.phservice.common.cis.internal.sso.SsoException;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.Service;
import com.vmware.vim.binding.lookup.ServiceEndpoint;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.fault.ServiceFault;
import java.net.URI;
import java.util.Arrays;

public class LookupSsoEndpointProviderImpl implements SsoEndpointProvider {
  private static final URI LS1_SERVICE_TYPE_SSO_STS = URI.create("urn:sso:sts");
  
  private static final URI LS1_SERVICE_TYPE_SSO_ADMIN = URI.create("urn:sso:admin");
  
  private static final ServiceRegistration.ServiceType LS2_SERVICE_TYPE_SSO = new ServiceRegistration.ServiceType("com.vmware.cis", "cs.identity");
  
  private static final ServiceRegistration.EndpointType LS2_ENDPOINT_TYPE_SSO_STS = new ServiceRegistration.EndpointType("wsTrust", "com.vmware.cis.cs.identity.sso");
  
  private static final ServiceRegistration.EndpointType LS2_ENDPOINT_TYPE_SSO_ADMIN = new ServiceRegistration.EndpointType("vmomi", "com.vmware.cis.cs.identity.admin");
  
  private final LookupClientBuilder _lookupClientBuilder;
  
  private final boolean _shouldUseEnvoySidecar;
  
  public LookupSsoEndpointProviderImpl(LookupClientBuilder lookupClientBuilder, boolean shouldUseEnvoySidecar) {
    this._lookupClientBuilder = lookupClientBuilder;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public SsoEndpoint getAdminEndpoint() throws SsoException {
    try (LookupClient lookupClient = this._lookupClientBuilder.build()) {
      if (lookupClient.isServiceRegistrationSupported())
        return getSsoEndpointViaServiceRegistration(lookupClient, LS2_SERVICE_TYPE_SSO, LS2_ENDPOINT_TYPE_SSO_ADMIN, this._shouldUseEnvoySidecar); 
      return getSsoEndpointViaLookupService(lookupClient, LS1_SERVICE_TYPE_SSO_ADMIN, this._shouldUseEnvoySidecar);
    } 
  }
  
  public SsoEndpoint getStsEndpoint() throws SsoException {
    try (LookupClient lookupClient = this._lookupClientBuilder.build()) {
      if (lookupClient.isServiceRegistrationSupported())
        return getSsoEndpointViaServiceRegistration(lookupClient, LS2_SERVICE_TYPE_SSO, LS2_ENDPOINT_TYPE_SSO_STS, this._shouldUseEnvoySidecar); 
      return getSsoEndpointViaLookupService(lookupClient, LS1_SERVICE_TYPE_SSO_STS, this._shouldUseEnvoySidecar);
    } 
  }
  
  private static SsoEndpoint getSsoEndpointViaLookupService(LookupClient lookupClient, URI ls1ServiceTypeUri, boolean shouldUseEnvoySidecar) throws SsoException {
    try {
      Service[] services = ServiceLocatorUtil.findServiceByServiceTypeUriViaLegacyLookup(lookupClient, ls1ServiceTypeUri);
      ServiceEndpoint[] endpoints = ServiceLocatorUtil.getFirstServiceEndpoints(services);
      if (endpoints == null || endpoints.length != 1)
        throw new SsoException("There must be exactly one endpoint for the STS service with endpointType='" + ls1ServiceTypeUri + "'. Actual found=" + 

            
            Arrays.toString(endpoints)); 
      URI endpointUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(endpoints[0]
          .getUrl(), shouldUseEnvoySidecar);
      String[] endpointSslTrust = { endpoints[0].getSslTrustAnchor() };
      return new SsoEndpoint(endpointUri, endpointSslTrust);
    } catch (ServiceFault e) {
      throw new SsoException(e);
    } 
  }
  
  private static SsoEndpoint getSsoEndpointViaServiceRegistration(LookupClient lookupClient, ServiceRegistration.ServiceType ls2ServiceType, ServiceRegistration.EndpointType ls2EndpointType, boolean shouldUseEnvoySidecar) throws SsoException {
    ServiceRegistration.Info[] serviceInfos = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, ls2ServiceType, ls2EndpointType, null);
    ServiceRegistration.Endpoint[] endpoints = ServiceLocatorUtil.getFirstServiceRegistrationEndpoints(serviceInfos);
    if (endpoints == null || endpoints.length != 1)
      throw new SsoException("There must be exactly one endpoint for the STS service with endpointType='" + ls2ServiceType + "'. Actual found=" + 

          
          Arrays.toString(endpoints)); 
    URI endpointUri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(endpoints[0]
        .getUrl(), shouldUseEnvoySidecar);
    String[] endpointSslTrust = endpoints[0].getSslTrust();
    return new SsoEndpoint(endpointUri, endpointSslTrust);
  }
}
