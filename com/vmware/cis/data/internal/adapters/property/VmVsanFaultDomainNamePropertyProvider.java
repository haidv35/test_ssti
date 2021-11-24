package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VmVsanFaultDomainNamePropertyProvider implements BackCompatPropertyProvider {
  private static final String VM = "VirtualMachine";
  
  private static final String VM_VSAN_FAULT_DOMAIN_NAME = "VirtualMachine/vsanFaultDomainName";
  
  private static final String VM_RUNTIME_HOST = "VirtualMachine/runtime/host";
  
  private static final String HOST = "HostSystem";
  
  private static final String HOST_VSAN_CONFIG_ENABLED = "HostSystem/config/vsanHostConfig/enabled";
  
  private static final String HOST_VSAN_FAULT_DOMAIN_INFO_NAME = "HostSystem/config/vsanHostConfig/faultDomainInfo/name";
  
  private static final Collection<String> _properties;
  
  static {
    List<String> properties = new ArrayList<>();
    properties.add("VirtualMachine/vsanFaultDomainName");
    _properties = Collections.unmodifiableList(properties);
  }
  
  public Collection<String> getProperties() {
    return _properties;
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> vmKeys, DataProvider provider, Client vlsiClient) {
    assert properties != null;
    assert properties.size() == 1;
    assert properties.contains("VirtualMachine/vsanFaultDomainName");
    Map<Object, Object> hostByVm = fetchHostByVm(vmKeys, provider);
    Collection<Object> hostKeys = hostByVm.values();
    Map<Object, String> vsanFaultDomainNamesByHost = fetchVsanFaultDomainNamesByHost(hostKeys, provider);
    List<String> vmVsanFaultDomainNames = new ArrayList<>();
    for (Object vmKey : vmKeys) {
      Object hostKey = hostByVm.get(vmKey);
      if (hostKey == null) {
        vmVsanFaultDomainNames.add(null);
        continue;
      } 
      String vsanFaultDomainName = vsanFaultDomainNamesByHost.get(hostKey);
      vmVsanFaultDomainNames.add(vsanFaultDomainName);
    } 
    List<Collection<?>> propertyValues = new ArrayList<>();
    propertyValues.add(vmVsanFaultDomainNames);
    return propertyValues;
  }
  
  private static Map<Object, String> fetchVsanFaultDomainNamesByHost(Collection<Object> hostKeys, DataProvider provider) {
    Query query = Query.Builder.select(new String[] { "HostSystem/config/vsanHostConfig/enabled", "HostSystem/config/vsanHostConfig/faultDomainInfo/name" }).from(new String[] { "HostSystem" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, hostKeys).build();
    ResultSet resultSet = provider.executeQuery(query);
    Map<Object, String> vsanFaultDomainNamesByHost = new LinkedHashMap<>();
    List<ResourceItem> items = resultSet.getItems();
    for (ResourceItem item : items) {
      String vsanFaultDomainName = getVsanFaultDomainName(item);
      vsanFaultDomainNamesByHost.put(item.getKey(), vsanFaultDomainName);
    } 
    return vsanFaultDomainNamesByHost;
  }
  
  private static String getVsanFaultDomainName(ResourceItem resourceItem) {
    Boolean vsanConfigEnabled = resourceItem.<Boolean>get("HostSystem/config/vsanHostConfig/enabled");
    if (vsanConfigEnabled == null || !vsanConfigEnabled.booleanValue())
      return null; 
    return resourceItem.<String>get("HostSystem/config/vsanHostConfig/faultDomainInfo/name");
  }
  
  private static Map<Object, Object> fetchHostByVm(Collection<Object> keys, DataProvider provider) {
    Query query = Query.Builder.select(new String[] { "VirtualMachine/runtime/host" }).from(new String[] { "VirtualMachine" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, keys).build();
    ResultSet resultSet = provider.executeQuery(query);
    Map<Object, Object> hostByVm = new LinkedHashMap<>();
    List<ResourceItem> items = resultSet.getItems();
    for (ResourceItem item : items) {
      Object hostKey = item.get("VirtualMachine/runtime/host");
      if (hostKey != null) {
        Object vmKey = item.getKey();
        hostByVm.put(vmKey, hostKey);
      } 
    } 
    return hostByVm;
  }
}
