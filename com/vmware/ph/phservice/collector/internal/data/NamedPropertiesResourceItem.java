package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.ResourceItem;
import java.util.List;

public class NamedPropertiesResourceItem {
  private final ResourceItem _resourceItem;
  
  private final List<String> _propertyNames;
  
  public NamedPropertiesResourceItem(ResourceItem resourceItem, List<String> propertyNames) {
    this._resourceItem = resourceItem;
    this._propertyNames = propertyNames;
  }
  
  public ResourceItem getResourceItem() {
    return this._resourceItem;
  }
  
  public List<String> getActualPropertyNames() {
    return this._propertyNames.subList(1, this._propertyNames.size());
  }
  
  public List<Object> getActualPropertyValues() {
    List<Object> propertyValues = this._resourceItem.getPropertyValues();
    return propertyValues.subList(1, propertyValues.size());
  }
  
  public Object getResourceObject() {
    return this._resourceItem.getPropertyValues().get(0);
  }
  
  public String toString() {
    return "NamedPropertiesResourceItem:\npropertyNames: " + this._propertyNames + "\nresourceItem:" + this._resourceItem + "\n";
  }
}
