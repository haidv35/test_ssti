package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.ResourceItemUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class RelationshipResultJoin {
  public static ResultSet joinResults(ResultSet leftResult, String leftJoinProperty, boolean removeLeftJoinProperty, ResultSet rightResult, String rightJoinProperty, boolean removeRightJoinProperty) {
    assert leftResult != null;
    assert leftJoinProperty != null;
    assert rightResult != null;
    assert rightJoinProperty != null;
    if (rightResult.getItems().isEmpty())
      return ResultSet.EMPTY_RESULT; 
    JoinInfo leftJoinInfo = new JoinInfo(leftJoinProperty, leftResult.getProperties(), removeLeftJoinProperty);
    JoinInfo rightJoinInfo = new JoinInfo(rightJoinProperty, rightResult.getProperties(), removeRightJoinProperty);
    return joinResults(leftResult, leftJoinInfo, rightResult, rightJoinInfo);
  }
  
  private static ResultSet joinResults(ResultSet leftResult, JoinInfo leftJoinInfo, ResultSet rightResult, JoinInfo rightJoinInfo) {
    assert leftJoinInfo != null;
    assert rightJoinInfo != null;
    List<String> joinedProperties = getJoinedProperties(leftJoinInfo, rightJoinInfo);
    Map<Object, Collection<ResourceItem>> rightResourceItemsByJoinProperty = ResourceItemUtil.getResourceItemsByPropertyValue(rightJoinInfo.getPropertyIndex(), rightResult);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(joinedProperties);
    for (ResourceItem item : leftResult.getItems()) {
      Object leftJoinPropertyValue = item.getPropertyValues().get(leftJoinInfo
          .getPropertyIndex());
      Collection<ResourceItem> rightResourceItems = findJoinResourceItems(leftJoinPropertyValue, rightResourceItemsByJoinProperty);
      List<Object> propertyValues = joinResourceItems(leftJoinInfo, item, rightJoinInfo, rightResourceItems);
      resultBuilder.item(item.getKey(), propertyValues);
    } 
    return resultBuilder.totalCount(Integer.valueOf(leftResult.getItems().size())).build();
  }
  
  private static List<String> getJoinedProperties(JoinInfo leftJoinInfo, JoinInfo rightJoinInfo) {
    assert leftJoinInfo != null;
    assert rightJoinInfo != null;
    List<String> joinedProperties = new ArrayList<>();
    List<String> leftResultProperties = new ArrayList<>(leftJoinInfo.getResultProperties());
    if (leftJoinInfo.removeProperty())
      leftResultProperties.remove(leftJoinInfo.getPropertyIndex()); 
    joinedProperties.addAll(leftResultProperties);
    List<String> rightResultProperties = new ArrayList<>(rightJoinInfo.getResultProperties());
    if (rightJoinInfo.removeProperty())
      rightResultProperties.remove(rightJoinInfo.getPropertyIndex()); 
    joinedProperties.addAll(rightResultProperties);
    return joinedProperties;
  }
  
  private static Collection<ResourceItem> findJoinResourceItems(Object leftJoinPropertyValue, Map<Object, Collection<ResourceItem>> rightResourceItemsByJoinProperty) {
    assert rightResourceItemsByJoinProperty != null;
    if (leftJoinPropertyValue == null || rightResourceItemsByJoinProperty.isEmpty())
      return Collections.emptySet(); 
    if (leftJoinPropertyValue instanceof Object[])
      return findJoinResourceItemsByArrayValues((Object[])leftJoinPropertyValue, rightResourceItemsByJoinProperty); 
    if (leftJoinPropertyValue instanceof Collection)
      return findJoinResourceItemsByCollectionValues((Collection<Object>)leftJoinPropertyValue, rightResourceItemsByJoinProperty); 
    Collection<ResourceItem> joinedResourceItems = rightResourceItemsByJoinProperty.get(leftJoinPropertyValue);
    if (joinedResourceItems == null)
      return Collections.emptySet(); 
    return joinedResourceItems;
  }
  
  private static Collection<ResourceItem> findJoinResourceItemsByArrayValues(Object[] leftJoinPropertyValues, Map<Object, Collection<ResourceItem>> rightResourceItemsByJoinProperty) {
    assert leftJoinPropertyValues != null;
    assert !rightResourceItemsByJoinProperty.isEmpty();
    if (leftJoinPropertyValues.length == 0)
      return Collections.emptySet(); 
    Collection<ResourceItem> joinedResourceItems = new LinkedHashSet<>();
    for (Object joinPropValue : leftJoinPropertyValues) {
      Collection<ResourceItem> resourceItems = rightResourceItemsByJoinProperty.get(joinPropValue);
      if (resourceItems != null)
        joinedResourceItems.addAll(resourceItems); 
    } 
    return joinedResourceItems;
  }
  
  private static Collection<ResourceItem> findJoinResourceItemsByCollectionValues(Collection<Object> leftJoinPropertyValues, Map<Object, Collection<ResourceItem>> rightResourceItemsByJoinProperty) {
    assert leftJoinPropertyValues != null;
    assert !rightResourceItemsByJoinProperty.isEmpty();
    if (leftJoinPropertyValues.isEmpty())
      return Collections.emptySet(); 
    Collection<ResourceItem> joinedResourceItems = new LinkedHashSet<>();
    for (Object joinPropValue : leftJoinPropertyValues) {
      Collection<ResourceItem> resourceItems = rightResourceItemsByJoinProperty.get(joinPropValue);
      if (resourceItems == null)
        continue; 
      joinedResourceItems.addAll(resourceItems);
    } 
    return joinedResourceItems;
  }
  
  private static List<Object> joinResourceItems(JoinInfo leftJoinInfo, ResourceItem leftResourceItem, JoinInfo rightJoinInfo, Collection<ResourceItem> rightResourceItems) {
    assert leftResourceItem != null;
    assert rightResourceItems != null;
    int rightResultPropertiesCount = rightJoinInfo.getResultProperties().size();
    if (rightResultPropertiesCount < 1)
      return leftResourceItem.getPropertyValues(); 
    List<Object> joinedPropertyValues = new ArrayList(leftResourceItem.getPropertyValues());
    if (leftJoinInfo.removeProperty())
      joinedPropertyValues.remove(leftJoinInfo.getPropertyIndex()); 
    for (int p = 0; p < rightResultPropertiesCount; p++) {
      if (!rightJoinInfo.removeProperty() || p != rightJoinInfo.getPropertyIndex())
        joinedPropertyValues.add(ResourceItemUtil.compactResourceItemsValues(p, rightResourceItems, 
              (p == rightJoinInfo.getPropertyIndex()))); 
    } 
    return joinedPropertyValues;
  }
  
  private static final class JoinInfo {
    private final int _propertyIndex;
    
    private final List<String> _resultProperties;
    
    private final boolean _removeProperty;
    
    private JoinInfo(String property, List<String> resultProperties, boolean removeProperty) {
      assert property != null;
      assert resultProperties != null;
      int propertyIndex = resultProperties.lastIndexOf(property);
      if (propertyIndex < 0)
        throw new IllegalArgumentException(String.format("The given join property [%s] is not among the result properties!", new Object[] { property })); 
      this._propertyIndex = propertyIndex;
      this._resultProperties = resultProperties;
      this._removeProperty = removeProperty;
    }
    
    int getPropertyIndex() {
      return this._propertyIndex;
    }
    
    List<String> getResultProperties() {
      return this._resultProperties;
    }
    
    boolean removeProperty() {
      return this._removeProperty;
    }
  }
}
