package com.vmware.ph.phservice.proxy.util;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentDetector {
  private static final Logger _logger = LoggerFactory.getLogger(EnvironmentDetector.class);
  
  private static final ServiceRegistration.ServiceType SERVICE_TYPE_SSO = new ServiceRegistration.ServiceType("com.vmware.cis", "cs.identity");
  
  private static final ServiceRegistration.EndpointType ENDPOINT_TYPE_SSO_STS = new ServiceRegistration.EndpointType("wsTrust", "com.vmware.cis.cs.identity.sso");
  
  private static final String DOMAIN_TYPE_ATTRIBUTE_KEY = "domainType";
  
  private static final String VMC_DOMAIN_TYPE = "CLOUD";
  
  private VimContextProvider _vimContextProvider;
  
  public EnvironmentDetector(VimContextProvider vimContextProvider) {
    this._vimContextProvider = vimContextProvider;
  }
  
  public boolean isCloudEnvironment() {
    VimContext vimContext = this._vimContextProvider.getVimContext();
    boolean isCloud = false;
    if (vimContext == null) {
      _logger.debug("Could not obtain VimContext. Will consider this is not a VMC environment.");
    } else {
      LookupClient lookupClient = vimContext.getLookupClientBuilder(true).build();
      ServiceRegistration.Filter filter = new ServiceRegistration.Filter();
      filter.setServiceType(SERVICE_TYPE_SSO);
      filter.setEndpointType(ENDPOINT_TYPE_SSO_STS);
      try {
        ServiceRegistration.Info[] serviceRegistrationInfos = lookupClient.getServiceRegistration().list(filter);
        _logger.debug("Found {} service registrations: {}", 
            Integer.valueOf(serviceRegistrationInfos.length), Arrays.toString((Object[])serviceRegistrationInfos));
        Optional<ServiceRegistration.Info> info = Arrays.<ServiceRegistration.Info>asList(serviceRegistrationInfos).stream().filter(serviceRegistrationInfo -> {
              ServiceRegistration.Attribute[] attributes = serviceRegistrationInfo.getServiceAttributes();
              return Arrays.<ServiceRegistration.Attribute>asList(attributes).stream().anyMatch(());
            }).findFirst();
        isCloud = info.isPresent();
      } catch (Exception e) {
        _logger.error("Error while checking if the environment is VMC. Will consider this is not a VMC environment.", e);
      } 
    } 
    return isCloud;
  }
}
