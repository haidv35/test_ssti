package com.vmware.cis.data.internal.provider.ext.relationship;

final class RelationshipHop {
  private final String _sourceModelProperty;
  
  private final String _targetModelProperty;
  
  RelationshipHop(String sourceModelProperty, String targetModelProperty) {
    assert sourceModelProperty != null;
    assert targetModelProperty != null;
    this._sourceModelProperty = sourceModelProperty;
    this._targetModelProperty = targetModelProperty;
  }
  
  public static final RelationshipHop buildForRelationshipDescriptor(RelationshipDescriptor relationshipDescriptor) {
    String sourceModelProperty;
    String targetModelProperty;
    assert relationshipDescriptor != null;
    if (relationshipDescriptor.isDefinedByTarget()) {
      sourceModelProperty = "@modelKey";
      targetModelProperty = relationshipDescriptor.getName();
    } else {
      sourceModelProperty = relationshipDescriptor.getName();
      targetModelProperty = "@modelKey";
    } 
    return new RelationshipHop(sourceModelProperty, targetModelProperty);
  }
  
  public String getSourceModelProperty() {
    return this._sourceModelProperty;
  }
  
  public String getTargetModelProperty() {
    return this._targetModelProperty;
  }
}
