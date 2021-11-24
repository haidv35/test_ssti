package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.PropertyPredicate;

final class PredicateAlgebra {
  public static PropertyPredicate negate(PropertyPredicate predicate) {
    assert predicate != null;
    Object comparableValue = predicate.getComparableValue();
    PropertyPredicate.ComparisonOperator operator = negateComparisonOperator(predicate);
    if (predicate.getOperator() == PropertyPredicate.ComparisonOperator.UNSET)
      comparableValue = Boolean.valueOf(!((Boolean)comparableValue).booleanValue()); 
    return new PropertyPredicate(predicate.getProperty(), operator, comparableValue, predicate

        
        .isIgnoreCase());
  }
  
  private static PropertyPredicate.ComparisonOperator negateComparisonOperator(PropertyPredicate predicate) {
    assert predicate != null;
    assert predicate.getOperator() != null;
    switch (predicate.getOperator()) {
      case EQUAL:
        return PropertyPredicate.ComparisonOperator.NOT_EQUAL;
      case GREATER:
        return PropertyPredicate.ComparisonOperator.LESS_OR_EQUAL;
      case GREATER_OR_EQUAL:
        return PropertyPredicate.ComparisonOperator.LESS;
      case IN:
        throw new IllegalArgumentException(String.format("Cannot negate predicate with operator 'IN': %s", new Object[] { predicate }));
      case LESS:
        return PropertyPredicate.ComparisonOperator.GREATER_OR_EQUAL;
      case LESS_OR_EQUAL:
        return PropertyPredicate.ComparisonOperator.GREATER;
      case NOT_EQUAL:
        return PropertyPredicate.ComparisonOperator.EQUAL;
      case UNSET:
        return PropertyPredicate.ComparisonOperator.UNSET;
    } 
    throw new IllegalArgumentException(String.format("Unsupported comparison operator '%s' in predicate: %s", new Object[] { predicate
            
            .getOperator(), predicate }));
  }
}
