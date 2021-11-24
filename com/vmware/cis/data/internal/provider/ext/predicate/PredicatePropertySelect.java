package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.filter.FilterEvaluator;
import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import com.vmware.cis.data.internal.provider.util.property.PropertyByNameBackedByResourceItem;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyByName;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyValueByNameViaIndexMap;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class PredicatePropertySelect {
  public static List<String> toExecutableSelect(List<String> originalSelect, Map<String, PredicatePropertyDescriptor> descriptors) {
    assert originalSelect != null;
    assert descriptors != null;
    if (descriptors.isEmpty())
      return originalSelect; 
    List<String> executableSelect = new ArrayList<>();
    for (String property : originalSelect) {
      PredicatePropertyDescriptor descriptor = descriptors.get(property);
      if (descriptor == null) {
        executableSelect.add(property);
        continue;
      } 
      executableSelect.addAll(getDependencies(descriptor));
    } 
    return new ArrayList<>(new LinkedHashSet<>(executableSelect));
  }
  
  public static ResultSet convertResult(ResultSet rawResult, List<String> originalSelect, Map<String, PredicatePropertyDescriptor> descriptors) {
    assert rawResult != null;
    assert originalSelect != null;
    assert descriptors != null;
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(originalSelect);
    ResourceItemPropertyByName propertyValueByName = new ResourceItemPropertyValueByNameViaIndexMap(rawResult.getProperties());
    for (ResourceItem rawItem : rawResult.getItems()) {
      PropertyByName valueByName = new PropertyByNameBackedByResourceItem(rawItem, propertyValueByName);
      List<Object> propertyValues = convertPropertyValues(valueByName, originalSelect, descriptors);
      resultBuilder.item(rawItem.getKey(), propertyValues);
    } 
    return resultBuilder.totalCount(rawResult.getTotalCount()).build();
  }
  
  private static List<Object> convertPropertyValues(PropertyByName valueByName, List<String> originalSelect, Map<String, PredicatePropertyDescriptor> descriptors) {
    assert valueByName != null;
    assert originalSelect != null;
    assert descriptors != null;
    List<Object> values = new ArrayList(originalSelect.size());
    for (String property : originalSelect) {
      Object value;
      PredicatePropertyDescriptor descriptor = descriptors.get(property);
      if (descriptor == null) {
        value = valueByName.getValue(property);
      } else {
        value = Boolean.valueOf(FilterEvaluator.eval(descriptor.getFilter(), valueByName));
      } 
      values.add(value);
    } 
    return values;
  }
  
  private static Collection<String> getDependencies(PredicatePropertyDescriptor descriptor) {
    assert descriptor != null;
    List<PropertyPredicate> predicates = descriptor.getFilter().getCriteria();
    List<String> dependencies = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      String property = predicate.getProperty();
      if (PropertyUtil.isModelKey(property))
        throw new IllegalArgumentException(String.format("Predicate property '%s' depends on '%s'", new Object[] { descriptor
                
                .getName(), property })); 
      dependencies.add(property);
    } 
    return dependencies;
  }
}
