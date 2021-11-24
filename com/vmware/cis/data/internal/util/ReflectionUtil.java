package com.vmware.cis.data.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

public final class ReflectionUtil {
  public static void setField(Object instance, Field field, Object value) {
    assert instance != null;
    assert field != null;
    boolean isFieldAccessible = field.isAccessible();
    try {
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      String msg = String.format("Unable to reflectively set field '%s' on instance of '%s' ", new Object[] { field
            
            .getName(), instance.getClass().getSimpleName() });
      throw new IllegalStateException(msg, e);
    } finally {
      field.setAccessible(isFieldAccessible);
    } 
  }
  
  public static Field getDeclaredField(Class<?> type, String fieldName) {
    try {
      Field field = type.getDeclaredField(fieldName);
      return field;
    } catch (NoSuchFieldException e) {
      return null;
    } catch (SecurityException e) {
      String msg = String.format("Unable to reflectively get field '%s' on class '%s' ", new Object[] { fieldName, type
            .getSimpleName() });
      throw new IllegalStateException(msg, e);
    } 
  }
  
  public static Field[] getAllFields(Class<?> clazz) {
    if (clazz.getSuperclass() != null)
      return (Field[])ArrayUtils.addAll((Object[])getAllFields(clazz.getSuperclass()), (Object[])clazz
          .getDeclaredFields()); 
    return clazz.getDeclaredFields();
  }
  
  public static List<Object> newInstances(Class<?> type, int size) {
    assert type != null;
    assert size >= 0;
    List<Object> instances = new ArrayList(size);
    for (int index = 0; index < size; index++)
      instances.add(newInstance(type)); 
    return instances;
  }
  
  public static Object newInstance(Class<?> type) {
    Constructor<?> constructor;
    try {
      constructor = type.getDeclaredConstructor(new Class[0]);
    } catch (NoSuchMethodException|SecurityException e) {
      String msg = String.format("Unable to reflectively access the default constructor on instance of '%s' ", new Object[] { type
            
            .getSimpleName() });
      throw new IllegalStateException(msg, e);
    } 
    boolean isConstructorAccessible = constructor.isAccessible();
    try {
      constructor.setAccessible(true);
      Object instance = constructor.newInstance(new Object[0]);
      return instance;
    } catch (IllegalAccessException|InstantiationException|IllegalArgumentException|java.lang.reflect.InvocationTargetException e) {
      String msg = String.format("Unable to reflectively create instance by using the default constructor on instance of '%s' ", new Object[] { type
            
            .getSimpleName() });
      throw new IllegalStateException(msg, e);
    } finally {
      constructor.setAccessible(isConstructorAccessible);
    } 
  }
  
  public static boolean hasDefaultConstructor(Class<?> type) {
    for (Constructor<?> constructor : type.getDeclaredConstructors()) {
      if ((constructor.getParameterTypes()).length == 0)
        return true; 
    } 
    return false;
  }
  
  public static Class<?> getType(Field field) {
    assert field != null;
    Class<?> type = field.getType();
    if (type.isArray())
      type = type.getComponentType(); 
    return type;
  }
}
