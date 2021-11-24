package com.vmware.cis.data.api.exception;

public class ResultLimitExceededException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final int _maxItems;
  
  public ResultLimitExceededException(int maxItems) {
    super(String.format("Maximum number of result items exceeded: %s. Please refine your query criteria.", new Object[] { Integer.valueOf(maxItems) }));
    this._maxItems = maxItems;
  }
  
  public int getMaxItems() {
    return this._maxItems;
  }
}
