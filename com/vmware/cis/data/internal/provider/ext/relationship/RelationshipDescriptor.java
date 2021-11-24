package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.internal.util.QualifiedProperty;

public final class RelationshipDescriptor {
  public static final char INVERSE_RELATIONSHIP_PROPERTY_SUFFIX = '~';
  
  private final String _name;
  
  private final boolean _isDefinedByTarget;
  
  public RelationshipDescriptor(String relationship) {
    assert relationship != null;
    String relationshipProperty = relationship;
    if (isInverseRelationship(relationshipProperty)) {
      relationshipProperty = relationshipProperty.substring(0, relationshipProperty
          .length() - 1);
      this._isDefinedByTarget = true;
    } else {
      this._isDefinedByTarget = false;
    } 
    QualifiedProperty qualifiedRelationship = QualifiedProperty.forQualifiedName(relationshipProperty);
    this._name = qualifiedRelationship.toString();
  }
  
  RelationshipDescriptor(String name, boolean isDefinedByTarget) {
    assert name != null;
    this._name = name;
    this._isDefinedByTarget = isDefinedByTarget;
  }
  
  public String getName() {
    return this._name;
  }
  
  public boolean isDefinedByTarget() {
    return this._isDefinedByTarget;
  }
  
  public static boolean isInverseRelationship(String relationshipProperty) {
    return relationshipProperty.endsWith(
        String.valueOf('~'));
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof RelationshipDescriptor))
      return false; 
    RelationshipDescriptor other = (RelationshipDescriptor)obj;
    return (this._name.equals(other._name) && this._isDefinedByTarget == other._isDefinedByTarget);
  }
  
  public int hashCode() {
    int hash = 19;
    hash = 31 * hash + this._name.hashCode();
    hash = 31 * hash + (this._isDefinedByTarget ? 1 : 0);
    return hash;
  }
  
  public String toString() {
    return "RelationshipDescriptor [_name=" + this._name + ", _isDefinedByTarget=" + this._isDefinedByTarget + "]";
  }
}
