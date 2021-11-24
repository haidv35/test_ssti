package com.vmware.cis.data.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public final class Filter {
  private final List<PropertyPredicate> _criteria;
  
  private final LogicalOperator _operator;
  
  public Filter(List<PropertyPredicate> criteria, LogicalOperator operator) {
    Validate.notEmpty(criteria, "The collection of predicates must not be null or empty");
    Validate.noNullElements(criteria, "The collection of predicates must not contain null elements");
    Validate.notNull(operator, "The logical operator must not be null");
    this._criteria = Collections.unmodifiableList(criteria);
    this._operator = operator;
  }
  
  public Filter(List<PropertyPredicate> criteria) {
    this(criteria, LogicalOperator.AND);
  }
  
  public List<PropertyPredicate> getCriteria() {
    return this._criteria;
  }
  
  public LogicalOperator getOperator() {
    return this._operator;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof Filter))
      return false; 
    Filter other = (Filter)obj;
    return (this._criteria.equals(other._criteria) && this._operator
      .equals(other._operator));
  }
  
  public int hashCode() {
    int hash = 19;
    hash = 31 * hash + this._criteria.hashCode();
    hash = 31 * hash + this._operator.hashCode();
    return hash;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("Filter [" + SystemUtils.LINE_SEPARATOR);
    sb.append("      _operator = " + this._operator);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("      _criteria = ");
    if (this._criteria.size() <= 1) {
      sb.append("[" + (!this._criteria.isEmpty() ? (String)this._criteria.get(0) : "") + "]");
    } else {
      sb.append("[");
      Iterator<PropertyPredicate> iterator = this._criteria.iterator();
      while (iterator.hasNext()) {
        sb.append(SystemUtils.LINE_SEPARATOR + "         ");
        sb.append(iterator.next());
        if (iterator.hasNext())
          sb.append(","); 
      } 
      sb.append(SystemUtils.LINE_SEPARATOR + "      ]");
    } 
    sb.append(SystemUtils.LINE_SEPARATOR + "   ]");
    return sb.toString();
  }
}
