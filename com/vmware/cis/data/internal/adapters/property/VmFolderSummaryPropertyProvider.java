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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VmFolderSummaryPropertyProvider implements BackCompatPropertyProvider {
  private static final Logger _logger = LoggerFactory.getLogger(VmFolderSummaryPropertyProvider.class);
  
  private static final String FOLDER = "Folder";
  
  private static final String FOLDER_ALL_NORMAL_VM_OR_PRIMARY_FTVM_FOR_VM_FOLDER_PROPERTY = "Folder/allNormalVMOrPrimaryFTVMForVMFolder";
  
  private static final String VM = "VirtualMachine";
  
  private static final String VM_COMMITTED_STORAGE_PROPERTY = "VirtualMachine/summary/storage/committed";
  
  private static final String VM_OVERALL_CPU_USAGE_PROPERTY = "VirtualMachine/summary/quickStats/overallCpuUsage";
  
  private static final String VM_GUEST_MEMORY_USAGE_PROPERTY = "VirtualMachine/summary/quickStats/guestMemoryUsage";
  
  private static final String FOLDER_CPU_USAGE_MHZ_PROPERTY = "Folder/cpuUsageMhz";
  
  private static final String FOLDER_STORAGE_USAGE_B_PROPERTY = "Folder/storageUsageB";
  
  private static final String FOLDER_MEMORY_USAGE_MB_PROPERTY = "Folder/memoryUsageMb";
  
  private static final Map<String, String> VM_PROPERTIES_BY_FOLDER_PROPERTIES;
  
  static {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("Folder/cpuUsageMhz", "VirtualMachine/summary/quickStats/overallCpuUsage");
    map.put("Folder/storageUsageB", "VirtualMachine/summary/storage/committed");
    map.put("Folder/memoryUsageMb", "VirtualMachine/summary/quickStats/guestMemoryUsage");
    VM_PROPERTIES_BY_FOLDER_PROPERTIES = Collections.unmodifiableMap(map);
  }
  
  public Collection<String> getProperties() {
    return VM_PROPERTIES_BY_FOLDER_PROPERTIES.keySet();
  }
  
  public List<Collection<?>> fetchPropertyValues(List<String> properties, Collection<Object> keys, DataProvider provider, Client vlsiClient) {
    assert VM_PROPERTIES_BY_FOLDER_PROPERTIES.keySet().containsAll(properties);
    assert !keys.isEmpty();
    ResultSet folderResultSet = executeFolderQuery(keys, provider);
    Map<Object, Object[]> folderVmsByFolder = createFolderVmsByFolder(folderResultSet);
    Collection<Object> vmKeys = extractAllVmKeys(folderVmsByFolder);
    ResultSet vmsResultSet = executeVmQuery(properties, provider, vmKeys);
    List<ResourceItem> vmResourceItems = vmsResultSet.getItems();
    Map<Object, ResourceItem> vmItemByRef = getVmItemByRef(vmResourceItems);
    List<Collection<?>> result = createResult(keys, folderVmsByFolder, vmItemByRef, properties);
    assert result.size() == properties.size();
    return result;
  }
  
  private static Map<Object, Object[]> createFolderVmsByFolder(ResultSet folderResultSet) {
    List<ResourceItem> folderResultItems = folderResultSet.getItems();
    Map<Object, Object[]> folderVmByFolder = (Map)new LinkedHashMap<>(folderResultItems.size());
    for (ResourceItem folder : folderResultItems) {
      Object folderKey = folder.getKey();
      Object[] folderVms = folder.<Object[]>get("Folder/allNormalVMOrPrimaryFTVMForVMFolder");
      folderVmByFolder.put(folderKey, folderVms);
    } 
    return folderVmByFolder;
  }
  
  private static ResultSet executeFolderQuery(Collection<Object> folderKeys, DataProvider provider) {
    Query query = Query.Builder.select(new String[] { "Folder/allNormalVMOrPrimaryFTVMForVMFolder" }).from(new String[] { "Folder" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, folderKeys).build();
    ResultSet resultSet = provider.executeQuery(query);
    return resultSet;
  }
  
  private ResultSet executeVmQuery(List<String> folderProperties, DataProvider provider, Collection<Object> vmKeys) {
    if (vmKeys.isEmpty())
      return ResultSet.EMPTY_RESULT; 
    List<String> vmProperties = getRequiredVmProperties(folderProperties);
    assert vmProperties.size() == folderProperties.size();
    Query query = Query.Builder.select(vmProperties).from(new String[] { "VirtualMachine" }).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, vmKeys).build();
    ResultSet vmsResultSet = provider.executeQuery(query);
    return vmsResultSet;
  }
  
  private static Map<Object, ResourceItem> getVmItemByRef(List<ResourceItem> vms) {
    Map<Object, ResourceItem> propertiesByVm = new LinkedHashMap<>();
    for (ResourceItem vm : vms)
      propertiesByVm.put(vm.getKey(), vm); 
    return propertiesByVm;
  }
  
  private static List<Collection<?>> createResult(Collection<Object> folderKeys, Map<Object, Object[]> folderVmsByFolder, Map<Object, ResourceItem> vmItemByRef, Collection<String> folderProperties) {
    List<Collection<?>> result = new ArrayList<>(folderProperties.size());
    for (String folderProperty : folderProperties) {
      List<Long> folderPropertyValues = new ArrayList<>(folderVmsByFolder.size());
      for (Object folderKey : folderKeys) {
        Object[] folderVmRefs = folderVmsByFolder.get(folderKey);
        if (folderVmRefs == null) {
          folderPropertyValues.add(Long.valueOf(0L));
          continue;
        } 
        long folderPropertyValue = getFolderPropertyValue(folderVmRefs, vmItemByRef, folderProperty);
        folderPropertyValues.add(Long.valueOf(folderPropertyValue));
      } 
      assert folderPropertyValues.size() == folderKeys.size();
      result.add(folderPropertyValues);
    } 
    return result;
  }
  
  private static long getFolderPropertyValue(Object[] vmKeys, Map<Object, ResourceItem> vmItemByRef, String folderProperty) {
    long sum = 0L;
    for (Object vmKey : vmKeys) {
      ResourceItem resourceItem = vmItemByRef.get(vmKey);
      if (resourceItem == null) {
        _logger.debug("VM with key {} was probably deleted. It will not be included in the evaluation of the property {}", vmKey, folderProperty);
      } else {
        String vmProperty = VM_PROPERTIES_BY_FOLDER_PROPERTIES.get(folderProperty);
        Object vmPropertyValueObject = resourceItem.get(vmProperty);
        if (vmPropertyValueObject != null) {
          Number vmPropertyValue = (Number)vmPropertyValueObject;
          sum += vmPropertyValue.longValue();
        } 
      } 
    } 
    return sum;
  }
  
  private static Collection<Object> extractAllVmKeys(Map<Object, Object[]> folderVmsByFolder) {
    Set<Object> vmKeysSet = new LinkedHashSet();
    for (Object folderKey : folderVmsByFolder.keySet()) {
      Object[] vmRefs = folderVmsByFolder.get(folderKey);
      if (vmRefs == null)
        continue; 
      for (Object vmRef : vmRefs)
        vmKeysSet.add(vmRef); 
    } 
    return vmKeysSet;
  }
  
  private static List<String> getRequiredVmProperties(Collection<String> folderProperties) {
    List<String> vmProperties = new ArrayList<>(folderProperties.size());
    for (String property : folderProperties) {
      String vmProperty = VM_PROPERTIES_BY_FOLDER_PROPERTIES.get(property);
      vmProperties.add(vmProperty);
    } 
    return vmProperties;
  }
}
