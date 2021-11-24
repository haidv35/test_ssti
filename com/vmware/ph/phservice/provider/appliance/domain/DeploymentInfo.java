package com.vmware.ph.phservice.provider.appliance.domain;

import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DeploymentInfo {
  static final ServiceRegistration.ServiceType SSO_ADMIN_SERVICE_TYPE = new ServiceRegistration.ServiceType("com.vmware.cis", "sso:admin");
  
  static final String HOST_ID_ATTRIBUTE = "com.vmware.cis.cm.HostId";
  
  private final String _hostName;
  
  private final LookupClient _lookupClient;
  
  public DeploymentInfo(String hostName, LookupClient lookupClient) {
    this._hostName = hostName;
    this._lookupClient = lookupClient;
  }
  
  public ServiceInfo[] getPscNodes() {
    ServiceRegistration.Filter serviceRegFilter = new ServiceRegistration.Filter();
    serviceRegFilter.setServiceType(SSO_ADMIN_SERVICE_TYPE);
    ServiceRegistration.Info[] infos = this._lookupClient.getServiceRegistration().list(serviceRegFilter);
    if (infos == null)
      return new ServiceInfo[0]; 
    List<ServiceInfo> result = new ArrayList<>(infos.length);
    for (ServiceRegistration.Info info : infos) {
      String serviceId = info.getServiceId();
      String nodeId = info.getNodeId();
      String ownerId = info.getOwnerId();
      String version = info.getServiceVersion();
      String hostId = null;
      ServiceRegistration.Attribute[] attributes = info.getServiceAttributes();
      if (attributes != null)
        for (ServiceRegistration.Attribute attribute : attributes) {
          if (attribute.getKey().equals("com.vmware.cis.cm.HostId"))
            hostId = attribute.getValue(); 
        }  
      if (hostId == null) {
        ServiceRegistration.Endpoint[] endpoints = info.getServiceEndpoints();
        if (endpoints != null && endpoints.length > 0) {
          ServiceRegistration.Endpoint endpoint = endpoints[0];
          URI uri = endpoint.getUrl();
          hostId = uri.getHost();
        } 
      } 
      ServiceInfo serviceInfo = new ServiceInfo(serviceId, nodeId, hostId, ownerId, version);
      result.add(serviceInfo);
    } 
    return result.<ServiceInfo>toArray(new ServiceInfo[result.size()]);
  }
  
  public String getHostName() {
    return this._hostName;
  }
  
  public static class ServiceInfo {
    private final String _serviceId;
    
    private final String _nodeId;
    
    private final String _hostId;
    
    private final String _ownerId;
    
    private final String _version;
    
    public ServiceInfo(String serviceId, String nodeId, String hostId, String ownerId, String version) {
      this._serviceId = serviceId;
      this._nodeId = nodeId;
      this._hostId = hostId;
      this._ownerId = ownerId;
      this._version = version;
    }
    
    public String getServiceId() {
      return this._serviceId;
    }
    
    public String getNodeId() {
      return this._nodeId;
    }
    
    public String getHostId() {
      return this._hostId;
    }
    
    public String getOwnerId() {
      return this._ownerId;
    }
    
    public String getVersion() {
      return this._version;
    }
  }
}
