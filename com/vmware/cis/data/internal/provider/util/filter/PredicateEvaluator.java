package com.vmware.cis.data.internal.provider.util.filter;

import com.vmware.cis.data.api.PropertyPredicate;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.collections4.iterators.ArrayIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PredicateEvaluator {
  private static Logger _logger = LoggerFactory.getLogger(PredicateEvaluator.class);
  
  public static boolean eval(PropertyPredicate predicate, Object propertyValue) {
    assert predicate != null;
    logPredicate(predicate, propertyValue);
    try {
      return evalImpl(predicate, propertyValue);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Error while evaluating predicate " + predicate, ex);
    } 
  }
  
  private static boolean evalImpl(PropertyPredicate predicate, Object propertyValue) {
    assert predicate != null;
    assert predicate.getOperator() != null;
    assert predicate.getComparableValue() != null;
    if (propertyValue == null && predicate
      .getOperator() != PropertyPredicate.ComparisonOperator.UNSET)
      return false; 
    validatePredicate(predicate, propertyValue);
    if (isCollection(propertyValue)) {
      Iterator<?> valueIterator = toIterator(propertyValue);
      return eval(predicate, valueIterator);
    } 
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    Object comparableValue = predicate.getComparableValue();
    if (comparableValue instanceof String) {
      if (!(propertyValue instanceof String))
        propertyValue = propertyValue.toString(); 
      if (predicate.isIgnoreCase()) {
        comparableValue = ((String)comparableValue).toLowerCase();
        propertyValue = ((String)propertyValue).toLowerCase();
      } 
    } 
    return eval(propertyValue, operator, comparableValue);
  }
  
  private static void validatePredicate(PropertyPredicate predicate, Object propertyValue) {
    assert predicate != null;
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    Object comparableValue = predicate.getComparableValue();
    if (isCollection(propertyValue)) {
      if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(operator))
        throw new IllegalArgumentException("Cannot filter collection/array property using operator: " + operator); 
      if (isCollection(comparableValue))
        throw new IllegalArgumentException("Cannot filter collection/array property using collection comparable value"); 
    } 
    if (!PropertyPredicate.ComparisonOperator.IN.equals(operator) && isCollection(comparableValue))
      throw new IllegalArgumentException("Only operator 'IN' can be used with collection comparable value"); 
  }
  
  private static boolean eval(PropertyPredicate predicate, Iterator<?> valueIt) {
    assert predicate != null;
    assert valueIt != null;
    while (valueIt.hasNext()) {
      Object propertyValue = valueIt.next();
      if (evalImpl(predicate, propertyValue))
        return true; 
    } 
    return false;
  }
  
  private static boolean eval(Object propertyValue, PropertyPredicate.ComparisonOperator operator, Object comparableValue) {
    assert operator != null;
    assert comparableValue != null;
    switch (operator) {
      case EQUAL:
        return isEqual(propertyValue, comparableValue);
      case GREATER:
        return (cmp(propertyValue, comparableValue) > 0);
      case GREATER_OR_EQUAL:
        return (cmp(propertyValue, comparableValue) >= 0);
      case IN:
        return isIn(propertyValue, comparableValue);
      case LESS:
        return (cmp(propertyValue, comparableValue) < 0);
      case LESS_OR_EQUAL:
        return (cmp(propertyValue, comparableValue) <= 0);
      case NOT_EQUAL:
        return !isEqual(propertyValue, comparableValue);
      case LIKE:
        return OperatorLikeEvaluator.evalLike((String)propertyValue, (String)comparableValue);
      case UNSET:
        return isUnset(propertyValue, (Boolean)comparableValue);
    } 
    throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
  }
  
  private static boolean isEqual(Object propertyValue, Object comparableValue) {
    assert propertyValue != null;
    assert comparableValue != null;
    logEqual(propertyValue, comparableValue);
    return comparableValue.equals(propertyValue);
  }
  
  private static int cmp(Object propertyValue, Object comparableValue) {
    assert propertyValue != null;
    assert comparableValue != null;
    assert comparableValue instanceof Comparable;
    logCmp(propertyValue, comparableValue);
    Comparable<Object> comparable = (Comparable<Object>)comparableValue;
    return comparable.compareTo(propertyValue) * -1;
  }
  
  private static boolean isIn(Object propertyValue, Object comparableValue) {
    assert propertyValue != null;
    assert comparableValue != null;
    assert comparableValue instanceof Collection;
    Collection<?> comparableCollection = (Collection)comparableValue;
    for (Object comparableElement : comparableCollection) {
      if (isEqual(propertyValue, comparableElement))
        return true; 
    } 
    return false;
  }
  
  private static boolean isCollection(Object obj) {
    if (obj == null)
      return false; 
    return (obj instanceof Collection || obj.getClass().isArray());
  }
  
  private static Iterator<?> toIterator(Object obj) {
    assert obj != null;
    if (obj instanceof Iterable)
      return ((Iterable)obj).iterator(); 
    assert obj.getClass().isArray();
    return (Iterator<?>)new ArrayIterator(obj);
  }
  
  private static boolean isUnset(Object propertyValue, Boolean comparableValue) {
    assert comparableValue != null;
    return (!comparableValue.booleanValue()) ^ ((propertyValue == null));
  }
  
  private static void logPredicate(PropertyPredicate predicate, Object propertyValue) {
    _logger.trace("Evaluating predicate {} on property value {}", predicate, propertyValue);
  }
  
  private static void logEqual(Object propertyValue, Object comparableValue) {
    if (propertyValue.getClass() != comparableValue.getClass() && _logger
      .isTraceEnabled())
      _logger.trace("Testing equality of property value of class {} against comparable value of class {}", propertyValue
          
          .getClass(), comparableValue.getClass()); 
  }
  
  private static void logCmp(Object propertyValue, Object comparableValue) {
    if (propertyValue.getClass() != comparableValue.getClass() && _logger
      .isTraceEnabled())
      _logger.trace("Comparing property value of class {} against comparable value of class {}", propertyValue
          
          .getClass(), comparableValue.getClass()); 
  }
}
