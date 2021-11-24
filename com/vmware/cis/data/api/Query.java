package com.vmware.cis.data.api;

import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public final class Query {
  private final List<String> _properties;
  
  private final Collection<String> _resourceModels;
  
  private final Filter _filter;
  
  private final List<SortCriterion> _sortCriteria;
  
  private final int _offset;
  
  private final int _limit;
  
  private final boolean _withTotalCount;
  
  private Query(List<String> properties, Collection<String> resourceModels, Filter filter, List<SortCriterion> sortCriteria, int offset, int limit, boolean withTotalCount) {
    if (limit != 0) {
      if (limit > 0)
        Validate.isTrue(!CollectionUtils.isEmpty(sortCriteria), "Sorting criteria should be set when limit is used"); 
    } else {
      Validate.isTrue(withTotalCount, "The `withTotalCount' flag must be set when the limit is zero.");
      Validate.isTrue(CollectionUtils.isEmpty(properties), "No properties should be requested when the limit is zero.");
      Validate.isTrue((CollectionUtils.isEmpty(sortCriteria) && offset == 0), "Sorting or offset cannot be used when the limit is zero.");
    } 
    if (offset > 0)
      Validate.notEmpty(sortCriteria, "Sorting criteria is required when the offset is positive."); 
    this
      ._properties = (properties != null) ? Collections.<String>unmodifiableList(properties) : Collections.<String>emptyList();
    this._resourceModels = resourceModels;
    this
      ._sortCriteria = (sortCriteria != null) ? Collections.<SortCriterion>unmodifiableList(sortCriteria) : Collections.<SortCriterion>emptyList();
    this._filter = filter;
    this._offset = offset;
    this._limit = limit;
    this._withTotalCount = withTotalCount;
  }
  
  public List<String> getProperties() {
    return this._properties;
  }
  
  public Collection<String> getResourceModels() {
    return this._resourceModels;
  }
  
  public Filter getFilter() {
    return this._filter;
  }
  
  public List<SortCriterion> getSortCriteria() {
    return this._sortCriteria;
  }
  
  public int getOffset() {
    return this._offset;
  }
  
  public int getLimit() {
    return this._limit;
  }
  
  public boolean getWithTotalCount() {
    return this._withTotalCount;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof Query))
      return false; 
    Query other = (Query)obj;
    return (this._properties.equals(other._properties) && 
      Iterables.elementsEqual(this._resourceModels, other._resourceModels) && 
      equalFilters(this._filter, other._filter) && this._sortCriteria
      .equals(other._sortCriteria) && this._offset == other._offset && this._limit == other._limit && this._withTotalCount == other._withTotalCount);
  }
  
  private static boolean equalFilters(Filter a, Filter b) {
    if (a == b)
      return true; 
    if (a == null)
      return false; 
    return a.equals(b);
  }
  
  public int hashCode() {
    int hash = 17;
    hash = 31 * hash + this._properties.hashCode();
    hash = 31 * hash + ((this._filter != null) ? this._filter.hashCode() : 0);
    hash = 31 * hash + this._sortCriteria.hashCode();
    for (String resourceModel : this._resourceModels)
      hash = 31 * hash + resourceModel.hashCode(); 
    hash = 31 * hash + this._offset;
    hash = 31 * hash + this._limit;
    hash = 31 * hash + (this._withTotalCount ? 1 : 0);
    return hash;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(SystemUtils.LINE_SEPARATOR);
    sb.append("Query [" + SystemUtils.LINE_SEPARATOR);
    sb.append("   _properties = " + this._properties);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _resourceModels = " + this._resourceModels);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _filter = " + this._filter);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _sortCriteria = ");
    if (this._sortCriteria.size() <= 1) {
      sb.append("[" + (!this._sortCriteria.isEmpty() ? (String)this._sortCriteria.get(0) : "") + "]");
    } else {
      sb.append("[");
      Iterator<SortCriterion> iterator = this._sortCriteria.iterator();
      while (iterator.hasNext()) {
        sb.append(SystemUtils.LINE_SEPARATOR + "      ");
        sb.append(iterator.next());
        if (iterator.hasNext())
          sb.append(","); 
      } 
      sb.append(SystemUtils.LINE_SEPARATOR + "   ]");
    } 
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _offset = " + this._offset);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _limit = " + this._limit);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _withTotalCount = " + this._withTotalCount);
    sb.append(SystemUtils.LINE_SEPARATOR + "]");
    return sb.toString();
  }
  
  public static final class Builder {
    private final List<String> _properties;
    
    private Collection<String> _resourceModels;
    
    private Filter _filter;
    
    private List<SortCriterion> _sortCriteria;
    
    private int _offset;
    
    private int _limit;
    
    private boolean _withTotalCount;
    
    private Builder(List<String> properties) {
      assert properties != null;
      this._properties = properties;
      this._offset = 0;
      this._limit = -1;
      this._withTotalCount = false;
    }
    
    public static Builder select(List<String> properties) {
      Validate.notNull(properties, "The collection of property names must not be null");
      Validate.noNullElements(properties, "The collection of property names must not contain null elements");
      Builder query = new Builder(properties);
      return query;
    }
    
    public static Builder select(String... properties) {
      Validate.notNull(properties, "The collection of property names must not be null");
      Validate.noNullElements((Object[])properties, "The collection of property names must not contain null elements");
      Builder query = select(Arrays.asList(properties));
      return query;
    }
    
    public Builder from(Collection<String> resourceModels) {
      Validate.notEmpty(resourceModels, "The collection of models must not be null or empty");
      Validate.noNullElements(resourceModels, "The collection of models must not contain null elements");
      this._resourceModels = resourceModels;
      return this;
    }
    
    public Builder from(String... resourceModels) {
      from(Arrays.asList(resourceModels));
      return this;
    }
    
    public Builder where(Filter filter) {
      this._filter = filter;
      return this;
    }
    
    public Builder where(PropertyPredicate... criteria) {
      this._filter = new Filter(Arrays.asList(criteria));
      return this;
    }
    
    public Builder where(LogicalOperator operator, PropertyPredicate... criteria) {
      this._filter = new Filter(Arrays.asList(criteria), operator);
      return this;
    }
    
    public Builder where(LogicalOperator operator, List<PropertyPredicate> criteria) {
      this._filter = new Filter(criteria, operator);
      return this;
    }
    
    public Builder where(String property, PropertyPredicate.ComparisonOperator operator, Object comparableValue) {
      return where(property, operator, comparableValue, false);
    }
    
    public Builder where(String property, PropertyPredicate.ComparisonOperator operator, Object comparableValue, boolean ignoreCase) {
      PropertyPredicate predicate = new PropertyPredicate(property, operator, comparableValue, ignoreCase);
      return where(new PropertyPredicate[] { predicate });
    }
    
    public Builder orderBy(String property) {
      return orderBy(property, SortCriterion.SortDirection.ASCENDING, false);
    }
    
    public Builder orderBy(String property, SortCriterion.SortDirection sortDirection, boolean ignoreCase) {
      this._sortCriteria = Arrays.asList(new SortCriterion[] { new SortCriterion(property, sortDirection, ignoreCase) });
      return this;
    }
    
    public Builder orderBy(List<SortCriterion> sortCriteria) {
      this._sortCriteria = sortCriteria;
      return this;
    }
    
    public Builder offset(int offset) {
      Validate.isTrue((offset >= 0), "Offset must not be negative");
      this._offset = offset;
      return this;
    }
    
    public Builder limit(int limit) {
      this._limit = limit;
      return this;
    }
    
    public Builder withTotalCount() {
      this._withTotalCount = true;
      return this;
    }
    
    public Query build() {
      Query query = new Query(this._properties, this._resourceModels, this._filter, this._sortCriteria, this._offset, this._limit, this._withTotalCount);
      return query;
    }
  }
}
