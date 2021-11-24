package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.ReflectionUtil;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.QueryModel;
import com.vmware.cis.data.model.Relationship;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelatedPropertyRepository implements RelatedPropertyLookup {
  private static final Logger _logger = LoggerFactory.getLogger(RelatedPropertyRepository.class);
  
  private final Map<String, RelatedPropertyDescriptor> _descriptorByRelatedProperty;
  
  public RelatedPropertyRepository(Collection<Class<?>> registeredQueryModels) {
    this(registerRelatedProperties(registeredQueryModels));
  }
  
  public RelatedPropertyRepository(Map<String, RelatedPropertyDescriptor> descriptorByRelatedProperty) {
    assert descriptorByRelatedProperty != null;
    this._descriptorByRelatedProperty = Collections.unmodifiableMap(descriptorByRelatedProperty);
  }
  
  public RelatedPropertyDescriptor getRelatedPropertyDescriptor(String modelProperty) {
    return this._descriptorByRelatedProperty.get(modelProperty);
  }
  
  public Map<String, RelatedPropertyDescriptor> getRelatedPropertyDescriptors(List<String> relatedProperties) {
    if (CollectionUtils.isEmpty(relatedProperties))
      return Collections.emptyMap(); 
    Map<String, RelatedPropertyDescriptor> relatedPropertyDescriptorsByName = new LinkedHashMap<>();
    for (String relatedProperty : relatedProperties) {
      RelatedPropertyDescriptor relatedPropertyDescriptor = this._descriptorByRelatedProperty.get(relatedProperty);
      if (relatedPropertyDescriptor != null)
        relatedPropertyDescriptorsByName.put(relatedProperty, relatedPropertyDescriptor); 
    } 
    return Collections.unmodifiableMap(relatedPropertyDescriptorsByName);
  }
  
  public QuerySchema addRelatedProps(QuerySchema schema) {
    QuerySchema relatedPropertySchema = calculateRelatedPropertySchema(schema);
    return SchemaUtil.merge(relatedPropertySchema, schema);
  }
  
  private QuerySchema calculateRelatedPropertySchema(QuerySchema schema) {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName = new HashMap<>(this._descriptorByRelatedProperty.size());
    for (String property : this._descriptorByRelatedProperty.keySet()) {
      RelatedPropertyDescriptor descriptor = this._descriptorByRelatedProperty.get(property);
      try {
        registerRelatedPropertyInfo(propertyInfoByQualifiedName, schema, descriptor);
      } catch (Exception e) {
        _logger.debug("Could not register custom defined related property " + property, e);
      } 
    } 
    return QuerySchema.forProperties(propertyInfoByQualifiedName);
  }
  
  public static Map<Field, RelatedPropertyDescriptor> collectRelatedPropertyDescriptorByField(String resourceModel, Class<?> modelClass, boolean allowModelOverwrite) {
    assert !StringUtils.isEmpty(resourceModel);
    assert modelClass != null;
    Map<Field, RelatedPropertyDescriptor> descriptorsByField = new HashMap<>();
    Class<?> baseClass = modelClass.getSuperclass();
    if (baseClass != null && !baseClass.equals(Object.class))
      collectDescriptorsByField(resourceModel, modelClass, 
          
          ReflectionUtil.getAllFields(baseClass), allowModelOverwrite, descriptorsByField); 
    collectDescriptorsByField(resourceModel, modelClass, modelClass
        
        .getDeclaredFields(), false, descriptorsByField);
    return descriptorsByField;
  }
  
  private static void collectDescriptorsByField(String resourceModel, Class<?> modelClass, Field[] fields, boolean allowModelOverwrite, Map<Field, RelatedPropertyDescriptor> descriptorsByField) {
    for (Field field : fields) {
      if (field.isAnnotationPresent((Class)Property.class) && (
        !field.isAnnotationPresent((Class)Property.class) || field
        .isAnnotationPresent((Class)Relationship.class))) {
        RelatedPropertyDescriptor descriptor;
        try {
          descriptor = RelatedPropertyDescriptor.fromField(resourceModel, field, allowModelOverwrite);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid related property declaration '%s' in class '%s'", new Object[] { field
                  
                  .getName(), modelClass.getCanonicalName() }), ex);
        } 
        descriptorsByField.put(field, descriptor);
      } 
    } 
  }
  
  private void registerRelatedPropertyInfo(Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName, QuerySchema schema, RelatedPropertyDescriptor descriptor) {
    String relatedPropertyName = descriptor.getName();
    boolean isRelatedPropertyFilterable = true;
    for (RelationshipDescriptor relation : descriptor.getRelationships()) {
      String relationName = relation.getName();
      QuerySchema.PropertyInfo relatedHopPropertyInfo = getRelatedHopPropertyInfo(schema, relationName);
      if (relatedHopPropertyInfo == null) {
        isRelatedPropertyFilterable = true;
        break;
      } 
      if (!relation.isDefinedByTarget())
        isRelatedPropertyFilterable &= relatedHopPropertyInfo.getFilterable(); 
    } 
    String targetPropertyName = descriptor.getTargetName();
    QuerySchema.PropertyInfo targetPropertyInfo = getTargetPropertyInfo(schema, targetPropertyName);
    if (targetPropertyInfo == null) {
      _logger.debug(String.format("Could not register custom defined related property '%s' as  it targets a non-existing property '%s'.", new Object[] { relatedPropertyName, targetPropertyName }));
      return;
    } 
    isRelatedPropertyFilterable &= targetPropertyInfo.getFilterable();
    QuerySchema.PropertyInfo propertyInfo = null;
    if (isRelatedPropertyFilterable) {
      propertyInfo = QuerySchema.PropertyInfo.forFilterableProperty(targetPropertyInfo.getType());
    } else {
      propertyInfo = QuerySchema.PropertyInfo.forNonFilterableProperty();
    } 
    propertyInfoByQualifiedName.put(relatedPropertyName, propertyInfo);
  }
  
  private static QuerySchema.PropertyInfo getRelatedHopPropertyInfo(QuerySchema schema, String relationName) {
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(relationName);
    QuerySchema.PropertyInfo relatedHopPropertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(schema, qualifiedProperty);
    return relatedHopPropertyInfo;
  }
  
  private static QuerySchema.PropertyInfo getTargetPropertyInfo(QuerySchema schema, String targetPropertyName) {
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(targetPropertyName);
    QuerySchema.PropertyInfo targetPropertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(schema, qualifiedProperty);
    return targetPropertyInfo;
  }
  
  private static Map<String, RelatedPropertyDescriptor> registerRelatedProperties(Collection<Class<?>> modelClasses) {
    Validate.notNull(modelClasses);
    if (modelClasses.isEmpty())
      return Collections.emptyMap(); 
    Map<String, RelatedPropertyDescriptor> relatedProperties = new HashMap<>();
    for (Class<?> modelClass : modelClasses)
      registerRelatedProperties(modelClass, relatedProperties); 
    return relatedProperties;
  }
  
  private static void registerRelatedProperties(Class<?> modelClass, Map<String, RelatedPropertyDescriptor> relatedProperties) {
    Validate.notNull(modelClass);
    Validate.notNull(relatedProperties);
    Collection<RelatedPropertyDescriptor> relatedPropertiesDescriptors = collectRelatedPropertyDescriptors(modelClass);
    if (relatedPropertiesDescriptors.isEmpty())
      return; 
    for (RelatedPropertyDescriptor relatedProperty : relatedPropertiesDescriptors) {
      if (relatedProperties.containsKey(relatedProperty.getName()))
        throw new IllegalArgumentException(String.format("Duplicate related property registration '%s' detected!", new Object[] { relatedProperty
                
                .getName() })); 
      relatedProperties.put(relatedProperty.getName(), relatedProperty);
    } 
  }
  
  private static Collection<RelatedPropertyDescriptor> collectRelatedPropertyDescriptors(Class<?> modelClass) {
    assert modelClass != null;
    QueryModel modelDefinition = modelClass.<QueryModel>getAnnotation(QueryModel.class);
    Validate.notNull(modelDefinition, "The provided class is not annotated with @QueryModel annotation.");
    String resourceModel = modelDefinition.value();
    return collectRelatedPropertyDescriptorByField(resourceModel, modelClass, false)
      .values();
  }
}
