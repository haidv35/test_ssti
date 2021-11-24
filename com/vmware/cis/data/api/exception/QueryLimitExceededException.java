package com.vmware.cis.data.api.exception;

public class QueryLimitExceededException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final String _field;
  
  private final int _actualSize;
  
  private final int _maxSize;
  
  public QueryLimitExceededException(String field, int actualSize, int maxSize) {
    super(String.format("The query size limit exceeded: %s contains %d items, but the limit is %d. Please refine your query criteria.", new Object[] { field, 
            Integer.valueOf(actualSize), Integer.valueOf(maxSize) }));
    this._field = field;
    this._actualSize = actualSize;
    this._maxSize = maxSize;
  }
  
  public String getField() {
    return this._field;
  }
  
  public int getActualSize() {
    return this._actualSize;
  }
  
  public int getMaxSize() {
    return this._maxSize;
  }
}
