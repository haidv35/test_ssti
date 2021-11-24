package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

public class QuerySchemaUtil {
  public static QuerySchema buildQuerySchemaFromModelInfo(String resourceModelName, QuerySchema.ModelInfo modelInfo) {
    Map<String, QuerySchema.ModelInfo> modelNameToModelInfo = new HashMap<>();
    modelNameToModelInfo.put(resourceModelName, modelInfo);
    QuerySchema querySchema = QuerySchema.forModels(modelNameToModelInfo);
    return querySchema;
  }
  
  public static QuerySchema resolveConflict(QuerySchema querySchema, DataProvider... conflictingDataProviders) {
    QuerySchema nonConflictQuerySchema = querySchema;
    for (DataProvider dataProvider : conflictingDataProviders)
      nonConflictQuerySchema = getDifference(nonConflictQuerySchema, dataProvider
          
          .getSchema()); 
    return nonConflictQuerySchema;
  }
  
  public static QuerySchema getDifference(QuerySchema schema1, QuerySchema schema2) {
    Map<String, QuerySchema.ModelInfo> schema1Models = schema1.getModels();
    Map<String, QuerySchema.ModelInfo> schema2Models = schema2.getModels();
    Map<String, QuerySchema.ModelInfo> difference = new HashMap<>(schema1Models);
    for (String modelKey : schema2Models.keySet())
      difference.remove(modelKey); 
    return QuerySchema.forModels(difference);
  }
  
  public static List<String> getSupportedQueryPropertyNames(String queryResourceModelType, List<String> queryPropertyNames, QuerySchema supportedQuerySchema) {
    QuerySchema.ModelInfo supportedQueryModelInfo = (QuerySchema.ModelInfo)supportedQuerySchema.getModels().get(queryResourceModelType);
    if (supportedQueryModelInfo == null)
      return new ArrayList<>(); 
    List<String> supportedQueryPropertyNames = new ArrayList<>();
    for (String queryPropertyName : queryPropertyNames) {
      String nonQualifiedQueryPropertyName = getActualPropertyName(queryPropertyName);
      if (supportedQueryModelInfo.getProperties().containsKey(nonQualifiedQueryPropertyName))
        supportedQueryPropertyNames.add(queryPropertyName); 
    } 
    return supportedQueryPropertyNames;
  }
  
  public static List<String> getNonQualifiedPropertyNames(List<String> queryProperties) {
    List<String> nonQualifiedPropertyNames = new ArrayList<>();
    for (String queryPropertyName : queryProperties) {
      String nonQualifiedQueryPropertyName = getActualPropertyName(queryPropertyName);
      nonQualifiedPropertyNames.add(nonQualifiedQueryPropertyName);
    } 
    return nonQualifiedPropertyNames;
  }
  
  public static String getActualPropertyName(String queryProperty) {
    if (queryProperty.startsWith("@"))
      return queryProperty; 
    int index = queryProperty.indexOf("/");
    if (index != -1)
      return queryProperty.substring(index + 1); 
    return queryProperty;
  }
  
  public static String qualifyProperty(String resourceModelName, String propertyName) {
    return resourceModelName + "/" + propertyName;
  }
  
  public static boolean isQueryPropertyModelKey(String queryProperty) {
    Validate.notEmpty(queryProperty);
    return queryProperty.endsWith("@modelKey");
  }
  
  public static boolean isQueryPropertySpecialProperty(String property) {
    Validate.notEmpty(property);
    return (property.equals("@type") || property.equals("@modelKey"));
  }
  
  public static Map<String, QuerySchema.ModelInfo> addPropertiesToModelInfos(Map<String, QuerySchema.ModelInfo> originalQuerySchemaModels, Map<String, QuerySchema.PropertyInfo> propertyNameToPropertyInfo) {
    Map<String, QuerySchema.ModelInfo> extendedQuerySchemaModels = new HashMap<>(originalQuerySchemaModels.size());
    QuerySchema.ModelInfo additionalPropertiesModelInfo = new QuerySchema.ModelInfo(propertyNameToPropertyInfo);
    for (Map.Entry<String, QuerySchema.ModelInfo> querySchemaModel : originalQuerySchemaModels.entrySet()) {
      String modelName = querySchemaModel.getKey();
      QuerySchema.ModelInfo currentModelInfo = querySchemaModel.getValue();
      QuerySchema.ModelInfo extendedModelInfo = QuerySchema.ModelInfo.merge(
          Arrays.asList(new QuerySchema.ModelInfo[] { currentModelInfo, additionalPropertiesModelInfo }));
      extendedQuerySchemaModels.put(modelName, extendedModelInfo);
    } 
    return extendedQuerySchemaModels;
  }
}
