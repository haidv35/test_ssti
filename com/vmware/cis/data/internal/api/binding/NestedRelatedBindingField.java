package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class NestedRelatedBindingField implements QueryBindingField {
  private final QueryBindingDescriptor _descriptor;
  
  private final RelatedPropertyDescriptor _relatedPropertyDescriptor;
  
  private final Field _field;
  
  private final String _relatedProperty;
  
  private final boolean _isArrayField;
  
  private final Class<?> _fieldType;
  
  NestedRelatedBindingField(Field field, QueryBindingDescriptor descriptor, RelatedPropertyDescriptor relatedPropertyDescriptor) {
    assert field != null;
    assert descriptor != null;
    assert relatedPropertyDescriptor != null;
    this._field = field;
    this._isArrayField = field.getType().isArray();
    this._fieldType = ReflectionUtil.getType(field);
    this._descriptor = descriptor;
    this._relatedPropertyDescriptor = relatedPropertyDescriptor;
    this._relatedProperty = this._relatedPropertyDescriptor.getName();
  }
  
  public List<String> getPropertiesToSelect() {
    return Collections.singletonList(this._relatedProperty);
  }
  
  public Collection<RelatedPropertyDescriptor> getRelatedPropertyDescriptors() {
    List<RelatedPropertyDescriptor> descriptors = new ArrayList<>(this._descriptor.getRelatedPropertyDescriptors());
    descriptors.add(this._relatedPropertyDescriptor);
    return descriptors;
  }
  
  public Collection<AliasPropertyDescriptor> getAliasPropertyDescriptors() {
    return this._descriptor.getAliasPropertyDescriptors();
  }
  
  public void set(DataProvider dataProvider, List<ResourceItem> items, List<Object> instances) {
    if (items.isEmpty())
      return; 
    Collection<Object> relatedKeys = getRelatedKeys(items, this._relatedProperty, this._isArrayField);
    if (relatedKeys.isEmpty())
      return; 
    List<ResourceItem> fieldValues = fetchItems(dataProvider, this._descriptor
        .getPropertiesToSelect(), relatedKeys);
    if (fieldValues.isEmpty())
      return; 
    Map<Object, Object> bindingByKey = toBindingByKey(dataProvider, this._descriptor
        .getQueryBindingFields(), fieldValues, this._descriptor.getType());
    set(items, instances, bindingByKey);
  }
  
  private void set(List<ResourceItem> items, List<Object> instances, Map<Object, Object> bindingByKey) {
    Iterator<Object> instanceIterator = instances.iterator();
    Iterator<ResourceItem> itemIterator = items.iterator();
    while (instanceIterator.hasNext() && itemIterator.hasNext()) {
      Object instance = instanceIterator.next();
      ResourceItem item = itemIterator.next();
      Object value = item.get(this._relatedProperty);
      if (value == null)
        continue; 
      if (this._isArrayField) {
        Object arrayValue = toBindingArray(this._fieldType, bindingByKey, value);
        ReflectionUtil.setField(instance, this._field, arrayValue);
        continue;
      } 
      Object binding = bindingByKey.get(value);
      ReflectionUtil.setField(instance, this._field, binding);
    } 
  }
  
  private static Object toBindingArray(Class<?> fieldType, Map<Object, Object> bindingByKey, Object keyArray) {
    List<Object> bindings = new ArrayList();
    for (int i = 0; i < Array.getLength(keyArray); i++) {
      Object key = Array.get(keyArray, i);
      Object binding = bindingByKey.get(key);
      if (binding != null)
        bindings.add(binding); 
    } 
    Object bindingArray = Array.newInstance(fieldType, bindings.size());
    for (int index = 0; index < bindings.size(); index++)
      Array.set(bindingArray, index, bindings.get(index)); 
    return bindingArray;
  }
  
  private static Collection<Object> getRelatedKeys(List<ResourceItem> items, String relatedProperty, boolean isArray) {
    Set<Object> keys = new HashSet();
    for (ResourceItem item : items) {
      Object value = item.get(relatedProperty);
      if (value == null)
        continue; 
      if (isArray) {
        for (int i = 0; i < Array.getLength(value); i++) {
          Object valueElement = Array.get(value, i);
          keys.add(valueElement);
        } 
        continue;
      } 
      keys.add(value);
    } 
    return keys;
  }
  
  private static Map<Object, Object> toBindingByKey(DataProvider dataProvider, List<QueryBindingField> queryBindingFields, List<ResourceItem> items, Class<?> type) {
    List<Object> instances = ReflectionUtil.newInstances(type, items.size());
    for (QueryBindingField queryBindingField : queryBindingFields)
      queryBindingField.set(dataProvider, items, instances); 
    Map<Object, Object> bindingByKey = new HashMap<>();
    Iterator<Object> instanceIterator = instances.iterator();
    Iterator<ResourceItem> itemIterator = items.iterator();
    while (instanceIterator.hasNext() && itemIterator.hasNext()) {
      Object instance = instanceIterator.next();
      ResourceItem item = itemIterator.next();
      Object key = item.getKey();
      bindingByKey.put(key, instance);
    } 
    return bindingByKey;
  }
  
  private static List<ResourceItem> fetchItems(DataProvider dataProvider, Collection<String> properties, Collection<Object> keys) {
    List<String> propertiesToSelect = new ArrayList<>(properties);
    if (!propertiesToSelect.contains("@modelKey"))
      propertiesToSelect.add("@modelKey"); 
    Collection<String> resourceModels = QueryQualifier.getFromClause(propertiesToSelect, null, null);
    Query query = Query.Builder.select(propertiesToSelect).from(resourceModels).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, keys).build();
    ResultSet resultSet = dataProvider.executeQuery(query);
    return resultSet.getItems();
  }
}
