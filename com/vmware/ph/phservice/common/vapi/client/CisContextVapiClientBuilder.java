package com.vmware.ph.phservice.common.vapi.client;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceCredentialsProvider;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.vapi.util.VapiServiceLocatorUtil;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CisContextVapiClientBuilder implements Builder<VapiClient> {
  private static final Log _log = LogFactory.getLog(CisContextVapiClientBuilder.class);
  
  private final CisContext _cisContext;
  
  private final ServiceRegistration.EndpointType _endpointType;
  
  private final ServiceRegistration.EndpointType _explicitLocalEndpointType;
  
  private final VapiClientFactory _vapiClientFactory;
  
  private boolean _shouldUseSessionAuthentication = false;
  
  public CisContextVapiClientBuilder(CisContext cisContext, ServiceRegistration.EndpointType endpointType) {
    this(cisContext, endpointType, null);
  }
  
  public CisContextVapiClientBuilder(CisContext cisContext, ServiceRegistration.EndpointType endpointType, ServiceRegistration.EndpointType explicitLocalEndpointType) {
    this(cisContext, new VapiClientFactory(cisContext
          .getTrustedStore()), endpointType, explicitLocalEndpointType);
  }
  
  CisContextVapiClientBuilder(CisContext cisContext, VapiClientFactory vapiClientFactory, ServiceRegistration.EndpointType endpointType, ServiceRegistration.EndpointType explicitLocalEndpointType) {
    this._cisContext = cisContext;
    this._vapiClientFactory = vapiClientFactory;
    this._endpointType = endpointType;
    this._explicitLocalEndpointType = explicitLocalEndpointType;
  }
  
  public CisContextVapiClientBuilder withSessionAuthentication() {
    this._shouldUseSessionAuthentication = true;
    return this;
  }
  
  public VapiClient build() {
    VapiClient vapiClient;
    URI vapiUri = null;
    try {
      vapiUri = getVapiUri();
    } catch (Exception e) {
      if (_log.isDebugEnabled())
        _log.debug("VAPI URI could not be located.", e); 
    } 
    if (vapiUri == null)
      throw new IllegalStateException("Unable to locate VAPI URI."); 
    ApplianceCredentialsProvider applianceCredentialsProvider = null;
    ApplianceContext applianceContext = this._cisContext.getApplianceContext();
    String applianceId = null;
    if (applianceContext != null) {
      applianceCredentialsProvider = applianceContext.getCredentialsProvider();
      applianceId = applianceContext.getId();
    } 
    if (this._shouldUseSessionAuthentication) {
      vapiClient = this._vapiClientFactory.createClient(vapiUri, this._cisContext
          
          .getSsoTokenProvider(), true);
    } else if (applianceCredentialsProvider != null) {
      vapiClient = this._vapiClientFactory.createClient(vapiUri, applianceCredentialsProvider
          
          .getUsername(), applianceCredentialsProvider
          .getPassword());
    } else {
      vapiClient = this._vapiClientFactory.createClient(vapiUri, this._cisContext
          
          .getSsoTokenProvider());
    } 
    if (vapiClient != null)
      vapiClient.setApplianceId(applianceId); 
    return vapiClient;
  }
  
  private URI getVapiUri() throws Exception {
    boolean tryGetLocalVapiUri = false;
    String nodeId = null;
    ApplianceContext applianceContext = this._cisContext.getApplianceContext();
    if (applianceContext != null) {
      tryGetLocalVapiUri = applianceContext.isLocal();
      nodeId = applianceContext.getNodeId();
    } 
    ServiceRegistration.EndpointType endpointType = this._endpointType;
    if (tryGetLocalVapiUri && this._explicitLocalEndpointType != null) {
      endpointType = this._explicitLocalEndpointType;
      tryGetLocalVapiUri = false;
    } 
    if (endpointType == null)
      return null; 
    LookupClientBuilder lookupClientBuilder = this._cisContext.getLookupClientBuilder();
    return VapiServiceLocatorUtil.discoverVapiServiceUri(lookupClientBuilder, endpointType, nodeId, tryGetLocalVapiUri, this._cisContext



        
        .getShouldUseEnvoySidecar());
  }
}
