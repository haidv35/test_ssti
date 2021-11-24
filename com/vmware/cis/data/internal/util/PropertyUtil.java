package com.vmware.cis.data.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;

public final class PropertyUtil {
  public static final List<String> PROPERTY_LIST_MODEL_KEY = Collections.singletonList("@modelKey");
  
  public static boolean isModelKey(String qualifiedProperty) {
    Validate.notEmpty(qualifiedProperty);
    return qualifiedProperty.endsWith("@modelKey");
  }
  
  public static boolean isType(String property) {
    Validate.notEmpty(property);
    return property.equals("@type");
  }
  
  public static boolean isSpecialProperty(String property) {
    Validate.notEmpty(property);
    return (property.equals("@type") || property.equals("@modelKey"));
  }
  
  public static boolean isInstanceUuid(String property) {
    Validate.notEmpty(property);
    return (property.endsWith("@instanceUuid") && "@instanceUuid"
      .equals(
        QualifiedProperty.forQualifiedName(property).getSimpleProperty()));
  }
  
  public static List<String> plusModelKey(List<String> properties) {
    if (properties == null)
      return Collections.singletonList("@modelKey"); 
    if (properties.contains("@modelKey"))
      return properties; 
    List<String> propsWithKey = new ArrayList<>(properties.size() + 1);
    propsWithKey.add("@modelKey");
    propsWithKey.addAll(properties);
    return propsWithKey;
  }
  
  public static String changeResourceModel(String qualifiedPropertyPath, String resourceModel) {
    Validate.notEmpty(qualifiedPropertyPath);
    Validate.notEmpty(resourceModel);
    if (qualifiedPropertyPath.startsWith(resourceModel + '/'))
      return qualifiedPropertyPath; 
    Validate.isTrue(QualifiedProperty.isSyntacticallyQualified(qualifiedPropertyPath), 
        String.format("The property '%s' must be qualified!", new Object[] { qualifiedPropertyPath }));
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(qualifiedPropertyPath);
    return 
      QualifiedProperty.forModelAndSimpleProperty(resourceModel, qualifiedProperty.getSimpleProperty())
      .toString();
  }
}
