package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

final class ModelKeyConverter {
  public static void validateModelKeyPropertyType(String propertyName, Class<?> propertyType) {
    assert propertyName != null;
    assert propertyType != null;
    if (!PropertyUtil.isModelKey(propertyName))
      return; 
    if (isModelKeySupportedStringType(propertyType) || 
      isModelKeySupportedMorType(propertyType) || 
      isModelKeySupportedUriType(propertyType))
      return; 
    if (propertyType.isArray())
      throw new UnsupportedOperationException(String.format("Unsupported return type '%s' declared for the related property '%s'!", new Object[] { propertyType
              
              .getComponentType().getClass().getName() + "[]", propertyName })); 
    throw new UnsupportedOperationException(String.format("Unsupported return type '%s' declared for the related property '%s'!", new Object[] { propertyType
            
            .getName(), propertyName }));
  }
  
  public static List<Object> convertModelKeyToReferences(String propertyName, Class<?> propertyType, List<Object> propertyValues, List<Object> resourceTypes) {
    assert propertyValues != null;
    assert !propertyValues.isEmpty();
    assert resourceTypes != null;
    assert !resourceTypes.isEmpty();
    assert propertyValues.size() == resourceTypes.size();
    validateModelKeyPropertyType(propertyName, propertyType);
    List<Object> convertedPropertyValues = new ArrayList();
    Iterator<Object> resourceTypesIterator = resourceTypes.iterator();
    for (Object propertyValue : propertyValues)
      convertedPropertyValues.add(convertModelKeyPropertyValue(propertyName, propertyType, propertyValue, resourceTypesIterator
            
            .next())); 
    return convertedPropertyValues;
  }
  
  public static boolean isArrayIdentifier(Class<?> type) {
    return (ManagedObjectReference[].class.equals(type) || URI[].class.equals(type));
  }
  
  private static Object convertModelKeyPropertyValue(String propertyName, Class<?> propertyType, Object propertyValue, Object resourceType) {
    if (propertyValue == null)
      return propertyValue; 
    assert resourceType != null;
    if (propertyType.isArray())
      return convertModelKeyPropertyValues(propertyName, propertyType
          .getComponentType(), propertyValue, resourceType); 
    Object modelKey = getSinglePropertyValue(propertyName, propertyValue);
    Object type = getSinglePropertyValue(propertyName, resourceType);
    if (modelKey == null)
      return null; 
    if (String.class.equals(propertyType))
      return String.valueOf(modelKey); 
    if (URI.class.equals(propertyType))
      return (type != null) ? toUri(modelKey) : null; 
    return modelKey;
  }
  
  private static Object convertModelKeyPropertyValues(String propertyName, Class<?> propertyType, Object propertyValue, Object resourceType) {
    Object[] refArray;
    assert propertyValue != null;
    assert resourceType != null;
    if (URI.class.equals(propertyType)) {
      refArray = convertModelKeyToUriArray(propertyValue, resourceType);
    } else {
      refArray = convertModelKeyToArray(propertyValue, propertyType);
    } 
    return (refArray != null && refArray.length > 0) ? refArray : null;
  }
  
  private static Object getSinglePropertyValue(String propertyName, Object propertyValue) {
    assert propertyValue != null;
    if (propertyValue instanceof Collection) {
      Collection<Object> collectionValue = (Collection<Object>)propertyValue;
      if (collectionValue.isEmpty())
        return null; 
      if (collectionValue.size() > 1)
        throw new IllegalArgumentException(String.format("Property '%s' declared to return single value, but multiple values detected - [%s]!", new Object[] { propertyName, collectionValue })); 
      return collectionValue.iterator().next();
    } 
    if (propertyValue instanceof Object[]) {
      Object[] arrayValue = (Object[])propertyValue;
      if (arrayValue.length == 0)
        return null; 
      if (arrayValue.length > 1)
        throw new IllegalArgumentException(String.format("Property '%s' declared to return single value, but multiple values detected - [%s]!", new Object[] { propertyName, 
                
                Arrays.toString(arrayValue) })); 
      return arrayValue[0];
    } 
    return propertyValue;
  }
  
  private static boolean isModelKeySupportedStringType(Class<?> propertyType) {
    return (String.class.equals(propertyType) || (propertyType
      .isArray() && String.class
      .equals(propertyType.getComponentType())));
  }
  
  private static boolean isModelKeySupportedMorType(Class<?> propertyType) {
    return (ManagedObjectReference.class.equals(propertyType) || (propertyType
      .isArray() && ManagedObjectReference.class
      .equals(propertyType
        .getComponentType())));
  }
  
  private static boolean isModelKeySupportedUriType(Class<?> propertyType) {
    return (URI.class.equals(propertyType) || (propertyType
      .isArray() && URI.class
      .equals(propertyType.getComponentType())));
  }
  
  private static Object[] convertModelKeyToArray(Object propertyValue, Class<?> arrayType) {
    Object[] modelKeyArray;
    if (propertyValue instanceof Object[])
      return (Object[])propertyValue; 
    assert propertyValue != null;
    assert arrayType != null;
    if (propertyValue instanceof Collection) {
      Collection<?> modelKeyCollection = (Collection)propertyValue;
      modelKeyArray = (Object[])Array.newInstance(arrayType, modelKeyCollection
          .size());
      int i = 0;
      for (Object modelKey : modelKeyCollection)
        modelKeyArray[i++] = modelKey; 
    } else {
      modelKeyArray = (Object[])Array.newInstance(arrayType, 1);
      modelKeyArray[0] = propertyValue;
    } 
    return modelKeyArray;
  }
  
  private static Object[] convertModelKeyToUriArray(Object propertyValue, Object resourceType) {
    assert propertyValue != null;
    assert resourceType != null;
    if (propertyValue instanceof Collection) {
      assert resourceType instanceof Collection;
      Collection<Object> propertyValueCollection = (Collection<Object>)propertyValue;
      Collection<Object> resourceTypeCollection = (Collection<Object>)resourceType;
      assert propertyValueCollection.size() == resourceTypeCollection.size();
      URI[] uriProperties = new URI[propertyValueCollection.size()];
      Iterator<Object> pvIterator = propertyValueCollection.iterator();
      int i = 0;
      while (pvIterator.hasNext())
        uriProperties[i++] = toUri(pvIterator.next()); 
      return (Object[])uriProperties;
    } 
    if (propertyValue instanceof Object[]) {
      assert resourceType instanceof Object[];
      Object[] propertyValueArray = (Object[])propertyValue;
      Object[] resourceTypeArray = (Object[])resourceType;
      assert propertyValueArray.length == resourceTypeArray.length;
      URI[] uriProperties = new URI[propertyValueArray.length];
      for (int i = 0; i < propertyValueArray.length; i++)
        uriProperties[i] = toUri(propertyValueArray[i]); 
      return (Object[])uriProperties;
    } 
    return (Object[])new URI[] { toUri(propertyValue) };
  }
  
  private static URI toUri(Object modelKey) {
    assert modelKey != null;
    if (modelKey instanceof URI)
      return (URI)modelKey; 
    throw new UnsupportedOperationException(String.format("Unsupported type of modelKey %s : %s", new Object[] { modelKey
            .getClass(), modelKey }));
  }
}
