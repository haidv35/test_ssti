package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelLookup;
import com.vmware.cis.data.internal.provider.ext.aggregated.DefaultAggregatedModels;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

final class RelationshipClientSideTypeFilter {
  static boolean mayFilterModelKeysOnClientSide(Query query) {
    assert query != null;
    for (String property : query.getProperties()) {
      if (!"@modelKey".equals(property) && !"@type".equals(property))
        return false; 
    } 
    if (!mayFilterOnClientSide(query.getFilter()))
      return false; 
    if (!maySortOnClientSide(query.getSortCriteria()))
      return false; 
    return true;
  }
  
  static ResultSet filterModelKeysOnClientSide(Query query) {
    assert mayFilterModelKeysOnClientSide(query);
    List<ManagedObjectReference> filteredResources = filterResources(query
        .getResourceModels(), query.getFilter().getCriteria().get(0));
    orderResources(filteredResources, query.getSortCriteria());
    ResultSet result = createResult(filteredResources, query);
    return result;
  }
  
  private static List<ManagedObjectReference> filterResources(Collection<String> resourceModels, PropertyPredicate predicate) {
    List<ManagedObjectReference> filteredResources = new ArrayList<>();
    Object comparableValue = predicate.getComparableValue();
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator())) {
      ManagedObjectReference mor = (ManagedObjectReference)comparableValue;
      if (acceptItem(mor, resourceModels))
        filteredResources.add(mor); 
    } else if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      for (Object value : comparableValue) {
        ManagedObjectReference mor = (ManagedObjectReference)value;
        if (acceptItem(mor, resourceModels))
          filteredResources.add(mor); 
      } 
    } else {
      throw new IllegalArgumentException("Only EQUAL or IN operators are supported for filtering by @type on client-side");
    } 
    return filteredResources;
  }
  
  private static void orderResources(List<ManagedObjectReference> filteredResources, List<SortCriterion> sortCriteria) {
    assert filteredResources != null;
    if (sortCriteria.isEmpty())
      return; 
    final int multiplier = SortCriterion.SortDirection.ASCENDING.equals(((SortCriterion)sortCriteria.get(0)).getSortDirection()) ? 1 : -1;
    Collections.sort(filteredResources, new Comparator<ManagedObjectReference>() {
          public int compare(ManagedObjectReference o1, ManagedObjectReference o2) {
            return multiplier * o1.getValue().compareTo(o2.getValue());
          }
        });
  }
  
  private static ResultSet createResult(List<ManagedObjectReference> filteredResources, Query query) {
    int limit = query.getLimit();
    if (limit == 0) {
      assert query.getWithTotalCount();
      return ResultSet.Builder.properties(new String[0]).totalCount(Integer.valueOf(filteredResources.size())).build();
    } 
    List<String> properties = query.getProperties();
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    int offset = query.getOffset();
    long endOfPage = offset + limit;
    long itemCount = 0L;
    Iterator<ManagedObjectReference> resourcesIterator = filteredResources.iterator();
    while (resourcesIterator.hasNext()) {
      ManagedObjectReference mor = resourcesIterator.next();
      itemCount++;
      if (itemCount > offset)
        resultBuilder.item(mor, getPropertyValues(mor, properties)); 
      if (limit > 0 && itemCount >= endOfPage)
        break; 
    } 
    if (query.getWithTotalCount())
      resultBuilder.totalCount(Integer.valueOf(filteredResources.size())); 
    return resultBuilder.build();
  }
  
  private static boolean acceptItem(ManagedObjectReference mor, Collection<String> resourceModels) {
    if (resourceModels.contains(mor.getType()))
      return true; 
    AggregatedModelLookup aggregatedModels = DefaultAggregatedModels.getModelLookup();
    Collection<String> resourceModelsWithChildren = new ArrayList<>(resourceModels);
    for (String resourceModel : resourceModels)
      resourceModelsWithChildren.addAll(aggregatedModels
          .getChildrenOfAggregatedModel(resourceModel)); 
    return resourceModelsWithChildren.contains(mor.getType());
  }
  
  private static List<Object> getPropertyValues(ManagedObjectReference mor, List<String> properties) {
    assert mor != null;
    assert properties != null;
    List<Object> propertyValues = new ArrayList(properties.size());
    for (String property : properties) {
      if (PropertyUtil.isModelKey(property)) {
        propertyValues.add(mor);
        continue;
      } 
      if (PropertyUtil.isType(property)) {
        propertyValues.add(mor.getType());
        continue;
      } 
      throw new IllegalArgumentException("Only @modelKey and/or @type property may be selected for filtering by @type on client-side");
    } 
    return propertyValues;
  }
  
  private static boolean mayFilterOnClientSide(Filter filter) {
    if (filter == null)
      return false; 
    List<PropertyPredicate> predicates = filter.getCriteria();
    if (predicates.size() > 1)
      return false; 
    PropertyPredicate predicate = predicates.get(0);
    if (!PropertyUtil.isModelKey(predicate.getProperty()))
      return false; 
    Object comparableValue = predicate.getComparableValue();
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()))
      return comparableValue instanceof ManagedObjectReference; 
    if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      Collection<?> collectionValue = (Collection)comparableValue;
      assert !collectionValue.isEmpty();
      if (!(collectionValue.iterator().next() instanceof ManagedObjectReference))
        return false; 
    } else {
      return false;
    } 
    return true;
  }
  
  private static boolean maySortOnClientSide(List<SortCriterion> sortCriteria) {
    if (sortCriteria.isEmpty())
      return true; 
    if (sortCriteria.size() > 1)
      return false; 
    SortCriterion sortCriterion = sortCriteria.get(0);
    if (!PropertyUtil.isModelKey(sortCriterion.getProperty()))
      return false; 
    return true;
  }
}
