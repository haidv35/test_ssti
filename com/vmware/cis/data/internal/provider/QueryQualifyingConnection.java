package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.provider.DataProvider;
import org.apache.commons.lang.Validate;

public final class QueryQualifyingConnection implements DataProvider {
  private final DataProvider _connection;
  
  private final QueryQualifier _queryQualifier;
  
  public QueryQualifyingConnection(DataProvider connection) {
    assert connection != null;
    this._connection = connection;
    this._queryQualifier = new QueryQualifier(this._connection.getSchema(), new QueryQualifier.QueryQualifierAmbiguityResolver() {
          public String pickModel(String model1, String model2, String simpleProperty) {
            return null;
          }
        });
  }
  
  public QueryQualifyingConnection(DataProvider connection, QueryQualifier.QueryQualifierAmbiguityResolver ambiguityResolver) {
    assert connection != null;
    assert ambiguityResolver != null;
    this._connection = connection;
    this._queryQualifier = new QueryQualifier(this._connection.getSchema(), ambiguityResolver);
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    QueryQualifier.QualifierContext qualifierContext = this._queryQualifier.qualifyQuery(query);
    Query qualifiedQuery = qualifierContext.getQualifiedQuery();
    ResultSet result = this._connection.executeQuery(qualifiedQuery);
    assert result != null;
    return this._queryQualifier.unqualifyResultSet(result, qualifierContext);
  }
  
  public QuerySchema getSchema() {
    return this._connection.getSchema();
  }
}
