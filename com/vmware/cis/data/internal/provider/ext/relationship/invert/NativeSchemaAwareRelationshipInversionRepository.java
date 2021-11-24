package com.vmware.cis.data.internal.provider.ext.relationship.invert;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import java.util.Collection;
import java.util.Collections;

public class NativeSchemaAwareRelationshipInversionRepository implements RelationshipInversionLookup {
  private final RelationshipInversionLookup _lookup;
  
  private final QuerySchema _nativeSchema;
  
  public NativeSchemaAwareRelationshipInversionRepository(RelationshipInversionLookup lookup, QuerySchema nativeSchema) {
    this._lookup = lookup;
    this._nativeSchema = nativeSchema;
  }
  
  public Collection<String> invert(String property) {
    assert property != null;
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._nativeSchema, property);
    if (propertyInfo == null || !propertyInfo.getFilterable())
      return this._lookup.invert(property); 
    return Collections.emptyList();
  }
  
  public QuerySchema addInvertibleRelationships(QuerySchema schema) {
    return this._lookup.addInvertibleRelationships(schema);
  }
}
