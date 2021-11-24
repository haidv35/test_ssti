package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vmomi.util.VmomiProperty;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.cis.data.provider.OptionalPropertyValue;
import com.vmware.vim.binding.cis.data.provider.ResourceItem;
import com.vmware.vim.binding.cis.data.provider.ResultSet;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.cis.CisIdConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

final class VmomiDataProviderResultSetConverter {
  public static ResultSet convertResultSet(ResultSet vmomiResultSet, Query query) {
    Validate.notNull(vmomiResultSet);
    Validate.notNull(query);
    List<String> properties = query.getProperties();
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    if (query.getLimit() != 0)
      convertItems(resultBuilder, vmomiResultSet.getItems(), vmomiResultSet
          .getProperties(), properties); 
    Integer totalCount = getTotalCount(vmomiResultSet, query.getWithTotalCount());
    ResultSet resultSet = resultBuilder.totalCount(totalCount).build();
    resultSet = NamePropertyValueConverter.unescapeNamesInResult(resultSet);
    return resultSet;
  }
  
  static Collection<ResultSet> convertResultSets(ResultSet[] vmomiResults, Collection<Query> queries) {
    assert vmomiResults != null;
    assert queries != null;
    assert vmomiResults.length == queries.size();
    Iterator<Query> queryIt = queries.iterator();
    Collection<ResultSet> results = new ArrayList<>(vmomiResults.length);
    for (ResultSet vmomiResult : vmomiResults) {
      Query query = queryIt.next();
      ResultSet result = convertResultSet(vmomiResult, query);
      results.add(result);
    } 
    return results;
  }
  
  private static Integer getTotalCount(ResultSet vmomiResultSet, boolean withTotalCount) {
    if (withTotalCount) {
      if (vmomiResultSet.getTotalCount() == null)
        throw new IllegalArgumentException("Response does not contain total item count"); 
      return Integer.valueOf(vmomiResultSet.getTotalCount().intValue());
    } 
    return null;
  }
  
  private static void convertItems(ResultSet.Builder resultBuilder, ResourceItem[] vmomiItems, String[] vmomiProperties, List<String> coreProperties) {
    if (vmomiItems == null)
      return; 
    int keyPosition = getKeyPosition(vmomiProperties);
    for (int i = 0; i < vmomiItems.length; i++) {
      ResourceItem vmomiItem = vmomiItems[i];
      OptionalPropertyValue optionalValueKey = vmomiItem.getPropertyValues()[keyPosition];
      List<Object> item = convertItem(vmomiItem, vmomiProperties, coreProperties);
      resultBuilder.item(optionalValueKey.value, item);
    } 
  }
  
  private static int getKeyPosition(String[] vmomiProperties) {
    int index = 0;
    for (String vmomiProperty : vmomiProperties) {
      if (PropertyUtil.isModelKey(vmomiProperty))
        return index; 
      index++;
    } 
    throw new IllegalArgumentException("No key in the vmomi properties.");
  }
  
  private static List<Object> convertItem(ResourceItem vmomiItem, String[] vmomiProperties, List<String> coreProperties) {
    Validate.notNull(vmomiItem);
    Validate.notNull(vmomiProperties);
    Validate.notNull(coreProperties);
    OptionalPropertyValue[] vmomiValues = vmomiItem.getPropertyValues();
    if (vmomiValues.length != vmomiProperties.length)
      throw new IllegalArgumentException(String.format("The number of property value in a VMOMI ResourceItem does not match the number of properties in the VMOMI ResultSet: %d != %d", new Object[] { Integer.valueOf(vmomiValues.length), Integer.valueOf(vmomiProperties.length) })); 
    Map<String, Object> vmomiValueByProperty = toVmomiValueByProperty(vmomiItem, vmomiProperties);
    List<Object> coreValues = new ArrayList(coreProperties.size());
    for (String coreProperty : coreProperties) {
      String vmomiProperty = VmomiProperty.toVmomiProperty(coreProperty);
      Object vmomiValue = vmomiValueByProperty.get(vmomiProperty);
      Object coreValue = convertPropertyValue(coreProperty, vmomiValue);
      coreValues.add(coreValue);
    } 
    return coreValues;
  }
  
  private static Map<String, Object> toVmomiValueByProperty(ResourceItem vmomiItem, String[] vmomiProperties) {
    assert vmomiItem != null;
    assert vmomiItem.getPropertyValues() != null;
    assert vmomiProperties != null;
    assert vmomiProperties.length == (vmomiItem.getPropertyValues()).length;
    OptionalPropertyValue[] vmomiValues = vmomiItem.getPropertyValues();
    Map<String, Object> valueByProperty = new HashMap<>(vmomiProperties.length);
    for (int i = 0; i < vmomiProperties.length; i++) {
      String vmomiProperty = vmomiProperties[i];
      OptionalPropertyValue vmomiValue = vmomiValues[i];
      valueByProperty.put(vmomiProperty, vmomiValue.getValue());
    } 
    return valueByProperty;
  }
  
  private static Object convertPropertyValue(String coreProperty, Object vmomiValue) {
    assert coreProperty != null;
    Object coreValue = null;
    if (PropertyUtil.isType(coreProperty)) {
      ManagedObjectReference mor = getMor(vmomiValue);
      if (mor != null)
        coreValue = mor.getType(); 
    } else if (VmomiProperty.isForeignKey(coreProperty)) {
      coreValue = convertToCisId(vmomiValue);
    } else {
      coreValue = vmomiValue;
    } 
    return coreValue;
  }
  
  private static Object convertToCisId(Object vmomiValue) {
    if (vmomiValue == null)
      return null; 
    if (vmomiValue instanceof ManagedObjectReference[]) {
      ManagedObjectReference[] mors = (ManagedObjectReference[])vmomiValue;
      String[] cisIds = new String[mors.length];
      int i = 0;
      for (ManagedObjectReference managedObjectReference : mors) {
        String str = CisIdConverter.toGlobalCisId(managedObjectReference, managedObjectReference.getServerGuid());
        cisIds[i++] = str;
      } 
      return cisIds;
    } 
    ManagedObjectReference mor = getMor(vmomiValue);
    String cisId = CisIdConverter.toGlobalCisId(mor, mor.getServerGuid());
    return cisId;
  }
  
  private static ManagedObjectReference getMor(Object vmomiValue) {
    if (vmomiValue == null)
      return null; 
    if (!(vmomiValue instanceof ManagedObjectReference))
      throw new IllegalArgumentException(
          String.format("VMOMI ResourceItem contains invalid model key value of class %s", new Object[] { vmomiValue.getClass().getCanonicalName() })); 
    return (ManagedObjectReference)vmomiValue;
  }
}
