package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NativeSchemaAwareRelatedPropertyRepository implements RelatedPropertyLookup {
  private final RelatedPropertyLookup _lookup;
  
  private final QuerySchema _schema;
  
  public NativeSchemaAwareRelatedPropertyRepository(RelatedPropertyLookup lookup, QuerySchema schema) {
    this._lookup = lookup;
    this._schema = schema;
  }
  
  public RelatedPropertyDescriptor getRelatedPropertyDescriptor(String property) {
    assert property != null;
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._schema, property);
    if (propertyInfo == null)
      return this._lookup.getRelatedPropertyDescriptor(property); 
    return null;
  }
  
  public Map<String, RelatedPropertyDescriptor> getRelatedPropertyDescriptors(List<String> properties) {
    assert properties != null;
    List<String> remained = new ArrayList<>(properties.size());
    for (String property : properties) {
      QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(this._schema, property);
      if (propertyInfo == null)
        remained.add(property); 
    } 
    return this._lookup.getRelatedPropertyDescriptors(remained);
  }
  
  public QuerySchema addRelatedProps(QuerySchema schema) {
    return this._lookup.addRelatedProps(schema);
  }
}
