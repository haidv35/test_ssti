package com.vmware.cis.data.api.binding;

import com.vmware.cis.data.internal.api.binding.QueryBindingProvider;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.lang.Validate;

public final class QueryBindingCommand<T> {
  private static final String QUERY_BINDING_INVOCATION_ID_PREFIX = "qb-";
  
  private final QueryBindingProvider _provider;
  
  private final String _opId;
  
  private QueryBindingCommand(QueryBindingProvider provider, String opId) {
    assert provider != null;
    assert opId != null;
    this._provider = provider;
    this._opId = opId;
  }
  
  public T fetch(Object key) {
    Validate.notNull(key);
    Collection<?> results = fetch(Collections.singleton(key));
    if (results.isEmpty())
      return null; 
    T result = results.iterator().next();
    return result;
  }
  
  public Collection<T> fetch(Collection<?> keys) {
    Validate.noNullElements(keys);
    if (keys.isEmpty())
      return Collections.emptyList(); 
    QueryIdLogConfigurator logConfigurator = QueryIdLogConfigurator.onQueryStart(this._opId, "qb-");
    try {
      Collection<T> results = (Collection)this._provider.fetch(keys);
      return Collections.unmodifiableCollection(results);
    } finally {
      logConfigurator.close();
    } 
  }
  
  public static final class Builder<T> {
    private final QueryBindingProvider _provider;
    
    private String _opId;
    
    Builder(QueryBindingProvider provider) {
      assert provider != null;
      this._provider = provider;
      this._opId = "";
    }
    
    public Builder<T> opId(String opId) {
      Validate.notEmpty(opId, "OpId must not be empty.");
      this._opId = opId;
      return this;
    }
    
    public QueryBindingCommand<T> build() {
      return new QueryBindingCommand<>(this._provider, this._opId);
    }
    
    public T fetch(Object key) {
      QueryBindingCommand<T> command = build();
      return command.fetch(key);
    }
    
    public Collection<T> fetch(Collection<?> keys) {
      QueryBindingCommand<T> command = build();
      return command.fetch(keys);
    }
  }
}
