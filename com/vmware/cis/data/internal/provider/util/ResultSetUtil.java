package com.vmware.cis.data.internal.provider.util;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyByName;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyValueByNameViaIndexMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class ResultSetUtil {
  public static ResultSet extractPropertyFromResultSet(ResultSet result, String propertyName) {
    assert result != null;
    assert propertyName != null;
    List<String> resultProperties = result.getProperties();
    int propertyIndex = resultProperties.indexOf(propertyName);
    if (propertyIndex < 0)
      return ResultSet.EMPTY_RESULT; 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(new String[] { propertyName });
    for (ResourceItem item : result.getItems()) {
      Object propertyValue = item.getPropertyValues().get(propertyIndex);
      resultBuilder.item(item.getKey(), new Object[] { propertyValue });
    } 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  public static ResultSet removePropertyFromResultSet(ResultSet result, String propertyName) {
    assert result != null;
    assert propertyName != null;
    List<String> resultProperties = new ArrayList<>(result.getProperties());
    int propertyIndex = resultProperties.indexOf(propertyName);
    if (propertyIndex < 0)
      return result; 
    if (resultProperties.size() == 1)
      return ResultSet.EMPTY_RESULT; 
    resultProperties.remove(propertyIndex);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(resultProperties);
    for (ResourceItem item : result.getItems()) {
      List<Object> newPropertyValues = new ArrayList(item.getPropertyValues());
      newPropertyValues.remove(propertyIndex);
      resultBuilder.item(item.getKey(), newPropertyValues);
    } 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  public static List<Object> extractNotNullPropertyValues(ResultSet resultSet, String propertyName) {
    Validate.notNull(resultSet);
    Validate.notEmpty(propertyName);
    if (resultSet.getItems().isEmpty())
      return Collections.emptyList(); 
    List<String> resultProperties = resultSet.getProperties();
    int propertyIndex = resultProperties.lastIndexOf(propertyName);
    if (propertyIndex < 0)
      throw new IllegalArgumentException(String.format("The given join property [%s] is not among the result properties!", new Object[] { Integer.valueOf(propertyIndex) })); 
    List<Object> propertyValues = ResultSetAnalyzer.gatherPropertyValuesByIndexOrdered(resultSet, propertyIndex);
    propertyValues = ResourceItemUtil.flattenNotNullPropertyValues(propertyValues);
    return Collections.unmodifiableList(propertyValues);
  }
  
  public static ResultSet reorderResultByPropertyValuesOrder(ResultSet result, String orderProperty, List<Object> orderedValues, boolean notMatchedItemsFirst) {
    assert result != null;
    assert orderProperty != null;
    assert orderedValues != null;
    List<ResourceItem> resultItems = result.getItems();
    if (resultItems.isEmpty() || orderedValues.isEmpty())
      return result; 
    int orderPropertyIndex = result.getProperties().lastIndexOf(orderProperty);
    if (orderPropertyIndex < 0)
      throw new IllegalArgumentException(String.format("The given property [%s] is not among the result properties!", new Object[] { Integer.valueOf(orderPropertyIndex) })); 
    Map<Object, List<ResourceItem>> orderedResourceItemsByValue = new LinkedHashMap<>();
    List<ResourceItem> notMatchedResourceItems = new ArrayList<>();
    for (Object orderValue : orderedValues)
      orderedResourceItemsByValue.put(orderValue, new ArrayList<>()); 
    for (ResourceItem resultItem : resultItems) {
      Object propertyValue = resultItem.getPropertyValues().get(orderPropertyIndex);
      List<ResourceItem> orderedResourceItems = orderedResourceItemsByValue.get(propertyValue);
      if (orderedResourceItems != null) {
        orderedResourceItems.add(resultItem);
        continue;
      } 
      notMatchedResourceItems.add(resultItem);
    } 
    List<ResourceItem> reorderedResultItems = new ArrayList<>(resultItems.size());
    for (List<ResourceItem> orderedResourceItems : orderedResourceItemsByValue.values())
      reorderedResultItems.addAll(orderedResourceItems); 
    if (notMatchedItemsFirst) {
      reorderedResultItems.addAll(0, notMatchedResourceItems);
    } else {
      reorderedResultItems.addAll(notMatchedResourceItems);
    } 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(result.getProperties());
    for (ResourceItem item : reorderedResultItems)
      resultBuilder.item(item.getKey(), item.getPropertyValues()); 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  public static ResultSet reorderResultByIndices(ResultSet result, List<Integer> resourceItemsIndexPermutation) {
    assert result != null;
    assert resourceItemsIndexPermutation != null;
    List<ResourceItem> resultItems = result.getItems();
    if (resultItems.isEmpty() || resourceItemsIndexPermutation.isEmpty())
      return result; 
    List<ResourceItem> reorderedItems = ResourceItemUtil.reorderResourceItems(resultItems, resourceItemsIndexPermutation);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(result.getProperties());
    for (ResourceItem item : reorderedItems)
      resultBuilder.item(item.getKey(), item.getPropertyValues()); 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  public static ResultSet applyLimitAndOffset(ResultSet resultSet, int limit, int offset) {
    List<ResourceItem> boundedItems;
    if (resultSet == null)
      return resultSet; 
    if (limit == 0)
      return ResultSet.Builder.properties(new String[0])
        .totalCount(resultSet.getTotalCount())
        .build(); 
    if (limit < 0 && offset == 0)
      return resultSet; 
    assert offset >= 0;
    List<ResourceItem> items = resultSet.getItems();
    if (offset > items.size()) {
      boundedItems = Collections.emptyList();
    } else {
      int endIndex;
      if (limit < 0 || limit + offset > items.size()) {
        if (offset == 0)
          return resultSet; 
        endIndex = items.size();
      } else {
        endIndex = limit + offset;
      } 
      boundedItems = items.subList(offset, endIndex);
    } 
    ResultSet.Builder boundedResultBuilder = ResultSet.Builder.properties(resultSet.getProperties());
    for (ResourceItem item : boundedItems)
      boundedResultBuilder.item(item.getKey(), item.getPropertyValues()); 
    return boundedResultBuilder.totalCount(resultSet.getTotalCount()).build();
  }
  
  public static ResultSet project(ResultSet result, List<String> projection) {
    assert result != null;
    assert projection != null;
    if (result.getProperties().equals(projection))
      return result; 
    ResourceItemPropertyByName propertyByName = new ResourceItemPropertyValueByNameViaIndexMap(result.getProperties());
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(projection);
    for (ResourceItem item : result.getItems()) {
      List<Object> values = new ArrayList(projection.size());
      for (String property : projection) {
        Object value = propertyByName.getValue(property, item);
        values.add(value);
      } 
      resultBuilder.item(item.getKey(), values);
    } 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  public static ResultSet emptyResult(Query query) {
    assert query != null;
    return ResultSet.Builder.properties(query.getProperties())
      .totalCount(query.getWithTotalCount() ? Integer.valueOf(0) : null)
      .build();
  }
  
  public static ResultSet toResult(Collection<?> keys, List<String> properties, List<Collection<?>> columns) {
    assert keys != null;
    assert properties != null;
    assert columns != null;
    List<List<Object>> rows = columnsToRows(columns, keys.size());
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    assert keys.size() == rows.size();
    Iterator<List<Object>> rowIterator = rows.iterator();
    for (Object key : keys) {
      List<Object> row = rowIterator.next();
      resultBuilder.item(key, row);
    } 
    ResultSet result = resultBuilder.build();
    return result;
  }
  
  private static List<List<Object>> columnsToRows(Collection<Collection<?>> columns, int rowCount) {
    assert columns != null;
    assert !columns.isEmpty();
    assert rowCount > 0;
    Collection<Iterator<?>> iterators = iterators(columns);
    List<List<Object>> rows = new ArrayList<>(rowCount);
    for (int i = 0; i < rowCount; i++) {
      List<Object> values = new ArrayList(columns.size());
      for (Iterator<?> it : iterators) {
        assert it.hasNext();
        Object value = it.next();
        values.add(value);
      } 
      rows.add(values);
    } 
    return rows;
  }
  
  private static Collection<Iterator<?>> iterators(Collection<Collection<?>> columns) {
    assert columns != null;
    List<Iterator<?>> iterators = new ArrayList<>(columns.size());
    for (Collection<?> column : columns) {
      Iterator<?> it = column.iterator();
      iterators.add(it);
    } 
    return iterators;
  }
}
