package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.Networking;
import com.vmware.appliance.networking.Interfaces;
import com.vmware.appliance.networking.InterfacesTypes;
import com.vmware.appliance.networking.Proxy;
import com.vmware.appliance.networking.ProxyTypes;
import com.vmware.appliance.networking.dns.Domains;
import com.vmware.appliance.networking.dns.Hostname;
import com.vmware.appliance.networking.dns.Servers;
import com.vmware.appliance.networking.interfaces.Ipv4;
import com.vmware.appliance.networking.interfaces.Ipv6;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NetworkingDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Networking.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Ipv4.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Ipv6.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Proxy.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Domains.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Hostname.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Servers.class);
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Interfaces.class);
  }
  
  public NetworkingDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<? extends Object, ? extends Object> executeService(Service service, Object filter) {
    if (service instanceof Networking || service instanceof Domains || service instanceof Hostname || service instanceof Servers) {
      Object value = null;
      if (service instanceof Domains) {
        value = DataProviderUtil.getMethodInvocationReturnValue(service, "list");
      } else {
        value = DataProviderUtil.getMethodInvocationReturnValue(service, "get");
      } 
      HashMap<Object, Object> result = new HashMap<>();
      result.put(null, value);
      return result;
    } 
    if (service instanceof Proxy) {
      Map<ProxyTypes.Protocol, ProxyTypes.Config> proxyProtocolToConfig = ((Proxy)service).list();
      return (Map)proxyProtocolToConfig;
    } 
    if (service instanceof Interfaces) {
      List<InterfacesTypes.InterfaceInfo> interfaces = ((Interfaces)service).list();
      Map<Object, Object> result = new LinkedHashMap<>();
      for (InterfacesTypes.InterfaceInfo interfaceInfo : interfaces)
        result.put(interfaceInfo.getName(), interfaceInfo); 
      return result;
    } 
    if (service instanceof Ipv4) {
      Interfaces interfacesService = (Interfaces)this._vapiClient.createStub(Interfaces.class);
      List<InterfacesTypes.InterfaceInfo> interfaces = interfacesService.list();
      Map<Object, Object> result = new LinkedHashMap<>();
      for (InterfacesTypes.InterfaceInfo interfaceInfo : interfaces)
        result.put(interfaceInfo.getName(), interfaceInfo.getIpv4()); 
      return result;
    } 
    if (service instanceof Ipv6) {
      Interfaces interfacesService = (Interfaces)this._vapiClient.createStub(Interfaces.class);
      List<InterfacesTypes.InterfaceInfo> interfaces = interfacesService.list();
      Map<Object, Object> result = new LinkedHashMap<>();
      for (InterfacesTypes.InterfaceInfo interfaceInfo : interfaces)
        result.put(interfaceInfo.getName(), interfaceInfo.getIpv6()); 
      return result;
    } 
    return null;
  }
}
