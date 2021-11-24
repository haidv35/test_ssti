package com.vmware.cis.data.internal.provider.util.property;

import com.vmware.cis.data.api.ResourceItem;

public final class PropertyByNameBackedByResourceItem implements PropertyByName {
  private final ResourceItem _item;
  
  private final ResourceItemPropertyByName _propertyValueByName;
  
  public PropertyByNameBackedByResourceItem(ResourceItem item, ResourceItemPropertyByName propertyValueByName) {
    assert item != null;
    assert propertyValueByName != null;
    this._item = item;
    this._propertyValueByName = propertyValueByName;
  }
  
  public Object getValue(String propertyName) {
    return this._propertyValueByName.getValue(propertyName, this._item);
  }
}
