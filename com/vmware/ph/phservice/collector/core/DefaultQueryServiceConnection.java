package com.vmware.ph.phservice.collector.core;

import com.vmware.cis.data.api.QueryLimitsSpec;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import com.vmware.ph.phservice.provider.common.internal.DefaultContextFactory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultQueryServiceConnection implements QueryServiceConnection {
  private static final Log _log = LogFactory.getLog(DefaultQueryServiceConnectionFactory.class);
  
  private static final int MAX_RESULTS_LIMIT = -1;
  
  protected final ExecutorService _executor;
  
  protected final QueryLimitsSpec _queryLimitsSpec;
  
  private final DataProvidersConnection _dataProvidersConnection;
  
  public DefaultQueryServiceConnection(DataProvidersConnection dataProvidersConnection) {
    this(dataProvidersConnection, null);
  }
  
  public DefaultQueryServiceConnection(DataProvidersConnection dataProvidersConnection, ExecutorService executor) {
    this._dataProvidersConnection = dataProvidersConnection;
    this._executor = executor;
    this._queryLimitsSpec = new QueryLimitsSpec(32, 128, -1);
  }
  
  public QueryService getQueryService() {
    QueryService queryService = null;
    try {
      List<DataProvider> dataProviders = this._dataProvidersConnection.getDataProviders();
      queryService = buildQueryServiceForDataProviders(dataProviders);
    } catch (Exception e) {
      _log.error("Error while getting the data providers list. The QueryService cannot be created.", e);
    } 
    return queryService;
  }
  
  public void close() {
    if (this._dataProvidersConnection != null)
      this._dataProvidersConnection.close(); 
  }
  
  public ContextFactory getContextFactory() {
    DefaultContextFactory defaultContextFactory;
    if (this._dataProvidersConnection != null && this._dataProvidersConnection instanceof ContextFactory) {
      ContextFactory contextFactory = (ContextFactory)this._dataProvidersConnection;
    } else {
      defaultContextFactory = new DefaultContextFactory();
    } 
    return (ContextFactory)defaultContextFactory;
  }
  
  protected QueryService buildQueryServiceForDataProviders(List<DataProvider> dataProviders) {
    QueryService queryService = QueryService.Builder.forProviders(dataProviders).withExecutor(this._executor).withQueryLimits(this._queryLimitsSpec).build();
    return queryService;
  }
}
