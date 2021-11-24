package com.vmware.cis.data.internal.provider.merge;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyByName;
import com.vmware.cis.data.internal.provider.util.property.ResourceItemPropertyValueByNameViaIndexMap;
import java.util.Comparator;
import java.util.List;

public final class DefaultItemComparator implements Comparator<ResourceItem> {
  private final ResourceItemPropertyByName _propertyByName;
  
  private final List<SortCriterion> _sortCriteria;
  
  public DefaultItemComparator(List<String> properties, List<SortCriterion> sortCriteria) {
    assert properties != null;
    assert sortCriteria != null;
    assert !sortCriteria.isEmpty();
    this._propertyByName = new ResourceItemPropertyValueByNameViaIndexMap(properties);
    this._sortCriteria = sortCriteria;
  }
  
  public int compare(ResourceItem o1, ResourceItem o2) {
    assert o1 != null;
    assert o2 != null;
    for (SortCriterion criterion : this._sortCriteria) {
      String property = criterion.getProperty();
      int cmp = compareByProperty(o1, o2, property, criterion.isIgnoreCase());
      if (SortCriterion.SortDirection.DESCENDING.equals(criterion.getSortDirection()))
        cmp = -cmp; 
      if (cmp != 0)
        return cmp; 
    } 
    return 0;
  }
  
  private int compareByProperty(ResourceItem o1, ResourceItem o2, String property, boolean ignoreCase) {
    Object value1 = this._propertyByName.getValue(property, o1);
    Object value2 = this._propertyByName.getValue(property, o2);
    return compareValues(value1, value2, ignoreCase);
  }
  
  private int compareValues(Object value1, Object value2, boolean ignoreCase) {
    if (value1 == null && value2 == null)
      return 0; 
    if (value1 == null)
      return 1; 
    if (value2 == null)
      return -1; 
    if (value1 instanceof String && value2 instanceof String) {
      String str1 = (String)value1;
      String str2 = (String)value2;
      return compareStrings(str1, str2, ignoreCase);
    } 
    if (value1 instanceof Comparable) {
      Comparable<Object> comparable1 = (Comparable<Object>)value1;
      return comparable1.compareTo(value2);
    } 
    String string1 = value1.toString();
    String string2 = value2.toString();
    return compareStrings(string1, string2, ignoreCase);
  }
  
  private int compareStrings(String string1, String string2, boolean ignoreCase) {
    if (ignoreCase)
      return string1.compareToIgnoreCase(string2); 
    return string1.compareTo(string2);
  }
}
