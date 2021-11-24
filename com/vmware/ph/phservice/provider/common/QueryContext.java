package com.vmware.ph.phservice.provider.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public class QueryContext {
  private final Map<String, List<Object>> _objectKeyToObjects;
  
  public QueryContext(String objectKey, List<Object> objects) {
    Objects.requireNonNull(objectKey, "The object key name must be specified.");
    Objects.requireNonNull(objects, "The objects must be specified.");
    this._objectKeyToObjects = new HashMap<>();
    this._objectKeyToObjects.put(objectKey, objects);
  }
  
  public QueryContext(Map<String, List<Object>> objectKeyToObjects) {
    Objects.requireNonNull(objectKeyToObjects, "Cannot create a context from a null set of context pairs.");
    this._objectKeyToObjects = objectKeyToObjects;
  }
  
  public List<Object> getObjects(String objecKey) {
    List<Object> objects = null;
    if (objecKey != null)
      objects = this._objectKeyToObjects.get(objecKey); 
    if (objects == null)
      objects = Collections.emptyList(); 
    return objects;
  }
  
  public <T> List<T> getObjects(String objectKey, @Nonnull Class<T> clazz) {
    List<Object> objects = getObjects(objectKey);
    List<T> result = null;
    if (objects.isEmpty()) {
      result = Collections.emptyList();
    } else {
      result = castObjectsToClassType(objects, clazz);
    } 
    return result;
  }
  
  public boolean isEmpty() {
    return this._objectKeyToObjects.isEmpty();
  }
  
  private static <T> List<T> castObjectsToClassType(List<Object> objects, Class<T> clazz) {
    List<T> objectsOfClassType = new ArrayList<>(objects.size());
    for (Object objectForType : objects) {
      try {
        objectsOfClassType.add(clazz.cast(objectForType));
      } catch (ClassCastException e) {
        throw new IllegalStateException(
            String.format("Only objects of class type %s are expected.", new Object[] { clazz.getSimpleName() }), e);
      } 
    } 
    return objectsOfClassType;
  }
}
