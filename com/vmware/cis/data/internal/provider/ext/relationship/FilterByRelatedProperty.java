package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;

final class FilterByRelatedProperty {
  public static Filter resolveRelatedPropertiesInFilter(Query query, Map<String, RelatedPropertyDescriptor> relatedSortProperties, Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder, RelatedPropertyLookup relatedPropertyLookup, DataProvider connection) {
    assert query != null;
    assert relatedSortProperties != null;
    assert relatedFilterPropertiesOrder != null;
    assert relatedPropertyLookup != null;
    assert connection != null;
    Filter filter = query.getFilter();
    Set<String> filterProperties = QueryClauseAnalyzer.gatherPropertiesFromFilter(filter);
    Map<String, RelatedPropertyDescriptor> relatedFilterProperties = relatedPropertyLookup.getRelatedPropertyDescriptors(Arrays.asList(filterProperties
          .toArray(new String[filterProperties.size()])));
    if (relatedFilterProperties.isEmpty())
      return filter; 
    Map<String, SortCriterion> relatedFilterPropertiesSortCriteria = SortByRelatedProperty.getSortCriteriaForRelatedPropertiesInFilter(query
        .getSortCriteria(), relatedSortProperties.keySet(), relatedFilterProperties
        .keySet());
    Map<String, PropertyPredicate> propertyPredicatesByRelatedProperty = evaluatePredicatesByRelatedProperty(filter, relatedFilterProperties, relatedFilterPropertiesSortCriteria, connection);
    if (propertyPredicatesByRelatedProperty.isEmpty())
      return filter; 
    Set<PropertyPredicate> extendedPredicates = prepareExtendedPredicates(filter, propertyPredicatesByRelatedProperty);
    if (extendedPredicates == null)
      return null; 
    collectOrderValuesForSortProperties(relatedSortProperties, relatedFilterPropertiesOrder, propertyPredicatesByRelatedProperty);
    List<PropertyPredicate> mergedPredicates = mergeInPropertyPredicates(extendedPredicates, filter.getOperator());
    if (CollectionUtils.isEmpty(mergedPredicates))
      return null; 
    return new Filter(mergedPredicates, filter.getOperator());
  }
  
  private static Map<String, PropertyPredicate> evaluatePredicatesByRelatedProperty(Filter filter, Map<String, RelatedPropertyDescriptor> relatedFilterProperties, Map<String, SortCriterion> relatedFilterPropertiesSortCriteria, DataProvider connection) {
    assert relatedFilterProperties != null;
    assert relatedFilterPropertiesSortCriteria != null;
    Map<String, Filter> partialFiltersByRelatedProperty = groupPredicatesByRelatedProperty(filter, relatedFilterProperties);
    if (partialFiltersByRelatedProperty.isEmpty())
      return Collections.emptyMap(); 
    return evaluatePredicatesByRelatedProperty(relatedFilterProperties, partialFiltersByRelatedProperty, relatedFilterPropertiesSortCriteria, connection);
  }
  
  private static Set<PropertyPredicate> prepareExtendedPredicates(Filter filter, Map<String, PropertyPredicate> propertyPredicatesByRelatedProperty) {
    assert filter != null;
    assert propertyPredicatesByRelatedProperty != null;
    List<PropertyPredicate> predicates = filter.getCriteria();
    Set<PropertyPredicate> extendedPredicates = new LinkedHashSet<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      String predicateProperty = predicate.getProperty();
      if (propertyPredicatesByRelatedProperty.containsKey(predicateProperty)) {
        PropertyPredicate extendedPropertyPredicate = propertyPredicatesByRelatedProperty.get(predicateProperty);
        if (extendedPropertyPredicate == null) {
          if (LogicalOperator.AND.equals(filter.getOperator()))
            return null; 
          continue;
        } 
        extendedPredicates.add(extendedPropertyPredicate);
        continue;
      } 
      extendedPredicates.add(predicate);
    } 
    return extendedPredicates;
  }
  
  private static void collectOrderValuesForSortProperties(Map<String, RelatedPropertyDescriptor> relatedSortProperties, Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder, Map<String, PropertyPredicate> propertyPredicatesByRelatedProperty) {
    assert relatedSortProperties != null;
    assert relatedFilterPropertiesOrder != null;
    assert propertyPredicatesByRelatedProperty != null;
    if (relatedSortProperties.isEmpty() || propertyPredicatesByRelatedProperty.isEmpty())
      return; 
    for (Map.Entry<String, RelatedPropertyDescriptor> relatedSortProperty : relatedSortProperties.entrySet()) {
      PropertyPredicate relatedPropertyPredicate = propertyPredicatesByRelatedProperty.get(relatedSortProperty.getKey());
      if (relatedPropertyPredicate != null) {
        List<Object> relatedPropertyPredicateValues = (List<Object>)relatedPropertyPredicate.getComparableValue();
        relatedFilterPropertiesOrder.put(relatedSortProperty
            .getValue(), relatedPropertyPredicateValues);
      } 
    } 
  }
  
  private static List<PropertyPredicate> mergeInPropertyPredicates(Set<PropertyPredicate> propertyPredicates, LogicalOperator operator) {
    assert propertyPredicates != null;
    assert operator != null;
    List<PropertyPredicate> mergedPropertyPerdicates = new ArrayList<>(propertyPredicates.size());
    Map<String, Set<Object>> predicateValuesByProperty = collectInPredicateValues(propertyPredicates, operator);
    Set<String> processedMergedPredicates = new HashSet<>();
    for (PropertyPredicate propertyPredicate : propertyPredicates) {
      if (!PropertyPredicate.ComparisonOperator.IN.equals(propertyPredicate.getOperator())) {
        mergedPropertyPerdicates.add(propertyPredicate);
        continue;
      } 
      if (processedMergedPredicates.contains(propertyPredicate.getProperty()))
        continue; 
      Set<Object> propertyPredicateValues = predicateValuesByProperty.get(propertyPredicate.getProperty());
      if (CollectionUtils.isEmpty(propertyPredicateValues)) {
        if (LogicalOperator.AND.equals(operator))
          return null; 
        continue;
      } 
      mergedPropertyPerdicates.add(new PropertyPredicate(propertyPredicate
            .getProperty(), PropertyPredicate.ComparisonOperator.IN, new ArrayList(propertyPredicateValues), propertyPredicate
            
            .isIgnoreCase()));
      processedMergedPredicates.add(propertyPredicate.getProperty());
    } 
    return mergedPropertyPerdicates;
  }
  
  private static Map<String, Set<Object>> collectInPredicateValues(Set<PropertyPredicate> propertyPredicates, LogicalOperator operator) {
    assert propertyPredicates != null;
    assert operator != null;
    Map<String, Set<Object>> predicateValuesByProperty = new LinkedHashMap<>();
    for (PropertyPredicate propertyPredicate : propertyPredicates) {
      if (!PropertyPredicate.ComparisonOperator.IN.equals(propertyPredicate.getOperator()))
        continue; 
      Set<Object> propertyPredicateValues = predicateValuesByProperty.get(propertyPredicate.getProperty());
      if (propertyPredicateValues == null) {
        propertyPredicateValues = new LinkedHashSet((Collection)propertyPredicate.getComparableValue());
        predicateValuesByProperty.put(propertyPredicate.getProperty(), propertyPredicateValues);
        continue;
      } 
      if (LogicalOperator.AND.equals(operator)) {
        propertyPredicateValues.retainAll((Collection)propertyPredicate
            .getComparableValue());
        continue;
      } 
      propertyPredicateValues.addAll((Collection)propertyPredicate
          .getComparableValue());
    } 
    return predicateValuesByProperty;
  }
  
  private static Map<String, Filter> groupPredicatesByRelatedProperty(Filter filter, Map<String, RelatedPropertyDescriptor> relatedPropertyDescriptorsByProperty) {
    Map<String, List<PropertyPredicate>> predicatesByRelatedProperty = new LinkedHashMap<>();
    for (PropertyPredicate filterPropertyPredicate : filter.getCriteria()) {
      String predicateProperty = filterPropertyPredicate.getProperty();
      if (relatedPropertyDescriptorsByProperty.get(predicateProperty) == null)
        continue; 
      List<PropertyPredicate> relatedPropertyPredicateGroup = predicatesByRelatedProperty.get(predicateProperty);
      if (relatedPropertyPredicateGroup == null) {
        relatedPropertyPredicateGroup = new ArrayList<>();
        predicatesByRelatedProperty.put(predicateProperty, relatedPropertyPredicateGroup);
      } 
      relatedPropertyPredicateGroup.add(filterPropertyPredicate);
    } 
    Map<String, Filter> filtersByRelatedProperty = new LinkedHashMap<>();
    for (Map.Entry<String, List<PropertyPredicate>> relatedPropertyPredicatesEntry : predicatesByRelatedProperty.entrySet())
      filtersByRelatedProperty.put(relatedPropertyPredicatesEntry.getKey(), new Filter(relatedPropertyPredicatesEntry
            .getValue(), filter
            .getOperator())); 
    return Collections.unmodifiableMap(filtersByRelatedProperty);
  }
  
  private static Map<String, PropertyPredicate> evaluatePredicatesByRelatedProperty(Map<String, RelatedPropertyDescriptor> relatedPropertyDescriptorsByProperty, Map<String, Filter> partialFiltersByRelatedProperty, Map<String, SortCriterion> relatedFilterPropertiesSortCriteria, DataProvider connection) {
    assert partialFiltersByRelatedProperty != null;
    Map<String, PropertyPredicate> propertyPredicatesByRelatedProperty = new HashMap<>();
    for (Map.Entry<String, Filter> relatedPropertyPartialFilterEntry : partialFiltersByRelatedProperty.entrySet()) {
      String relatedProperty = relatedPropertyPartialFilterEntry.getKey();
      RelatedPropertyDescriptor relatedPropertyDescriptor = relatedPropertyDescriptorsByProperty.get(relatedProperty);
      Filter relatedPropertyPartialFilter = relatedPropertyPartialFilterEntry.getValue();
      PropertyPredicate relatedPropertyPredicate = prepareFilterPredicateForRelatedProperty(relatedPropertyDescriptor, relatedPropertyPartialFilter, relatedFilterPropertiesSortCriteria
          
          .get(relatedProperty), connection);
      propertyPredicatesByRelatedProperty.put(relatedProperty, relatedPropertyPredicate);
    } 
    return Collections.unmodifiableMap(propertyPredicatesByRelatedProperty);
  }
  
  private static PropertyPredicate prepareFilterPredicateForRelatedProperty(RelatedPropertyDescriptor relatedPropertyDescriptor, Filter relatedPropertyPartialFilter, SortCriterion relatedPropertySortCriterion, DataProvider connection) {
    assert relatedPropertyDescriptor != null;
    assert relatedPropertyPartialFilter != null;
    List<RelationshipHop> relationshipHops = new ArrayList<>();
    List<RelationshipQuery> relationshipQueries = RelationshipQueryFactory.createRelationshipQueriesForFilter(relatedPropertyDescriptor, relationshipHops);
    ResultSet relatedResult = RelationshipQueryExecutor.executeRelationshipQueriesForFilter(relatedPropertyDescriptor
        .getName(), relationshipQueries, relatedPropertyPartialFilter, relatedPropertySortCriterion, connection);
    if (relatedResult == null || relatedResult.getItems().isEmpty())
      return null; 
    RelationshipHop lastRelationshipHop = relationshipHops.get(relationshipHops.size() - 1);
    List<Object> relatedPropertyValues = ResultSetUtil.extractNotNullPropertyValues(relatedResult, lastRelationshipHop
        .getSourceModelProperty());
    if (relatedPropertyValues.isEmpty())
      return null; 
    return new PropertyPredicate(lastRelationshipHop.getTargetModelProperty(), PropertyPredicate.ComparisonOperator.IN, relatedPropertyValues);
  }
}
