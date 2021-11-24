package com.vmware.cis.data.internal.adapters.customfield;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelLookup;
import com.vmware.cis.data.internal.provider.util.filter.PredicateEvaluator;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.impl.vmodl.TypeNameImpl;
import com.vmware.vim.binding.vim.CustomFieldsManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.TypeName;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class CustomFieldDefDataProvider implements DataProvider {
  private static final Comparator<ResourceItem> RESOURCE_ITEM_BY_KEY_COMPARATOR = new Comparator<ResourceItem>() {
      public int compare(ResourceItem item1, ResourceItem item2) {
        ManagedObjectReference key1 = item1.<ManagedObjectReference>getKey();
        ManagedObjectReference key2 = item2.<ManagedObjectReference>getKey();
        return key1.getValue().compareTo(key2.getValue());
      }
    };
  
  private static final Map<String, String> _vimBaseTypeByDerivedType = getVimBaseTypeByDerivedType();
  
  private final CustomFieldRepository _customFields;
  
  private final String _serverGuid;
  
  private final AggregatedModelLookup _aggregatedModels;
  
  public CustomFieldDefDataProvider(Client vlsiClient, String serverGuid, AggregatedModelLookup aggregatedModels) {
    this(new VimCustomFieldsManagerRepository(vlsiClient), serverGuid, aggregatedModels);
  }
  
  CustomFieldDefDataProvider(CustomFieldRepository customFields, String serverGuid, AggregatedModelLookup aggregatedModels) {
    Validate.notNull(customFields);
    Validate.notNull(serverGuid);
    Validate.notNull(aggregatedModels);
    this._customFields = customFields;
    this._serverGuid = serverGuid;
    this._aggregatedModels = aggregatedModels;
  }
  
  public ResultSet executeQuery(Query query) {
    validateQuery(query);
    Collection<CustomFieldsManager.FieldDef> customFieldDefs = this._customFields.getCustomFieldDefs();
    List<String> properties = query.getProperties();
    Filter filter = query.getFilter();
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(properties);
    int totalCount = 0;
    for (CustomFieldsManager.FieldDef fieldDef : customFieldDefs) {
      if (evalFilter(filter, fieldDef)) {
        totalCount++;
        resultSetBuilder.item(buildKey(fieldDef), 
            getPropertyValues(properties, fieldDef));
      } 
    } 
    if (!query.getSortCriteria().isEmpty())
      resultSetBuilder.sortItems(RESOURCE_ITEM_BY_KEY_COMPARATOR); 
    if (query.getWithTotalCount())
      resultSetBuilder.totalCount(Integer.valueOf(totalCount)); 
    return resultSetBuilder.build();
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.PropertyInfo> props = new LinkedHashMap<>();
    props.put("CustomFieldDef/key", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT));
    props.put("CustomFieldDef/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    props.put("CustomFieldDef/managedObjectType", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    props.put("CustomFieldDef/applicableType", 
        QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    return QuerySchema.forProperties(props);
  }
  
  private ManagedObjectReference buildKey(CustomFieldsManager.FieldDef fieldDef) {
    assert fieldDef != null;
    return new ManagedObjectReference("CustomFieldDef", "field-" + 
        String.valueOf(fieldDef.getKey()), this._serverGuid);
  }
  
  private void validateQuery(Query query) {
    Validate.notNull(query);
    for (String model : query.getResourceModels()) {
      if (!"CustomFieldDef".equals(model))
        throw new IllegalArgumentException("Unsupported resource model: " + model); 
    } 
    List<SortCriterion> sortCriteria = query.getSortCriteria();
    if (!sortCriteria.isEmpty())
      for (SortCriterion sortCriterion : sortCriteria) {
        if (!sortCriterion.getProperty().equals("@modelKey"))
          throw new IllegalArgumentException("Cannot order by property: " + sortCriterion
              .getProperty()); 
        if (!sortCriterion.getSortDirection().equals(SortCriterion.SortDirection.ASCENDING))
          throw new IllegalArgumentException("Sort direction not allowed: " + sortCriterion
              .getSortDirection()); 
      }  
    int limit = query.getLimit();
    if (limit >= 0)
      throw new IllegalArgumentException("Limit is not allowed."); 
    int offset = query.getOffset();
    if (offset > 0)
      throw new IllegalArgumentException("Offset is not allowed."); 
  }
  
  private boolean evalFilter(Filter filter, CustomFieldsManager.FieldDef fieldDef) {
    if (filter == null)
      return true; 
    LogicalOperator logicalOperator = filter.getOperator();
    List<PropertyPredicate> criteria = filter.getCriteria();
    switch (logicalOperator) {
      case AND:
        return operatorAnd(criteria, fieldDef);
      case OR:
        return operatorOr(criteria, fieldDef);
    } 
    throw new IllegalArgumentException("Unsupported logical operator: " + logicalOperator);
  }
  
  private boolean operatorAnd(List<PropertyPredicate> criteria, CustomFieldsManager.FieldDef fieldDef) {
    for (PropertyPredicate propertyPredicate : criteria) {
      if (!evalPredicate(propertyPredicate, fieldDef))
        return false; 
    } 
    return true;
  }
  
  private boolean operatorOr(List<PropertyPredicate> criteria, CustomFieldsManager.FieldDef fieldDef) {
    for (PropertyPredicate propertyPredicate : criteria) {
      if (evalPredicate(propertyPredicate, fieldDef))
        return true; 
    } 
    return false;
  }
  
  private boolean evalPredicate(PropertyPredicate propertyPredicate, CustomFieldsManager.FieldDef fieldDef) {
    PropertyPredicate.ComparisonOperator operator = propertyPredicate.getOperator();
    if (operator != PropertyPredicate.ComparisonOperator.IN && operator != PropertyPredicate.ComparisonOperator.EQUAL && operator != PropertyPredicate.ComparisonOperator.UNSET)
      throw new IllegalArgumentException("Operator " + operator + " is not supported."); 
    Object comparableValue = propertyPredicate.getComparableValue();
    String property = propertyPredicate.getProperty();
    Object propertyValue = getPropertyValue(property, fieldDef);
    if ("CustomFieldDef/applicableType".equals(property)) {
      if (operator != PropertyPredicate.ComparisonOperator.EQUAL)
        throw new IllegalArgumentException("Operator " + operator + " is not supported for filtering by " + property + "."); 
      if (isCollection(comparableValue))
        throw new IllegalArgumentException("Collection comparable value for property " + property + " is not supported."); 
      if (propertyValue == null)
        return true; 
      propertyPredicate = replaceComparableValueIfNeeded(propertyPredicate);
    } 
    return PredicateEvaluator.eval(propertyPredicate, propertyValue);
  }
  
  private PropertyPredicate replaceComparableValueIfNeeded(PropertyPredicate originalPredicate) {
    Set<String> newComparableValues = null;
    String model = (String)originalPredicate.getComparableValue();
    if (isAggregatedType(model))
      newComparableValues = new LinkedHashSet<>(this._aggregatedModels.getChildrenOfAggregatedModel(model)); 
    String parentModel = _vimBaseTypeByDerivedType.get(model);
    if (parentModel != null)
      newComparableValues = new LinkedHashSet<>(Arrays.asList(new String[] { model, parentModel })); 
    addBaseModels(newComparableValues);
    String propertyName = originalPredicate.getProperty();
    return (newComparableValues == null) ? originalPredicate : new PropertyPredicate(propertyName, PropertyPredicate.ComparisonOperator.IN, newComparableValues);
  }
  
  private void addBaseModels(Set<String> models) {
    if (models == null || models.isEmpty())
      return; 
    Set<String> baseModels = getBaseModelsOf(models);
    while (!baseModels.isEmpty()) {
      models.addAll(baseModels);
      baseModels = getBaseModelsOf(baseModels);
    } 
  }
  
  private Set<String> getBaseModelsOf(Set<String> models) {
    Set<String> baseModels = new LinkedHashSet<>();
    for (String model : models) {
      String baseModel = _vimBaseTypeByDerivedType.get(model);
      if (baseModel != null)
        baseModels.add(baseModel); 
    } 
    return baseModels;
  }
  
  private boolean isAggregatedType(Object comparableValue) {
    return (comparableValue instanceof String && this._aggregatedModels
      .getAllAggregatedModels().contains(comparableValue));
  }
  
  private static boolean isCollection(Object obj) {
    return (obj instanceof Collection || obj.getClass().isArray());
  }
  
  private List<Object> getPropertyValues(List<String> properties, CustomFieldsManager.FieldDef fieldDef) {
    List<Object> result = new ArrayList(properties.size());
    for (String property : properties)
      result.add(getPropertyValue(property, fieldDef)); 
    return result;
  }
  
  private Object getPropertyValue(String property, CustomFieldsManager.FieldDef fieldDef) {
    TypeName moType;
    assert fieldDef != null;
    switch (property) {
      case "@modelKey":
        return buildKey(fieldDef);
      case "@type":
        return "CustomFieldDef";
      case "CustomFieldDef/key":
        return Integer.valueOf(fieldDef.getKey());
      case "CustomFieldDef/name":
        return fieldDef.getName();
      case "CustomFieldDef/managedObjectType":
      case "CustomFieldDef/applicableType":
        moType = fieldDef.getManagedObjectType();
        return (moType == null) ? null : ((TypeNameImpl)moType).getWsdlName();
    } 
    throw new IllegalArgumentException("No such property: " + property + " for model " + "CustomFieldDef");
  }
  
  private static Map<String, String> getVimBaseTypeByDerivedType() {
    Map<String, String> vimBaseTypeByDerivedType = new LinkedHashMap<>();
    vimBaseTypeByDerivedType.put("OpaqueNetwork", "Network");
    vimBaseTypeByDerivedType.put("DistributedVirtualPortgroup", "Network");
    vimBaseTypeByDerivedType.put("VmwareDistributedVirtualSwitch", "DistributedVirtualSwitch");
    vimBaseTypeByDerivedType.put("VirtualApp", "ResourcePool");
    vimBaseTypeByDerivedType.put("ClusterComputeResource", "ComputeResource");
    vimBaseTypeByDerivedType.put("StoragePod", "Folder");
    return Collections.unmodifiableMap(vimBaseTypeByDerivedType);
  }
}
