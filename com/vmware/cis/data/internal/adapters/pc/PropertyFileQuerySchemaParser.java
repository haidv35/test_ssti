package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.api.QuerySchema;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

final class PropertyFileQuerySchemaParser {
  private static final String SCHEMA_FILE = "/metadata/property_collector/property_collector_query_schema.properties";
  
  private static final String TYPE_NON_FILTERABLE = "NON_FILTERABLE";
  
  static QuerySchema parseSchema() {
    try (InputStream in = PropertyFileQuerySchemaParser.class.getResourceAsStream("/metadata/property_collector/property_collector_query_schema.properties")) {
      Properties prop = new Properties();
      prop.load(in);
      return parseProperties(prop);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static QuerySchema parseProperties(Properties properties) {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName = new LinkedHashMap<>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      String property = (String)entry.getKey();
      String type = (String)entry.getValue();
      propertyInfoByQualifiedName.put(property, getPropertyInfo(type));
    } 
    return QuerySchema.forProperties(propertyInfoByQualifiedName);
  }
  
  private static QuerySchema.PropertyInfo getPropertyInfo(String type) {
    if ("NON_FILTERABLE".equals(type))
      return QuerySchema.PropertyInfo.forNonFilterableProperty(); 
    return QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.valueOf(type));
  }
}
