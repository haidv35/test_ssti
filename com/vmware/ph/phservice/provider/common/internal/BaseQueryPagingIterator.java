package com.vmware.ph.phservice.provider.common.internal;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseQueryPagingIterator implements Iterator<ResultSet> {
  private static final Log _log = LogFactory.getLog(BaseQueryPagingIterator.class);
  
  private static final int NO_RESULT_SET_YET = -1;
  
  private final Query _baseQuery;
  
  private final int _forcedMaxResultCount;
  
  private final AtomicInteger _currObjectCounter;
  
  private final String _estimatedTotalPages;
  
  private int _pageSize;
  
  private int _currentResultSetItemCount;
  
  private int _currentOffset;
  
  private int _currentPageIndex;
  
  public BaseQueryPagingIterator(Query query, int defaultPageSize, Integer queryPageSize) {
    this._baseQuery = query;
    int queryLimit = query.getLimit();
    this._forcedMaxResultCount = (queryLimit < 0) ? Integer.MAX_VALUE : queryLimit;
    if (queryPageSize != null) {
      this._pageSize = Math.min(queryPageSize.intValue(), this._forcedMaxResultCount);
    } else {
      this._pageSize = Math.min(defaultPageSize, this._forcedMaxResultCount);
    } 
    this._currentOffset = query.getOffset();
    this._currentResultSetItemCount = (this._currentOffset > 0) ? this._currentOffset : -1;
    this._currentPageIndex = 0;
    this._currObjectCounter = new AtomicInteger();
    if (this._forcedMaxResultCount == 0) {
      this._estimatedTotalPages = String.valueOf(0);
    } else {
      this
        ._estimatedTotalPages = (this._forcedMaxResultCount < Integer.MAX_VALUE) ? String.valueOf((this._forcedMaxResultCount + this._pageSize - 1) / this._pageSize) : "unknown";
    } 
  }
  
  public String getPageInfo() {
    return "page " + this._currentPageIndex + " of " + this._estimatedTotalPages;
  }
  
  public int getCurrentPageIndex() {
    return this._currentPageIndex;
  }
  
  public boolean hasNext() {
    return (this._currentResultSetItemCount == -1 || (this._currentResultSetItemCount == this._pageSize && this._currentOffset < this._forcedMaxResultCount));
  }
  
  public ResultSet next() {
    this._pageSize = Math.min(this._pageSize, this._forcedMaxResultCount - this._currentOffset);
    Query nextPageQuery = getQueryForNextPage();
    ResultSet nextPageResultSet = fetchNextResultSet(nextPageQuery);
    List<ResourceItem> nextPageResultItems = nextPageResultSet.getItems();
    if (nextPageResultItems.isEmpty()) {
      this._currentResultSetItemCount = 0;
    } else {
      this._currentResultSetItemCount = nextPageResultItems.size();
      if (this._currentResultSetItemCount > this._pageSize)
        if (_log.isWarnEnabled())
          _log.warn(
              String.format("The following query returned %s items with a limit of %s. This is an issue with the underlying DataProvider since the contract isn't respected:\n %s", new Object[] { Integer.valueOf(this._currentResultSetItemCount), 
                  Integer.valueOf(this._pageSize), nextPageQuery }));  
    } 
    this._currentOffset += this._currentResultSetItemCount;
    this._currentPageIndex++;
    return nextPageResultSet;
  }
  
  public AtomicInteger getCurrentObjectCounter() {
    return this._currObjectCounter;
  }
  
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  private Query getQueryForNextPage() {
    Query.Builder nextQueryBuilder = Query.Builder.select(this._baseQuery.getProperties()).from(this._baseQuery.getResourceModels()).offset(this._currentOffset).limit(this._pageSize).where(this._baseQuery.getFilter()).orderBy(this._baseQuery.getSortCriteria());
    if (this._baseQuery.getWithTotalCount())
      nextQueryBuilder.withTotalCount(); 
    return nextQueryBuilder.build();
  }
  
  protected abstract ResultSet fetchNextResultSet(Query paramQuery);
}
