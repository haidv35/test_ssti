package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class PredicatePropertyFilter {
  public static Filter toExecutableFilter(Filter filter, Map<String, PredicatePropertyDescriptor> descriptors) {
    assert descriptors != null;
    if (filter == null)
      return null; 
    if (descriptors.isEmpty())
      return filter; 
    LogicalOperator effectiveOperator = getEffectiveOperator(filter);
    List<PropertyPredicate> executablePredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      String property = predicate.getProperty();
      PredicatePropertyDescriptor descriptor = descriptors.get(property);
      if (descriptor == null) {
        executablePredicates.add(predicate);
        continue;
      } 
      Filter filterForCustomProp = isNegative(predicate) ? FilterAlgebra.negate(descriptor.getFilter()) : descriptor.getFilter();
      LogicalOperator operatorForCustomProp = getEffectiveOperator(filterForCustomProp);
      if (incompatibleOperators(effectiveOperator, operatorForCustomProp))
        throw new IllegalArgumentException(
            String.format("Predicate property '%s' used in a query with incompatible logical operator", new Object[] { property })); 
      if (effectiveOperator == null)
        effectiveOperator = operatorForCustomProp; 
      executablePredicates.addAll(filterForCustomProp.getCriteria());
    } 
    return new Filter(executablePredicates, (effectiveOperator != null) ? effectiveOperator : filter
        
        .getOperator());
  }
  
  private static boolean incompatibleOperators(LogicalOperator a, LogicalOperator b) {
    if (a == null)
      return false; 
    if (b == null)
      return false; 
    return !a.equals(b);
  }
  
  private static LogicalOperator getEffectiveOperator(Filter filter) {
    assert filter != null;
    if (filter.getCriteria().size() > 1)
      return filter.getOperator(); 
    return null;
  }
  
  private static boolean isNegative(PropertyPredicate predicate) {
    assert predicate != null;
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()) && 
      !PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(predicate.getOperator()))
      throw new IllegalArgumentException("Unsupported comparison operator for predicate property: " + predicate); 
    if (!(predicate.getComparableValue() instanceof Boolean))
      throw new IllegalArgumentException("Unsupported comparable value for predicate property: " + predicate); 
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    Boolean comparableValue = (Boolean)predicate.getComparableValue();
    if (comparableValue.booleanValue())
      return PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(operator); 
    return PropertyPredicate.ComparisonOperator.EQUAL.equals(operator);
  }
}
