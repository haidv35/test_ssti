package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.api.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class BatchQueryPagingIterator implements Iterator<NamedQueryResultSet> {
  private static final Log _log = LogFactory.getLog(BatchQueryPagingIterator.class);
  
  private final QueryService _queryService;
  
  private final NamedQuery[] _queries;
  
  private final int _pageSize;
  
  private int _currentQueryIdx;
  
  private QueryPagingIterator _currentQueryPager;
  
  public BatchQueryPagingIterator(QueryService queryService, NamedQuery[] queries, int pageSize) {
    this._queryService = queryService;
    this._queries = queries;
    this._pageSize = pageSize;
    this._currentQueryIdx = 0;
    this._currentQueryPager = new QueryPagingIterator(queries[0], this._pageSize, this._queryService);
  }
  
  public boolean hasNext() {
    return (this._currentQueryIdx < this._queries.length - 1 || this._currentQueryPager.hasNext());
  }
  
  public NamedQueryResultSet next() {
    long startTimeNanos = System.nanoTime();
    if (!hasNext())
      throw new NoSuchElementException("No next item is available in the iterator."); 
    if (!this._currentQueryPager.hasNext()) {
      this._currentQueryIdx++;
      NamedQuery nextQuery = this._queries[this._currentQueryIdx];
      this._currentQueryPager = new QueryPagingIterator(nextQuery, this._pageSize, this._queryService);
    } 
    ResultSet nextPage = this._currentQueryPager.next();
    logResultForDebugging(this._currentQueryPager, nextPage);
    NamedQueryResultSet namedQueryResultSet = new NamedQueryResultSet(nextPage, getCurrentQueryName(), this._currentQueryPager.getCurrentObjectCounter(), this._currentQueryPager.getCurrentPageIndex(), startTimeNanos, getCurrentQueryPageSize(), getNextQueryPageSize());
    return namedQueryResultSet;
  }
  
  private Integer getNextQueryPageSize() {
    if (this._currentQueryIdx < this._queries.length - 1)
      return this._queries[this._currentQueryIdx + 1].getPageSize(); 
    return null;
  }
  
  private Integer getCurrentQueryPageSize() {
    NamedQuery namedQuery = this._currentQueryPager.getBaseNamedQuery();
    return namedQuery.getPageSize();
  }
  
  public void remove() {
    throw new UnsupportedOperationException();
  }
  
  private String getCurrentQueryName() {
    NamedQuery namedQuery = this._currentQueryPager.getBaseNamedQuery();
    String name = namedQuery.getName();
    Collection<String> resourceModels = namedQuery.getQuery().getResourceModels();
    String constraint = resourceModels.iterator().next();
    if (name != null)
      return name; 
    return constraint;
  }
  
  private void logResultForDebugging(QueryPagingIterator pager, ResultSet resultSet) {
    if (_log.isTraceEnabled())
      _log.trace(
          String.format("Usage data collection result for query #%d (%s):\n%s", new Object[] { Integer.valueOf(this._currentQueryIdx), pager
              .getPageInfo(), resultSet })); 
  }
}
