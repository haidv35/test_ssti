package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.util.UnqualifiedProperty;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public final class FederationQuerySchemaLookup {
  private final Map<String, QuerySchema> _schemaBynodeId;
  
  public FederationQuerySchemaLookup(Map<String, QuerySchema> schemaBynodeId) {
    this._schemaBynodeId = schemaBynodeId;
  }
  
  public boolean isSelectableAnywhere(String model, String property) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    for (QuerySchema schema : this._schemaBynodeId.values()) {
      if (isSelectable(schema, model, property))
        return true; 
    } 
    return false;
  }
  
  public boolean isSelectable(String model, String property, String nodeId) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    assert !StringUtils.isEmpty(nodeId);
    QuerySchema schema = this._schemaBynodeId.get(nodeId);
    if (schema == null)
      return false; 
    return isSelectable(schema, model, property);
  }
  
  public boolean isFilterableAnywhere(String model, String property) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    for (QuerySchema schema : this._schemaBynodeId.values()) {
      if (isFilterable(schema, model, property))
        return true; 
    } 
    return false;
  }
  
  public boolean isFilterableByUnsetAnywhere(String model, String property) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    for (QuerySchema schema : this._schemaBynodeId.values()) {
      if (isFilterableByUnset(schema, model, property))
        return true; 
    } 
    return false;
  }
  
  public boolean isFilterable(String model, String property, String nodeId) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    assert !StringUtils.isEmpty(nodeId);
    QuerySchema schema = this._schemaBynodeId.get(nodeId);
    if (schema == null)
      return false; 
    return isFilterable(schema, model, property);
  }
  
  public boolean isFilterableByUnset(String model, String property, String nodeId) {
    assert !StringUtils.isEmpty(model);
    assert !StringUtils.isEmpty(property);
    assert !StringUtils.isEmpty(nodeId);
    QuerySchema schema = this._schemaBynodeId.get(nodeId);
    if (schema == null)
      return false; 
    return isFilterableByUnset(schema, model, property);
  }
  
  public boolean isSortableAnywhere(String model, String property) {
    return isFilterableAnywhere(model, property);
  }
  
  public boolean isSortable(String model, String property, String nodeId) {
    return isFilterable(model, property, nodeId);
  }
  
  public boolean isModelSupported(String model, String nodeId) {
    QuerySchema schema = this._schemaBynodeId.get(nodeId);
    if (schema == null)
      return false; 
    return schema.getModels().containsKey(model);
  }
  
  private static boolean isSelectable(QuerySchema schema, String model, String property) {
    QuerySchema.ModelInfo modelInfo = schema.getModels().get(model);
    if (modelInfo == null)
      return false; 
    String rootproperty = UnqualifiedProperty.getRootProperty(property);
    return modelInfo.getProperties().containsKey(rootproperty);
  }
  
  private static boolean isFilterable(QuerySchema schema, String model, String property) {
    QuerySchema.ModelInfo modelInfo = schema.getModels().get(model);
    if (modelInfo == null)
      return false; 
    QuerySchema.PropertyInfo propertyInfo = modelInfo.getProperties().get(property);
    if (propertyInfo == null)
      return false; 
    return propertyInfo.getFilterable();
  }
  
  private static boolean isFilterableByUnset(QuerySchema schema, String model, String property) {
    QuerySchema.ModelInfo modelInfo = schema.getModels().get(model);
    if (modelInfo == null)
      return false; 
    QuerySchema.PropertyInfo propertyInfo = modelInfo.getProperties().get(property);
    if (propertyInfo == null)
      return false; 
    return (propertyInfo.getFilterable() || propertyInfo.getFilterableByUnset());
  }
}
