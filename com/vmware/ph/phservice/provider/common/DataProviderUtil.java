package com.vmware.ph.phservice.provider.common;

import com.vmware.ph.phservice.common.vapi.util.VapiResultUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataProviderUtil {
  private static final Log log = LogFactory.getLog(DataProviderUtil.class);
  
  public static final String GET_VALUE_METHOD_NAME_PREFIX = "get";
  
  public static final String SET_VALUE_METHOD_NAME_PREFIX = "set";
  
  public static final String IS_VALUE_METHOD_NAME_PREFIX = "is";
  
  public static final String MODEL_KEY_PATTERN = "%s:%s";
  
  public static final String MODEL_KEY_PREFIX = "@";
  
  public static final String PROPERTY_PATH_SEPARATOR = "/";
  
  public static URI createModelKey(Class<?> objectClass, String objectKey) {
    String modelKey = String.format("%s:%s", new Object[] { objectClass.getSimpleName(), objectKey });
    String validModelKey = VapiResultUtil.convertToValidModelKey(modelKey);
    return URI.create(validModelKey);
  }
  
  public static URI createModelKey(String objectType, String objectKey) {
    String modelKey = String.format("%s:%s", new Object[] { objectType, objectKey });
    String validModelKey = VapiResultUtil.convertToValidModelKey(modelKey);
    return URI.create(validModelKey);
  }
  
  public static Map<String, Object> getMethodInvocationReturnValues(Object targetObject, List<String> methodsToInvoke) {
    Map<String, Object> methodValues = new HashMap<>();
    for (String methodName : methodsToInvoke) {
      Object invocationResult = getMethodInvocationReturnValue(targetObject, methodName);
      methodValues.put(methodName, invocationResult);
    } 
    return methodValues;
  }
  
  public static Object getMethodInvocationReturnValue(Object targetObject, String methodName) {
    Object invocationResult = null;
    try {
      Method method = targetObject.getClass().getMethod(methodName, new Class[0]);
      invocationResult = method.invoke(targetObject, new Object[0]);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    } 
    return invocationResult;
  }
  
  public static List<Object> getPropertyValues(Object targetObject, Object objectModelKey, List<String> properties) {
    return getPropertyValuesFromObjectAndValueMap(targetObject, objectModelKey, properties, null);
  }
  
  public static List<Object> getPropertyValuesFromObjectAndValueMap(Object targetObject, Object objectModelKey, List<String> properties, Map<String, Object> fallbackPropertyToValue) {
    List<Object> propertyValues = new ArrayList(properties.size());
    propertyValues.add(objectModelKey);
    for (String property : properties) {
      if (!property.startsWith("@")) {
        Object propertyValue = getPropertyValue(targetObject, property);
        if (fallbackPropertyToValue != null && propertyValue == null)
          propertyValue = fallbackPropertyToValue.get(property); 
        propertyValues.add(propertyValue);
      } 
    } 
    return propertyValues;
  }
  
  public static Object getPropertyValue(Object vmodlObject, String propertyName) {
    String className = vmodlObject.getClass().getSimpleName();
    if (log.isTraceEnabled())
      log.trace("Getting property : " + propertyName + " from " + className + " object"); 
    int index = propertyName.indexOf("/");
    try {
      if (index != -1) {
        String basePropertyName = propertyName.substring(0, index);
        Object basePropertyValue = getPropertyValueViaAccessor(vmodlObject, basePropertyName);
        if (basePropertyValue != null) {
          String subPropertyName = propertyName.substring(index + 1);
          return getPropertyValue(basePropertyValue, subPropertyName);
        } 
        return null;
      } 
      Object result = getPropertyValueViaAccessor(vmodlObject, propertyName);
      if (log.isTraceEnabled())
        log.trace("Got property : " + propertyName + " from " + className + " object"); 
      return result;
    } catch (IllegalArgumentException|SecurityException e) {
      if (log.isDebugEnabled()) {
        log.debug("Error occured while trying to get " + propertyName + " from " + className + " object.");
        log.debug(e.getMessage());
      } 
      return null;
    } 
  }
  
  public static void setPropertyValue(Object targetObject, String propertyPath, Object propertyValue) {
    String[] propertiesNames = propertyPath.split("/");
    Object currentObject = targetObject;
    for (int i = 0; i < propertiesNames.length; i++) {
      String currentPropertyName = propertiesNames[i];
      Object currentPropertyValue = getPropertyValue(currentObject, currentPropertyName);
      if (currentPropertyValue == null) {
        Class<?> propertyType = getPropertyTypeViaAccessor(currentObject
            .getClass(), currentPropertyName);
        if (i < propertiesNames.length - 1) {
          try {
            currentPropertyValue = propertyType.newInstance();
          } catch (InstantiationException|IllegalAccessException e) {
            if (log.isDebugEnabled())
              log.debug(
                  String.format("Failed to create instance of class '%s'.", new Object[] { propertyType.getCanonicalName() }), e); 
          } 
        } else {
          currentPropertyValue = propertyValue;
        } 
        setPropertyValueViaMutator(currentObject, currentPropertyName, currentPropertyValue, propertyType);
      } 
      currentObject = currentPropertyValue;
    } 
  }
  
  public static List<String> getPropertyNames(Class<?> objectClass, String accessorMethodPrefix, boolean startWithLowerCase) {
    List<String> supportedProperties = new LinkedList<>();
    Method[] methods = objectClass.getDeclaredMethods();
    for (Method method : methods) {
      String methodName = method.getName();
      int methodModifiers = method.getModifiers();
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      if (Modifier.isPublic(methodModifiers) && methodParameterTypes.length == 0 && methodName
        
        .startsWith(accessorMethodPrefix)) {
        String propertyName = methodName.replaceFirst(accessorMethodPrefix, "");
        if (startWithLowerCase)
          propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1); 
        supportedProperties.add(propertyName);
      } 
    } 
    return supportedProperties;
  }
  
  public static Object getPropertyValueViaAccessor(Object targetObject, String propertyName) {
    String[] possibleAccessorMethodNames = getPossibleAccessorMethodNames(propertyName);
    Method accessorMethod = null;
    for (String possibleAccessorMethodName : possibleAccessorMethodNames) {
      try {
        accessorMethod = targetObject.getClass().getMethod(possibleAccessorMethodName, new Class[0]);
        break;
      } catch (NoSuchMethodException e) {
        if (log.isDebugEnabled())
          log.debug("Failed to discover accessor method " + possibleAccessorMethodName); 
      } catch (SecurityException e) {
        if (log.isDebugEnabled())
          log.debug("Security restrictions do not allow accessor method discovery for object " + targetObject
              
              .getClass().getName(), e); 
      } 
    } 
    Object propertyValue = null;
    if (accessorMethod != null)
      try {
        propertyValue = accessorMethod.invoke(targetObject, new Object[0]);
      } catch (IllegalAccessException|IllegalArgumentException|java.lang.reflect.InvocationTargetException e) {
        if (log.isDebugEnabled())
          log.debug("Failed to invoke accessor method for property " + propertyName, e); 
      }  
    return propertyValue;
  }
  
  public static void setPropertyValueViaMutator(Object targetObject, String propertyName, Object propertyValue, Class<?> propertyType) {
    String mutatorMethodName = "set" + capitalizeString(propertyName);
    try {
      Method mutatorMethod = targetObject.getClass().getMethod(mutatorMethodName, new Class[] { propertyType });
      mutatorMethod.invoke(targetObject, new Object[] { propertyValue });
    } catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|java.lang.reflect.InvocationTargetException e) {
      if (log.isDebugEnabled())
        log.debug("Failed to set property value via mutator method " + mutatorMethodName); 
    } 
  }
  
  public static Class<?> getPropertyTypeViaAccessor(Class<?> targetClass, String propertyName) {
    String[] possibleAccessorMethodNames = getPossibleAccessorMethodNames(propertyName);
    Class<?> propertyType = null;
    for (String possibleAccessorMethodName : possibleAccessorMethodNames) {
      try {
        Method accessorMethod = targetClass.getMethod(possibleAccessorMethodName, new Class[0]);
        propertyType = accessorMethod.getReturnType();
        break;
      } catch (NoSuchMethodException|SecurityException e) {
        if (log.isDebugEnabled())
          log.debug("Failed to discover accessor method " + possibleAccessorMethodName); 
      } 
    } 
    return propertyType;
  }
  
  public static Class<?> getTypeOfLastPropertyInPropertyPath(Class<?> startClass, String propertyPath) {
    String[] proprertiesNames = propertyPath.split("/");
    Class<?> currentClass = startClass;
    for (String currentPropertyName : proprertiesNames) {
      if (currentClass == null)
        break; 
      currentClass = getPropertyTypeViaAccessor(currentClass, currentPropertyName);
    } 
    return currentClass;
  }
  
  private static String[] getPossibleAccessorMethodNames(String propertyName) {
    String capitalizedPropertyName = capitalizeString(propertyName);
    String[] possibleAccessorMethodNames = { "get" + capitalizedPropertyName, "is" + capitalizedPropertyName };
    return possibleAccessorMethodNames;
  }
  
  private static String capitalizeString(String string) {
    return Character.toUpperCase(string.charAt(0)) + string.substring(1);
  }
}
