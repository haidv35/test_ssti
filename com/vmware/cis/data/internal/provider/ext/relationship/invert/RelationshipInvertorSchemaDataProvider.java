package com.vmware.cis.data.internal.provider.ext.relationship.invert;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;

public final class RelationshipInvertorSchemaDataProvider implements DataProvider {
  private final DataProvider _dataProvider;
  
  private final RelationshipInversionRepository _relationshipInversions;
  
  public RelationshipInvertorSchemaDataProvider(DataProvider dataProvider, RelationshipInversionRepository relationshipInversions) {
    assert dataProvider != null;
    assert relationshipInversions != null;
    this._dataProvider = dataProvider;
    this._relationshipInversions = relationshipInversions;
  }
  
  public ResultSet executeQuery(Query query) {
    return this._dataProvider.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    return this._relationshipInversions.addInvertibleRelationships(this._dataProvider.getSchema());
  }
  
  public String toString() {
    return this._dataProvider.toString();
  }
}
