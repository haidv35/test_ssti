package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.model.PredicateProperty;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class PredicatePropertyRepository implements PredicatePropertyLookup {
  private final Map<String, PredicatePropertyDescriptor> _descriptorByPredicateProperty;
  
  public PredicatePropertyRepository(Collection<Class<?>> registeredQueryModels) {
    this._descriptorByPredicateProperty = Collections.unmodifiableMap(
        registerPredicateProperties(registeredQueryModels));
  }
  
  public PredicatePropertyDescriptor getPredicatePropertyDescriptor(String property) {
    if (PropertyUtil.isSpecialProperty(property))
      return null; 
    return this._descriptorByPredicateProperty.get(property);
  }
  
  public QuerySchema addPredicateProps(QuerySchema schema) {
    QuerySchema predicatePropertySchema = buildPredicatePropertySchema(this._descriptorByPredicateProperty);
    return SchemaUtil.merge(predicatePropertySchema, schema);
  }
  
  public static List<PredicatePropertyDescriptor> collectPredicatePropertyDescriptors(Class<?> modelClass) {
    assert modelClass != null;
    List<PredicatePropertyDescriptor> predicateProperties = new ArrayList<>();
    for (Method method : modelClass.getMethods()) {
      if (method.isAnnotationPresent((Class)PredicateProperty.class)) {
        PredicatePropertyDescriptor descriptor;
        PredicateProperty predicateProperty = method.<PredicateProperty>getAnnotation(PredicateProperty.class);
        String predicatePropertyName = predicateProperty.value();
        try {
          descriptor = PredicatePropertyDescriptor.fromMethod(predicatePropertyName, method);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid predicate property method '%s' in class '%s'", new Object[] { method
                  
                  .getName(), modelClass.getCanonicalName() }), ex);
        } 
        predicateProperties.add(descriptor);
      } 
    } 
    return predicateProperties;
  }
  
  private static Map<String, PredicatePropertyDescriptor> registerPredicateProperties(Collection<Class<?>> modelClasses) {
    Validate.notNull(modelClasses);
    if (modelClasses.isEmpty())
      return Collections.emptyMap(); 
    Map<String, PredicatePropertyDescriptor> predicateProperties = new HashMap<>();
    for (Class<?> modelClass : modelClasses)
      registerPredicateProperties(modelClass, predicateProperties); 
    return predicateProperties;
  }
  
  private static void registerPredicateProperties(Class<?> modelClass, Map<String, PredicatePropertyDescriptor> predicateProperties) {
    Validate.notNull(modelClass);
    Validate.notNull(predicateProperties);
    List<PredicatePropertyDescriptor> predicatePropertiesDescriptors = collectPredicatePropertyDescriptors(modelClass);
    if (predicatePropertiesDescriptors.isEmpty())
      return; 
    for (PredicatePropertyDescriptor predicateProperty : predicatePropertiesDescriptors) {
      if (predicateProperties.containsKey(predicateProperty.getName()))
        throw new IllegalArgumentException(String.format("Duplicate predicate property registration '%s' detected!", new Object[] { predicateProperty
                
                .getName() })); 
      predicateProperties.put(predicateProperty.getName(), predicateProperty);
    } 
  }
  
  private static QuerySchema buildPredicatePropertySchema(Map<String, PredicatePropertyDescriptor> predicateProperties) {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByName = new HashMap<>(predicateProperties.size());
    for (String property : predicateProperties.keySet())
      propertyInfoByName.put(property, QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN)); 
    return QuerySchema.forProperties(propertyInfoByName);
  }
}
