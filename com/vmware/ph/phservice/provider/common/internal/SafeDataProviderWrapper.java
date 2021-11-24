package com.vmware.ph.phservice.provider.common.internal;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SafeDataProviderWrapper implements DataProvider {
  private static final Log _log = LogFactory.getLog(SafeDataProviderWrapper.class);
  
  private final DataProvider _wrappedDataProvider;
  
  public SafeDataProviderWrapper(DataProvider wrappedDataProvider) {
    this._wrappedDataProvider = wrappedDataProvider;
  }
  
  public ResultSet executeQuery(Query query) {
    long queryStartTimestamp = System.currentTimeMillis();
    try {
      return this._wrappedDataProvider.executeQuery(query);
    } catch (IllegalArgumentException e) {
      throw (IllegalArgumentException)ExceptionsContextManager.store(e);
    } catch (Throwable e) {
      ExceptionsContextManager.store(e);
      _log.error("Error while executing data provider query.Will return empty response.", e);
      ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
      return resultSetBuilder.build();
    } finally {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("Executing a query against %s took %d milliseconds.", new Object[] { query.getResourceModels(), 
                Long.valueOf(System.currentTimeMillis() - queryStartTimestamp) })); 
    } 
  }
  
  public QuerySchema getSchema() {
    try {
      return this._wrappedDataProvider.getSchema();
    } catch (Throwable e) {
      _log.error("Error while retrieving data provider schema.", e);
      return QuerySchema.EMPTY_SCHEMA;
    } 
  }
  
  public DataProvider getWrappedDataProvider() {
    return this._wrappedDataProvider;
  }
}
