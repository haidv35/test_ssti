package com.vmware.cis.data.internal.api.binding;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyDescriptor;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyDescriptor;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QueryBindingDescriptor {
  private final Class<?> _resultType;
  
  private final String _resourceModel;
  
  private final List<QueryBindingField> _queryBindingFields;
  
  private final List<String> _propertiesToSelect;
  
  private final List<AliasPropertyDescriptor> _aliasPropertyDescriptors;
  
  private final List<RelatedPropertyDescriptor> _relatedPropertyDescriptors;
  
  QueryBindingDescriptor(Class<?> resultType, String resourceModel, List<QueryBindingField> queryBindingFields) {
    assert resultType != null;
    assert resourceModel != null;
    assert queryBindingFields != null;
    this._resultType = resultType;
    this._resourceModel = resourceModel;
    this._queryBindingFields = Collections.unmodifiableList(queryBindingFields);
    this
      ._propertiesToSelect = Collections.unmodifiableList(getPropertiesToSelect(queryBindingFields));
    this
      ._aliasPropertyDescriptors = Collections.unmodifiableList(getAliasPropertyDescriptors(queryBindingFields));
    this
      ._relatedPropertyDescriptors = Collections.unmodifiableList(getRelatedPropertyDescriptors(queryBindingFields));
  }
  
  public Class<?> getType() {
    return this._resultType;
  }
  
  public String getResourceModel() {
    return this._resourceModel;
  }
  
  public List<QueryBindingField> getQueryBindingFields() {
    return this._queryBindingFields;
  }
  
  public List<String> getPropertiesToSelect() {
    return this._propertiesToSelect;
  }
  
  public List<RelatedPropertyDescriptor> getRelatedPropertyDescriptors() {
    return this._relatedPropertyDescriptors;
  }
  
  public List<AliasPropertyDescriptor> getAliasPropertyDescriptors() {
    return this._aliasPropertyDescriptors;
  }
  
  public List<Object> map(DataProvider dataProvider, List<ResourceItem> items) {
    assert dataProvider != null;
    assert items != null;
    if (items.isEmpty())
      return Collections.emptyList(); 
    List<Object> instances = ReflectionUtil.newInstances(this._resultType, items.size());
    for (QueryBindingField queryBindingField : this._queryBindingFields)
      queryBindingField.set(dataProvider, items, instances); 
    return instances;
  }
  
  private static List<String> getPropertiesToSelect(List<QueryBindingField> queryBindingFields) {
    Set<String> properties = new HashSet<>();
    for (QueryBindingField queryBindingField : queryBindingFields)
      properties.addAll(queryBindingField.getPropertiesToSelect()); 
    return new ArrayList<>(properties);
  }
  
  private static List<RelatedPropertyDescriptor> getRelatedPropertyDescriptors(List<QueryBindingField> queryBindingFields) {
    List<RelatedPropertyDescriptor> descriptors = new ArrayList<>();
    for (QueryBindingField queryBindingField : queryBindingFields)
      descriptors.addAll(queryBindingField.getRelatedPropertyDescriptors()); 
    return descriptors;
  }
  
  private static List<AliasPropertyDescriptor> getAliasPropertyDescriptors(List<QueryBindingField> queryBindingFields) {
    List<AliasPropertyDescriptor> descriptors = new ArrayList<>();
    for (QueryBindingField queryBindingField : queryBindingFields)
      descriptors.addAll(queryBindingField.getAliasPropertyDescriptors()); 
    return descriptors;
  }
}
