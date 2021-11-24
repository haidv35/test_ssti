package com.vmware.ph.phservice.provider.fcd.collector.schema;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FcdCustomObjectSchemaGenerator {
  private static final Log log = LogFactory.getLog(FcdCustomObjectSchemaGenerator.class);
  
  private final Set<Class<?>> _schemaClasses;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final VmodlVersion _vmodlVersion;
  
  public FcdCustomObjectSchemaGenerator(VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion, Set<Class<?>> schemaClasses) {
    this._schemaClasses = schemaClasses;
    this._vmodlTypeMap = vmodlTypeMap;
    this._vmodlVersion = vmodlVersion;
  }
  
  public Map<String, QuerySchema.ModelInfo> getQuerySchemaModel() {
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>();
    for (Class<?> clazz : this._schemaClasses)
      models.putAll(getModel(clazz.getName())); 
    return models;
  }
  
  private Map<String, QuerySchema.ModelInfo> getModel(String modelName) {
    String typeName = getPropertyName(modelName);
    List<String> propertyNames = getPropertyNamesForCustomClass(modelName);
    Map<String, QuerySchema.ModelInfo> model = new HashMap<>();
    model.put(typeName, VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(propertyNames));
    return model;
  }
  
  private List<String> getPropertyNamesForCustomClass(String modelName) {
    try {
      Class<?> clazz = Class.forName(modelName);
      return getPropertyNamesInClass(clazz);
    } catch (ClassNotFoundException e) {
      if (log.isTraceEnabled())
        log.trace("Class " + modelName + " not found.", e); 
      return Collections.emptyList();
    } 
  }
  
  private List<String> getPropertyNamesInClass(Class<?> clazz) {
    Set<String> propertyNames = new HashSet<>();
    for (Field field : clazz.getFields())
      propertyNames.add(getPropertyName(field.toString())); 
    for (VmodlType vmodlType : this._vmodlTypeMap.getVmodlTypes()) {
      if (vmodlType.getTypeClass().isAssignableFrom(clazz))
        propertyNames.addAll(VmodlUtil.getProperties(vmodlType, 4, this._vmodlTypeMap, this._vmodlVersion)); 
    } 
    return new ArrayList<>(propertyNames);
  }
  
  private static String getPropertyName(String fieldName) {
    String[] arr = fieldName.split(Pattern.quote("."));
    if (arr != null && arr.length > 0)
      return arr[arr.length - 1]; 
    return null;
  }
}
