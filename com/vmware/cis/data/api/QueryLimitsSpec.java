package com.vmware.cis.data.api;

public final class QueryLimitsSpec {
  public static final int DEFAULT_MAX_CRITERIA_SIZE = 32;
  
  public static final int DEFAULT_MAX_COMPARABLE_LIST_SIZE = 128;
  
  public static final int DEFAULT_MAX_RESULTS_LIMIT = 4096;
  
  private final int _maxCriteriaSize;
  
  private final int _maxComparableListSize;
  
  private final int _maxResultSize;
  
  public QueryLimitsSpec(int maxCriteriaSize, int maxComparableListSize, int maxResultSize) {
    this._maxCriteriaSize = maxCriteriaSize;
    this._maxComparableListSize = maxComparableListSize;
    this._maxResultSize = maxResultSize;
  }
  
  public int getMaxCriteriaSize() {
    return this._maxCriteriaSize;
  }
  
  public int getMaxComparableListSize() {
    return this._maxComparableListSize;
  }
  
  public int getMaxResultSize() {
    return this._maxResultSize;
  }
}
