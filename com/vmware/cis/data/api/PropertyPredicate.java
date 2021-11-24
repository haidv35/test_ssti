package com.vmware.cis.data.api;

import com.google.common.collect.Iterables;
import com.vmware.cis.data.internal.provider.util.filter.OperatorLikeEvaluator;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyPredicate {
  private static final Logger _logger = LoggerFactory.getLogger(PropertyPredicate.class);
  
  private final String _property;
  
  private final ComparisonOperator _operator;
  
  private final Object _comparableValue;
  
  private final boolean _ignoreCase;
  
  public enum ComparisonOperator {
    EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL, IN, NOT_IN, LIKE, UNSET;
  }
  
  public PropertyPredicate(String property, ComparisonOperator operator, Object comparableValue, boolean ignoreCase) {
    Validate.notEmpty(property, "Argument `property' must not be null or empty.");
    Validate.notNull(operator, "Argument `operator' must not be null.");
    validateComparableValue(property, operator, comparableValue, ignoreCase);
    this._property = property;
    this._operator = operator;
    this._comparableValue = comparableValue;
    this._ignoreCase = ignoreCase;
  }
  
  public PropertyPredicate(String property, ComparisonOperator operator, Object comparableValue) {
    this(property, operator, comparableValue, false);
  }
  
  public static PropertyPredicate containsIgnoreCase(String property, String text) {
    Validate.notEmpty(text, "The text must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(text, OperatorLikeEvaluator.StringMatchingMode.Contains);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, true);
  }
  
  public static PropertyPredicate contains(String property, String text) {
    Validate.notEmpty(text, "The text must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(text, OperatorLikeEvaluator.StringMatchingMode.Contains);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, false);
  }
  
  public static PropertyPredicate endsWithIgnoreCase(String property, String suffix) {
    Validate.notEmpty(suffix, "The suffix must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(suffix, OperatorLikeEvaluator.StringMatchingMode.EndsWith);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, true);
  }
  
  public static PropertyPredicate endsWith(String property, String suffix) {
    Validate.notEmpty(suffix, "The suffix must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(suffix, OperatorLikeEvaluator.StringMatchingMode.EndsWith);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, false);
  }
  
  public static PropertyPredicate startsWithIgnoreCase(String property, String prefix) {
    Validate.notEmpty(prefix, "The prefix must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(prefix, OperatorLikeEvaluator.StringMatchingMode.StartsWith);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, true);
  }
  
  public static PropertyPredicate startsWith(String property, String prefix) {
    Validate.notEmpty(prefix, "The prefix must not be empty.");
    String template = OperatorLikeEvaluator.toSearchTemplate(prefix, OperatorLikeEvaluator.StringMatchingMode.StartsWith);
    return new PropertyPredicate(property, ComparisonOperator.LIKE, template, false);
  }
  
  private void validateComparableValue(String property, ComparisonOperator operator, Object comparableValue, boolean ignoreCase) {
    assert operator != null;
    Validate.notNull(comparableValue, "Argument `comparableValue' must not be null.");
    switch (operator) {
      case IN:
      case NOT_IN:
        Validate.isTrue(comparableValue instanceof Collection, "IN operator can only be used together with a Collection<?> comparable value.");
        Validate.notEmpty((Collection)comparableValue, "The collection of comparable values must not be null or empty");
        Validate.noNullElements((Collection)comparableValue, "The collection of comparable values must not contain null elements");
        break;
      case LIKE:
        Validate.isTrue(comparableValue instanceof String, "LIKE operator can only be used together with a String comparable value.");
        break;
      case UNSET:
        Validate.isTrue(comparableValue instanceof Boolean, "UNSET operator can only be used together with a Boolean comparable value.");
        break;
    } 
    validateIgnoreCase(property, operator, comparableValue, ignoreCase);
  }
  
  private static void validateIgnoreCase(String property, ComparisonOperator operator, Object comparableValue, boolean ignoreCase) {
    if (!ignoreCase)
      return; 
    Validate.isTrue(!PropertyUtil.isModelKey(property), "The ignoreCase flag cannot be set to true for @modelKey properties.");
    Validate.isTrue(!ComparisonOperator.UNSET.equals(operator), "The ignoreCase flag cannot be set to true with UNSET operator.");
    if (comparableValue instanceof Collection) {
      Collection<?> comparableCollection = (Collection)comparableValue;
      for (Object value : comparableCollection) {
        Validate.isTrue(value instanceof String, String.format("The ignoreCase flag cannot be set to true for non String objects: %s.", new Object[] { value }));
      } 
    } else {
      Validate.isTrue(comparableValue instanceof String, String.format("The ignoreCase flag cannot be set to true for non String objects: %s.", new Object[] { comparableValue }));
    } 
  }
  
  public String getProperty() {
    return this._property;
  }
  
  public ComparisonOperator getOperator() {
    return this._operator;
  }
  
  public Object getComparableValue() {
    return this._comparableValue;
  }
  
  public boolean isIgnoreCase() {
    return this._ignoreCase;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof PropertyPredicate))
      return false; 
    PropertyPredicate other = (PropertyPredicate)obj;
    if (!this._property.equals(other._property) || 
      !this._operator.equals(other._operator) || this._ignoreCase != other._ignoreCase)
      return false; 
    if (!isCollectionOperator(this._operator))
      return this._comparableValue.equals(other._comparableValue); 
    return Iterables.elementsEqual((Collection)this._comparableValue, (Collection)other._comparableValue);
  }
  
  public int hashCode() {
    int hash = 23;
    hash = 31 * hash + this._property.hashCode();
    hash = 31 * hash + this._operator.hashCode();
    hash = 31 * hash + (this._ignoreCase ? 1 : 0);
    if (isCollectionOperator(this._operator)) {
      Collection<?> comparableCollection = (Collection)this._comparableValue;
      for (Object value : comparableCollection)
        hash = 31 * hash + value.hashCode(); 
    } else {
      hash = 31 * hash + this._comparableValue.hashCode();
    } 
    return hash;
  }
  
  public String toString() {
    return "PropertyPredicate [_property = " + this._property + ", _operator = " + this._operator + ", _comparableValue = " + 
      
      comparableValueToString(this._operator, this._comparableValue) + ", _ignoreCase = " + this._ignoreCase + "]";
  }
  
  private static String comparableValueToString(ComparisonOperator operator, Object comparableValue) {
    if (!isCollectionOperator(operator))
      return String.valueOf(comparableValue); 
    if (_logger.isTraceEnabled())
      return String.valueOf(comparableValue); 
    Collection<?> comparableCollection = (Collection)comparableValue;
    if (comparableCollection.isEmpty())
      return "[]"; 
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    Iterator<?> it = comparableCollection.iterator();
    int i = 0;
    while (true) {
      Object e = it.next();
      sb.append(e);
      i++;
      if (!it.hasNext())
        break; 
      int remaining = comparableCollection.size() - i;
      if (i > 4 && remaining > 2) {
        sb.append(", ... (");
        sb.append(remaining);
        sb.append(" more)");
        break;
      } 
      sb.append(',');
      sb.append(' ');
    } 
    sb.append(']');
    return sb.toString();
  }
  
  private static boolean isCollectionOperator(ComparisonOperator operator) {
    return (ComparisonOperator.IN.equals(operator) || ComparisonOperator.NOT_IN
      .equals(operator));
  }
}
