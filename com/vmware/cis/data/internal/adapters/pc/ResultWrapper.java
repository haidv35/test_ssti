package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final class ResultWrapper {
  private final Map<ManagedObjectReference, ItemValueMap> resultsByKey = new HashMap<>();
  
  public void add(ManagedObjectReference key, ItemValueMap itemValueMap) {
    this.resultsByKey.put(key, itemValueMap);
  }
  
  public Object get(ManagedObjectReference key) {
    return this.resultsByKey.get(key);
  }
  
  public Set<Map.Entry<ManagedObjectReference, ItemValueMap>> entrySet() {
    return this.resultsByKey.entrySet();
  }
  
  static class ItemValueMap implements PropertyByName {
    Map<String, Object> valueByProperty = new HashMap<>();
    
    public void add(String property, Object value) {
      this.valueByProperty.put(property, value);
    }
    
    public <T> T get(String property) {
      T value = (T)this.valueByProperty.get(property);
      return value;
    }
    
    public Object getValue(String property) {
      return this.valueByProperty.get(property);
    }
  }
  
  public void union(ResultWrapper other) {
    for (Map.Entry<ManagedObjectReference, ItemValueMap> entry : other.entrySet())
      this.resultsByKey.put(entry.getKey(), entry.getValue()); 
  }
  
  public void intersection(ResultWrapper other) {
    Iterator<Map.Entry<ManagedObjectReference, ItemValueMap>> it = this.resultsByKey.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<ManagedObjectReference, ItemValueMap> entry = it.next();
      if (other.get(entry.getKey()) == null)
        it.remove(); 
    } 
  }
}
