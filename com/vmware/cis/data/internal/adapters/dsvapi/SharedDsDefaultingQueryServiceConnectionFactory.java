package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharedDsDefaultingQueryServiceConnectionFactory extends DsDefaultingQueryServiceConnectionFactory {
  private static final Log _log = LogFactory.getLog(SharedDsDefaultingQueryServiceConnectionFactory.class);
  
  private final Object _sharedConnectionLock = new Object();
  
  private SharedQueryServiceConnection _queryServiceConnection;
  
  public SharedDsDefaultingQueryServiceConnectionFactory(Builder<VapiClient> vapiClientBuilder) {
    this(vapiClientBuilder, Executors.newSingleThreadExecutor());
  }
  
  public SharedDsDefaultingQueryServiceConnectionFactory(Builder<VapiClient> vapiClientBuilder, ExecutorService executorService) {
    super(vapiClientBuilder, executorService);
  }
  
  public QueryServiceConnection createQueryServiceConnection(Builder<DataProvidersConnection> dataProvidersConnectionBuilder) {
    synchronized (this._sharedConnectionLock) {
      if (this._queryServiceConnection == null) {
        QueryServiceConnection queryServiceConnection = super.createQueryServiceConnection(dataProvidersConnectionBuilder);
        if (queryServiceConnection != null)
          this._queryServiceConnection = new SharedQueryServiceConnection(queryServiceConnection, this, this._sharedConnectionLock); 
      } else {
        _log.debug("Will use the existing shared QueryService connection.");
        this._queryServiceConnection.incrementUsageCount();
      } 
      return this._queryServiceConnection;
    } 
  }
  
  private void handleConnectionClosed() {
    synchronized (this._sharedConnectionLock) {
      this._queryServiceConnection = null;
    } 
  }
  
  private static class SharedQueryServiceConnection implements QueryServiceConnection {
    private final Object _sharedConnectionLock;
    
    private final SharedDsDefaultingQueryServiceConnectionFactory _queryServiceConnectionFactory;
    
    private QueryServiceConnection _queryServiceConnection;
    
    private QueryService _queryService;
    
    private int _usageCount;
    
    private SharedQueryServiceConnection(QueryServiceConnection queryServiceConnection, SharedDsDefaultingQueryServiceConnectionFactory queryServiceConnectionFactory, Object sharedConnectionLock) {
      this._queryServiceConnection = queryServiceConnection;
      this._queryServiceConnectionFactory = queryServiceConnectionFactory;
      this._sharedConnectionLock = sharedConnectionLock;
      this._usageCount = 1;
    }
    
    public synchronized ContextFactory getContextFactory() {
      if (this._queryServiceConnection == null)
        throw new IllegalStateException("Connection was already closed!"); 
      return this._queryServiceConnection.getContextFactory();
    }
    
    public QueryService getQueryService() {
      synchronized (this._sharedConnectionLock) {
        if (this._queryService == null && this._queryServiceConnection != null)
          this._queryService = this._queryServiceConnection.getQueryService(); 
        return this._queryService;
      } 
    }
    
    public void close() {
      synchronized (this._sharedConnectionLock) {
        if (this._usageCount > 0) {
          this._usageCount--;
        } else {
          SharedDsDefaultingQueryServiceConnectionFactory._log.warn("Trying to close an already closed connection. Make sure there are no resource leaks!");
        } 
        if (this._usageCount == 0) {
          if (this._queryServiceConnection != null)
            this._queryServiceConnection.close(); 
          this._queryServiceConnection = null;
          this._queryService = null;
          this._queryServiceConnectionFactory.handleConnectionClosed();
        } else if (SharedDsDefaultingQueryServiceConnectionFactory._log.isDebugEnabled()) {
          SharedDsDefaultingQueryServiceConnectionFactory._log.debug(
              String.format("Will not close the shared QueryService connection. It is used by %d other parallel collection(s).", new Object[] { Integer.valueOf(this._usageCount) }));
        } 
      } 
    }
    
    private void incrementUsageCount() {
      this._usageCount++;
    }
  }
}
