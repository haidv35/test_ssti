package com.vmware.cis.data.internal.provider.util.filter;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.internal.provider.util.property.PropertyByName;
import java.util.List;

public final class FilterEvaluator {
  public static boolean eval(Filter filter, PropertyByName propertyValueByName) {
    assert filter != null;
    assert propertyValueByName != null;
    switch (filter.getOperator()) {
      case AND:
        return evalConjunction(filter.getCriteria(), propertyValueByName);
      case OR:
        return evalDisjunction(filter.getCriteria(), propertyValueByName);
    } 
    throw new IllegalStateException("Unsupported logical operator " + filter
        .getOperator());
  }
  
  private static boolean evalConjunction(List<PropertyPredicate> predicates, PropertyByName propertyValueByName) {
    assert predicates != null;
    for (PropertyPredicate predicate : predicates) {
      if (!eval(predicate, propertyValueByName))
        return false; 
    } 
    return true;
  }
  
  private static boolean evalDisjunction(List<PropertyPredicate> predicates, PropertyByName propertyValueByName) {
    assert predicates != null;
    for (PropertyPredicate predicate : predicates) {
      if (eval(predicate, propertyValueByName))
        return true; 
    } 
    return false;
  }
  
  private static boolean eval(PropertyPredicate predicate, PropertyByName propertyValueByName) {
    assert predicate != null;
    assert propertyValueByName != null;
    Object value = propertyValueByName.getValue(predicate.getProperty());
    return PredicateEvaluator.eval(predicate, value);
  }
}
