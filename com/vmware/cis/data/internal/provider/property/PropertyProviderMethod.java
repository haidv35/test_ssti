package com.vmware.cis.data.internal.provider.property;

import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.model.PropertyProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyProviderMethod {
  public static final String PROPERTY_PROVIDER_METHOD_NAME_PREFIX = "get";
  
  private static Logger _logger = LoggerFactory.getLogger(PropertyProviderMethod.class);
  
  private final Object _obj;
  
  private final Method _method;
  
  private final String _propertyName;
  
  private final String _providerName;
  
  private final boolean _isBatch;
  
  public static Collection<PropertyProviderMethod> forPropertyProvider(Object propertyProvider) {
    assert propertyProvider != null;
    List<PropertyProviderMethod> providerMethods = new ArrayList<>();
    for (Method method : propertyProvider.getClass().getMethods()) {
      if (method.isAnnotationPresent((Class)PropertyProvider.class)) {
        PropertyProviderMethod providerMethod;
        try {
          validatePropertyProviderMethod(method);
          String propertyName = getPropertyName(method);
          boolean isBatch = isBatch(method);
          providerMethod = new PropertyProviderMethod(propertyProvider, method, propertyName, isBatch);
        } catch (RuntimeException ex) {
          throw new IllegalArgumentException(String.format("Invalid property provider method '%s' in %s", new Object[] { method
                  
                  .getName(), propertyProvider.getClass().getCanonicalName() }), ex);
        } 
        if (_logger.isDebugEnabled())
          _logger.debug("Register method '{}' from property provider '{}' for property '{}'", new Object[] { method
                
                .getName(), propertyProvider
                .getClass().getCanonicalName(), providerMethod
                .getPropertyName() }); 
        providerMethods.add(providerMethod);
      } 
    } 
    return providerMethods;
  }
  
  private static void validatePropertyProviderMethod(Method method) {
    assert method != null;
    if (!Modifier.isPublic(method.getModifiers()))
      throw new IllegalArgumentException("Property provider method must be public."); 
    Class<?>[] params = method.getParameterTypes();
    if (params.length != 1)
      throw new IllegalArgumentException("Property provider method must accept a single parameter"); 
    if (isBatch(method) && !Collection.class.isAssignableFrom(method.getReturnType()))
      throw new IllegalArgumentException("Property provider method must return an instance of java.util.Collection"); 
  }
  
  private static String getPropertyName(Method method) {
    assert method != null;
    assert method.isAnnotationPresent((Class)PropertyProvider.class);
    PropertyProvider providerAnnotation = method.<PropertyProvider>getAnnotation(PropertyProvider.class);
    String annotationValue = providerAnnotation.value();
    Validate.notEmpty(annotationValue, "@PropertyProvider annotation must contain model-qualified property name");
    if (annotationValue.indexOf('/') > 0)
      return annotationValue; 
    String model = annotationValue;
    String methodName = method.getName();
    if (!methodName.startsWith("get"))
      throw new IllegalArgumentException(String.format("Property provider method name must start with 'get' prefix: '%s'", new Object[] { methodName })); 
    if (methodName.codePointCount(0, methodName.length()) != methodName
      .length())
      throw new IllegalArgumentException(String.format("Property provider method name must use only ASCII characters: '%s'", new Object[] { methodName })); 
    String rawPropertyName = methodName.substring("get".length());
    if (rawPropertyName.isEmpty())
      throw new IllegalArgumentException(String.format("Derived property method name must contain property name after the 'get' prefix: '%s'", new Object[] { methodName })); 
    String simpleProperty = Character.toLowerCase(rawPropertyName.charAt(0)) + rawPropertyName.substring(1);
    return QualifiedProperty.forModelAndSimpleProperty(model, simpleProperty)
      .toString();
  }
  
  private static boolean isBatch(Method method) {
    assert method != null;
    Class<?>[] params = method.getParameterTypes();
    assert params.length == 1;
    return (params[0] == Collection.class);
  }
  
  private PropertyProviderMethod(Object obj, Method method, String propertyName, boolean isBatch) {
    assert obj != null;
    assert method != null;
    assert propertyName != null;
    this._obj = obj;
    this._method = method;
    this._propertyName = propertyName;
    this._providerName = toProviderName(obj, method);
    this._isBatch = isBatch;
  }
  
  public String getPropertyName() {
    return this._propertyName;
  }
  
  public Collection<?> getPropertyValuesForKeys(Collection<?> keys) {
    Object result;
    assert keys != null;
    assert !keys.isEmpty();
    if (_logger.isTraceEnabled())
      _logger.trace("Invoking property provider method '{}' of '{}' for resources: {}", new Object[] { this._method

            
            .getName(), this._obj
            .getClass().getCanonicalName(), keys }); 
    QueryIdLogConfigurator logCongigurator = QueryIdLogConfigurator.onPropertyProviderStart(this._providerName);
    try {
      result = invokePropertyProviderMethod(keys);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException(String.format("Property provider method '%s' of '%s' is inaccessible", new Object[] { this._method
              
              .getName(), this._obj.getClass().getCanonicalName() }), ex);
    } catch (InvocationTargetException ex) {
      throw new IllegalStateException(String.format("Property provider method '%s' of '%s' threw exception", new Object[] { this._method
              
              .getName(), this._obj.getClass().getCanonicalName() }), ex.getTargetException());
    } finally {
      logCongigurator.close();
    } 
    if (_logger.isTraceEnabled())
      _logger.trace("Property provider method '{}' of '{}' returned: {}", new Object[] { this._method
            
            .getName(), this._obj
            .getClass().getCanonicalName(), result }); 
    if (result == null)
      throw new IllegalStateException(String.format("Property provider method '%s' of '%s' returned null", new Object[] { this._method
              
              .getName(), this._obj.getClass().getCanonicalName() })); 
    if (!(result instanceof Collection))
      throw new IllegalStateException(String.format("Property provider method '%s' of '%s' returned %s instead of java.util.Collection", new Object[] { this._method

              
              .getName(), this._obj
              .getClass().getCanonicalName(), result
              .getClass() })); 
    Collection<?> values = (Collection)result;
    if (values.size() != keys.size())
      throw new IllegalStateException(String.format("Property provider method '%s' of '%s' returned %d property values instead of %d", new Object[] { this._method

              
              .getName(), this._obj
              .getClass().getCanonicalName(), 
              Integer.valueOf(values.size()), 
              Integer.valueOf(keys.size()) })); 
    return values;
  }
  
  private Object invokePropertyProviderMethod(Collection<?> keys) throws IllegalAccessException, InvocationTargetException {
    if (this._isBatch)
      return this._method.invoke(this._obj, new Object[] { keys }); 
    if (keys.size() > 1)
      _logger.warn("Performance problem: Property provider method '{}' of '{}' expects single resource but it is invoked for {} resources.", new Object[] { this._method

            
            .getName(), this._obj.getClass().getCanonicalName(), Integer.valueOf(keys.size()) }); 
    List<Object> results = new ArrayList(keys.size());
    for (Object key : keys) {
      Object result = this._method.invoke(this._obj, new Object[] { key });
      results.add(result);
    } 
    return results;
  }
  
  private String toProviderName(Object bean, Method method) {
    return bean.getClass().getSimpleName() + "#" + method.getName();
  }
  
  public String toString() {
    return this._propertyName;
  }
}
