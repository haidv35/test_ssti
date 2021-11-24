package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.UnqualifiedProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

final class AggregatedModelPropertyConverter {
  private final String _aggregatedModel;
  
  private final String _childModel;
  
  private final Set<String> _childModelPropertiesNonQualified;
  
  AggregatedModelPropertyConverter(String aggregatedModel, String childModel, Set<String> childModelPropertiesNonQualified) {
    assert aggregatedModel != null;
    assert childModel != null;
    assert childModelPropertiesNonQualified != null;
    this._aggregatedModel = aggregatedModel;
    this._childModel = childModel;
    this._childModelPropertiesNonQualified = childModelPropertiesNonQualified;
  }
  
  public String toChildProperty(String aggregatedProperty) {
    assert aggregatedProperty != null;
    if (PropertyUtil.isSpecialProperty(aggregatedProperty))
      return aggregatedProperty; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(aggregatedProperty);
    String model = qualifiedProperty.getResourceModel();
    String propertyNonQualified = qualifiedProperty.getSimpleProperty();
    if (!this._aggregatedModel.equals(model))
      throw new UnsupportedOperationException(String.format("Property for unexpected model in query for aggregated model '%s': '%s'", new Object[] { this._aggregatedModel, aggregatedProperty })); 
    if (!this._childModelPropertiesNonQualified.contains(
        UnqualifiedProperty.getRootProperty(propertyNonQualified)))
      return null; 
    return QualifiedProperty.forModelAndSimpleProperty(this._childModel, propertyNonQualified)
      .toString();
  }
  
  public Collection<String> toChildModels(Collection<String> resourceModels) {
    assert resourceModels != null;
    return Collections.singletonList(this._childModel);
  }
}
