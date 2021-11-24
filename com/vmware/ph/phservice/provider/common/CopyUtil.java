package com.vmware.ph.phservice.provider.common;

import java.lang.reflect.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CopyUtil {
  private static final Log log = LogFactory.getLog(CopyUtil.class);
  
  public static void copyPublicFields(Object srcObject, Object destObject) {
    Class<?> destType = destObject.getClass();
    Field[] fields = destType.getFields();
    for (Field field : fields) {
      try {
        Field srcField = srcObject.getClass().getField(field.getName());
        if (srcField != null && srcField.getType().isAssignableFrom(field.getType()))
          field.set(destObject, srcField.get(srcObject)); 
      } catch (IllegalArgumentException|IllegalAccessException|NoSuchFieldException|SecurityException e) {
        if (log.isDebugEnabled())
          log.debug("Cannot copy field with name " + field.getName() + " because " + e.getMessage()); 
      } 
    } 
  }
}
