package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.model.Property;
import com.vmware.cis.data.model.Relationship;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public final class RelatedPropertyDescriptor {
  private final String _name;
  
  private final Class<?> _type;
  
  private final String _targetName;
  
  private final List<RelationshipDescriptor> _relationships;
  
  public static RelatedPropertyDescriptor fromField(String resourceModel, Field relatedPropertyField, boolean allowModelOverwrite) {
    Validate.notEmpty(resourceModel, "Model name must not be empty!");
    Validate.notNull(relatedPropertyField, "Related property field must not be null!");
    validateRelatedPropertyField(relatedPropertyField);
    String[] relationships = getRelationships(relatedPropertyField);
    String targetPropertyName = getTargetPropertyName(relatedPropertyField);
    return of(resourceModel, relatedPropertyField.getName(), relatedPropertyField
        .getType(), relationships, targetPropertyName, allowModelOverwrite);
  }
  
  public static RelatedPropertyDescriptor of(String resourceModel, String simpleProperty, Class<?> type, String[] relationships, String targetName, boolean allowModelOverwrite) {
    String relatedProperty = QualifiedProperty.forModelAndSimpleProperty(resourceModel, simpleProperty).toString();
    validateRelatedPropertyData(relatedProperty, type, targetName, relationships);
    List<RelationshipDescriptor> relationshipDescriptors = getRelationshipDescriptors(resourceModel, relationships, allowModelOverwrite);
    return new RelatedPropertyDescriptor(relatedProperty, type, targetName, relationshipDescriptors);
  }
  
  RelatedPropertyDescriptor(String name, Class<?> type, String targetName, List<RelationshipDescriptor> relationships) {
    assert name != null;
    assert type != null;
    assert targetName != null;
    assert relationships != null;
    assert !relationships.isEmpty();
    this._name = name;
    this._type = type;
    this._targetName = targetName;
    this._relationships = relationships;
  }
  
  public String getSourceModelProperty() {
    String sourceModelProperty;
    RelationshipDescriptor firstRelationshipDescriptor = getRelationships().get(0);
    if (firstRelationshipDescriptor.isDefinedByTarget()) {
      sourceModelProperty = "@modelKey";
    } else {
      sourceModelProperty = firstRelationshipDescriptor.getName();
    } 
    return sourceModelProperty;
  }
  
  public String getName() {
    return this._name;
  }
  
  public Class<?> getType() {
    return this._type;
  }
  
  public String getTargetName() {
    return this._targetName;
  }
  
  public List<RelationshipDescriptor> getRelationships() {
    return this._relationships;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof RelatedPropertyDescriptor))
      return false; 
    RelatedPropertyDescriptor other = (RelatedPropertyDescriptor)obj;
    return (this._name.equals(other._name) && this._type
      .equals(other._type) && this._targetName
      .equals(other._targetName) && this._relationships
      .equals(other._relationships));
  }
  
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + this._name.hashCode();
    hash = 31 * hash + this._type.hashCode();
    hash = 31 * hash + this._targetName.hashCode();
    hash = 31 * hash + this._relationships.hashCode();
    return hash;
  }
  
  public String toString() {
    return "RelatedPropertyDescriptor [_name=" + this._name + ", _type=" + this._type
      .getName() + ", _targetName=" + this._targetName + ", _relationships=" + this._relationships + "]";
  }
  
  private static String getTargetPropertyName(Field relatedPropertyField) {
    Property property = relatedPropertyField.<Property>getAnnotation(Property.class);
    return property.value();
  }
  
  private static String[] getRelationships(Field relatedPropertyField) {
    Relationship relationship = relatedPropertyField.<Relationship>getAnnotation(Relationship.class);
    return relationship.value();
  }
  
  private static List<RelationshipDescriptor> getRelationshipDescriptors(String resourceModel, String[] relationships, boolean allowModelOverwrite) {
    List<RelationshipDescriptor> relationshipDescriptors = new ArrayList<>();
    for (int i = 0; i < relationships.length; i++) {
      String relationship = relationships[i];
      if (i == 0 && allowModelOverwrite && 
        !RelationshipDescriptor.isInverseRelationship(relationship))
        relationship = PropertyUtil.changeResourceModel(relationship, resourceModel); 
      relationshipDescriptors.add(new RelationshipDescriptor(relationship));
    } 
    return relationshipDescriptors;
  }
  
  private static void validateRelatedPropertyField(Field relatedPropertyField) {
    assert relatedPropertyField != null;
    assert relatedPropertyField.isAnnotationPresent((Class)Property.class);
    assert relatedPropertyField.isAnnotationPresent((Class)Relationship.class);
  }
  
  private static void validateRelatedPropertyData(String relatedProperty, Class<?> relatedPropertyType, String targetPropertyName, String[] relationships) {
    assert relatedProperty != null;
    assert relatedPropertyType != null;
    assert relationships != null;
    ModelKeyConverter.validateModelKeyPropertyType(relatedProperty, relatedPropertyType);
    if (StringUtils.isEmpty(targetPropertyName)) {
      String msg = String.format("The target property for related property '%s' must not be empty!", new Object[] { relatedProperty });
      throw new IllegalArgumentException(msg);
    } 
    if (!QualifiedProperty.isSyntacticallyQualified(targetPropertyName)) {
      String msg = String.format("The target property '%s' for related property '%s' must be qualified!", new Object[] { targetPropertyName, relatedProperty });
      throw new IllegalArgumentException(msg);
    } 
    if (ArrayUtils.isEmpty((Object[])relationships)) {
      String msg = String.format("The defined related property '%s' must have at least one relationship defined!", new Object[] { relatedProperty });
      throw new IllegalArgumentException(msg);
    } 
    if (relationships.length > 3) {
      String msg = String.format("The defined related property '%s' contains %d multi-hop relationships which is more that the allowed maximum level of %d!", new Object[] { relatedProperty, 
            
            Integer.valueOf(relationships.length), Integer.valueOf(3) });
      throw new IllegalArgumentException(msg);
    } 
    for (String relationship : relationships) {
      if (StringUtils.isEmpty(relationship)) {
        String msg = String.format("Empty relationship defined for related property '%s'!", new Object[] { relatedProperty });
        throw new IllegalArgumentException(msg);
      } 
    } 
  }
}
