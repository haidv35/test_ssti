package com.vmware.ph.phservice.collector.internal;

import org.apache.commons.lang.Validate;

public abstract class ContainsErrorInfo {
  private final Exception _collectorError;
  
  private final boolean _fatalError;
  
  protected ContainsErrorInfo(Exception collectorError, boolean fatalError) {
    this._collectorError = collectorError;
    this._fatalError = fatalError;
  }
  
  public final Exception getCollectorError() {
    return this._collectorError;
  }
  
  public final boolean hasFatalError() {
    return this._fatalError;
  }
  
  public static abstract class Builder<T extends Builder<T>> {
    protected Exception _error = null;
    
    protected boolean _fatal = false;
    
    public final T setError(Exception collectorError, boolean isFatal) {
      Validate.notNull(collectorError, "Argument `collectorError' is required.");
      this._error = collectorError;
      this._fatal = isFatal;
      return (T)this;
    }
    
    public final T setTransientError(Exception collectionError) {
      return setError(collectionError, false);
    }
    
    public final T setFatalError(Exception collectionError) {
      return setError(collectionError, true);
    }
  }
}
