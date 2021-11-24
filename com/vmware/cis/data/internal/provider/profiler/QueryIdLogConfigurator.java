package com.vmware.cis.data.internal.provider.profiler;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.util.QueryMarker;
import com.vmware.cis.data.provider.DataProvider;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryIdLogConfigurator implements AutoCloseable {
  private static final Logger _logger = LoggerFactory.getLogger(QueryIdLogConfigurator.class);
  
  public static final String DEFAULT_OP_ID = "";
  
  private static final String INVOCATION_SEPARATOR = ":";
  
  private static final AtomicLong INVOCATION_ID_GENERATOR = new AtomicLong(0L);
  
  private final String _queryId;
  
  private final String _parentQueryId;
  
  private final String _queryControl;
  
  private final String _providerName;
  
  private QueryIdLogConfigurator(String queryId, String parentQueryId, String queryControl, String providerName) {
    this._queryId = queryId;
    this._parentQueryId = parentQueryId;
    this._queryControl = queryControl;
    this._providerName = providerName;
  }
  
  public void close() {
    QueryMarker.setQueryId(this._queryId);
    QueryMarker.setParentQueryId(this._parentQueryId);
    QueryMarker.setQueryControl(this._queryControl);
    QueryMarker.setProviderName(this._providerName);
  }
  
  public static DataProvider withQueryCounter(DataProvider provider, String invocationPrefix) {
    assert provider != null;
    assert invocationPrefix != null;
    return new QueryCounterDataProvider(provider, invocationPrefix);
  }
  
  public static QueryCounter newQueryCounter(@Nonnull String invocationPrefix) {
    return new QueryCounter(invocationPrefix);
  }
  
  public static QueryIdLogConfigurator onPropertyProviderStart(String providerName) {
    return onProviderStart("external", providerName);
  }
  
  public static QueryIdLogConfigurator onProviderStart(String newQueryControl, String newProviderName) {
    String queryId = QueryMarker.getQueryId();
    String parentQueryId = QueryMarker.getParentQueryId();
    String queryControl = QueryMarker.getQueryControl();
    String providerName = QueryMarker.getProviderName();
    QueryMarker.setQueryControl(newQueryControl);
    QueryMarker.setProviderName(newProviderName);
    return new QueryIdLogConfigurator(queryId, parentQueryId, queryControl, providerName);
  }
  
  public static QueryIdLogConfigurator onQueryStart(String opId, @Nonnull String invocationId) {
    assert opId != null;
    String queryId = QueryMarker.getQueryId();
    String parentQueryId = QueryMarker.getParentQueryId();
    String queryControl = QueryMarker.getQueryControl();
    String providerName = QueryMarker.getProviderName();
    if (!"internal".equals(queryControl)) {
      String newQueryId = nextQueryId(opId, invocationId);
      QueryMarker.setQueryId(newQueryId);
      QueryMarker.setParentQueryId(queryId);
      QueryMarker.setQueryControl("internal");
      logNestedQueryStartInDebug(queryId, providerName);
    } 
    return new QueryIdLogConfigurator(queryId, parentQueryId, queryControl, providerName);
  }
  
  private static void logNestedQueryStartInDebug(String parentQueryId, String providerName) {
    if (parentQueryId == null || providerName == null)
      return; 
    if (_logger.isDebugEnabled())
      _logger.debug("Start execution of a query called by provider: {}, parent queryId: {}", providerName, parentQueryId); 
  }
  
  private static String nextQueryId(String opId, String invocationIdPrefix) {
    assert opId != null;
    if (opId.isEmpty())
      return nextInvocationId(invocationIdPrefix); 
    return nextInvocationId(invocationIdPrefix) + ":" + opId;
  }
  
  private static String nextInvocationId(String invocationIdPrefix) {
    return invocationIdPrefix + INVOCATION_ID_GENERATOR.incrementAndGet();
  }
  
  private static final class QueryCounterDataProvider implements DataProvider {
    private final DataProvider _provider;
    
    private final QueryIdLogConfigurator.QueryCounter _queryCounter;
    
    QueryCounterDataProvider(DataProvider provider, String invocationPrefix) {
      assert provider != null;
      assert invocationPrefix != null;
      this._provider = provider;
      this._queryCounter = new QueryIdLogConfigurator.QueryCounter(invocationPrefix);
    }
    
    public ResultSet executeQuery(Query query) {
      QueryIdLogConfigurator logConfigurator = this._queryCounter.onQueryStart();
      try {
        return this._provider.executeQuery(query);
      } finally {
        logConfigurator.close();
      } 
    }
    
    public QuerySchema getSchema() {
      return this._provider.getSchema();
    }
    
    public String toString() {
      return this._provider.toString();
    }
  }
  
  public static final class QueryCounter {
    private final AtomicInteger _counter = new AtomicInteger(0);
    
    private final String _invocationPrefix;
    
    QueryCounter(String invocationPrefix) {
      assert invocationPrefix != null;
      this._invocationPrefix = invocationPrefix;
    }
    
    public QueryIdLogConfigurator onQueryStart() {
      String queryId = QueryMarker.getQueryId();
      String parentQueryId = QueryMarker.getParentQueryId();
      String queryControl = QueryMarker.getQueryControl();
      String providerName = QueryMarker.getProviderName();
      String newQueryId = queryId + String.format(":%s%02d", new Object[] { this._invocationPrefix, 
            Integer.valueOf(this._counter.incrementAndGet()) });
      QueryMarker.setQueryId(newQueryId);
      return new QueryIdLogConfigurator(queryId, parentQueryId, queryControl, providerName);
    }
  }
}
