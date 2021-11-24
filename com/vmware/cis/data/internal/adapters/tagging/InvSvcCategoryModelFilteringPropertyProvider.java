package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class InvSvcCategoryModelFilteringPropertyProvider implements FilteringPropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  InvSvcCategoryModelFilteringPropertyProvider(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
  }
  
  public List<?> getKeys(PropertyPredicate predicate) {
    List<String> tagIds;
    assert predicate != null;
    if (!"inventoryservice:InventoryServiceCategory/childTags".equals(predicate.getProperty()))
      return null; 
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator())) {
      Object comparableValue = predicate.getComparableValue();
      tagIds = Collections.singletonList(toTagId(comparableValue));
    } else if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      Collection<?> comparableValues = (Collection)predicate.getComparableValue();
      tagIds = toTagIds(comparableValues);
    } else {
      return null;
    } 
    List<TagModel> tags = this._tagging.getTags(tagIds);
    if (tags.isEmpty())
      return Collections.EMPTY_LIST; 
    Collection<String> categoryIds = gatherCategoryIds(tags);
    return toMors(categoryIds);
  }
  
  private static Collection<String> gatherCategoryIds(List<TagModel> tags) {
    Set<String> categoryIds = new LinkedHashSet<>();
    for (TagModel tag : tags)
      categoryIds.add(tag.getCategoryId()); 
    return categoryIds;
  }
  
  private static List<String> toTagIds(Collection<?> keys) {
    assert keys != null;
    List<String> tagIds = new ArrayList<>(keys.size());
    for (Object key : keys) {
      String tagId = toTagId(key);
      tagIds.add(tagId);
    } 
    return tagIds;
  }
  
  private static String toTagId(Object key) {
    assert key != null;
    if (key instanceof ManagedObjectReference) {
      ManagedObjectReference ref = (ManagedObjectReference)key;
      return InvSvcTaggingIdConverter.taggingMorToId(ref);
    } 
    throw new IllegalArgumentException(String.format("Key for model '%s' must be a ManagedObjectReference and not %s: <%s>", new Object[] { "inventoryservice:InventoryServiceTag", key
            
            .getClass().getCanonicalName(), key }));
  }
  
  private static List<ManagedObjectReference> toMors(Collection<String> ids) {
    assert ids != null;
    List<ManagedObjectReference> refs = new ArrayList<>(ids.size());
    for (String id : ids) {
      ManagedObjectReference ref = InvSvcTaggingIdConverter.taggingIdToMor(id);
      refs.add(ref);
    } 
    return refs;
  }
}
