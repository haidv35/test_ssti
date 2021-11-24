package com.vmware.cis.data.api;

import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public final class QuerySchema {
  public static final QuerySchema EMPTY_SCHEMA = forModels(
      Collections.emptyMap());
  
  private final Map<String, ModelInfo> _models;
  
  public static final class ModelInfo {
    private final Map<String, QuerySchema.PropertyInfo> _properties;
    
    public static ModelInfo merge(Collection<ModelInfo> infosForSameModel) {
      Validate.notNull(infosForSameModel);
      Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>();
      for (ModelInfo modelInfo : infosForSameModel) {
        for (Map.Entry<String, QuerySchema.PropertyInfo> e : modelInfo.getProperties()
          .entrySet())
          properties.put(e.getKey(), e.getValue()); 
      } 
      return new ModelInfo(properties);
    }
    
    public ModelInfo(Map<String, QuerySchema.PropertyInfo> propertyInfoByName) {
      Validate.notNull(propertyInfoByName, "properties");
      Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>(propertyInfoByName);
      properties.put("@modelKey", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
      properties.put("@type", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
      this._properties = Collections.unmodifiableMap(properties);
    }
    
    public Map<String, QuerySchema.PropertyInfo> getProperties() {
      return this._properties;
    }
    
    public boolean equals(Object obj) {
      if (this == obj)
        return true; 
      if (!(obj instanceof ModelInfo))
        return false; 
      ModelInfo other = (ModelInfo)obj;
      return this._properties.equals(other._properties);
    }
    
    public int hashCode() {
      return this._properties.hashCode();
    }
    
    public String toString() {
      return "ModelInfo [_properties=" + this._properties + "]";
    }
  }
  
  public static final class PropertyInfo {
    private static final PropertyInfo FILTERABLE_STRING = new PropertyInfo(true, false, QuerySchema.PropertyType.STRING);
    
    private static final PropertyInfo FILTERABLE_BYTE = new PropertyInfo(true, false, QuerySchema.PropertyType.BYTE);
    
    private static final PropertyInfo FILTERABLE_SHORT = new PropertyInfo(true, false, QuerySchema.PropertyType.SHORT);
    
    private static final PropertyInfo FILTERABLE_INT = new PropertyInfo(true, false, QuerySchema.PropertyType.INT);
    
    private static final PropertyInfo FILTERABLE_LONG = new PropertyInfo(true, false, QuerySchema.PropertyType.LONG);
    
    private static final PropertyInfo FILTERABLE_FLOAT = new PropertyInfo(true, false, QuerySchema.PropertyType.FLOAT);
    
    private static final PropertyInfo FILTERABLE_DOUBLE = new PropertyInfo(true, false, QuerySchema.PropertyType.DOUBLE);
    
    private static final PropertyInfo FILTERABLE_BOOLEAN = new PropertyInfo(true, false, QuerySchema.PropertyType.BOOLEAN);
    
    private static final PropertyInfo FILTERABLE_ID = new PropertyInfo(true, false, QuerySchema.PropertyType.ID);
    
    private static final PropertyInfo FILTERABLE_ENUM = new PropertyInfo(true, false, QuerySchema.PropertyType.ENUM);
    
    private static final PropertyInfo FILTERABLE_BY_UNSET = new PropertyInfo(false, true, null);
    
    private static final PropertyInfo NONFILTERABLE = new PropertyInfo(false, false, null);
    
    private final boolean _filterable;
    
    private final boolean _filterableByUnset;
    
    private final QuerySchema.PropertyType _type;
    
    private PropertyInfo(boolean filterable, boolean filterableByUnset, QuerySchema.PropertyType type) {
      assert !((filterableByUnset && filterable) ? 1 : 0);
      assert !((filterable && type == null) ? 1 : 0);
      this._filterable = filterable;
      this._filterableByUnset = filterableByUnset;
      this._type = type;
    }
    
    public static PropertyInfo forFilterableByUnsetProperty() {
      return FILTERABLE_BY_UNSET;
    }
    
    public static PropertyInfo forNonFilterableProperty() {
      return NONFILTERABLE;
    }
    
    public static PropertyInfo forFilterableProperty(QuerySchema.PropertyType type) {
      Validate.notNull(type, "Type is required for filterable properties.");
      switch (type) {
        case STRING:
          return FILTERABLE_STRING;
        case BOOLEAN:
          return FILTERABLE_BOOLEAN;
        case BYTE:
          return FILTERABLE_BYTE;
        case SHORT:
          return FILTERABLE_SHORT;
        case INT:
          return FILTERABLE_INT;
        case LONG:
          return FILTERABLE_LONG;
        case FLOAT:
          return FILTERABLE_FLOAT;
        case DOUBLE:
          return FILTERABLE_DOUBLE;
        case ID:
          return FILTERABLE_ID;
        case ENUM:
          return FILTERABLE_ENUM;
      } 
      throw new IllegalArgumentException("The passed type is not supported.");
    }
    
    public boolean getFilterable() {
      return this._filterable;
    }
    
    public boolean getFilterableByUnset() {
      return this._filterableByUnset;
    }
    
    public QuerySchema.PropertyType getType() {
      return this._type;
    }
    
    public boolean equals(Object obj) {
      if (this == obj)
        return true; 
      if (!(obj instanceof PropertyInfo))
        return false; 
      PropertyInfo other = (PropertyInfo)obj;
      return (this._filterable == other._filterable && 
        ObjectUtils.equals(this._type, other._type));
    }
    
    public int hashCode() {
      int hash = 23;
      hash = 31 * hash + (this._filterable ? 1 : 0);
      hash = 31 * hash + ((this._type != null) ? this._type.hashCode() : 0);
      return hash;
    }
    
    public String toString() {
      return "PropertyInfo [_filterable=" + this._filterable + ", _type=" + this._type + "]";
    }
  }
  
  public enum PropertyType {
    STRING, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, ID, ENUM;
    
    public String toString() {
      return name().toLowerCase();
    }
  }
  
  private QuerySchema(Map<String, ModelInfo> models) {
    assert models != null;
    this._models = Collections.unmodifiableMap(models);
  }
  
  public static QuerySchema forModels(Map<String, ModelInfo> modelInfoByModelName) {
    Validate.notNull(modelInfoByModelName);
    return new QuerySchema(modelInfoByModelName);
  }
  
  public static QuerySchema forProperties(Map<String, PropertyInfo> propertyInfoByQualifiedName) {
    Validate.notNull(propertyInfoByQualifiedName);
    Map<String, Map<String, PropertyInfo>> propertyInfoByNameByModel = new HashMap<>();
    for (String qualifiedName : propertyInfoByQualifiedName.keySet()) {
      QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(qualifiedName);
      String modelName = qualifiedProperty.getResourceModel();
      Map<String, PropertyInfo> propertyInfoByName = propertyInfoByNameByModel.get(modelName);
      if (propertyInfoByName == null) {
        propertyInfoByName = new HashMap<>();
        propertyInfoByNameByModel.put(modelName, propertyInfoByName);
      } 
      PropertyInfo propertyInfo = propertyInfoByQualifiedName.get(qualifiedName);
      propertyInfoByName.put(qualifiedProperty.getSimpleProperty(), propertyInfo);
    } 
    return toSchema(propertyInfoByNameByModel);
  }
  
  private static QuerySchema toSchema(Map<String, Map<String, PropertyInfo>> propertyInfoByNameByModel) {
    Map<String, ModelInfo> modelInfoByModelName = new HashMap<>();
    for (String modelName : propertyInfoByNameByModel.keySet()) {
      Map<String, PropertyInfo> properties = propertyInfoByNameByModel.get(modelName);
      modelInfoByModelName.put(modelName, new ModelInfo(properties));
    } 
    return new QuerySchema(modelInfoByModelName);
  }
  
  public Map<String, ModelInfo> getModels() {
    return this._models;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof QuerySchema))
      return false; 
    QuerySchema other = (QuerySchema)obj;
    return this._models.equals(other._models);
  }
  
  public int hashCode() {
    return this._models.hashCode();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(SystemUtils.LINE_SEPARATOR);
    sb.append("DataProviderSchema [");
    SortedSet<String> sortedModelNames = new TreeSet<>(this._models.keySet());
    Iterator<String> modelIterator = sortedModelNames.iterator();
    while (modelIterator.hasNext()) {
      String modelName = modelIterator.next();
      ModelInfo modelInfo = this._models.get(modelName);
      Map<String, PropertyInfo> properties = modelInfo.getProperties();
      SortedSet<String> sortedProperties = new TreeSet<>(properties.keySet());
      Iterator<String> propertyIterator = sortedProperties.iterator();
      sb.append(SystemUtils.LINE_SEPARATOR + "   ");
      sb.append(modelName + " [");
      while (propertyIterator.hasNext()) {
        String propertyName = propertyIterator.next();
        PropertyInfo propertyInfo = properties.get(propertyName);
        sb.append(SystemUtils.LINE_SEPARATOR + "      ");
        sb.append(propertyName);
        if (propertyInfo.getFilterable()) {
          sb.append(" [filterable, ");
          sb.append(propertyInfo.getType() + "]");
        } else if (propertyInfo.getFilterableByUnset()) {
          sb.append(" [filterableByUnset]");
        } 
        if (propertyIterator.hasNext())
          sb.append(","); 
      } 
      sb.append(SystemUtils.LINE_SEPARATOR + "   ]");
      if (modelIterator.hasNext())
        sb.append("," + SystemUtils.LINE_SEPARATOR); 
    } 
    sb.append(SystemUtils.LINE_SEPARATOR + "]");
    return sb.toString();
  }
}
