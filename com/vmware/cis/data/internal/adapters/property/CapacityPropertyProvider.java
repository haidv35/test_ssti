package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.internal.adapters.vmomi.impl.VlsiClientUtil;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.vim.ClusterComputeResource;
import com.vmware.vim.binding.vim.cluster.ResourceUsageSummary;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CapacityPropertyProvider implements BackCompatPropertyProvider {
  private static final String CLUSTER_MODEL = "ClusterComputeResource";
  
  private static final String PROP_CPU_FREE = "ClusterComputeResource/freeCpuCapacity";
  
  private static final String PROP_CPU_TOTAL = "ClusterComputeResource/totalCpuCapacity";
  
  private static final String PROP_CPU_USED = "ClusterComputeResource/usedCpuCapacity";
  
  private static final String PROP_MEMORY_FREE = "ClusterComputeResource/freeMemoryCapacity";
  
  private static final String PROP_MEMORY_TOTAL = "ClusterComputeResource/totalMemoryCapacity";
  
  private static final String PROP_MEMORY_USED = "ClusterComputeResource/usedMemoryCapacity";
  
  private static final String PROP_STORAGE_FREE = "ClusterComputeResource/freeStorageCapacity";
  
  private static final String PROP_STORAGE_TOTAL = "ClusterComputeResource/totalStorageCapacity";
  
  private static final String PROP_STORAGE_USED = "ClusterComputeResource/usedStorageCapacity";
  
  private static final List<String> SUPPORTED_PROPERTIES = Arrays.asList(new String[] { "ClusterComputeResource/freeCpuCapacity", "ClusterComputeResource/totalCpuCapacity", "ClusterComputeResource/usedCpuCapacity", "ClusterComputeResource/freeMemoryCapacity", "ClusterComputeResource/totalMemoryCapacity", "ClusterComputeResource/usedMemoryCapacity", "ClusterComputeResource/freeStorageCapacity", "ClusterComputeResource/totalStorageCapacity", "ClusterComputeResource/usedStorageCapacity" });
  
  public Collection<String> getProperties() {
    return SUPPORTED_PROPERTIES;
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> keys, DataProvider provider, Client vlsiClient) {
    assert SUPPORTED_PROPERTIES.containsAll(properties);
    Collection<ResourceUsageSummary> resourceUsages = getResourceUsages(keys, vlsiClient);
    List<Collection<?>> propertyValues = evalPropertyValues(properties, resourceUsages);
    return propertyValues;
  }
  
  private static List<Collection<?>> evalPropertyValues(List<String> properties, Collection<ResourceUsageSummary> resourceUsages) {
    List<Collection<?>> propertyValues = new ArrayList<>(properties.size());
    for (String property : properties) {
      List<Object> values = new ArrayList(resourceUsages.size());
      for (ResourceUsageSummary resourceUsage : resourceUsages) {
        Object value = evalPropertyValue(property, resourceUsage);
        values.add(value);
      } 
      propertyValues.add(values);
    } 
    return propertyValues;
  }
  
  private static Object evalPropertyValue(String property, ResourceUsageSummary resourceUsage) {
    switch (property) {
      case "ClusterComputeResource/freeCpuCapacity":
        return Integer.valueOf(resourceUsage.cpuCapacityMHz - resourceUsage.cpuUsedMHz);
      case "ClusterComputeResource/totalCpuCapacity":
        return Integer.valueOf(resourceUsage.cpuCapacityMHz);
      case "ClusterComputeResource/usedCpuCapacity":
        return Integer.valueOf(resourceUsage.cpuUsedMHz);
      case "ClusterComputeResource/freeMemoryCapacity":
        return Integer.valueOf(resourceUsage.memCapacityMB - resourceUsage.memUsedMB);
      case "ClusterComputeResource/totalMemoryCapacity":
        return Integer.valueOf(resourceUsage.memCapacityMB);
      case "ClusterComputeResource/usedMemoryCapacity":
        return Integer.valueOf(resourceUsage.memUsedMB);
      case "ClusterComputeResource/freeStorageCapacity":
        return Long.valueOf(resourceUsage.storageCapacityMB - resourceUsage.storageUsedMB);
      case "ClusterComputeResource/totalStorageCapacity":
        return Long.valueOf(resourceUsage.storageCapacityMB);
      case "ClusterComputeResource/usedStorageCapacity":
        return Long.valueOf(resourceUsage.storageUsedMB);
    } 
    throw new IllegalArgumentException("Unsupported property: " + property);
  }
  
  private static Collection<ResourceUsageSummary> getResourceUsages(Collection<Object> keys, Client vlsiClient) {
    List<ResourceUsageSummary> resourceUsages = new ArrayList<>(keys.size());
    for (Object key : keys) {
      assert key instanceof ManagedObjectReference;
      ManagedObjectReference mor = (ManagedObjectReference)key;
      ClusterComputeResource cluster = VlsiClientUtil.<ClusterComputeResource>createStub(vlsiClient, ClusterComputeResource.class, mor);
      ResourceUsageSummary resourceUsage = cluster.getResourceUsage();
      resourceUsages.add(resourceUsage);
    } 
    return resourceUsages;
  }
}
