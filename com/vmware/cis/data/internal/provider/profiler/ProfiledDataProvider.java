package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProfiledDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(ProfiledDataProvider.class);
  
  private final DataProvider _dataProvider;
  
  private ProfiledDataProvider(DataProvider dataProvider) {
    assert dataProvider != null;
    this._dataProvider = dataProvider;
  }
  
  public static DataProvider create(DataProvider provider) {
    boolean profilingEnabled = _logger.isDebugEnabled();
    return profilingEnabled ? new ProfiledDataProvider(provider) : provider;
  }
  
  public ResultSet executeQuery(Query query) {
    long startTime = System.currentTimeMillis();
    ResultSet resultSet = this._dataProvider.executeQuery(query);
    if (_logger.isDebugEnabled()) {
      long executionTime = System.currentTimeMillis() - startTime;
      _logger.debug("'{}' executed query in {} ms", this._dataProvider
          .getClass().getCanonicalName(), Long.valueOf(executionTime));
    } 
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    long startTime = System.currentTimeMillis();
    QuerySchema querySchema = this._dataProvider.getSchema();
    if (_logger.isDebugEnabled()) {
      long executionTime = System.currentTimeMillis() - startTime;
      _logger.debug("'{}' retrieved schema in {} ms", this._dataProvider
          .getClass().getCanonicalName(), Long.valueOf(executionTime));
    } 
    return querySchema;
  }
  
  public String toString() {
    return this._dataProvider.toString();
  }
}
