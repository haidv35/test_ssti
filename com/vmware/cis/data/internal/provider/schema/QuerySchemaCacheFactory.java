package com.vmware.cis.data.internal.provider.schema;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.vmware.cis.data.api.QuerySchema;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuerySchemaCacheFactory {
  private static final Logger _logger = LoggerFactory.getLogger(QuerySchemaCacheDecorator.class);
  
  public static QuerySchemaCache createCacheWithSoftValues(int maxSize) {
    Cache<String, QuerySchema> cache = CacheBuilder.newBuilder().softValues().maximumSize(maxSize).removalListener(new QuerySchemaRemovalListener()).build();
    return new GuavaQuerySchemaCache(cache);
  }
  
  public static QuerySchemaCache createNoOpCache() {
    return new NoOpQuerySchemaCache();
  }
  
  private static final class GuavaQuerySchemaCache implements QuerySchemaCache {
    private Cache<String, QuerySchema> _cache;
    
    private GuavaQuerySchemaCache(Cache<String, QuerySchema> cache) {
      this._cache = cache;
    }
    
    public QuerySchema get(String key, Callable<QuerySchema> callable) {
      try {
        QuerySchema querySchema = (QuerySchema)this._cache.getIfPresent(key);
        if (querySchema == null) {
          querySchema = callable.call();
          if (!querySchema.getModels().isEmpty())
            this._cache.put(key, querySchema); 
        } 
        return querySchema;
      } catch (Exception e) {
        String msg = String.format("Couldn't fetch the query schema for key '%s' and callable '%s'.", new Object[] { key, callable });
        throw new RuntimeException(msg, e);
      } 
    }
    
    public String toString() {
      return getClass().getSimpleName() + " with keys: " + this._cache
        .asMap().keySet().toString();
    }
  }
  
  private static final class NoOpQuerySchemaCache implements QuerySchemaCache {
    private NoOpQuerySchemaCache() {}
    
    public QuerySchema get(String key, Callable<QuerySchema> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        String msg = String.format("Couldn't fetch the query schema for key '%s' and callable '%s'.", new Object[] { key, callable });
        throw new RuntimeException(msg, e);
      } 
    }
  }
  
  private static final class QuerySchemaRemovalListener implements RemovalListener<String, QuerySchema> {
    private QuerySchemaRemovalListener() {}
    
    public void onRemoval(RemovalNotification<String, QuerySchema> notification) {
      String key = (String)notification.getKey();
      RemovalCause cause = notification.getCause();
      QuerySchemaCacheFactory._logger.info("The query schema with key '{}' was removed. The removal cause is '{}'", key, cause);
    }
  }
}
