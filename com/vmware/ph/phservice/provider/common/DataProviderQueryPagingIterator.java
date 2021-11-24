package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.internal.BaseQueryPagingIterator;

public class DataProviderQueryPagingIterator extends BaseQueryPagingIterator {
  private final DataProvider _wrappedDataProvider;
  
  public DataProviderQueryPagingIterator(DataProvider wrappedDataProvider, Query namedQuery, int defaultPageSize) {
    super(namedQuery, defaultPageSize, null);
    this._wrappedDataProvider = wrappedDataProvider;
  }
  
  protected ResultSet fetchNextResultSet(Query query) {
    return this._wrappedDataProvider.executeQuery(query);
  }
}
