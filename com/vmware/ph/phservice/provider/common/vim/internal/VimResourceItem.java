package com.vmware.ph.phservice.provider.common.vim.internal;

import java.util.List;

public class VimResourceItem {
  private final List<Object> _propertyValues;
  
  public VimResourceItem(List<Object> propertyValues) {
    this._propertyValues = propertyValues;
  }
  
  public List<Object> getPropertyValues() {
    return this._propertyValues;
  }
}
