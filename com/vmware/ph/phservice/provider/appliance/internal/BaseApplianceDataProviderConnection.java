package com.vmware.ph.phservice.provider.appliance.internal;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.vapi.client.CisContextVapiClientBuilder;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.util.Objects;

public abstract class BaseApplianceDataProviderConnection implements DataProvidersConnection {
  private final CisContext _cisContext;
  
  private ServiceRegistration.EndpointType _endpointType;
  
  private ServiceRegistration.EndpointType _explicitLocalEndpointType;
  
  private VapiClient _vapiClient;
  
  public BaseApplianceDataProviderConnection(CisContext cisContext) {
    Objects.requireNonNull(cisContext.getApplianceContext());
    this._cisContext = cisContext;
  }
  
  public void setEndpointType(ServiceRegistration.EndpointType endpointType) {
    this._endpointType = endpointType;
  }
  
  public void setExplicitLocalEndpointType(ServiceRegistration.EndpointType explicitLocalEndpointType) {
    this._explicitLocalEndpointType = explicitLocalEndpointType;
  }
  
  protected String getApplianceId() {
    return this._cisContext.getApplianceContext().getId();
  }
  
  protected VapiClient getVapiClient() {
    if (this._endpointType == null)
      return null; 
    if (this._vapiClient == null)
      this


        
        ._vapiClient = (new CisContextVapiClientBuilder(this._cisContext, this._endpointType, this._explicitLocalEndpointType)).build(); 
    return this._vapiClient;
  }
  
  public void close() {
    if (this._vapiClient != null) {
      try {
        this._vapiClient.close();
      } catch (Exception exception) {}
      this._vapiClient = null;
    } 
  }
}
