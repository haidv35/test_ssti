package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OperationThresholdDataProvider implements DataProvider {
  private static Logger _logger = LoggerFactory.getLogger(OperationThresholdDataProvider.class);
  
  private final DataProvider _dataProvider;
  
  private final long _threshold;
  
  public OperationThresholdDataProvider(DataProvider dataProvider, long threshold) {
    assert dataProvider != null;
    assert threshold > 0L;
    this._dataProvider = dataProvider;
    this._threshold = threshold;
  }
  
  public ResultSet executeQuery(Query query) {
    long startTime = System.currentTimeMillis();
    ResultSet resultSet = this._dataProvider.executeQuery(query);
    long executionTime = System.currentTimeMillis() - startTime;
    if (executionTime > this._threshold)
      _logger.warn("Slow execution detected while retrieving query results from '{}': {} ms\n{}", new Object[] { this._dataProvider, 
            
            Long.valueOf(executionTime), query }); 
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    long startTime = System.currentTimeMillis();
    QuerySchema schema = this._dataProvider.getSchema();
    long executionTime = System.currentTimeMillis() - startTime;
    if (executionTime > this._threshold)
      _logger.warn("Slow execution detected while retrieving query schema from {}: {} ms.", this._dataProvider, 
          Long.valueOf(executionTime)); 
    return schema;
  }
  
  public String toString() {
    return this._dataProvider.toString();
  }
}
