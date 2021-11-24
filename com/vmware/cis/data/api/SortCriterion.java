package com.vmware.cis.data.api;

import org.apache.commons.lang.Validate;

public final class SortCriterion {
  private final String _property;
  
  private final SortDirection _sortDirection;
  
  private final boolean _ignoreCase;
  
  public enum SortDirection {
    ASCENDING, DESCENDING;
  }
  
  public SortCriterion(String property, SortDirection sortDirection, boolean ignoreCase) {
    Validate.notEmpty(property, "The name of the sort property must not be null or empty");
    Validate.notNull(sortDirection, "The sort direction must not be null");
    this._property = property;
    this._sortDirection = sortDirection;
    this._ignoreCase = ignoreCase;
  }
  
  public SortCriterion(String property, SortDirection sortDirection) {
    this(property, sortDirection, false);
  }
  
  public SortCriterion(String property) {
    this(property, SortDirection.ASCENDING);
  }
  
  public String getProperty() {
    return this._property;
  }
  
  public SortDirection getSortDirection() {
    return this._sortDirection;
  }
  
  public boolean isIgnoreCase() {
    return this._ignoreCase;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof SortCriterion))
      return false; 
    SortCriterion other = (SortCriterion)obj;
    return (this._property.equals(other._property) && this._sortDirection
      .equals(other._sortDirection) && this._ignoreCase == other._ignoreCase);
  }
  
  public int hashCode() {
    int hash = 29;
    hash = 31 * hash + this._property.hashCode();
    hash = 31 * hash + this._sortDirection.hashCode();
    hash = 31 * (this._ignoreCase ? 1 : 0);
    return hash;
  }
  
  public String toString() {
    return "SortCriterion [_property = " + this._property + ", _sortDirection = " + this._sortDirection + ", _ignoreCase = " + this._ignoreCase + "]";
  }
}
