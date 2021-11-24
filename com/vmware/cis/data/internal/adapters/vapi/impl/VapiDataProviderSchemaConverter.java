package com.vmware.cis.data.internal.adapters.vapi.impl;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.provider.metadata.vapi.SchemaTypes;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VapiDataProviderSchemaConverter {
  private static final Logger _logger = LoggerFactory.getLogger(VapiDataProviderSchemaConverter.class);
  
  public static QuerySchema convertSchema(SchemaTypes.SchemaInfo schemaInfo) {
    Validate.notNull(schemaInfo);
    Map<String, SchemaTypes.ModelInfo> vapiModels = schemaInfo.getModels();
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>(vapiModels.size());
    for (String modelName : vapiModels.keySet()) {
      SchemaTypes.ModelInfo vapiModel = vapiModels.get(modelName);
      models.put(modelName, convertModelInfo(modelName, vapiModel));
    } 
    return QuerySchema.forModels(models);
  }
  
  private static QuerySchema.ModelInfo convertModelInfo(String modelName, SchemaTypes.ModelInfo vapiModel) {
    Map<String, SchemaTypes.PropertyInfo> vapiProperties = vapiModel.getProperties();
    Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>(vapiProperties.size());
    for (String propertyName : vapiProperties.keySet()) {
      SchemaTypes.PropertyInfo vapiProperty = vapiProperties.get(propertyName);
      try {
        properties.put(propertyName, convertProperty(modelName, propertyName, vapiProperty));
      } catch (Exception e) {
        String msg = String.format("There is an error in the registration of %s/%s.", new Object[] { modelName, propertyName });
        _logger.error(msg, e);
      } 
    } 
    return new QuerySchema.ModelInfo(properties);
  }
  
  private static QuerySchema.PropertyInfo convertProperty(String modelName, String propertyName, SchemaTypes.PropertyInfo vapiProperty) {
    boolean isPropertyFilterable = (vapiProperty.getFilterable() != null && vapiProperty.getFilterable().booleanValue());
    if (isPropertyFilterable)
      return convertFilterableProperty(modelName, propertyName, vapiProperty.getType()); 
    boolean isPropertyFilterableByUnset = (vapiProperty.getFilterableByUnset() != null && vapiProperty.getFilterableByUnset().booleanValue());
    if (isPropertyFilterableByUnset)
      return QuerySchema.PropertyInfo.forFilterableByUnsetProperty(); 
    return QuerySchema.PropertyInfo.forNonFilterableProperty();
  }
  
  private static QuerySchema.PropertyInfo convertFilterableProperty(String modelName, String propertyName, SchemaTypes.PropertyInfo.PropertyType propertyType) {
    if (propertyType == null) {
      String str = String.format("Property %s/%s marked as filterable has invalid type: %s.", new Object[] { modelName, propertyName, propertyType });
      throw new IllegalArgumentException(str);
    } 
    switch (propertyType.getEnumValue()) {
      case STRING:
        return fixFilterableString(modelName, propertyName);
      case LONG:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG);
      case DOUBLE:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.DOUBLE);
      case BOOLEAN:
        return fixFilterableBoolean(modelName, propertyName);
      case ID:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID);
      case ENUM:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM);
    } 
    String msg = String.format("Property %s/%s marked as filterable contains type that doesn't have a core equivalent: %s.", new Object[] { modelName, propertyName, propertyType });
    throw new IllegalArgumentException(msg);
  }
  
  private static QuerySchema.PropertyInfo fixFilterableString(String modelName, String propertyName) {
    if ("com.vmware.content.LibraryModel".equals(modelName) && "type".equals(propertyName))
      return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM); 
    return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING);
  }
  
  private static QuerySchema.PropertyInfo fixFilterableBoolean(String modelName, String propertyName) {
    if ("com.vmware.content.LibraryModel".equals(modelName) && ("passwordProtected"
      .equals(propertyName) || "isPublishedLibrary".equals(propertyName)))
      return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING); 
    return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN);
  }
}
