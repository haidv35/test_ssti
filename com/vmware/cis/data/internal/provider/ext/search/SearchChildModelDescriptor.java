package com.vmware.cis.data.internal.provider.ext.search;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public final class SearchChildModelDescriptor {
  private static final String NON_SYS_FOLDER = "NonSystemFolder";
  
  private static final String NON_ROOT_RP = "NonRootResourcePool";
  
  private static final String FOLDER = "Folder";
  
  private static final String RP = "ResourcePool";
  
  private static final String IS_ROOT_RP = "isRootRP";
  
  private static final String IS_SYSTEM_FOLDER = "isSystemFolder";
  
  private static final PropertyPredicate isNotSystemFolder = new PropertyPredicate("Folder/isSystemFolder", PropertyPredicate.ComparisonOperator.EQUAL, 


      
      Boolean.valueOf(false));
  
  private static final PropertyPredicate isNotRootRp = new PropertyPredicate("ResourcePool/isRootRP", PropertyPredicate.ComparisonOperator.EQUAL, 


      
      Boolean.valueOf(false));
  
  private final String _modelName;
  
  private final String _nativeModelName;
  
  private final Collection<FilterPropInfo> _filterProps;
  
  private final Map<String, String> _qualifiedByUnqualifiedSelectable;
  
  private SearchChildModelDescriptor(String modelName, String nativeModelName, Collection<FilterPropInfo> filterProps, Map<String, String> qualifiedByUnqualifiedSelectable) {
    assert modelName != null;
    assert nativeModelName != null;
    assert filterProps != null;
    assert qualifiedByUnqualifiedSelectable != null;
    this._modelName = modelName;
    this._nativeModelName = nativeModelName;
    this._filterProps = filterProps;
    this._qualifiedByUnqualifiedSelectable = qualifiedByUnqualifiedSelectable;
  }
  
  Collection<String> getUnqualifiedProperties() {
    Collection<String> props = new ArrayList<>(this._qualifiedByUnqualifiedSelectable.size() + this._filterProps.size());
    props.addAll(this._qualifiedByUnqualifiedSelectable.keySet());
    for (FilterPropInfo info : this._filterProps) {
      String unqualified = QualifiedProperty.forQualifiedName(info.propertyName).getSimpleProperty();
      props.add(unqualified);
    } 
    return props;
  }
  
  Query createQuery(String searchTerm, Collection<String> unqualifiedSelect, int limit) {
    assert searchTerm != null;
    assert unqualifiedSelect != null;
    assert limit >= 0;
    List<String> select = createSelect(unqualifiedSelect);
    Filter filter = createFilter(searchTerm);
    Query query = Query.Builder.select(select).withTotalCount().from(new String[] { this._nativeModelName }).where(filter).orderBy("@modelKey").limit(limit).build();
    return query;
  }
  
  List<Object> reorderPropertyValues(Collection<String> unqualifiedColumns, ResourceItem resultItem) {
    assert unqualifiedColumns != null;
    assert resultItem != null;
    List<Object> values = new ArrayList(unqualifiedColumns.size());
    for (String unqualifiedProp : unqualifiedColumns) {
      String childProp = this._qualifiedByUnqualifiedSelectable.get(unqualifiedProp);
      Object value = null;
      if (childProp != null)
        value = resultItem.get(childProp); 
      values.add(value);
    } 
    return values;
  }
  
  private List<String> createSelect(Collection<String> unqualifiedSelect) {
    assert unqualifiedSelect != null;
    List<String> select = new ArrayList<>(unqualifiedSelect.size());
    for (String unqualifiedProp : unqualifiedSelect) {
      String prop = this._qualifiedByUnqualifiedSelectable.get(unqualifiedProp);
      if (prop == null)
        continue; 
      select.add(prop);
    } 
    return select;
  }
  
  private Filter createFilter(String searchTerm) {
    assert searchTerm != null;
    List<PropertyPredicate> predicates = new ArrayList<>(this._filterProps.size());
    for (FilterPropInfo filterPropInfo : this._filterProps) {
      PropertyPredicate predicate = filterPropInfo.filterBy(searchTerm);
      predicates.add(predicate);
    } 
    LogicalOperator op = LogicalOperator.OR;
    if ("NonSystemFolder".equals(this._modelName)) {
      assert predicates.size() == 1;
      predicates.add(isNotSystemFolder);
      op = LogicalOperator.AND;
    } 
    if ("NonRootResourcePool".equals(this._modelName)) {
      assert predicates.size() == 1;
      predicates.add(isNotRootRp);
      op = LogicalOperator.AND;
    } 
    if (predicates.size() == 1)
      return new Filter(predicates); 
    return new Filter(predicates, op);
  }
  
  public static Builder childModel(String modelName) {
    return new Builder(modelName);
  }
  
  public static SearchChildModelDescriptor childModel(String modelName, String unqualifiedProp) {
    return (new Builder(modelName))
      .matchIgnoreCase(unqualifiedProp)
      .selectable(unqualifiedProp)
      .build();
  }
  
  public static final class Builder {
    private final String _modelName;
    
    private final String _nativeModelName;
    
    private Collection<SearchChildModelDescriptor.FilterPropInfo> _filterProps;
    
    private Collection<String> _unqualifiedSelectProps;
    
    private Builder(String modelName) {
      this._modelName = modelName;
      this._nativeModelName = getNativeModel(modelName);
      this._filterProps = new ArrayList<>();
      this._unqualifiedSelectProps = new ArrayList<>();
    }
    
    public Builder matchIgnoreCase(@Nonnull String unqualifiedFilterProp) {
      return match(unqualifiedFilterProp, false);
    }
    
    public Builder exactMatchIgnoreCase(String unqualifiedFilterProp) {
      return match(unqualifiedFilterProp, true);
    }
    
    private Builder match(String unqualifiedFilterProp, boolean exactMatch) {
      assert unqualifiedFilterProp != null;
      String prop = qualify(unqualifiedFilterProp);
      SearchChildModelDescriptor.FilterPropInfo info = new SearchChildModelDescriptor.FilterPropInfo(prop, exactMatch);
      this._filterProps.add(info);
      return this;
    }
    
    public Builder selectable(@Nonnull String unqualifiedSelectProp) {
      this._unqualifiedSelectProps.add(unqualifiedSelectProp);
      return this;
    }
    
    public SearchChildModelDescriptor build() {
      assert !this._filterProps.isEmpty();
      assert !this._unqualifiedSelectProps.isEmpty();
      Map<String, String> qualifiedByUnqualified = new LinkedHashMap<>(this._unqualifiedSelectProps.size());
      for (String unqualifiedProp : this._unqualifiedSelectProps) {
        String prop = qualify(unqualifiedProp);
        qualifiedByUnqualified.put(unqualifiedProp, prop);
      } 
      qualifiedByUnqualified.put("@modelKey", "@modelKey");
      qualifiedByUnqualified.put("@type", "@type");
      return new SearchChildModelDescriptor(this._modelName, this._nativeModelName, this._filterProps, qualifiedByUnqualified);
    }
    
    private String qualify(String unqualifiedProp) {
      assert unqualifiedProp != null;
      String prop = this._nativeModelName + '/' + unqualifiedProp;
      return prop;
    }
    
    private static String getNativeModel(String model) {
      assert model != null;
      if ("NonSystemFolder".equals(model))
        return "Folder"; 
      if ("NonRootResourcePool".equals(model))
        return "ResourcePool"; 
      return model;
    }
  }
  
  private static final class FilterPropInfo {
    private final String propertyName;
    
    private final boolean exactMatch;
    
    private FilterPropInfo(String propertyName, boolean exactMatch) {
      assert propertyName != null;
      this.propertyName = propertyName;
      this.exactMatch = exactMatch;
    }
    
    private PropertyPredicate filterBy(String searchTerm) {
      assert searchTerm != null;
      if (this.exactMatch)
        return new PropertyPredicate(this.propertyName, PropertyPredicate.ComparisonOperator.EQUAL, searchTerm, true); 
      return PropertyPredicate.containsIgnoreCase(this.propertyName, searchTerm);
    }
  }
}
