package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.vim.binding.cis.data.provider.schema.PropertyInfo;
import com.vmware.vim.binding.cis.data.provider.schema.ResourceModelInfo;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VmomiDataProviderSchemaConverter {
  private static final Logger _logger = LoggerFactory.getLogger(VmomiDataProviderSchemaConverter.class);
  
  public static QuerySchema convertSchema(ResourceModelInfo[] vmomiModels) {
    Validate.notEmpty((Object[])vmomiModels);
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>(vmomiModels.length);
    for (ResourceModelInfo vmomiModel : vmomiModels)
      models.put(vmomiModel.getName(), convertModel(vmomiModel)); 
    return QuerySchema.forModels(models);
  }
  
  private static QuerySchema.ModelInfo convertModel(ResourceModelInfo vmomiModel) {
    Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>((vmomiModel.getProperties()).length);
    for (PropertyInfo propertyInfo : vmomiModel.getProperties()) {
      String modelName = vmomiModel.getName();
      String propertyName = propertyInfo.getName();
      try {
        properties.put(propertyName, convertProperty(modelName, propertyInfo));
      } catch (Exception e) {
        String msg = String.format("There is an error in the registration of %s/%s.", new Object[] { modelName, propertyName });
        _logger.error(msg, e);
      } 
    } 
    return new QuerySchema.ModelInfo(properties);
  }
  
  private static QuerySchema.PropertyInfo convertProperty(String modelName, PropertyInfo vmomiProperty) {
    boolean isPropertyFilterable = (vmomiProperty.getFilterable() != null && vmomiProperty.getFilterable().booleanValue());
    if (isPropertyFilterable)
      return convertFilterableProperty(modelName, vmomiProperty.getName(), vmomiProperty
          .getType()); 
    boolean isPropertyFilterableByUnset = (vmomiProperty.getFilterableByUnset() != null && vmomiProperty.getFilterableByUnset().booleanValue());
    if (isPropertyFilterableByUnset)
      return QuerySchema.PropertyInfo.forFilterableByUnsetProperty(); 
    return QuerySchema.PropertyInfo.forNonFilterableProperty();
  }
  
  private static QuerySchema.PropertyInfo convertFilterableProperty(String modelName, String propertyName, String vmomiPropertyType) {
    PropertyInfo.PropertyType propertyType;
    try {
      propertyType = PropertyInfo.PropertyType.valueOf(vmomiPropertyType);
    } catch (Exception e) {
      String str = String.format("Property %s/%s marked as filterable has invalid type: %s.", new Object[] { modelName, propertyName, vmomiPropertyType });
      throw new IllegalArgumentException(str);
    } 
    switch (propertyType) {
      case STRING:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING);
      case BYTE:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BYTE);
      case SHORT:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.SHORT);
      case INT:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT);
      case LONG:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.LONG);
      case FLOAT:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.FLOAT);
      case DOUBLE:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.DOUBLE);
      case BOOLEAN:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN);
      case MOREF:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID);
      case ENUM:
        return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ENUM);
    } 
    String msg = String.format("Property %s/%s marked as filterable contains type that doesn't have a core equivalent: %s.", new Object[] { modelName, propertyName, propertyType });
    throw new IllegalArgumentException(msg);
  }
}
