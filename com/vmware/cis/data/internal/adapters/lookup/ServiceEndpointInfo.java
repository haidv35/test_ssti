package com.vmware.cis.data.internal.adapters.lookup;

import com.vmware.cis.data.internal.provider.SsoDomain;
import java.net.URI;
import java.security.KeyStore;
import org.apache.commons.lang.Validate;

public final class ServiceEndpointInfo {
  private final String _serviceType;
  
  private final URI _endpointAddress;
  
  private final String _nodeId;
  
  private final KeyStore _trustStore;
  
  private final String _endpointType;
  
  private final String _endpointProtocol;
  
  private final String _serviceUuid;
  
  private final Class<?> _vcVmodlVersion;
  
  private final SsoDomain _ssoDomain;
  
  public ServiceEndpointInfo(String serviceType, URI endpointAddress, String nodeId, KeyStore trustStore, String endpointType, String endpointProtocol, String serviceUuid, Class<?> vcVmodlVersion, SsoDomain ssoDomain) {
    Validate.notEmpty(serviceType, "serviceType is required.");
    Validate.notNull(endpointAddress, "endpointAddress is required.");
    if ("https".equalsIgnoreCase(endpointAddress.getScheme()))
      Validate.notNull(trustStore, "trustStore is required for https endpoint"); 
    Validate.notEmpty(endpointType, "endpointType is required.");
    Validate.notEmpty(endpointProtocol, "endpointProtocol is required.");
    Validate.notEmpty(serviceUuid, "serviceUuid is required");
    this._serviceType = serviceType;
    this._endpointAddress = endpointAddress;
    this._nodeId = nodeId;
    this._trustStore = trustStore;
    this._endpointType = endpointType;
    this._endpointProtocol = endpointProtocol;
    this._serviceUuid = serviceUuid;
    this._vcVmodlVersion = vcVmodlVersion;
    this._ssoDomain = ssoDomain;
  }
  
  public String getServiceType() {
    return this._serviceType;
  }
  
  public URI getUrl() {
    return this._endpointAddress;
  }
  
  public String getNodeId() {
    return this._nodeId;
  }
  
  public KeyStore getTrustStore() {
    return this._trustStore;
  }
  
  public String getEndpointType() {
    return this._endpointType;
  }
  
  public String getEndpointProtocol() {
    return this._endpointProtocol;
  }
  
  public String getServiceUuid() {
    return this._serviceUuid;
  }
  
  public Class<?> getVcVmodlVersion() {
    return this._vcVmodlVersion;
  }
  
  public SsoDomain getSsoDomain() {
    return this._ssoDomain;
  }
  
  public String toString() {
    return String.format("ServiceEndpoint[serviceType = %s, url = %s, uuid = %s, node = %s, endpointType = %s, protocol = %s, vimVersion = %s, ssoDomain = %s]", new Object[] { this._serviceType, this._endpointAddress, this._serviceUuid, this._nodeId, this._endpointType, this._endpointProtocol, this._vcVmodlVersion, this._ssoDomain });
  }
}
