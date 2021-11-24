package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class RelationalAlgebra {
  public static ResultSet joinAndSelect(Collection<ResultSet> results, Collection<Object> selection) {
    assert results != null;
    assert !results.isEmpty();
    List<String> projection = ResultSetAnalyzer.gatherReturnedPropertyNames(results);
    return joinSelectAndProject(results, selection, projection);
  }
  
  public static ResultSet joinSelectAndProject(Collection<ResultSet> results, Collection<Object> selection, List<String> projection) {
    assert results != null;
    assert !results.isEmpty();
    assert projection != null;
    assert !projection.isEmpty();
    Map<Object, Map<String, Object>> propValByNameByKey = null;
    if (!projection.isEmpty() || selection == null)
      propValByNameByKey = mapPropertyValuesByNameByModelKey(results); 
    Collection<Object> modelKeys = (selection != null) ? selection : propValByNameByKey.keySet();
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(projection);
    for (Object modelKey : modelKeys) {
      List<Object> propertyValues = new ArrayList(projection.size());
      Map<String, Object> propertyValueByName = propValByNameByKey.get(modelKey);
      for (String property : projection) {
        Object propertyValue = propertyValueByName.get(property);
        propertyValues.add(propertyValue);
      } 
      resultBuilder.item(modelKey, propertyValues);
    } 
    ResultSet result = resultBuilder.totalCount(Integer.valueOf(modelKeys.size())).build();
    return result;
  }
  
  private static Map<Object, Map<String, Object>> mapPropertyValuesByNameByModelKey(Collection<ResultSet> results) {
    Map<Object, Map<String, Object>> propValByNameByModelKey = new HashMap<>();
    for (ResultSet resultSet : results) {
      for (ResourceItem item : resultSet.getItems()) {
        Object modelKey = item.getKey();
        assert modelKey != null;
        Iterator<String> nameIterator = resultSet.getProperties().iterator();
        Iterator<Object> valueIterator = item.getPropertyValues().iterator();
        while (nameIterator.hasNext()) {
          String propertyName = nameIterator.next();
          Object propertyValue = valueIterator.next();
          Map<String, Object> propValByName = propValByNameByModelKey.get(modelKey);
          if (propValByName == null) {
            propValByName = new HashMap<>();
            propValByNameByModelKey.put(modelKey, propValByName);
          } 
          propValByName.put(propertyName, propertyValue);
        } 
      } 
    } 
    return propValByNameByModelKey;
  }
}
