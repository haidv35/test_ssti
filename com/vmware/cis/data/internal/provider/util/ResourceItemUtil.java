package com.vmware.cis.data.internal.provider.util;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

public final class ResourceItemUtil {
  public static List<Object> compactResourceItemsValues(int propertyIndex, Collection<ResourceItem> resourceItems, boolean deduplicateValues) {
    assert resourceItems != null;
    if (resourceItems.isEmpty())
      Collections.emptyList(); 
    List<Object> compactedPropertyValues = new ArrayList();
    for (ResourceItem item : resourceItems) {
      Object propertyValue = item.getPropertyValues().get(propertyIndex);
      if (propertyValue == null)
        continue; 
      if (propertyValue instanceof Object[]) {
        compactResourceItemValues(compactedPropertyValues, 
            Arrays.asList((Object[])propertyValue), deduplicateValues);
        continue;
      } 
      if (propertyValue instanceof Collection) {
        compactResourceItemValues(compactedPropertyValues, (Collection<Object>)propertyValue, deduplicateValues);
        continue;
      } 
      compactedPropertyValues.add(propertyValue);
    } 
    return compactedPropertyValues;
  }
  
  private static void compactResourceItemValues(List<Object> compactedPropertyValues, Collection<Object> propertyValues, boolean deduplicateValues) {
    if (deduplicateValues) {
      compactedPropertyValues.addAll(new LinkedHashSet(propertyValues));
    } else {
      compactedPropertyValues.addAll(propertyValues);
    } 
  }
  
  public static List<Object> flattenNotNullPropertyValues(List<Object> propertyValues) {
    if (CollectionUtils.isEmpty(propertyValues))
      return Collections.emptyList(); 
    Set<Object> flattenedPropertyValues = new LinkedHashSet();
    for (Object propertyValue : propertyValues) {
      if (propertyValue == null)
        continue; 
      if (propertyValue instanceof Object[]) {
        flattenedPropertyValues.addAll(Arrays.asList((Object[])propertyValue));
        continue;
      } 
      if (propertyValue instanceof Collection) {
        flattenedPropertyValues.addAll((Collection)propertyValue);
        continue;
      } 
      flattenedPropertyValues.add(propertyValue);
    } 
    return new ArrayList(flattenedPropertyValues);
  }
  
  public static List<ResourceItem> reorderResourceItems(List<ResourceItem> resourceItems, List<Integer> resourceItemsIndexPermutation) {
    assert resourceItems != null;
    assert resourceItemsIndexPermutation != null;
    if (resourceItems.isEmpty() || resourceItemsIndexPermutation.isEmpty())
      return resourceItems; 
    assert resourceItems.size() == resourceItemsIndexPermutation.size();
    ResourceItem[] reorderedResourceItems = new ResourceItem[resourceItems.size()];
    for (int i = 0; i < resourceItems.size(); i++)
      reorderedResourceItems[i] = resourceItems.get(((Integer)resourceItemsIndexPermutation.get(i)).intValue()); 
    return new ArrayList<>(Arrays.asList(reorderedResourceItems));
  }
  
  public static List<Integer> getResourceItemsPermutation(ResultSet leftResultSet, String leftJoinProperty, ResultSet rightResultSet, String rightJoinProperty, boolean notMappedItemsFirst) {
    assert leftResultSet != null;
    assert leftJoinProperty != null;
    assert rightResultSet != null;
    assert rightJoinProperty != null;
    if (leftResultSet.getItems().isEmpty() || rightResultSet.getItems().isEmpty())
      return Collections.emptyList(); 
    int leftPropertyIndex = leftResultSet.getProperties().lastIndexOf(leftJoinProperty);
    if (leftPropertyIndex < 0)
      throw new IllegalArgumentException(String.format("The given property [%s] is not among the result properties!", new Object[] { Integer.valueOf(leftPropertyIndex) })); 
    Map<Object, Integer> rightItemIndexByPropertyValue = mapItemIndexByPropertyValue(rightResultSet, rightJoinProperty);
    Map<Integer, Integer> leftToRightIndexMapping = new HashMap<>();
    int leftItemIndex = 0;
    for (ResourceItem leftItem : leftResultSet.getItems()) {
      Object leftPropertyValue = leftItem.getPropertyValues().get(leftPropertyIndex);
      if (leftPropertyValue instanceof Object[]) {
        for (Object propValue : (Object[])leftPropertyValue)
          mapLeftToRightIndex(propValue, Integer.valueOf(leftItemIndex), rightItemIndexByPropertyValue, leftToRightIndexMapping); 
      } else if (leftPropertyValue instanceof Collection) {
        for (Object propValue : leftPropertyValue)
          mapLeftToRightIndex(propValue, Integer.valueOf(leftItemIndex), rightItemIndexByPropertyValue, leftToRightIndexMapping); 
      } else {
        mapLeftToRightIndex(leftPropertyValue, Integer.valueOf(leftItemIndex), rightItemIndexByPropertyValue, leftToRightIndexMapping);
      } 
      leftItemIndex++;
    } 
    return getResourceItemsPermutation(leftResultSet
        .getItems().size(), leftToRightIndexMapping, notMappedItemsFirst);
  }
  
  private static List<Integer> getResourceItemsPermutation(int itemsCount, Map<Integer, Integer> indexMapping, boolean notMappedItemsFirst) {
    assert indexMapping != null;
    if (itemsCount <= 0 || indexMapping.isEmpty())
      return Collections.emptyList(); 
    Map<Integer, List<Integer>> itemsByNewIndex = new HashMap<>();
    int maxOrderIndex = -1;
    for (int i = 0; i < itemsCount; i++) {
      Integer newOrderIndex = indexMapping.get(Integer.valueOf(i));
      newOrderIndex = Integer.valueOf((newOrderIndex == null) ? -1 : newOrderIndex.intValue());
      maxOrderIndex = (newOrderIndex.intValue() > maxOrderIndex) ? newOrderIndex.intValue() : maxOrderIndex;
      List<Integer> itemsForIndex = itemsByNewIndex.get(newOrderIndex);
      if (itemsForIndex == null) {
        itemsForIndex = new ArrayList<>();
        itemsByNewIndex.put(newOrderIndex, itemsForIndex);
      } 
      itemsForIndex.add(Integer.valueOf(i));
    } 
    List<Integer> reorderedItemsIndices = new ArrayList<>();
    for (int j = 0; j <= maxOrderIndex; j++) {
      if (itemsByNewIndex.containsKey(Integer.valueOf(j)))
        reorderedItemsIndices.addAll(itemsByNewIndex.get(Integer.valueOf(j))); 
    } 
    List<Integer> notMappedItemsIndices = itemsByNewIndex.get(Integer.valueOf(-1));
    if (notMappedItemsIndices != null)
      if (notMappedItemsFirst) {
        reorderedItemsIndices.addAll(0, notMappedItemsIndices);
      } else {
        reorderedItemsIndices.addAll(notMappedItemsIndices);
      }  
    return reorderedItemsIndices;
  }
  
  private static void mapLeftToRightIndex(Object leftPropertyValue, Integer leftItemIndex, Map<Object, Integer> rightItemIndexByPropertyValue, Map<Integer, Integer> leftToRightIndexMapping) {
    assert rightItemIndexByPropertyValue != null;
    assert leftItemIndex.intValue() >= 0;
    if (leftPropertyValue == null || rightItemIndexByPropertyValue.isEmpty())
      return; 
    assert leftToRightIndexMapping != null;
    Integer rightItemIndex = rightItemIndexByPropertyValue.get(leftPropertyValue);
    if (rightItemIndex != null)
      leftToRightIndexMapping.put(leftItemIndex, rightItemIndex); 
  }
  
  private static Map<Object, Integer> mapItemIndexByPropertyValue(ResultSet result, String property) {
    assert result != null;
    assert property != null;
    if (result.getItems().isEmpty())
      return Collections.emptyMap(); 
    int propertyIndex = result.getProperties().lastIndexOf(property);
    if (propertyIndex < 0)
      throw new IllegalArgumentException(String.format("The given property [%s] is not among the result properties!", new Object[] { Integer.valueOf(propertyIndex) })); 
    Map<Object, Integer> smallestItemIndexByPropertyValue = new HashMap<>();
    int itemIndex = 0;
    for (ResourceItem item : result.getItems()) {
      Object propertyValue = item.getPropertyValues().get(propertyIndex);
      if (propertyValue == null)
        continue; 
      if (propertyValue instanceof Object[]) {
        for (Object propValue : (Object[])propertyValue)
          mapFirstItemIndexByPropertyValue(smallestItemIndexByPropertyValue, propValue, 
              Integer.valueOf(itemIndex)); 
      } else if (propertyValue instanceof Collection) {
        for (Object propValue : propertyValue)
          mapFirstItemIndexByPropertyValue(smallestItemIndexByPropertyValue, propValue, 
              Integer.valueOf(itemIndex)); 
      } else {
        mapFirstItemIndexByPropertyValue(smallestItemIndexByPropertyValue, propertyValue, 
            Integer.valueOf(itemIndex));
      } 
      itemIndex++;
    } 
    return smallestItemIndexByPropertyValue;
  }
  
  private static void mapFirstItemIndexByPropertyValue(Map<Object, Integer> smallestItemIndexByPropertyValue, Object propertyValue, Integer itemIndex) {
    if (propertyValue == null || smallestItemIndexByPropertyValue.containsKey(propertyValue))
      return; 
    smallestItemIndexByPropertyValue.put(propertyValue, itemIndex);
  }
  
  public static Map<Object, Collection<ResourceItem>> getResourceItemsByPropertyValue(int propertyIndex, ResultSet result) {
    assert propertyIndex >= 0;
    assert result != null;
    Map<Object, Collection<ResourceItem>> itemsByPropertyValue = new HashMap<>();
    List<Object> propertyValues = ResultSetAnalyzer.gatherPropertyValuesByIndexOrdered(result, propertyIndex);
    List<ResourceItem> resourceItems = result.getItems();
    assert propertyValues.size() == resourceItems.size();
    int i = 0;
    for (Object propertyValue : propertyValues)
      mapResourceItemByPropertyValue(itemsByPropertyValue, propertyValue, resourceItems.get(i++)); 
    return itemsByPropertyValue;
  }
  
  private static void mapResourceItemByPropertyValue(Map<Object, Collection<ResourceItem>> itemsByPropertyValue, Object propertyValue, ResourceItem resourceItem) {
    if (propertyValue == null)
      return; 
    assert itemsByPropertyValue != null;
    assert resourceItem != null;
    if (propertyValue instanceof Object[]) {
      for (Object propValue : (Object[])propertyValue)
        mapItemByPropertyValue(itemsByPropertyValue, propValue, resourceItem); 
    } else if (propertyValue instanceof Collection) {
      for (Object propValue : propertyValue)
        mapItemByPropertyValue(itemsByPropertyValue, propValue, resourceItem); 
    } else {
      mapItemByPropertyValue(itemsByPropertyValue, propertyValue, resourceItem);
    } 
  }
  
  private static void mapItemByPropertyValue(Map<Object, Collection<ResourceItem>> itemsByPropertyValue, Object propertyValue, ResourceItem resourceItem) {
    Collection<ResourceItem> resourceItemsForProperty = itemsByPropertyValue.get(propertyValue);
    if (resourceItemsForProperty == null) {
      resourceItemsForProperty = new ArrayList<>();
      itemsByPropertyValue.put(propertyValue, resourceItemsForProperty);
    } 
    resourceItemsForProperty.add(resourceItem);
  }
}
