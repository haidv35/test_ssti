package com.vmware.cis.data.internal.provider.util.property;

import com.vmware.cis.data.api.ResourceItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResourceItemPropertyValueByNameViaIndexMap implements ResourceItemPropertyByName {
  private final Map<String, Integer> _propertyIndexByName;
  
  public ResourceItemPropertyValueByNameViaIndexMap(List<String> properties) {
    assert properties != null;
    this
      ._propertyIndexByName = Collections.unmodifiableMap(getPropertyIndexByName(properties));
  }
  
  public Object getValue(String property, ResourceItem item) {
    Integer index = this._propertyIndexByName.get(property);
    if (index == null)
      return null; 
    assert index.intValue() >= 0;
    assert index.intValue() < item.getPropertyValues().size();
    return item.getPropertyValues().get(index.intValue());
  }
  
  private static Map<String, Integer> getPropertyIndexByName(List<String> properties) {
    Map<String, Integer> propertyIndexByName = new HashMap<>(properties.size());
    int i = 0;
    for (String property : properties)
      propertyIndexByName.put(property, Integer.valueOf(i++)); 
    return propertyIndexByName;
  }
}
