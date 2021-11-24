package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public final class QueryUtil {
  public static boolean selectsActualProperty(Query query, @Nonnull String nonQualifiedPropertyName) {
    if (query == null)
      return false; 
    List<String> queryProperties = query.getProperties();
    boolean containsProperty = false;
    for (String queryProperty : queryProperties) {
      if (nonQualifiedPropertyName.equals(
          QuerySchemaUtil.getActualPropertyName(queryProperty)))
        containsProperty = true; 
    } 
    return containsProperty;
  }
  
  public static <T> List<T> getFilterPropertyComparableValues(Query query, String nonQualifiedFilterPropertyName) {
    List<T> comparableValues = new ArrayList<>();
    Object comparableValue = getFilterPropertyComparableValue(query, nonQualifiedFilterPropertyName);
    if (comparableValue != null)
      if (comparableValue instanceof List) {
        for (T value : comparableValue)
          comparableValues.add(value); 
      } else {
        comparableValues.add((T)comparableValue);
      }  
    return comparableValues;
  }
  
  public static Object getFilterPropertyComparableValue(Query query, String nonQualifiedFilterPropertyName) {
    Object comparableValue = null;
    if (query != null && query.getFilter() != null && nonQualifiedFilterPropertyName != null) {
      List<PropertyPredicate> predicates = query.getFilter().getCriteria();
      for (PropertyPredicate predicate : predicates) {
        if (QuerySchemaUtil.getActualPropertyName(predicate
            .getProperty()).equals(nonQualifiedFilterPropertyName)) {
          comparableValue = predicate.getComparableValue();
          break;
        } 
      } 
    } 
    return comparableValue;
  }
  
  public static Map<String, Object> getNonQualifiedFilterPropertyToComparableValue(Filter queryFilter) {
    Map<String, Object> filterPropertyToComparableValue = new HashMap<>();
    List<PropertyPredicate> propertyPredicates = queryFilter.getCriteria();
    for (PropertyPredicate propertyPredicate : propertyPredicates)
      filterPropertyToComparableValue.put(
          QuerySchemaUtil.getActualPropertyName(propertyPredicate.getProperty()), propertyPredicate
          .getComparableValue()); 
    return filterPropertyToComparableValue;
  }
  
  public static <T> Filter createFilterForPropertyAndValue(String qualifiedPropertyName, T propertyValue) {
    Map<String, T> qualifiedPropertyNameToPropertyValue = new HashMap<>();
    qualifiedPropertyNameToPropertyValue.put(qualifiedPropertyName, propertyValue);
    return createFilterForPropertiesAndValues(qualifiedPropertyNameToPropertyValue);
  }
  
  public static <T> Filter createFilterForPropertiesAndValues(Map<String, T> qualifiedPropertyNameToPropertyValue) {
    List<PropertyPredicate> contextPropertyPredicates = new ArrayList<>();
    for (Map.Entry<String, T> qualifiedPropertyNameToPropertyValueEntry : qualifiedPropertyNameToPropertyValue.entrySet()) {
      String qualifiedPropertyName = qualifiedPropertyNameToPropertyValueEntry.getKey();
      T propertyValue = qualifiedPropertyNameToPropertyValueEntry.getValue();
      contextPropertyPredicates.add(new PropertyPredicate(qualifiedPropertyName, PropertyPredicate.ComparisonOperator.EQUAL, propertyValue));
    } 
    Filter contextFilter = new Filter(Collections.unmodifiableList(contextPropertyPredicates));
    return contextFilter;
  }
  
  public static Query createQueryWithAdditionalFilter(Query query, Filter filter) {
    if (filter == null)
      return query; 
    Filter mergedQueryFilter = mergeFilters(new Filter[] { query.getFilter(), filter });
    return substituteQueryFilter(query, mergedQueryFilter);
  }
  
  public static Query removePredicateFromQueryFilter(Query query, String qualifiedPredicatePropertyName) {
    Filter filter = query.getFilter();
    if (filter == null)
      return query; 
    List<PropertyPredicate> initalPredicates = filter.getCriteria();
    List<PropertyPredicate> predicatesToKeep = new ArrayList<>();
    for (PropertyPredicate predicate : initalPredicates) {
      if (!predicate.getProperty().equals(qualifiedPredicatePropertyName))
        predicatesToKeep.add(predicate); 
    } 
    if (initalPredicates.size() == predicatesToKeep.size())
      return query; 
    Filter filterWithRemovedPredicate = null;
    if (!predicatesToKeep.isEmpty())
      filterWithRemovedPredicate = new Filter(Collections.unmodifiableList(predicatesToKeep)); 
    return substituteQueryFilter(query, filterWithRemovedPredicate);
  }
  
  public static Query substituteQueryFilter(Query query, Filter filter) {
    Query.Builder newQueryBuilder = Query.Builder.select(query.getProperties()).from(query.getResourceModels()).offset(query.getOffset()).limit(query.getLimit()).where(filter).orderBy(query.getSortCriteria());
    if (query.getWithTotalCount())
      newQueryBuilder.withTotalCount(); 
    return newQueryBuilder.build();
  }
  
  private static Filter mergeFilters(Filter... filtersToMerge) {
    Filter mergedFilter = null;
    if (filtersToMerge != null) {
      List<PropertyPredicate> mergedPropertyPredicates = new ArrayList<>();
      for (Filter filter : filtersToMerge) {
        if (filter != null)
          mergedPropertyPredicates.addAll(filter.getCriteria()); 
      } 
      mergedFilter = new Filter(Collections.unmodifiableList(mergedPropertyPredicates));
    } 
    return mergedFilter;
  }
}
