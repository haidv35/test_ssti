package com.vmware.cis.data.internal.adapters.vmomi.util;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import org.apache.commons.lang.Validate;

public final class VmomiProperty {
  public static final String FOREIGN_KEY_SUFFIX = "@moId";
  
  public static boolean isForeignKey(String property) {
    Validate.notEmpty(property);
    return property.endsWith("/@moId");
  }
  
  public static String stripForeignKeySuffix(String property) {
    String clearedProperty = property.substring(0, property.length() - "@moId"
        .length() + 1);
    return clearedProperty;
  }
  
  public static String appendForeignKeySuffix(String property) {
    return property + '/' + "@moId";
  }
  
  public static String toVmomiProperty(String coreProperty) {
    String simpleProperty;
    assert coreProperty != null;
    if (PropertyUtil.isType(coreProperty))
      return "@modelKey"; 
    if (PropertyUtil.isModelKey(coreProperty))
      return coreProperty; 
    if (Character.isUpperCase(coreProperty.charAt(0)) && coreProperty
      .indexOf('/') > 0) {
      simpleProperty = QualifiedProperty.forQualifiedName(coreProperty).getSimpleProperty();
    } else {
      simpleProperty = coreProperty;
    } 
    if (isForeignKey(simpleProperty))
      return stripForeignKeySuffix(simpleProperty); 
    return simpleProperty;
  }
}
