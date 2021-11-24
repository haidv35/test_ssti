package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

final class NestedBindingField implements QueryBindingField {
  private final Field _field;
  
  private final QueryBindingDescriptor _descriptor;
  
  NestedBindingField(Field field, QueryBindingDescriptor descriptor) {
    assert field != null;
    assert descriptor != null;
    this._field = field;
    this._descriptor = descriptor;
  }
  
  public List<String> getPropertiesToSelect() {
    return this._descriptor.getPropertiesToSelect();
  }
  
  public Collection<RelatedPropertyDescriptor> getRelatedPropertyDescriptors() {
    return this._descriptor.getRelatedPropertyDescriptors();
  }
  
  public Collection<AliasPropertyDescriptor> getAliasPropertyDescriptors() {
    return this._descriptor.getAliasPropertyDescriptors();
  }
  
  public void set(DataProvider dataProvider, List<ResourceItem> items, List<Object> instances) {
    List<Object> fieldValues = this._descriptor.map(dataProvider, items);
    setField(instances, this._field, fieldValues);
  }
  
  private static void setField(List<Object> instances, Field field, List<Object> fieldValues) {
    assert instances != null;
    assert field != null;
    assert fieldValues != null;
    assert instances.size() == fieldValues.size();
    Iterator<Object> instanceIterator = instances.iterator();
    Iterator<Object> fieldValueIterator = fieldValues.iterator();
    while (instanceIterator.hasNext() && fieldValueIterator.hasNext()) {
      Object instance = instanceIterator.next();
      Object fieldValue = fieldValueIterator.next();
      ReflectionUtil.setField(instance, field, fieldValue);
    } 
  }
}
