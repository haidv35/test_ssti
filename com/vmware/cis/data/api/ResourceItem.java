package com.vmware.cis.data.api;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public final class ResourceItem {
  private final Object _key;
  
  private final List<Object> _propertyValues;
  
  private final ResultSet.PropertyIndex _propertyIndex;
  
  ResourceItem(Object key, List<Object> propertyValues, ResultSet.PropertyIndex propertyIndex) {
    assert key != null;
    assert propertyValues != null;
    assert propertyIndex != null;
    this._key = key;
    this._propertyValues = Collections.unmodifiableList(propertyValues);
    this._propertyIndex = propertyIndex;
  }
  
  public List<Object> getPropertyValues() {
    return this._propertyValues;
  }
  
  public <T> T get(int propertyIndex) {
    return getValueAt(propertyIndex);
  }
  
  public <T> T get(String property) {
    Validate.notEmpty(property, "Property name must not be null or empty");
    int propertyIndex = this._propertyIndex.getIndexOfProperty(property);
    if (propertyIndex < 0)
      throw new IllegalArgumentException("ResultSet does not contain property: " + property); 
    return getValueAt(propertyIndex);
  }
  
  public <T> T getKey() {
    T key = (T)this._key;
    return key;
  }
  
  private <T> T getValueAt(int propertyIndex) {
    if (propertyIndex < 0 || propertyIndex >= this._propertyValues.size())
      throw new IllegalArgumentException("Invalid property index: " + propertyIndex); 
    T value = (T)this._propertyValues.get(propertyIndex);
    return value;
  }
  
  public int hashCode() {
    int hashCode = 17;
    hashCode = 31 * hashCode + this._key.hashCode();
    for (Object propertyValue : this._propertyValues)
      hashCode = 31 * hashCode + calculateHash(propertyValue); 
    return hashCode;
  }
  
  private static int calculateHash(Object propertyValue) {
    if (isArray(propertyValue))
      return calculateArrayHash(propertyValue); 
    return Objects.hashCode(propertyValue);
  }
  
  private static int calculateArrayHash(Object array) {
    int hashCode = 1;
    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      Object object = Array.get(array, i);
      hashCode = 31 * hashCode + Objects.hashCode(object);
    } 
    return hashCode;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof ResourceItem))
      return false; 
    ResourceItem other = (ResourceItem)obj;
    return (this._key.equals(other._key) && 
      areEqual(this._propertyValues, other.getPropertyValues()));
  }
  
  private boolean areEqual(List<Object> list1, List<Object> list2) {
    if (list1 == list2)
      return true; 
    if (list1 == null || list2 == null)
      return false; 
    if (list1.size() != list2.size())
      return false; 
    Iterator<Object> iterator1 = list1.iterator();
    Iterator<Object> iterator2 = list2.iterator();
    while (iterator1.hasNext()) {
      Object object1 = iterator1.next();
      Object object2 = iterator2.next();
      if (!areEqualsPropertyValues(object1, object2))
        return false; 
    } 
    return true;
  }
  
  private boolean areEqualsPropertyValues(Object object1, Object object2) {
    if (isArray(object1)) {
      if (!isArray(object2))
        return false; 
      if (!areEqualArrayPropertyValues(object1, object2))
        return false; 
    } else if (!Objects.equals(object1, object2)) {
      return false;
    } 
    return true;
  }
  
  private static boolean areEqualArrayPropertyValues(Object array1, Object array2) {
    if (!array1.getClass().equals(array2.getClass()))
      return false; 
    int length = Array.getLength(array1);
    if (length != Array.getLength(array2))
      return false; 
    for (int i = 0; i < length; i++) {
      Object element1 = Array.get(array1, i);
      Object element2 = Array.get(array2, i);
      if (!Objects.equals(element1, element2))
        return false; 
    } 
    return true;
  }
  
  private static boolean isArray(Object o) {
    return (o == null) ? false : o.getClass().isArray();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("ResourceItem [" + SystemUtils.LINE_SEPARATOR);
    sb.append("         _key = " + this._key + SystemUtils.LINE_SEPARATOR);
    sb.append("         _propertyValues = ");
    if (this._propertyValues.size() == 1) {
      sb.append("[" + valueToString(this._propertyValues.get(0)) + "]");
    } else {
      sb.append("[");
      Iterator<Object> iterator = this._propertyValues.iterator();
      while (iterator.hasNext()) {
        sb.append(SystemUtils.LINE_SEPARATOR + "            ");
        sb.append(valueToString(iterator.next()));
        if (iterator.hasNext())
          sb.append(","); 
      } 
      sb.append(SystemUtils.LINE_SEPARATOR + "         ]");
    } 
    sb.append(SystemUtils.LINE_SEPARATOR + "      ]");
    return sb.toString();
  }
  
  private static String valueToString(Object propertyValue) {
    if (isArray(propertyValue))
      return arrayToString(propertyValue); 
    return String.valueOf(propertyValue);
  }
  
  private static String arrayToString(Object propertyValue) {
    StringBuilder sb = new StringBuilder("[");
    int length = Array.getLength(propertyValue);
    for (int i = 0; i < length; i++) {
      if (i != 0)
        sb.append(", "); 
      sb.append(Array.get(propertyValue, i));
    } 
    sb.append("]");
    return sb.toString();
  }
}
