package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;

public class AggregatedSchemaProviderConnection implements DataProvider {
  private final DataProvider _connection;
  
  private final AggregatedModelLookup _aggregatedModelLookup;
  
  public AggregatedSchemaProviderConnection(DataProvider connection, AggregatedModelLookup aggregatedModelLookup) {
    assert connection != null;
    assert aggregatedModelLookup != null;
    this._connection = connection;
    this._aggregatedModelLookup = aggregatedModelLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    return this._connection.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    QuerySchema schema = this._connection.getSchema();
    return AggregatedModelSchema.addAggregatedModels(schema, this._aggregatedModelLookup);
  }
  
  public String toString() {
    return this._connection.toString();
  }
}
