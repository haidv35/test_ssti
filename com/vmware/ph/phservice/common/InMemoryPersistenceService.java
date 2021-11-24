package com.vmware.ph.phservice.common;

import com.vmware.ph.phservice.common.internal.BasePersistenceService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class InMemoryPersistenceService extends BasePersistenceService {
  private final Map<String, String> _keyToValue;
  
  public InMemoryPersistenceService() {
    this._keyToValue = new LinkedHashMap<>();
  }
  
  public InMemoryPersistenceService(Map<String, String> keyToValue) {
    this._keyToValue = keyToValue;
  }
  
  public final Map<String, String> readValues(Iterable<String> keys) throws PersistenceServiceException {
    Map<String, String> keyValues = new HashMap<>();
    for (String key : keys)
      keyValues.put(key, this._keyToValue.get(key)); 
    return keyValues;
  }
  
  public final void writeValues(Map<String, String> propertyKeyToPropertyValueMap) throws PersistenceServiceException {
    Objects.requireNonNull(propertyKeyToPropertyValueMap);
    for (Map.Entry<String, String> keyValuePair : propertyKeyToPropertyValueMap.entrySet()) {
      String key = keyValuePair.getKey();
      Objects.requireNonNull(key);
      String value = keyValuePair.getValue();
      this._keyToValue.put(key, value);
    } 
  }
  
  public Set<String> getAllKeys() throws PersistenceServiceException {
    return Collections.unmodifiableSet(this._keyToValue.keySet());
  }
  
  public void deleteValues(Set<String> keys) throws PersistenceServiceException {
    if (keys != null)
      for (String key : keys)
        this._keyToValue.remove(key);  
  }
  
  protected String readValue(String key) throws PersistenceServiceException {
    return this._keyToValue.get(key);
  }
  
  protected void writeValue(String key, String value) throws PersistenceServiceException {
    this._keyToValue.put(key, value);
  }
}
