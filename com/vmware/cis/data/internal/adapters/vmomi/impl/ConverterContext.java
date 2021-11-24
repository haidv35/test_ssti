package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.vim.binding.cis.data.provider.QuerySpec;

public final class ConverterContext {
  private final QuerySpec _querySpec;
  
  private final boolean _isTypeRequired;
  
  private final boolean _isModelKeyRequired;
  
  ConverterContext(QuerySpec querySpec, boolean isTypeRequired, boolean isModelKeyRequired) {
    this._querySpec = querySpec;
    this._isTypeRequired = isTypeRequired;
    this._isModelKeyRequired = isModelKeyRequired;
  }
  
  public QuerySpec getQuery() {
    return this._querySpec;
  }
  
  public boolean isTypeRequired() {
    return this._isTypeRequired;
  }
  
  public boolean isModelKeyRequired() {
    return this._isModelKeyRequired;
  }
}
