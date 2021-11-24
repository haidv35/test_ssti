package com.vmware.cis.data.internal.provider.ext.alias;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.Relationship;
import java.lang.reflect.Field;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public final class AliasPropertyDescriptor {
  private final String _name;
  
  private final String _targetName;
  
  public static AliasPropertyDescriptor fromField(String resourceModel, Field aliasPropertyField, boolean allowModelOverwrite) {
    Validate.notEmpty(resourceModel, "Model name must not be empty!");
    Validate.notNull(aliasPropertyField, "Alias property field must not be null!");
    validateAliasPropertyField(aliasPropertyField);
    QualifiedProperty qualifiedAliasProperty = QualifiedProperty.forModelAndSimpleProperty(resourceModel, aliasPropertyField.getName());
    String targetPropertyName = getTargetPropertyName(aliasPropertyField);
    validateAliasPropertyData(qualifiedAliasProperty.toString(), targetPropertyName);
    if (allowModelOverwrite && !PropertyUtil.isSpecialProperty(targetPropertyName))
      targetPropertyName = PropertyUtil.changeResourceModel(targetPropertyName, resourceModel); 
    return new AliasPropertyDescriptor(qualifiedAliasProperty.toString(), targetPropertyName);
  }
  
  AliasPropertyDescriptor(String name, String targetName) {
    assert name != null;
    assert targetName != null;
    this._name = name;
    this._targetName = targetName;
  }
  
  public String getName() {
    return this._name;
  }
  
  public String getTargetName() {
    return this._targetName;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof AliasPropertyDescriptor))
      return false; 
    AliasPropertyDescriptor other = (AliasPropertyDescriptor)obj;
    return (this._name.equals(other._name) && this._targetName
      .equals(other._targetName));
  }
  
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + this._name.hashCode();
    hash = 31 * hash + this._targetName.hashCode();
    return hash;
  }
  
  public String toString() {
    return "AliasPropertyDescriptor [_name=" + this._name + ", _targetName=" + this._targetName + "]";
  }
  
  private static String getTargetPropertyName(Field aliasPropertyField) {
    Property property = aliasPropertyField.<Property>getAnnotation(Property.class);
    return property.value();
  }
  
  private static void validateAliasPropertyField(Field aliasPropertyField) {
    assert aliasPropertyField != null;
    assert aliasPropertyField.isAnnotationPresent((Class)Property.class);
    assert !aliasPropertyField.isAnnotationPresent((Class)Relationship.class);
  }
  
  private static void validateAliasPropertyData(String aliasProperty, String targetPropertyName) {
    Validate.isTrue(StringUtils.isNotEmpty(targetPropertyName), 
        String.format("The target property for alias property '%s' must not be empty!", new Object[] { aliasProperty }));
    if (PropertyUtil.isSpecialProperty(targetPropertyName))
      return; 
    Validate.isTrue(QualifiedProperty.isSyntacticallyQualified(targetPropertyName), 
        String.format("The target property '%s' for alias property '%s' must be qualified!", new Object[] { targetPropertyName, aliasProperty }));
  }
}
