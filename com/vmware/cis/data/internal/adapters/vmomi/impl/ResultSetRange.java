package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.merge.DefaultItemComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class ResultSetRange {
  private final List<String> _properties;
  
  private final List<ResourceItem> _items;
  
  private final Integer _totalCount;
  
  private final int _offset;
  
  private final int _limit;
  
  ResultSetRange(ResultSet resultSet, int offset, int limit) {
    assert resultSet != null;
    assert resultSet.getProperties().contains("@modelKey");
    this._offset = offset;
    this._limit = limit;
    this._properties = resultSet.getProperties();
    this._items = resultSet.getItems();
    this._totalCount = resultSet.getTotalCount();
  }
  
  private ResultSetRange(List<String> properties, List<ResourceItem> items, Integer totalCount, int offset, int limit) {
    this._offset = offset;
    this._limit = limit;
    this._properties = properties;
    this._items = items;
    this._totalCount = totalCount;
  }
  
  public int getOffset() {
    return this._offset;
  }
  
  public boolean isEmptyPage(int offset, int limit) {
    return (getPageSize(offset, limit) == 0);
  }
  
  public Collection<?> getAbsent(Collection<?> keys) {
    assert keys != null;
    List<Object> absentKeys = new ArrayList();
    for (Object key : keys) {
      if (contains(key))
        continue; 
      absentKeys.add(key);
    } 
    return absentKeys;
  }
  
  public ResultSetRange exclude(Collection<Object> keysToExclude) {
    assert keysToExclude != null;
    List<ResourceItem> modifiedItems = new ArrayList<>();
    int excludedKeysCount = 0;
    for (ResourceItem item : this._items) {
      if (keysToExclude.contains(item.getKey())) {
        excludedKeysCount++;
        continue;
      } 
      modifiedItems.add(item);
    } 
    Integer modifiedTotalCount = this._totalCount;
    if (modifiedTotalCount != null) {
      int newTotalCount = this._totalCount.intValue() - excludedKeysCount;
      modifiedTotalCount = Integer.valueOf((newTotalCount < modifiedItems.size()) ? modifiedItems
          .size() : newTotalCount);
    } 
    return new ResultSetRange(this._properties, modifiedItems, modifiedTotalCount, this._offset, this._limit);
  }
  
  public ResultSetRange exclude(List<ResourceItem> itemsToExclude, List<SortCriterion> sortCriteria) {
    assert itemsToExclude != null;
    assert sortCriteria != null;
    assert !sortCriteria.isEmpty();
    if (this._items.isEmpty())
      throw new IllegalArgumentException("Exclude cannot be invoked on empty result set range."); 
    List<String> properties = new ArrayList<>(sortCriteria.size());
    for (SortCriterion sort : sortCriteria)
      properties.add(sort.getProperty()); 
    Comparator<ResourceItem> itemComparator = new DefaultItemComparator(properties, sortCriteria);
    ResourceItem firstItem = this._items.get(0);
    int modifiedOffset = this._offset;
    for (ResourceItem item : itemsToExclude) {
      int comparison = itemComparator.compare(item, firstItem);
      assert comparison != 0;
      if (comparison < 0 && modifiedOffset != 0)
        modifiedOffset--; 
    } 
    Integer modifiedTotalCount = this._totalCount;
    if (modifiedTotalCount != null) {
      int newTotalCount = this._totalCount.intValue() - itemsToExclude.size();
      modifiedTotalCount = Integer.valueOf((newTotalCount < this._items.size()) ? this._items
          .size() : newTotalCount);
    } 
    return new ResultSetRange(this._properties, this._items, modifiedTotalCount, modifiedOffset, this._limit);
  }
  
  public ResultSet page(int offset, int limit) {
    List<ResourceItem> pagedItems;
    int size = getPageSize(offset, limit);
    if (size == 0) {
      pagedItems = Collections.emptyList();
    } else {
      int startPosition = offset - this._offset;
      pagedItems = this._items.subList(startPosition, startPosition + size);
    } 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(this._properties);
    for (ResourceItem item : pagedItems)
      resultBuilder.item(item.getKey(), item.getPropertyValues()); 
    return resultBuilder.totalCount(this._totalCount).build();
  }
  
  private int getPageSize(int offset, int limit) {
    int rangeLimitPosition = getLimitPosition(this._offset, this._limit);
    assert offset >= this._offset && offset <= rangeLimitPosition;
    assert getLimitPosition(offset, limit) <= rangeLimitPosition;
    if (limit == 0)
      return 0; 
    if (this._items.isEmpty())
      return 0; 
    assert this._limit != 0;
    int startPosition = offset - this._offset;
    if (startPosition >= this._items.size())
      return 0; 
    if (limit < 0)
      return this._items.size() - startPosition; 
    int endPosition = (int)Math.min(2147483647L, startPosition + this._limit);
    if (endPosition > this._items.size())
      return this._items.size() - startPosition; 
    return endPosition - startPosition;
  }
  
  private boolean contains(Object key) {
    for (ResourceItem item : this._items) {
      if (key.equals(item.getKey()))
        return true; 
    } 
    return false;
  }
  
  private static int getLimitPosition(int offset, int limit) {
    if (limit < 0)
      return Integer.MAX_VALUE; 
    return (int)Math.min(2147483647L, offset + limit);
  }
}
