package com.vmware.cis.data.internal.provider.merge;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class ResultMergePolicy {
  private final SequenceMergePolicy<ResourceItem> _itemMergePolicy;
  
  public ResultMergePolicy(SequenceMergePolicy<ResourceItem> itemMergePolicy) {
    assert itemMergePolicy != null;
    this._itemMergePolicy = itemMergePolicy;
  }
  
  public ResultSet merge(Collection<ResultSet> results, boolean withTotalCount, int offset, int limit) {
    assert results != null;
    assert !results.isEmpty();
    assert offset >= 0;
    Integer totalCount = sumTotalCount(results, withTotalCount);
    if (limit != 0) {
      Iterator<ResourceItem> mergedItemsIterator = this._itemMergePolicy.merge(itemIterators(results));
      ResultSet.Builder resultBuilder = ResultSet.Builder.properties(((ResultSet)results.iterator().next()).getProperties());
      List<ResourceItem> pagedItems = page(mergedItemsIterator, offset, limit);
      for (ResourceItem item : pagedItems)
        resultBuilder.item(item.getKey(), item.getPropertyValues()); 
      return resultBuilder.totalCount(sumTotalCount(results, withTotalCount))
        .build();
    } 
    return ResultSet.Builder.properties(new String[0])
      .totalCount(totalCount)
      .build();
  }
  
  private static Integer sumTotalCount(Collection<ResultSet> results, boolean withTotalCount) {
    assert results != null;
    if (!withTotalCount)
      return null; 
    int sum = 0;
    for (ResultSet result : results) {
      assert result != null;
      assert result.getTotalCount() != null;
      sum += result.getTotalCount().intValue();
    } 
    return Integer.valueOf(sum);
  }
  
  private static List<ResourceItem> page(Iterator<ResourceItem> itemIterator, int offset, int limit) {
    List<ResourceItem> items;
    if (limit > 0) {
      items = new ArrayList<>(Math.min(limit, 128));
    } else {
      items = new ArrayList<>();
    } 
    long itemCount = 0L;
    long endOfPage = offset + limit;
    while (itemIterator.hasNext()) {
      ResourceItem item = itemIterator.next();
      itemCount++;
      if (itemCount > offset)
        items.add(item); 
      if (limit > 0 && itemCount >= endOfPage)
        break; 
    } 
    return items;
  }
  
  private static Collection<Iterator<ResourceItem>> itemIterators(Collection<ResultSet> results) {
    assert results != null;
    List<Iterator<ResourceItem>> itemIterators = new ArrayList<>(results.size());
    for (ResultSet result : results)
      itemIterators.add(result.getItems().iterator()); 
    return itemIterators;
  }
}
