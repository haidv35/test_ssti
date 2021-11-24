package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

public final class ResultSetAnalyzer {
  public static List<String> gatherReturnedPropertyNames(Collection<ResultSet> results) {
    assert results != null;
    assert !results.isEmpty();
    List<String> properties = new ArrayList<>();
    for (ResultSet result : results)
      properties.addAll(result.getProperties()); 
    return new ArrayList<>(new LinkedHashSet<>(properties));
  }
  
  public static Set<Object> gatherModelKeys(ResultSet result) {
    assert result != null;
    if (CollectionUtils.isEmpty(result.getItems()))
      return Collections.emptySet(); 
    Set<Object> modelKeys = new HashSet(result.getItems().size());
    gatherModelKeys(result, modelKeys);
    return modelKeys;
  }
  
  public static List<Object> gatherModelKeysOrdered(ResultSet result) {
    assert result != null;
    if (result.getItems().isEmpty())
      return Collections.emptyList(); 
    List<Object> modelKeys = new ArrayList(result.getItems().size());
    gatherModelKeys(result, modelKeys);
    return modelKeys;
  }
  
  private static void gatherModelKeys(ResultSet resultSet, Collection<Object> modelKeys) {
    for (ResourceItem item : resultSet.getItems()) {
      Object modelKey = item.getKey();
      modelKeys.add(modelKey);
    } 
  }
  
  public static List<Object> getPropertyValuesOrderedFromResult(ResultSet result, String property) {
    assert result != null;
    assert property != null;
    if ("@modelKey".equals(property))
      return new ArrayList(gatherModelKeysOrdered(result)); 
    return gatherPropertyValuesByIndexOrdered(result, result
        .getProperties().indexOf(property));
  }
  
  public static List<Object> gatherPropertyValuesByIndexOrdered(ResultSet result, int propertyIndex) {
    assert result != null;
    List<Object> propertyValues = new ArrayList(result.getItems().size());
    gatherPropertyValuesByIndex(result, propertyIndex, propertyValues);
    return propertyValues;
  }
  
  private static void gatherPropertyValuesByIndex(ResultSet result, int propertyIndex, List<Object> propertyValues) {
    assert result != null;
    assert propertyValues != null;
    if (propertyIndex < 0 || propertyIndex >= result.getProperties().size())
      throw new IllegalArgumentException("The given property index is out of bound of the result properties!"); 
    for (ResourceItem item : result.getItems())
      propertyValues.add(item.getPropertyValues().get(propertyIndex)); 
  }
}
