package com.vmware.cis.data.internal.provider.util;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.UnqualifiedProperty;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class SchemaUtil {
  public static QuerySchema union(Collection<QuerySchema> schemas) {
    Validate.noNullElements(schemas);
    Map<String, Map<String, QuerySchema.PropertyInfo>> infoByPropertyByModel = new HashMap<>();
    for (QuerySchema schema : schemas) {
      for (String modelName : schema.getModels().keySet()) {
        QuerySchema.ModelInfo modelInfo = schema.getModels().get(modelName);
        Map<String, QuerySchema.PropertyInfo> infoByProperty = infoByPropertyByModel.get(modelName);
        if (infoByProperty == null) {
          infoByProperty = new HashMap<>();
          infoByPropertyByModel.put(modelName, infoByProperty);
        } 
        for (String propertyName : modelInfo.getProperties().keySet()) {
          QuerySchema.PropertyInfo oldPropertyInfo = infoByProperty.get(propertyName);
          QuerySchema.PropertyInfo newPropertyInfo = modelInfo.getProperties().get(propertyName);
          if (oldPropertyInfo == null) {
            infoByProperty.put(propertyName, newPropertyInfo);
            continue;
          } 
          if (oldPropertyInfo.equals(newPropertyInfo))
            continue; 
          if (oldPropertyInfo.getFilterable() && newPropertyInfo.getFilterable()) {
            String msg = String.format("Cannot merge query schemas because property '%s' has different property types: '%s', '%s'", new Object[] { propertyName, oldPropertyInfo
                  
                  .getType(), newPropertyInfo.getType() });
            throw new IllegalArgumentException(msg);
          } 
          if (newPropertyInfo.getFilterable())
            infoByProperty.put(propertyName, newPropertyInfo); 
        } 
      } 
    } 
    Map<String, QuerySchema.ModelInfo> infoByModel = new HashMap<>();
    for (String modelName : infoByPropertyByModel.keySet()) {
      Map<String, QuerySchema.PropertyInfo> infoByProperty = infoByPropertyByModel.get(modelName);
      QuerySchema.ModelInfo modelInfo = new QuerySchema.ModelInfo(infoByProperty);
      infoByModel.put(modelName, modelInfo);
    } 
    return QuerySchema.forModels(infoByModel);
  }
  
  public static QuerySchema merge(QuerySchema first, QuerySchema second) {
    Validate.notNull(first, "first schema");
    Validate.notNull(second, "second schema");
    Map<String, QuerySchema.ModelInfo> modelInfoByModelName = new HashMap<>(first.getModels());
    for (Map.Entry<String, QuerySchema.ModelInfo> e : second.getModels().entrySet()) {
      QuerySchema.ModelInfo modelInfo = modelInfoByModelName.get(e.getKey());
      QuerySchema.ModelInfo mergedModelInfo = e.getValue();
      if (modelInfo != null)
        mergedModelInfo = QuerySchema.ModelInfo.merge(Arrays.asList(new QuerySchema.ModelInfo[] { modelInfo, e
                .getValue() })); 
      modelInfoByModelName.put(e.getKey(), mergedModelInfo);
    } 
    return QuerySchema.forModels(modelInfoByModelName);
  }
  
  public static QuerySchema.PropertyInfo getPropertyInfoForQualifiedName(QuerySchema schema, String qualifiedName) {
    if (PropertyUtil.isModelKey(qualifiedName))
      return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID); 
    if (PropertyUtil.isType(qualifiedName))
      return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING); 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(qualifiedName);
    return getPropertyInfoForQualifiedName(schema, qualifiedProperty);
  }
  
  public static QuerySchema.PropertyInfo getPropertyInfoForQualifiedName(QuerySchema schema, QualifiedProperty qualifiedProperty) {
    Validate.notNull(schema);
    Validate.notNull(qualifiedProperty);
    Map<String, QuerySchema.ModelInfo> models = schema.getModels();
    QuerySchema.ModelInfo model = models.get(qualifiedProperty.getResourceModel());
    if (model == null)
      return null; 
    String simpleProperty = qualifiedProperty.getSimpleProperty();
    if (PropertyUtil.isModelKey(simpleProperty))
      return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID); 
    if (PropertyUtil.isType(simpleProperty))
      return QuerySchema.PropertyInfo.forNonFilterableProperty(); 
    Map<String, QuerySchema.PropertyInfo> properties = model.getProperties();
    QuerySchema.PropertyInfo propertyInfo = properties.get(simpleProperty);
    if (propertyInfo != null)
      return propertyInfo; 
    String rootProperty = UnqualifiedProperty.getRootProperty(simpleProperty);
    if (properties.containsKey(rootProperty))
      return QuerySchema.PropertyInfo.forNonFilterableProperty(); 
    return null;
  }
}
