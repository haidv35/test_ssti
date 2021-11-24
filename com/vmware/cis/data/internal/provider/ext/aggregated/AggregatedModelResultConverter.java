package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class AggregatedModelResultConverter {
  private final AggregatedModelPropertyConverter _propertyConverter;
  
  public AggregatedModelResultConverter(String aggregatedModel, String childModel, Set<String> childModelPropertiesNonQualified) {
    assert aggregatedModel != null;
    assert childModel != null;
    assert childModelPropertiesNonQualified != null;
    this._propertyConverter = new AggregatedModelPropertyConverter(aggregatedModel, childModel, childModelPropertiesNonQualified);
  }
  
  public ResultSet fromChildResult(ResultSet childResult, List<String> aggregatedProperties) {
    assert childResult != null;
    assert aggregatedProperties != null;
    return renameAndReorder(childResult, aggregatedProperties);
  }
  
  private ResultSet renameAndReorder(ResultSet childResult, List<String> aggregatedProperties) {
    assert childResult != null;
    assert aggregatedProperties != null;
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(aggregatedProperties);
    for (ResourceItem childItem : childResult.getItems()) {
      List<Object> aggregatedItem = renameAndReorder(childItem, aggregatedProperties);
      resultBuilder.item(childItem.getKey(), aggregatedItem);
    } 
    return resultBuilder.totalCount(childResult.getTotalCount()).build();
  }
  
  private List<Object> renameAndReorder(ResourceItem childItem, List<String> aggregatedProperties) {
    assert childItem != null;
    assert aggregatedProperties != null;
    List<Object> values = new ArrayList(aggregatedProperties.size());
    for (String aggregatedProperty : aggregatedProperties) {
      String childProperty = this._propertyConverter.toChildProperty(aggregatedProperty);
      Object value = null;
      if (childProperty != null)
        value = childItem.get(childProperty); 
      values.add(value);
    } 
    return values;
  }
}
