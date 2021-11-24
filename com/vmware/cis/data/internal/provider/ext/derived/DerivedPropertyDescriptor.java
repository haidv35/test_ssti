package com.vmware.cis.data.internal.provider.ext.derived;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.model.SourceProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

final class DerivedPropertyDescriptor {
  private final String _propertyName;
  
  private final Method _derivedPropertyMethod;
  
  private final String[] _propertyForParam;
  
  private final Set<String> _sourceProperties;
  
  public static DerivedPropertyDescriptor fromMethod(String derivedPropertyName, Method derivedPropertyMethod) {
    validateDerivedPropertyMethod(derivedPropertyMethod);
    validateDerivedPropertyName(derivedPropertyName, derivedPropertyMethod);
    String[] propertyForParam = getPropertiesForParams(derivedPropertyMethod);
    validatePropertiesForParams(propertyForParam, derivedPropertyMethod);
    Set<String> sourceProperties = new HashSet<>(Arrays.asList(propertyForParam));
    return new DerivedPropertyDescriptor(derivedPropertyName, derivedPropertyMethod, propertyForParam, sourceProperties);
  }
  
  private DerivedPropertyDescriptor(String propertyName, Method derivedPropertyMethod, String[] propertyForParam, Set<String> sourceProperties) {
    assert propertyName != null;
    assert !propertyName.isEmpty();
    assert derivedPropertyMethod != null;
    assert propertyForParam != null;
    assert sourceProperties != null;
    this._propertyName = propertyName;
    this._derivedPropertyMethod = derivedPropertyMethod;
    this._propertyForParam = propertyForParam;
    this._sourceProperties = Collections.unmodifiableSet(sourceProperties);
  }
  
  public String getName() {
    return this._propertyName;
  }
  
  public Collection<String> getSourceProperties() {
    return this._sourceProperties;
  }
  
  public Object invokeDerivedPropertyMethod(ResourceItem item) {
    Validate.notNull(item);
    Object[] paramValues = new Object[this._propertyForParam.length];
    for (int i = 0; i < this._propertyForParam.length; i++) {
      String property = this._propertyForParam[i];
      Object paramValue = item.get(property);
      paramValues[i] = paramValue;
    } 
    try {
      return this._derivedPropertyMethod.invoke(null, paramValues);
    } catch (Exception ex) {
      throw new RuntimeException(String.format("Error while invoking derived property method '%s'", new Object[] { this._derivedPropertyMethod
              
              .getName() }), ex);
    } 
  }
  
  private static void validateDerivedPropertyMethod(Method derivedPropertyMethod) {
    Validate.notNull(derivedPropertyMethod, "Derived property method must not be null");
    if (!Modifier.isPublic(derivedPropertyMethod.getModifiers()))
      throw new IllegalArgumentException(String.format("Derived property method '%s' must be public.", new Object[] { derivedPropertyMethod
              
              .getName() })); 
    if (!Modifier.isStatic(derivedPropertyMethod.getModifiers()))
      throw new IllegalArgumentException(String.format("Derived property method '%s' must be static.", new Object[] { derivedPropertyMethod
              
              .getName() })); 
    if (void.class.equals(derivedPropertyMethod.getReturnType()))
      throw new IllegalArgumentException(String.format("Derived property method '%s' must not return void.", new Object[] { derivedPropertyMethod
              
              .getName() })); 
  }
  
  private static void validateDerivedPropertyName(String derivedPropertyName, Method derivedPropertyMethod) {
    try {
      QualifiedProperty.forQualifiedName(derivedPropertyName);
    } catch (RuntimeException e) {
      String methodName = derivedPropertyMethod.getName();
      String msg = String.format("Invalid name of derived property: '%s' on method '%s'", new Object[] { derivedPropertyName, methodName });
      throw new IllegalArgumentException(msg);
    } 
  }
  
  private static String[] getPropertiesForParams(Method derivedPropertyMethod) {
    assert derivedPropertyMethod != null;
    if ((derivedPropertyMethod.getParameterTypes()).length == 0)
      throw new IllegalArgumentException(String.format("Derived property method '%s' has no parameters", new Object[] { derivedPropertyMethod
              
              .getName() })); 
    Class<?>[] paramTypes = derivedPropertyMethod.getParameterTypes();
    Annotation[][] paramAnnotations = derivedPropertyMethod.getParameterAnnotations();
    assert paramTypes.length == paramAnnotations.length;
    String[] paramProperties = new String[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++)
      paramProperties[i] = getPropertyForParam(derivedPropertyMethod.getName(), paramTypes[i], paramAnnotations[i]); 
    return paramProperties;
  }
  
  private static void validatePropertiesForParams(String[] propertyForParam, Method derivedPropertyMethod) {
    assert propertyForParam != null;
    Set<String> properties = new HashSet<>(propertyForParam.length);
    for (String property : propertyForParam) {
      boolean added = properties.add(property);
      if (!added)
        throw new IllegalArgumentException(String.format("Derived property method '%s' has multiple parameters annotated with @SourceProperty for property '%s'", new Object[] { derivedPropertyMethod


                
                .getName(), property })); 
    } 
  }
  
  private static String getPropertyForParam(String methodName, Class<?> paramType, Annotation[] paramAnnotations) {
    assert methodName != null;
    assert !methodName.isEmpty();
    assert paramType != null;
    assert paramAnnotations != null;
    String property = null;
    for (Annotation a : paramAnnotations) {
      String propertyCandidate = getPropertyForAnnotation(a, paramType, methodName);
      if (propertyCandidate != null) {
        if (property != null)
          throw new IllegalArgumentException(String.format("Derived property method '%s' has multiple parameter annotations on parameter of type '%s'", new Object[] { methodName, paramType

                  
                  .getCanonicalName() })); 
        property = propertyCandidate;
      } 
    } 
    if (property == null)
      throw new IllegalArgumentException(String.format("Derived property method '%s' has no parameter annotations on parameter of type '%s'", new Object[] { methodName, paramType

              
              .getCanonicalName() })); 
    return property;
  }
  
  private static String getPropertyForAnnotation(Annotation a, Class<?> paramType, String methodName) {
    assert a != null;
    assert paramType != null;
    assert methodName != null;
    assert !methodName.isEmpty();
    if (a instanceof SourceProperty) {
      SourceProperty sourceProperty = (SourceProperty)a;
      String property = sourceProperty.value();
      if (StringUtils.isEmpty(property))
        throw new IllegalArgumentException(String.format("Derived property method '%s' has @SourceProperty annotation with empty property name on parameter of type '%s'", new Object[] { methodName, paramType


                
                .getCanonicalName() })); 
      if (PropertyUtil.isModelKey(property))
        throw new IllegalArgumentException(String.format("Derived property method '%s' has @SourceProperty annotation with property name '%s'", new Object[] { methodName, property })); 
      return property;
    } 
    return null;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof DerivedPropertyDescriptor))
      return false; 
    DerivedPropertyDescriptor other = (DerivedPropertyDescriptor)obj;
    return (this._propertyName.equals(other._propertyName) && this._derivedPropertyMethod
      .equals(other._derivedPropertyMethod) && 
      Arrays.equals((Object[])this._propertyForParam, (Object[])other._propertyForParam) && this._sourceProperties
      .equals(other._sourceProperties));
  }
  
  public int hashCode() {
    int hash = 23;
    hash = 31 * hash + this._propertyName.hashCode();
    hash = 31 * hash + this._derivedPropertyMethod.hashCode();
    hash = 31 * hash + Arrays.hashCode((Object[])this._propertyForParam);
    hash = 31 * hash + this._sourceProperties.hashCode();
    return hash;
  }
  
  public String toString() {
    return "DerivedPropertyDescriptor [_propertyName=" + this._propertyName + ", _derivedPropertyMethod=" + this._derivedPropertyMethod + ", _propertyForParam=" + 
      
      Arrays.toString((Object[])this._propertyForParam) + ", _sourceProperties=" + this._sourceProperties + "]";
  }
}
