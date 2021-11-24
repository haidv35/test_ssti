package com.vmware.cis.data.api;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.vmware.cis.data.internal.provider.QueryDispatcher;
import com.vmware.cis.data.internal.provider.QueryLimitsEnforcer;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.Validate;

public final class QueryService {
  private final DataProvider _provider;
  
  private final QueryLimitsEnforcer _limitsEnforcer;
  
  private QueryService(DataProvider provider, QueryLimitsEnforcer limitsEnforcer) {
    assert provider != null;
    assert limitsEnforcer != null;
    this._provider = provider;
    this._limitsEnforcer = limitsEnforcer;
  }
  
  ResultSet executeQueryImpl(Query query) {
    assert query != null;
    this._limitsEnforcer.enforceQuerySizeLimits(query);
    ResultSet result = this._provider.executeQuery(query);
    this._limitsEnforcer.enforceResultSizeLimits(result);
    return ResultSetUtil.project(result, query.getProperties());
  }
  
  public QueryCommand.Builder select(List<String> properties) {
    Validate.notNull(properties, "The collection of property names must not be null");
    Validate.noNullElements(properties, "The collection of property names must not contain null elements");
    return new QueryCommand.Builder(this, Query.Builder.select(properties));
  }
  
  public QueryCommand.Builder select(String... properties) {
    Validate.notNull(properties, "The collection of property names must not be null");
    return select(Arrays.asList(properties));
  }
  
  public QuerySchema getSchema() {
    return this._provider.getSchema();
  }
  
  public static final class Builder {
    private final Collection<DataProvider> _providers;
    
    private final boolean _useDispatcher;
    
    private ExecutorService _executor;
    
    private QueryLimitsSpec _queryLimitsSpec;
    
    private Builder(Collection<DataProvider> providers, boolean useDispatcher) {
      this._providers = providers;
      this._useDispatcher = useDispatcher;
    }
    
    public static Builder forProvider(DataProvider provider) {
      Validate.notNull(provider, "Data provider must not be null");
      return new Builder(Collections.singletonList(provider), false);
    }
    
    public static Builder forProviders(Collection<DataProvider> providers) {
      Validate.notNull(providers, "The collection of data providers must not be null");
      Validate.noNullElements(providers, "The collection of data providers must not contain null elements");
      return new Builder(providers, true);
    }
    
    public Builder withExecutor(ExecutorService executor) {
      this._executor = executor;
      return this;
    }
    
    public Builder withQueryLimits(QueryLimitsSpec queryLimitSpec) {
      this._queryLimitsSpec = queryLimitSpec;
      return this;
    }
    
    public QueryService build() {
      ListeningExecutorService listeningExecutorService;
      ExecutorService executor = this._executor;
      if (this._executor == null)
        listeningExecutorService = MoreExecutors.newDirectExecutorService(); 
      DataProvider provider = null;
      if (this._useDispatcher) {
        provider = QueryDispatcher.createDispatcher(this._providers, (ExecutorService)listeningExecutorService);
      } else {
        provider = this._providers.iterator().next();
      } 
      QueryLimitsSpec limitsSpec = this._queryLimitsSpec;
      if (limitsSpec == null)
        limitsSpec = new QueryLimitsSpec(-1, -1, -1); 
      QueryLimitsEnforcer limitsEnforcer = new QueryLimitsEnforcer(limitsSpec);
      return new QueryService(provider, limitsEnforcer);
    }
  }
}
