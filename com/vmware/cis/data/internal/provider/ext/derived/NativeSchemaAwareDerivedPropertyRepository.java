package com.vmware.cis.data.internal.provider.ext.derived;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import java.util.Arrays;
import java.util.List;

public final class NativeSchemaAwareDerivedPropertyRepository implements DerivedPropertyLookup {
  private final DerivedPropertyLookup _lookup;
  
  private final QuerySchema _nativeSchema;
  
  private final List<String> OVERWRITING_PROPERTIES = Arrays.asList(new String[] { "com.vmware.content.LibraryModel/vcenter" });
  
  public NativeSchemaAwareDerivedPropertyRepository(DerivedPropertyLookup lookup, QuerySchema nativeSchema) {
    this._lookup = lookup;
    this._nativeSchema = nativeSchema;
  }
  
  public DerivedPropertyDescriptor getDerivedPropertyDescriptor(String property) {
    assert property != null;
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._nativeSchema, property);
    if (propertyInfo == null || this.OVERWRITING_PROPERTIES.contains(property))
      return this._lookup.getDerivedPropertyDescriptor(property); 
    return null;
  }
  
  public QuerySchema addDerivedProps(QuerySchema schema) {
    return this._lookup.addDerivedProps(schema);
  }
}
