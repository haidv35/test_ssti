package com.vmware.cis.data.internal.provider.schema;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.provider.DataProvider;
import java.util.concurrent.Callable;

public final class QuerySchemaCacheDecorator {
  public static DataProviderConnector cacheConnector(DataProviderConnector connector, String key, QuerySchemaCache cache) {
    assert connector != null;
    assert key != null;
    assert cache != null;
    return new QuerySchemaCacheDataProviderConnector(connector, key, cache);
  }
  
  public static DataProvider cacheProvider(DataProvider provider, String key, QuerySchemaCache cache) {
    assert provider != null;
    assert key != null;
    assert cache != null;
    return new QuerySchemaCacheDataProvider(provider, key, cache);
  }
  
  private static final class QuerySchemaCacheDataProviderConnector implements DataProviderConnector {
    private final DataProviderConnector _connector;
    
    private final String _key;
    
    private final QuerySchemaCache _cache;
    
    QuerySchemaCacheDataProviderConnector(DataProviderConnector connector, String key, QuerySchemaCache cache) {
      assert connector != null;
      assert key != null;
      assert cache != null;
      this._connector = connector;
      this._key = key;
      this._cache = cache;
    }
    
    public DataProviderConnection getConnection(AuthenticationTokenSource authn) {
      final DataProviderConnection connection = this._connector.getConnection(authn);
      return new DataProviderConnection() {
          public void close() throws Exception {
            connection.close();
          }
          
          public DataProvider getDataProvider() {
            DataProvider provider = connection.getDataProvider();
            return new QuerySchemaCacheDecorator.QuerySchemaCacheDataProvider(provider, QuerySchemaCacheDecorator.QuerySchemaCacheDataProviderConnector.this._key, QuerySchemaCacheDecorator.QuerySchemaCacheDataProviderConnector.this._cache);
          }
        };
    }
    
    public String toString() {
      return getClass().getSimpleName() + "(" + this._connector.toString() + ")";
    }
  }
  
  private static final class QuerySchemaCacheDataProvider implements DataProvider {
    private final DataProvider _provider;
    
    private final String _key;
    
    private final QuerySchemaCache _cache;
    
    private final QuerySchemaCacheDecorator.FetchQuerySchemaTask _fetchQuerySchema;
    
    QuerySchemaCacheDataProvider(DataProvider provider, String key, QuerySchemaCache cache) {
      assert provider != null;
      assert key != null;
      assert cache != null;
      this._provider = provider;
      this._key = key;
      this._cache = cache;
      this._fetchQuerySchema = new QuerySchemaCacheDecorator.FetchQuerySchemaTask(provider);
    }
    
    public ResultSet executeQuery(Query query) {
      return this._provider.executeQuery(query);
    }
    
    public QuerySchema getSchema() {
      QuerySchema schema = this._cache.get(this._key, this._fetchQuerySchema);
      return schema;
    }
    
    public String toString() {
      return this._provider.toString();
    }
  }
  
  private static final class FetchQuerySchemaTask implements Callable<QuerySchema> {
    private final DataProvider _provider;
    
    public FetchQuerySchemaTask(DataProvider provider) {
      assert provider != null;
      this._provider = provider;
    }
    
    public QuerySchema call() throws Exception {
      return this._provider.getSchema();
    }
    
    public String toString() {
      return "Fetch schema from " + this._provider.toString();
    }
  }
}
