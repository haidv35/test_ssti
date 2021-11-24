package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedQueryResultSet {
  private final ResultSet _resultSet;
  
  private final String _queryName;
  
  private final AtomicInteger _currObjectCounter;
  
  private final int _currPageIndex;
  
  private final long _startTimeNanos;
  
  private final Integer _currentQueryPageSize;
  
  private final Integer _nextQueryPageSize;
  
  public NamedQueryResultSet(ResultSet resultSet, String queryName, AtomicInteger currObjectCounter, int currPageIndex, long startTimeNanos, Integer currentQueryPageSize, Integer nextQueryPageSize) {
    this._resultSet = resultSet;
    this._queryName = queryName;
    this._currObjectCounter = currObjectCounter;
    this._currPageIndex = currPageIndex;
    this._startTimeNanos = startTimeNanos;
    this._currentQueryPageSize = currentQueryPageSize;
    this._nextQueryPageSize = nextQueryPageSize;
  }
  
  public ResultSet getResultSet() {
    return this._resultSet;
  }
  
  public String getQueryName() {
    return this._queryName;
  }
  
  public AtomicInteger getCurrObjectCounter() {
    return this._currObjectCounter;
  }
  
  public int getCurrPageIndex() {
    return this._currPageIndex;
  }
  
  public long getStartTimeNanos() {
    return this._startTimeNanos;
  }
  
  public Integer getCurrentQueryPageSize() {
    return this._currentQueryPageSize;
  }
  
  public Integer getNextQueryPageSize() {
    return this._nextQueryPageSize;
  }
  
  public String toString() {
    return "NamedQueryResultSet:\nqueryName: " + this._queryName + "\nresultSet: " + this._resultSet + "\ncurrentQueryPageSize: " + this._currentQueryPageSize + "\nnextQueryPageSize: " + this._nextQueryPageSize + "\n";
  }
}
