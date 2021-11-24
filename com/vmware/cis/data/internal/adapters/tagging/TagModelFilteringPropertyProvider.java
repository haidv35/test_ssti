package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.internal.provider.util.filter.OperatorLikeEvaluator;
import com.vmware.cis.data.internal.provider.util.filter.PredicateEvaluator;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class TagModelFilteringPropertyProvider implements FilteringPropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  TagModelFilteringPropertyProvider(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
  }
  
  public List<?> getKeys(PropertyPredicate predicate) {
    assert predicate != null;
    if (!"com.vmware.cis.tagging.TagModel/name".equals(predicate.getProperty()) && 
      !"inventoryservice:InventoryServiceTag/name".equals(predicate.getProperty()))
      return null; 
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()) && 
      !PropertyPredicate.ComparisonOperator.LIKE.equals(predicate.getOperator()))
      return null; 
    if (!(predicate.getComparableValue() instanceof String))
      return null; 
    List<String> tagIds = findTagIds(predicate);
    if ("inventoryservice:InventoryServiceTag/name".equals(predicate.getProperty()))
      return toMors(tagIds); 
    return tagIds;
  }
  
  private List<String> findTagIds(PropertyPredicate predicate) {
    boolean needExtraFiltering;
    String tagNamePattern;
    assert predicate != null;
    String comparable = (String)predicate.getComparableValue();
    if (PropertyPredicate.ComparisonOperator.LIKE.equals(predicate.getOperator())) {
      OperatorLikeEvaluator.StringMatchingInfo matchInfo = OperatorLikeEvaluator.analyzeTemplate(comparable);
      needExtraFiltering = (!OperatorLikeEvaluator.StringMatchingMode.Contains.equals(matchInfo.getMode()) || !predicate.isIgnoreCase());
      tagNamePattern = matchInfo.getSearchText();
    } else {
      needExtraFiltering = true;
      tagNamePattern = comparable;
    } 
    List<String> tagIds = this._tagging.findTagsByName(tagNamePattern);
    if (!needExtraFiltering)
      return tagIds; 
    List<TagModel> tagModels = this._tagging.getTags(tagIds);
    List<String> filtered = new ArrayList<>(tagIds.size());
    for (TagModel tagModel : tagModels) {
      if (PredicateEvaluator.eval(predicate, tagModel.getName()))
        filtered.add(tagModel.getId()); 
    } 
    return filtered;
  }
  
  private static List<ManagedObjectReference> toMors(Collection<String> tagIds) {
    assert tagIds != null;
    List<ManagedObjectReference> refs = new ArrayList<>(tagIds.size());
    for (String tagId : tagIds) {
      ManagedObjectReference ref = InvSvcTaggingIdConverter.taggingIdToMor(tagId);
      refs.add(ref);
    } 
    return refs;
  }
}
