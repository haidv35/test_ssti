package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.cis.data.ResourceModelTypes;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.vapi.data.DataValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class DsVapiQueryConverter {
  private final VapiPropertyValueConverter _propertyValueConverter;
  
  DsVapiQueryConverter(VapiPropertyValueConverter propertyValueConverter) {
    assert propertyValueConverter != null;
    this._propertyValueConverter = propertyValueConverter;
  }
  
  ResourceModelTypes.QuerySpec convertQuery(Query query) {
    assert query != null;
    ResourceModelTypes.QuerySpec querySpec = new ResourceModelTypes.QuerySpec();
    querySpec.setResourceModels(new ArrayList<>(query.getResourceModels()));
    querySpec.setProperties(extractConvertedProperties(query));
    querySpec.setFilter(convertFilter(query.getFilter()));
    querySpec.setOffset(convertOffset(query));
    querySpec.setLimit(convertLimit(query));
    querySpec.setSortCriteria(convertSortCriteria(query.getSortCriteria()));
    querySpec.setReturnTotalCount(Boolean.valueOf(query.getWithTotalCount()));
    return querySpec;
  }
  
  private List<String> extractConvertedProperties(Query query) {
    if (query.getWithTotalCount())
      return Collections.emptyList(); 
    Set<String> qualifiedProperties = getQualifiedProperties(query);
    return convertProperties(new ArrayList<>(qualifiedProperties));
  }
  
  private Set<String> getQualifiedProperties(Query query) {
    List<String> properties = query.getProperties();
    String resourceModel = QueryClauseAnalyzer.getQueryResourceModel(query);
    return QueryClauseAnalyzer.qualifyPropertiesForResourceModel(properties, resourceModel);
  }
  
  private List<String> convertProperties(List<String> properties) {
    assert properties != null;
    List<String> vapiProperties = new ArrayList<>(properties.size());
    boolean includedModelKey = false;
    boolean includedType = false;
    for (String property : properties) {
      String vapiProperty = convertProperty(property);
      if (PropertyUtil.isModelKey(vapiProperty)) {
        includedModelKey = true;
      } else if (PropertyUtil.isType(vapiProperty)) {
        includedType = true;
      } 
      vapiProperties.add(vapiProperty);
    } 
    if (!includedModelKey)
      vapiProperties.add("@modelKey"); 
    if (!includedType && this._propertyValueConverter.isTypeRequired())
      vapiProperties.add("@type"); 
    return vapiProperties;
  }
  
  private List<ResourceModelTypes.SortCriterion> convertSortCriteria(List<SortCriterion> criteria) {
    assert criteria != null;
    if (criteria.isEmpty())
      return null; 
    criteria = excludeSortByType(criteria);
    if (criteria.isEmpty())
      return null; 
    List<ResourceModelTypes.SortCriterion> vapiSortCriteria = new ArrayList<>(criteria.size());
    for (SortCriterion criterion : criteria)
      vapiSortCriteria.add(convertSortCriterion(criterion)); 
    return vapiSortCriteria;
  }
  
  private static List<SortCriterion> excludeSortByType(List<SortCriterion> criteria) {
    List<SortCriterion> filteredSortingCriteria = new ArrayList<>(criteria.size());
    for (SortCriterion criterion : criteria) {
      if (PropertyUtil.isType(criterion.getProperty()))
        continue; 
      filteredSortingCriteria.add(criterion);
    } 
    return filteredSortingCriteria;
  }
  
  private ResourceModelTypes.SortCriterion convertSortCriterion(SortCriterion criterion) {
    assert criterion != null;
    assert criterion.getProperty() != null;
    assert criterion.getProperty().length() > 0;
    assert criterion.getSortDirection() != null;
    ResourceModelTypes.SortCriterion vapiSort = new ResourceModelTypes.SortCriterion();
    vapiSort.setProperty(convertProperty(criterion.getProperty()));
    vapiSort.setSortDirection(convertSortDirection(criterion
          .getSortDirection()));
    return vapiSort;
  }
  
  private static ResourceModelTypes.SortCriterion.SortDirection convertSortDirection(SortCriterion.SortDirection sortDirection) {
    assert sortDirection != null;
    switch (sortDirection) {
      case AND:
        return ResourceModelTypes.SortCriterion.SortDirection.ASCENDING;
      case OR:
        return ResourceModelTypes.SortCriterion.SortDirection.DESCENDING;
    } 
    throw new IllegalArgumentException(String.format("Unsupported sort direction: '%s'", new Object[] { sortDirection
            .name() }));
  }
  
  private ResourceModelTypes.Filter convertFilter(Filter filter) {
    if (filter == null)
      return null; 
    ResourceModelTypes.Filter vapiFilter = new ResourceModelTypes.Filter();
    vapiFilter.setOperator(convertLogicalOperator(filter.getOperator()));
    vapiFilter.setCriteria(convertPropertyPredicates(filter.getCriteria()));
    return vapiFilter;
  }
  
  private List<ResourceModelTypes.PropertyPredicate> convertPropertyPredicates(List<PropertyPredicate> predicates) {
    assert predicates != null;
    assert predicates.size() > 0;
    List<ResourceModelTypes.PropertyPredicate> vapiPredicates = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates)
      vapiPredicates.add(convertPropertyPredicate(predicate)); 
    return vapiPredicates;
  }
  
  private ResourceModelTypes.PropertyPredicate convertPropertyPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    String property = predicate.getProperty();
    Object comparableValue = predicate.getComparableValue();
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    if (predicate.isIgnoreCase())
      throw new IllegalArgumentException("Case-sensitive predicates are not supported"); 
    ResourceModelTypes.PropertyPredicate vapiPredicate = new ResourceModelTypes.PropertyPredicate();
    vapiPredicate.setProperty(convertProperty(property));
    vapiPredicate.setOperator(convertComparisonOperator(operator));
    if (PropertyPredicate.ComparisonOperator.IN.equals(operator) || PropertyPredicate.ComparisonOperator.NOT_IN.equals(operator)) {
      vapiPredicate.setComparableList(convertComparableCollection(predicate
            .getProperty(), comparableValue));
    } else {
      vapiPredicate.setComparableValue(convertComparableValue(predicate
            .getProperty(), comparableValue));
    } 
    return vapiPredicate;
  }
  
  private List<DataValue> convertComparableCollection(String property, Object comparableValue) {
    assert comparableValue instanceof Collection;
    Collection<?> comparableList = (Collection)comparableValue;
    List<DataValue> vapiComparableList = new ArrayList<>(comparableList.size());
    for (Object comparableElement : comparableList) {
      DataValue vapiComparableElement = this._propertyValueConverter.toVapiComparableValue(property, comparableElement);
      vapiComparableList.add(vapiComparableElement);
    } 
    return vapiComparableList;
  }
  
  private DataValue convertComparableValue(String property, Object comparableValue) {
    assert comparableValue != null;
    return this._propertyValueConverter.toVapiComparableValue(property, comparableValue);
  }
  
  private static ResourceModelTypes.PropertyPredicate.ComparisonOperator convertComparisonOperator(PropertyPredicate.ComparisonOperator operator) {
    assert operator != null;
    switch (operator) {
      case AND:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.EQUAL;
      case OR:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.NOT_EQUAL;
      case null:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.GREATER;
      case null:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.GREATER_OR_EQUAL;
      case null:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.LESS;
      case null:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.LESS_OR_EQUAL;
      case null:
        return ResourceModelTypes.PropertyPredicate.ComparisonOperator.IN;
    } 
    throw new IllegalArgumentException(String.format("Unsupported comparison operator: '%s'", new Object[] { operator
            .name() }));
  }
  
  private static ResourceModelTypes.Filter.LogicalOperator convertLogicalOperator(LogicalOperator operator) {
    assert operator != null;
    switch (operator) {
      case AND:
        return ResourceModelTypes.Filter.LogicalOperator.AND;
      case OR:
        return ResourceModelTypes.Filter.LogicalOperator.OR;
    } 
    throw new IllegalArgumentException(String.format("Unsupported logical operator: '%s'", new Object[] { operator
            .name() }));
  }
  
  private static String convertProperty(String property) {
    assert property != null;
    if (PropertyUtil.isSpecialProperty(property))
      return property; 
    return QualifiedProperty.forQualifiedName(property).getSimpleProperty();
  }
  
  private static Long convertOffset(Query query) {
    assert query.getOffset() >= 0;
    int offset = query.getOffset();
    if (offset == 0)
      return null; 
    assert query.getSortCriteria() != null;
    assert query.getSortCriteria().size() > 0;
    return Long.valueOf(offset);
  }
  
  private static Long convertLimit(Query query) {
    int limit = query.getLimit();
    if (limit <= 0)
      return null; 
    assert query.getSortCriteria() != null;
    assert query.getLimit() == 0 || query.getSortCriteria().size() > 0;
    return Long.valueOf(limit);
  }
}
