package com.vmware.cis.data.internal.provider.ext.alias;

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

public final class AliasPropertyRepository implements AliasPropertyLookup {
  private static final Logger _logger = LoggerFactory.getLogger(AliasPropertyRepository.class);
  
  private final Map<String, AliasPropertyDescriptor> _descriptorByAliasProperty;
  
  public AliasPropertyRepository(Collection<Class<?>> registeredQueryModels) {
    this(registerAliasProperties(registeredQueryModels));
  }
  
  public AliasPropertyRepository(Map<String, AliasPropertyDescriptor> descriptorByAliasProperty) {
    assert descriptorByAliasProperty != null;
    this._descriptorByAliasProperty = Collections.unmodifiableMap(descriptorByAliasProperty);
  }
  
  public AliasPropertyDescriptor getAliasPropertyDescriptor(String modelProperty) {
    return this._descriptorByAliasProperty.get(modelProperty);
  }
  
  public Map<String, AliasPropertyDescriptor> getAliasPropertyDescriptors(List<String> aliasProperties) {
    if (CollectionUtils.isEmpty(aliasProperties))
      return Collections.emptyMap(); 
    Map<String, AliasPropertyDescriptor> aliasPropertyDescriptorsByName = new LinkedHashMap<>();
    for (String aliasProperty : aliasProperties) {
      AliasPropertyDescriptor aliasPropertyDescriptor = this._descriptorByAliasProperty.get(aliasProperty);
      if (aliasPropertyDescriptor != null)
        aliasPropertyDescriptorsByName.put(aliasProperty, aliasPropertyDescriptor); 
    } 
    return Collections.unmodifiableMap(aliasPropertyDescriptorsByName);
  }
  
  public QuerySchema calculateAliasPropertySchema(QuerySchema schema) {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName = new HashMap<>(this._descriptorByAliasProperty.size());
    for (AliasPropertyDescriptor descriptor : this._descriptorByAliasProperty.values()) {
      try {
        registerAliasPropertyInfo(propertyInfoByQualifiedName, schema, descriptor);
      } catch (Exception e) {
        _logger.debug("Could not register the custom defined alias " + descriptor
            .getName(), e);
      } 
    } 
    return QuerySchema.forProperties(propertyInfoByQualifiedName);
  }
  
  public static Map<Field, AliasPropertyDescriptor> collectAliasPropertyDescriptorByField(String resourceModel, Class<?> modelClass, boolean allowModelOverwrite) {
    assert !StringUtils.isEmpty(resourceModel);
    assert modelClass != null;
    Map<Field, AliasPropertyDescriptor> descriptorsByField = new HashMap<>();
    Class<?> baseClass = modelClass.getSuperclass();
    if (baseClass != null && !baseClass.equals(Object.class))
      collectDescriptorsByField(resourceModel, modelClass, 
          
          ReflectionUtil.getAllFields(baseClass), allowModelOverwrite, descriptorsByField); 
    collectDescriptorsByField(resourceModel, modelClass, modelClass
        
        .getDeclaredFields(), false, descriptorsByField);
    return descriptorsByField;
  }
  
  private static void collectDescriptorsByField(String resourceModel, Class<?> modelClass, Field[] fields, boolean allowModelOverwrite, Map<Field, AliasPropertyDescriptor> descriptorsByField) {
    for (Field field : fields) {
      if (field.isAnnotationPresent((Class)Property.class) && 
        !field.isAnnotationPresent((Class)Relationship.class)) {
        AliasPropertyDescriptor descriptor;
        try {
          descriptor = AliasPropertyDescriptor.fromField(resourceModel, field, allowModelOverwrite);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid alias property declaration '%s' in class '%s'", new Object[] { field
                  
                  .getName(), modelClass.getCanonicalName() }), ex);
        } 
        descriptorsByField.put(field, descriptor);
      } 
    } 
  }
  
  private void registerAliasPropertyInfo(Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName, QuerySchema schema, AliasPropertyDescriptor descriptor) {
    String aliasPropertyName = descriptor.getName();
    String targetPropertyName = descriptor.getTargetName();
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(targetPropertyName);
    QuerySchema.PropertyInfo propertyInfo = SchemaUtil.getPropertyInfoForQualifiedName(schema, qualifiedProperty);
    if (propertyInfo != null) {
      propertyInfoByQualifiedName.put(aliasPropertyName, propertyInfo);
    } else {
      _logger.debug(String.format("Could not register the custom defined alias '%s' as  it targets a non-existing property '%s'.", new Object[] { aliasPropertyName, targetPropertyName }));
    } 
  }
  
  private static Map<String, AliasPropertyDescriptor> registerAliasProperties(Collection<Class<?>> modelClasses) {
    Validate.notNull(modelClasses);
    if (modelClasses.isEmpty())
      return Collections.emptyMap(); 
    Map<String, AliasPropertyDescriptor> aliasProperties = new HashMap<>();
    for (Class<?> modelClass : modelClasses)
      registerAliasProperties(modelClass, aliasProperties); 
    return aliasProperties;
  }
  
  private static void registerAliasProperties(Class<?> modelClass, Map<String, AliasPropertyDescriptor> aliasProperties) {
    Validate.notNull(modelClass);
    Validate.notNull(aliasProperties);
    Collection<AliasPropertyDescriptor> aliasPropertiesDescriptors = collectAliasPropertyDescriptors(modelClass);
    if (aliasPropertiesDescriptors.isEmpty())
      return; 
    for (AliasPropertyDescriptor aliasProperty : aliasPropertiesDescriptors) {
      if (aliasProperties.containsKey(aliasProperty.getName()))
        throw new IllegalArgumentException(String.format("Duplicate alias property registration '%s' detected!", new Object[] { aliasProperty
                
                .getName() })); 
      aliasProperties.put(aliasProperty.getName(), aliasProperty);
    } 
  }
  
  private static Collection<AliasPropertyDescriptor> collectAliasPropertyDescriptors(Class<?> modelClass) {
    assert modelClass != null;
    QueryModel modelDefinition = modelClass.<QueryModel>getAnnotation(QueryModel.class);
    Validate.notNull(modelDefinition, "The provided class is not annotated with @QueryModel annotation.");
    return collectAliasPropertyDescriptorByField(modelDefinition.value(), modelClass, false)
      .values();
  }
}
