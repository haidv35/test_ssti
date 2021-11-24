package com.vmware.cis.data.api.binding;

import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.internal.api.binding.QueryBindingDescriptor;
import com.vmware.cis.data.internal.api.binding.QueryBindingParser;
import com.vmware.cis.data.internal.api.binding.QueryBindingProvider;
import com.vmware.cis.data.internal.api.binding.QueryServiceBasedBindingProvider;
import org.apache.commons.lang.Validate;

public final class QueryBindingService {
  private final QueryService _queryService;
  
  private final QueryBindingProvider _bindingProvider;
  
  @Deprecated
  public QueryBindingService(QueryService queryService) {
    Validate.notNull(queryService);
    this._queryService = queryService;
    this._bindingProvider = null;
  }
  
  QueryBindingService(QueryBindingProvider bindingProvider) {
    this(null, bindingProvider);
  }
  
  private QueryBindingService(QueryService queryService, QueryBindingProvider bindingProvider) {
    assert false;
    this._queryService = queryService;
    this._bindingProvider = bindingProvider;
  }
  
  public static QueryBindingService forQueryService(QueryService queryService) {
    Validate.notNull(queryService);
    return new QueryBindingService(queryService, null);
  }
  
  public <T> QueryBindingCommand.Builder<T> prepare(Class<T> resultType) {
    Validate.notNull(resultType);
    QueryBindingDescriptor descriptor = QueryBindingParser.parse(resultType);
    if (this._queryService != null)
      return new QueryBindingCommand.Builder<>(new QueryServiceBasedBindingProvider(this._queryService, descriptor)); 
    return new QueryBindingCommand.Builder<>(this._bindingProvider);
  }
}
