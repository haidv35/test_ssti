package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;

public final class NativeSchemaAwarePredicatePropertyRepository implements PredicatePropertyLookup {
  private final PredicatePropertyLookup _lookup;
  
  private final QuerySchema _nativeSchema;
  
  public NativeSchemaAwarePredicatePropertyRepository(PredicatePropertyLookup lookup, QuerySchema nativeSchema) {
    this._lookup = lookup;
    this._nativeSchema = nativeSchema;
  }
  
  public PredicatePropertyDescriptor getPredicatePropertyDescriptor(String property) {
    assert property != null;
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._nativeSchema, property);
    if (propertyInfo == null)
      return this._lookup.getPredicatePropertyDescriptor(property); 
    return null;
  }
  
  public QuerySchema addPredicateProps(QuerySchema schema) {
    return this._lookup.addPredicateProps(schema);
  }
}
