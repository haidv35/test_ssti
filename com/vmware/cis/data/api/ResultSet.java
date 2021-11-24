package com.vmware.cis.data.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public final class ResultSet {
  public static final ResultSet EMPTY_RESULT = new ResultSet(null, null, Integer.valueOf(0));
  
  private final List<String> _properties;
  
  private final List<ResourceItem> _items;
  
  private final Integer _totalCount;
  
  private ResultSet(List<String> properties, List<ResourceItem> items, Integer totalCount) {
    this
      ._properties = (properties != null) ? Collections.<String>unmodifiableList(properties) : Collections.<String>emptyList();
    this
      ._items = (items != null) ? Collections.<ResourceItem>unmodifiableList(items) : Collections.<ResourceItem>emptyList();
    this._totalCount = totalCount;
    for (ResourceItem item : this._items)
      Validate.isTrue((this._properties.size() == item.getPropertyValues().size()), "The number of values in the result item does not match the number of properties in the result"); 
    if (this._totalCount != null && this._totalCount.intValue() < this._items.size())
      throw new IllegalArgumentException("The number of total matches is less than the number of returned entities"); 
  }
  
  public List<String> getProperties() {
    return this._properties;
  }
  
  public List<ResourceItem> getItems() {
    return this._items;
  }
  
  public Integer getTotalCount() {
    return this._totalCount;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof ResultSet))
      return false; 
    ResultSet other = (ResultSet)obj;
    return (this._properties.equals(other._properties) && this._items
      .equals(other._items) && ((this._totalCount == null) ? (other._totalCount == null) : this._totalCount
      
      .equals(other._totalCount)));
  }
  
  public int hashCode() {
    int hash = 11;
    hash = 31 * hash + this._properties.hashCode();
    hash = 31 * hash + this._items.hashCode();
    hash = 31 * hash + ((this._totalCount != null) ? this._totalCount.intValue() : 0);
    return hash;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(SystemUtils.LINE_SEPARATOR);
    sb.append("ResultSet [" + SystemUtils.LINE_SEPARATOR);
    sb.append("   _properties = " + this._properties);
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _items = ");
    if (this._items.size() <= 1) {
      sb.append("[" + (!this._items.isEmpty() ? (String)this._items.get(0) : "") + "]");
    } else {
      sb.append("[");
      Iterator<ResourceItem> iterator = this._items.iterator();
      while (iterator.hasNext()) {
        sb.append(SystemUtils.LINE_SEPARATOR + "      ");
        sb.append(iterator.next());
        if (iterator.hasNext())
          sb.append(","); 
      } 
      sb.append(SystemUtils.LINE_SEPARATOR + "   ]");
    } 
    sb.append(SystemUtils.LINE_SEPARATOR);
    sb.append("   _totalCount = " + this._totalCount);
    sb.append(SystemUtils.LINE_SEPARATOR + "]");
    return sb.toString();
  }
  
  public static final class Builder {
    private final List<String> _properties;
    
    private final ResultSet.PropertyIndex _propertyIndex;
    
    private List<ResourceItem> _items;
    
    private List<ResourceItem> _appendedItems;
    
    private Integer _totalCount;
    
    private Builder(List<String> properties) {
      assert properties != null;
      this._properties = properties;
      this._propertyIndex = ResultSet.createPropertyIndex(properties);
    }
    
    public static Builder properties(List<String> properties) {
      Validate.notNull(properties, "The collection of property names must not be null");
      Validate.noNullElements(properties, "The collection of property names must not contain null elements");
      return new Builder(properties);
    }
    
    public static Builder properties(String... properties) {
      Validate.notNull(properties, "The collection of property names must not be null");
      Validate.noNullElements((Object[])properties, "The collection of property names must not contain null elements");
      return properties(Arrays.asList(properties));
    }
    
    public static Builder copy(ResultSet other) {
      Validate.notNull(other, "Cannot copy null result");
      Builder builder = new Builder(other._properties);
      builder._items = other._items;
      builder._totalCount = other._totalCount;
      return builder;
    }
    
    public Builder item(Object key, List<Object> propertyValues) {
      Validate.notNull(key, "Cannot create result item with no key.");
      Validate.notNull(propertyValues, "The collection of property values must not be null");
      Validate.isTrue((propertyValues.size() == this._properties.size()), "The number of values in the result item must match the number of property names in the result");
      Validate.isTrue((this._items == null), "Cannot use item() and items() methods in the same builder chain");
      if (this._appendedItems == null)
        this._appendedItems = new ArrayList<>(); 
      ResourceItem item = new ResourceItem(key, propertyValues, this._propertyIndex);
      this._appendedItems.add(item);
      return this;
    }
    
    public Builder item(Object key, Object... propertyValues) {
      Validate.notNull(propertyValues, "The collection of property values must not be null");
      return item(key, Arrays.asList(propertyValues));
    }
    
    public Builder totalCount(Integer totalCount) {
      this._totalCount = totalCount;
      return this;
    }
    
    public Builder sortItems(Comparator<ResourceItem> comparator) {
      Validate.notNull(comparator);
      Validate.isTrue((this._items == null), "Cannot order items of copied ResultSet.");
      if (this._appendedItems != null)
        Collections.sort(this._appendedItems, comparator); 
      return this;
    }
    
    public ResultSet build() {
      List<ResourceItem> items = (this._items != null) ? this._items : this._appendedItems;
      return new ResultSet(this._properties, items, this._totalCount);
    }
  }
  
  private static PropertyIndex createPropertyIndex(List<String> properties) {
    assert properties != null;
    if (properties.size() > 7)
      return new MapBasedPropertyIndex(properties); 
    return new ListBasedPropertyIndex(properties);
  }
  
  static interface PropertyIndex {
    int getIndexOfProperty(String param1String);
  }
  
  private static final class MapBasedPropertyIndex implements PropertyIndex {
    private final Map<String, Integer> _indexByProperty;
    
    MapBasedPropertyIndex(List<String> properties) {
      assert properties != null;
      Map<String, Integer> indexByProperty = new HashMap<>(properties.size());
      int index = 0;
      for (String property : properties)
        indexByProperty.put(property, Integer.valueOf(index++)); 
      this._indexByProperty = indexByProperty;
    }
    
    public int getIndexOfProperty(String property) {
      Integer index = this._indexByProperty.get(property);
      if (index != null)
        return index.intValue(); 
      return -1;
    }
  }
  
  private static final class ListBasedPropertyIndex implements PropertyIndex {
    private final List<String> _properties;
    
    ListBasedPropertyIndex(List<String> properties) {
      assert properties != null;
      this._properties = properties;
    }
    
    public int getIndexOfProperty(String property) {
      return this._properties.indexOf(property);
    }
  }
}
