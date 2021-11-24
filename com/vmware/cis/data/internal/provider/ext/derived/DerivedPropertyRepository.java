package com.vmware.cis.data.internal.provider.ext.derived;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.model.DerivedProperty;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class DerivedPropertyRepository implements DerivedPropertyLookup {
  private final Map<String, DerivedPropertyDescriptor> _descriptorByDerivedProperty;
  
  public DerivedPropertyRepository(Collection<Class<?>> registeredQueryModels) {
    this._descriptorByDerivedProperty = Collections.unmodifiableMap(
        registerDerivedProperties(registeredQueryModels));
  }
  
  public DerivedPropertyDescriptor getDerivedPropertyDescriptor(String property) {
    if (PropertyUtil.isSpecialProperty(property))
      return null; 
    return this._descriptorByDerivedProperty.get(property);
  }
  
  public QuerySchema addDerivedProps(QuerySchema schema) {
    QuerySchema derivedPropertySchema = buildDerivedPropertySchema(this._descriptorByDerivedProperty);
    return SchemaUtil.merge(derivedPropertySchema, schema);
  }
  
  private static Map<String, DerivedPropertyDescriptor> registerDerivedProperties(Collection<Class<?>> modelClasses) {
    Validate.notNull(modelClasses);
    if (modelClasses.isEmpty())
      return Collections.emptyMap(); 
    Map<String, DerivedPropertyDescriptor> derivedProperties = new HashMap<>();
    for (Class<?> modelClass : modelClasses)
      registerDerivedProperties(modelClass, derivedProperties); 
    return derivedProperties;
  }
  
  private static void registerDerivedProperties(Class<?> modelClass, Map<String, DerivedPropertyDescriptor> derivedProperties) {
    Validate.notNull(modelClass);
    Validate.notNull(derivedProperties);
    List<DerivedPropertyDescriptor> derivedPropertiesDescriptors = collectDerivedPropertyDescriptors(modelClass);
    if (derivedPropertiesDescriptors.isEmpty())
      return; 
    for (DerivedPropertyDescriptor derivedProperty : derivedPropertiesDescriptors) {
      if (derivedProperties.containsKey(derivedProperty.getName()))
        throw new IllegalArgumentException(String.format("Duplicate derived property registration '%s' detected!", new Object[] { derivedProperty
                
                .getName() })); 
      derivedProperties.put(derivedProperty.getName(), derivedProperty);
    } 
  }
  
  private static List<DerivedPropertyDescriptor> collectDerivedPropertyDescriptors(Class<?> modelClass) {
    assert modelClass != null;
    List<DerivedPropertyDescriptor> derivedProperties = new ArrayList<>();
    for (Method method : modelClass.getMethods()) {
      if (method.isAnnotationPresent((Class)DerivedProperty.class)) {
        DerivedPropertyDescriptor descriptor;
        DerivedProperty derivedProperty = method.<DerivedProperty>getAnnotation(DerivedProperty.class);
        String derivedPropertyName = derivedProperty.value();
        try {
          descriptor = DerivedPropertyDescriptor.fromMethod(derivedPropertyName, method);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid derived property method '%s' in class '%s'", new Object[] { method
                  
                  .getName(), modelClass.getCanonicalName() }), ex);
        } 
        derivedProperties.add(descriptor);
      } 
    } 
    return derivedProperties;
  }
  
  private static QuerySchema buildDerivedPropertySchema(Map<String, DerivedPropertyDescriptor> derivedProperties) {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByName = new HashMap<>(derivedProperties.size());
    for (String property : derivedProperties.keySet())
      propertyInfoByName.put(property, QuerySchema.PropertyInfo.forNonFilterableProperty()); 
    return QuerySchema.forProperties(propertyInfoByName);
  }
}
