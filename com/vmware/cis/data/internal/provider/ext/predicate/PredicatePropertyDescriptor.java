package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

public final class PredicatePropertyDescriptor {
  private final String _propertyName;
  
  private final Filter _filter;
  
  public static PredicatePropertyDescriptor fromMethod(String predicatePropertyName, Method predicatePropertyMethod) {
    validatePredicatePropertyMethod(predicatePropertyMethod);
    validatePredicatePropertyName(predicatePropertyName, predicatePropertyMethod);
    Filter filter = getPredicatePropertyFilter(predicatePropertyMethod);
    validateFilter(predicatePropertyName, filter);
    return new PredicatePropertyDescriptor(predicatePropertyName, filter);
  }
  
  private PredicatePropertyDescriptor(String propertyName, Filter filter) {
    assert propertyName != null;
    assert filter != null;
    this._propertyName = propertyName;
    this._filter = filter;
  }
  
  public String getName() {
    return this._propertyName;
  }
  
  public Filter getFilter() {
    return this._filter;
  }
  
  private static void validatePredicatePropertyMethod(Method predicatePropertyMethod) {
    Validate.notNull(predicatePropertyMethod, "Predicate property method must not be null");
    Validate.isTrue(
        Modifier.isPublic(predicatePropertyMethod.getModifiers()), 
        String.format("Predicate property method '%s' must be public.", new Object[] { predicatePropertyMethod.getName() }));
    Validate.isTrue(
        Modifier.isStatic(predicatePropertyMethod.getModifiers()), 
        String.format("Predicate property method '%s' must be static.", new Object[] { predicatePropertyMethod.getName() }));
    Class<?> returnType = predicatePropertyMethod.getReturnType();
    Validate.isTrue((PropertyPredicate.class.equals(returnType) || Filter.class
        .equals(returnType)), 
        String.format("Predicate property method '%s' must return PropertyPredicate or Filter and not %s", new Object[] { predicatePropertyMethod.getName(), returnType
            .getCanonicalName() }));
    Validate.isTrue(
        ArrayUtils.isEmpty((Object[])predicatePropertyMethod.getParameterTypes()), 
        String.format("Predicate property method '%s' must have no arguments", new Object[] { predicatePropertyMethod.getName() }));
  }
  
  private static void validatePredicatePropertyName(String predicatePropertyName, Method predicatePropertyMethod) {
    try {
      QualifiedProperty.forQualifiedName(predicatePropertyName);
    } catch (RuntimeException e) {
      String methodName = predicatePropertyMethod.getName();
      String msg = String.format("Invalid name of predicate property: '%s' on method '%s'", new Object[] { predicatePropertyName, methodName });
      throw new IllegalArgumentException(msg);
    } 
  }
  
  private static Filter getPredicatePropertyFilter(Method predicatePropertyMethod) {
    Object obj;
    assert predicatePropertyMethod != null;
    try {
      obj = predicatePropertyMethod.invoke(null, new Object[0]);
    } catch (Exception ex) {
      throw new IllegalStateException(String.format("Error while invoking predicate property method '%s'", new Object[] { predicatePropertyMethod
              
              .getName() }), ex);
    } 
    if (obj instanceof Filter)
      return (Filter)obj; 
    if (!(obj instanceof PropertyPredicate))
      throw new IllegalStateException(String.format("Predicate property method '%s' returned %s instead of Filter/PropertyPredicate", new Object[] { predicatePropertyMethod

              
              .getName(), obj })); 
    return new Filter(Collections.singletonList((PropertyPredicate)obj));
  }
  
  private static void validateFilter(String predicatePropertyName, Filter filter) {
    assert filter != null;
    for (PropertyPredicate predicate : filter.getCriteria()) {
      assert predicate != null;
      String property = predicate.getProperty();
      PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
      Object comparableValue = predicate.getComparableValue();
      assert property != null;
      assert operator != null;
      assert comparableValue != null;
      if (PropertyUtil.isModelKey(property))
        throw new IllegalArgumentException(String.format("Predicate property '%s' depends on '%s'", new Object[] { predicatePropertyName, property })); 
      if ((PropertyPredicate.ComparisonOperator.GREATER.equals(operator) || PropertyPredicate.ComparisonOperator.GREATER_OR_EQUAL.equals(operator) || PropertyPredicate.ComparisonOperator.LESS
        .equals(operator) || PropertyPredicate.ComparisonOperator.LESS_OR_EQUAL.equals(operator)) && 
        !(comparableValue instanceof Comparable))
        throw new IllegalArgumentException(String.format("Predicate property '%s' uses predicate with operator '%s' but the comparable value does not implement java.util.Comparable: %s", new Object[] { predicatePropertyName, operator, predicate })); 
    } 
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof PredicatePropertyDescriptor))
      return false; 
    PredicatePropertyDescriptor other = (PredicatePropertyDescriptor)obj;
    return (this._propertyName.equals(other._propertyName) && this._filter
      .equals(other._filter));
  }
  
  public int hashCode() {
    int hash = 29;
    hash = 31 * hash + this._propertyName.hashCode();
    hash = 31 * hash + this._filter.hashCode();
    return hash;
  }
  
  public String toString() {
    return "PredicatePropertyDescriptor[_propertyName=" + this._propertyName + ", _filter=" + this._filter + "]";
  }
}
