package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.ClassUtils;

final class PropertyBindingField implements QueryBindingField {
  private final Class<?> _resultType;
  
  private final String _property;
  
  private final Field _field;
  
  private final List<AliasPropertyDescriptor> _aliasPropertyDescriptors;
  
  private final List<RelatedPropertyDescriptor> _relatedPropertyDescriptors;
  
  static PropertyBindingField forAliasProperty(Class<?> resultType, Field field, AliasPropertyDescriptor aliasPropertyDescriptor) {
    assert aliasPropertyDescriptor != null;
    return new PropertyBindingField(resultType, field, aliasPropertyDescriptor
        .getName(), 
        Collections.singletonList(aliasPropertyDescriptor), 
        Collections.emptyList());
  }
  
  static PropertyBindingField forRelatedProperty(Class<?> resultType, Field field, RelatedPropertyDescriptor relatedPropertyDescriptor) {
    assert relatedPropertyDescriptor != null;
    return new PropertyBindingField(resultType, field, relatedPropertyDescriptor
        .getName(), 
        Collections.emptyList(), 
        Collections.singletonList(relatedPropertyDescriptor));
  }
  
  private PropertyBindingField(Class<?> resultType, Field field, String property, List<AliasPropertyDescriptor> aliasPropertyDescriptors, List<RelatedPropertyDescriptor> relatedPropertyDescriptors) {
    assert resultType != null;
    assert field != null;
    assert property != null;
    assert aliasPropertyDescriptors != null;
    assert relatedPropertyDescriptors != null;
    this._resultType = resultType;
    this._field = field;
    this._property = property;
    this._aliasPropertyDescriptors = aliasPropertyDescriptors;
    this._relatedPropertyDescriptors = relatedPropertyDescriptors;
  }
  
  public List<String> getPropertiesToSelect() {
    return Collections.singletonList(this._property);
  }
  
  public void set(DataProvider dataProvider, List<ResourceItem> items, List<Object> instances) {
    assert items.size() == instances.size();
    Iterator<Object> instanceIterator = instances.iterator();
    Iterator<ResourceItem> itemIterator = items.iterator();
    while (instanceIterator.hasNext() && itemIterator.hasNext()) {
      Object instance = instanceIterator.next();
      ResourceItem item = itemIterator.next();
      set(item, instance);
    } 
  }
  
  public Collection<RelatedPropertyDescriptor> getRelatedPropertyDescriptors() {
    return this._relatedPropertyDescriptors;
  }
  
  public Collection<AliasPropertyDescriptor> getAliasPropertyDescriptors() {
    return this._aliasPropertyDescriptors;
  }
  
  private void set(ResourceItem item, Object instance) {
    Object value = item.get(this._property);
    if (value != null) {
      value = convertValue(value, this._field, this._resultType.getName());
    } else if (this._field.getType().isPrimitive()) {
      return;
    } 
    ReflectionUtil.setField(instance, this._field, value);
  }
  
  private static Object convertValue(Object value, Field field, String bindingName) {
    assert value != null;
    assert field != null;
    assert bindingName != null;
    Class<?> targetType = field.getType();
    Class<?> valueType = value.getClass();
    if (ClassUtils.isAssignable(valueType, targetType, true))
      return value; 
    if (!valueType.isArray())
      throw new UnsupportedOperationException(String.format("Error while converting value [%s] for binding field [%s.%s]: converting from '%s' to '%s' is not supported.", new Object[] { value, bindingName, field

              
              .getName(), valueType.getSimpleName(), targetType
              .getSimpleName() })); 
    if (!targetType.isArray())
      throw new UnsupportedOperationException(
          String.format("Error while converting value [%s] for binding field [%s.%s]: trying to convert from an array value to a non-array type [%s].", new Object[] { value, bindingName, field.getName(), targetType.getSimpleName() })); 
    int arrayLength = Array.getLength(value);
    Object convertedArray = Array.newInstance(targetType.getComponentType(), arrayLength);
    for (int i = 0; i < arrayLength; i++) {
      Object element = Array.get(value, i);
      if (element != null)
        Array.set(convertedArray, i, element); 
    } 
    return convertedArray;
  }
}
