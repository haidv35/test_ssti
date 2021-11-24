package com.vmware.cis.data.internal.util;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.Validate;

public final class QualifiedProperty {
  public static final char RESOURCE_MODEL_SEPARATOR = '/';
  
  public static final char PROPERTY_PATH_SEPARATOR = '/';
  
  private static final char PACKAGE_PATH_SEPARATOR = '.';
  
  private final String _qualifiedProperty;
  
  private final String _resourceModel;
  
  private final String _propertyPath;
  
  public static QualifiedProperty forQualifiedName(String providerProperty) {
    Validate.notEmpty(providerProperty, "Argument `providerProperty' must not be null or empty.");
    int modelSeparatorIndex = getModelSeparatorIndex(providerProperty);
    String resourceModel = providerProperty.substring(0, modelSeparatorIndex);
    String propertyPathWithSlashes = providerProperty.substring(modelSeparatorIndex + 1);
    return new QualifiedProperty(providerProperty, resourceModel, propertyPathWithSlashes);
  }
  
  public static boolean isSyntacticallyQualified(String providerProperty) {
    assert providerProperty != null;
    if (providerProperty.isEmpty() || providerProperty
      .charAt(0) == '/' || providerProperty
      .charAt(providerProperty.length() - 1) == '/')
      return false; 
    boolean hasSeparator = false;
    for (int i = 0; i < providerProperty.length(); i++) {
      char symbol = providerProperty.charAt(i);
      if (symbol == '/') {
        hasSeparator = true;
        char nextSymbol = providerProperty.charAt(i + 1);
        if (nextSymbol == '/')
          return false; 
      } else if (!CharUtils.isAsciiAlphanumeric(symbol) && symbol != '@' && symbol != '.' && symbol != ':' && symbol != '_' && symbol != '-') {
        return false;
      } 
    } 
    if (!hasSeparator)
      return false; 
    return true;
  }
  
  public static QualifiedProperty forModelAndSimpleProperty(String resourceModel, String propertyPath) {
    Validate.notEmpty(resourceModel, "Argument `resourceModel' must not be null or empty.");
    Validate.notEmpty(propertyPath, "Argument `propertyPath' must not be null or empty.");
    if (resourceModel.indexOf('/') >= 0)
      throw new IllegalArgumentException(String.format("Invalid resource model name: '%s'", new Object[] { resourceModel })); 
    String qualifiedPropertyWithSlashes = resourceModel + '/' + propertyPath;
    return new QualifiedProperty(qualifiedPropertyWithSlashes, resourceModel, propertyPath);
  }
  
  private QualifiedProperty(String qualifiedPropertyWithSlashes, String resourceModel, String propertyPathWithSlashes) {
    if (!isSyntacticallyQualified(qualifiedPropertyWithSlashes))
      throw new IllegalArgumentException(String.format("Invalid property '%s'", new Object[] { qualifiedPropertyWithSlashes })); 
    this._qualifiedProperty = qualifiedPropertyWithSlashes;
    this._propertyPath = propertyPathWithSlashes;
    this._resourceModel = resourceModel;
  }
  
  public String getResourceModel() {
    return this._resourceModel;
  }
  
  public String getSimpleProperty() {
    return this._propertyPath;
  }
  
  public boolean isVmodl1() {
    return (this._resourceModel.indexOf('.') < 0);
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof QualifiedProperty))
      return false; 
    QualifiedProperty other = (QualifiedProperty)obj;
    return this._qualifiedProperty
      .equals(other._qualifiedProperty);
  }
  
  public int hashCode() {
    return this._qualifiedProperty.hashCode();
  }
  
  public String toString() {
    return this._qualifiedProperty;
  }
  
  public static String getRootProperty(String qualifiedPropertyPath) {
    Validate.notEmpty(qualifiedPropertyPath);
    if (PropertyUtil.isSpecialProperty(qualifiedPropertyPath))
      return qualifiedPropertyPath; 
    int modelSeparatorIndex = getModelSeparatorIndex(qualifiedPropertyPath);
    int firstTokenSeparatorIndex = qualifiedPropertyPath.indexOf('/', modelSeparatorIndex + 1);
    if (firstTokenSeparatorIndex < 0)
      return qualifiedPropertyPath; 
    return qualifiedPropertyPath.substring(0, firstTokenSeparatorIndex);
  }
  
  private static int getModelSeparatorIndex(String qualifiedPropertyPath) {
    Validate.notEmpty(qualifiedPropertyPath);
    int modelSeparatorIndex = qualifiedPropertyPath.indexOf('/');
    if (modelSeparatorIndex <= 0)
      throw new IllegalArgumentException(String.format("Property name lacks resource model: '%s'", new Object[] { qualifiedPropertyPath })); 
    if (modelSeparatorIndex + 1 >= qualifiedPropertyPath.length())
      throw new IllegalArgumentException(
          String.format("Property name has only resource model but no property path: '%s'", new Object[] { qualifiedPropertyPath })); 
    return modelSeparatorIndex;
  }
}
