package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.adapters.vmomi.util.VmomiProperty;
import com.vmware.cis.data.internal.provider.util.filter.KeyPredicateMerger;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.cis.data.provider.Filter;
import com.vmware.vim.binding.cis.data.provider.PropertyPredicate;
import com.vmware.vim.binding.cis.data.provider.QuerySpec;
import com.vmware.vim.binding.cis.data.provider.SortCriterion;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.cis.CisIdConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VmomiDataProviderQueryConverter {
  private static final Logger _logger = LoggerFactory.getLogger(VmomiDataProviderQueryConverter.class);
  
  static final String TYPE_MANAGED_OBJECT = "ManagedObject";
  
  public static QuerySpec convertQuery(Query query) {
    assert query != null;
    query = KeyPredicateMerger.mergeKeyPredicates(query);
    if (query == null)
      return null; 
    query = NamePropertyValueConverter.escapeNamesInFilter(query);
    String resourceModel = getQueryResourceModel(query);
    QuerySpec vmomiQuery = new QuerySpec();
    vmomiQuery.setResourceModel(resourceModel);
    Filter filter = query.getFilter();
    if (filter != null) {
      Filter vmomiFilter = convertFilter(filter, resourceModel);
      if (vmomiFilter == null)
        return null; 
      vmomiQuery.setFilter(vmomiFilter);
    } 
    vmomiQuery.setOffset(convertOffset(query));
    vmomiQuery.setLimit(convertLimit(query));
    vmomiQuery.setReturnTotalCount(Boolean.valueOf(query.getWithTotalCount()));
    if (query.getLimit() == 0) {
      vmomiQuery.setProperties(new String[] { "@modelKey" });
      vmomiQuery.setSortCriteria(new SortCriterion[] { stubSortForTotalCountQuery() });
    } else {
      vmomiQuery.setProperties(convertProperties(query.getProperties()));
      vmomiQuery.setSortCriteria(convertSortCriteria(query.getSortCriteria()));
    } 
    return vmomiQuery;
  }
  
  static QuerySpec[] convertQueries(Collection<Query> batch) {
    assert batch != null;
    QuerySpec[] querySpecs = new QuerySpec[batch.size()];
    int i = 0;
    for (Query query : batch) {
      QuerySpec querySpec = convertQuery(query);
      if (querySpec == null)
        throw new IllegalArgumentException("Invalid query"); 
      querySpecs[i++] = querySpec;
    } 
    return querySpecs;
  }
  
  private static String getQueryResourceModel(Query query) {
    assert query.getResourceModels().size() <= 1;
    return query.getResourceModels().iterator().next();
  }
  
  private static Filter convertFilter(Filter filter, String resourceModel) {
    Filter vmomiFilter = new Filter();
    vmomiFilter.setOperator(convertLogicalOperator(filter.getOperator())
        .name());
    PropertyPredicate[] vmomiPredicates = convertPropertyPredicates(filter.getCriteria(), filter.getOperator(), resourceModel);
    if (vmomiPredicates == null)
      return null; 
    vmomiFilter.setCriteria(vmomiPredicates);
    return vmomiFilter;
  }
  
  private static Filter.LogicalOperator convertLogicalOperator(LogicalOperator operator) {
    assert operator != null;
    switch (operator) {
      case ASCENDING:
        return Filter.LogicalOperator.AND;
      case DESCENDING:
        return Filter.LogicalOperator.OR;
    } 
    throw new IllegalArgumentException(String.format("Unsupported logical operator: '%s'", new Object[] { operator
            .name() }));
  }
  
  private static PropertyPredicate[] convertPropertyPredicates(List<PropertyPredicate> predicates, LogicalOperator operator, String resourceModel) {
    assert predicates != null;
    assert predicates.size() > 0;
    List<PropertyPredicate> vmomiPredicates = new ArrayList<>();
    for (PropertyPredicate predicate : predicates) {
      PropertyPredicate vmomiPredicate = convertPropertyPredicate(predicate, resourceModel);
      if (vmomiPredicate != null) {
        vmomiPredicates.add(vmomiPredicate);
        continue;
      } 
      if (LogicalOperator.OR.equals(operator))
        continue; 
      return null;
    } 
    return vmomiPredicates.isEmpty() ? null : vmomiPredicates.<PropertyPredicate>toArray(
        new PropertyPredicate[vmomiPredicates.size()]);
  }
  
  private static PropertyPredicate convertPropertyPredicate(PropertyPredicate predicate, String resourceModel) {
    assert predicate != null;
    assert predicate.getComparableValue() != null;
    String property = predicate.getProperty();
    Object comparableValue = predicate.getComparableValue();
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    boolean isModelKey = PropertyUtil.isModelKey(property);
    boolean isForeignKey = VmomiProperty.isForeignKey(property);
    PropertyPredicate vmomiPredicate = new PropertyPredicate();
    vmomiPredicate.setProperty(convertProperty(property));
    vmomiPredicate.setOperator(convertComparisonOperator(operator).name());
    if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      Object[] compList = convertComparableCollection(comparableValue, isModelKey, isForeignKey, resourceModel);
      if (compList == null)
        return null; 
      vmomiPredicate.setComparableList(compList);
    } else {
      Object compValue = convertComparableValue(comparableValue, isModelKey, isForeignKey, resourceModel);
      if (compValue == null)
        return null; 
      vmomiPredicate.setComparableValue(compValue);
    } 
    vmomiPredicate.setIgnoreCase(predicate.isIgnoreCase() ? Boolean.TRUE : null);
    return vmomiPredicate;
  }
  
  private static PropertyPredicate.ComparisonOperator convertComparisonOperator(PropertyPredicate.ComparisonOperator operator) {
    assert operator != null;
    switch (operator) {
      case ASCENDING:
        return PropertyPredicate.ComparisonOperator.EQUAL;
      case DESCENDING:
        return PropertyPredicate.ComparisonOperator.NOT_EQUAL;
      case null:
        return PropertyPredicate.ComparisonOperator.GREATER;
      case null:
        return PropertyPredicate.ComparisonOperator.GREATER_OR_EQUAL;
      case null:
        return PropertyPredicate.ComparisonOperator.LESS;
      case null:
        return PropertyPredicate.ComparisonOperator.LESS_OR_EQUAL;
      case null:
        return PropertyPredicate.ComparisonOperator.IN;
      case null:
        return PropertyPredicate.ComparisonOperator.LIKE;
      case null:
        return PropertyPredicate.ComparisonOperator.UNSET;
    } 
    throw new IllegalArgumentException(String.format("Unsupported comparison operator: '%s'", new Object[] { operator
            .name() }));
  }
  
  private static Object[] convertComparableCollection(Object comparableValue, boolean isModelKey, boolean isForeignKey, String resourceModel) {
    assert comparableValue instanceof Collection;
    Collection<?> comparableCollection = (Collection)comparableValue;
    assert comparableCollection.size() > 0;
    List<Object> vmomiComparableList = new ArrayList();
    for (Object comparableElement : comparableCollection) {
      Object compValue = convertComparableValue(comparableElement, isModelKey, isForeignKey, resourceModel);
      if (compValue == null)
        continue; 
      vmomiComparableList.add(compValue);
    } 
    if (vmomiComparableList.isEmpty())
      return null; 
    return vmomiComparableList.toArray(new Object[vmomiComparableList.size()]);
  }
  
  private static Object convertComparableValue(Object comparableValue, boolean isModelKey, boolean isForeignKey, String resourceModel) {
    if (isModelKey)
      return convertModelKeyValue(comparableValue, resourceModel); 
    if (isForeignKey)
      return convertForeignKeyValue(comparableValue); 
    return comparableValue;
  }
  
  private static ManagedObjectReference convertForeignKeyValue(Object value) {
    Validate.isTrue(value instanceof String, "When the property '@moId' is used, the identifier have to be represented in string format.");
    ManagedObjectReference mor = null;
    try {
      mor = CisIdConverter.fromGlobalCisId((String)value, "ManagedObject");
    } catch (IllegalArgumentException iae) {
      _logger.warn("Error has occurred while converting foreign key comparable value", iae);
    } 
    return mor;
  }
  
  private static ManagedObjectReference convertModelKeyValue(Object comparableValue, String resourceModel) {
    if (comparableValue instanceof ManagedObjectReference)
      return (ManagedObjectReference)comparableValue; 
    assert comparableValue instanceof String;
    ManagedObjectReference mor = null;
    try {
      mor = CisIdConverter.fromGlobalCisId((String)comparableValue, resourceModel);
    } catch (IllegalArgumentException iae) {
      _logger.warn("Error has occurred while converting model key comparable value", iae);
    } 
    return mor;
  }
  
  private static String[] convertProperties(List<String> properties) {
    assert properties != null;
    List<String> vmomiProperties = new ArrayList<>(properties.size());
    boolean includedModelKey = false;
    for (String property : properties) {
      String vmomiProperty = convertProperty(property);
      if (PropertyUtil.isModelKey(vmomiProperty)) {
        if (includedModelKey)
          continue; 
        includedModelKey = true;
      } 
      vmomiProperties.add(vmomiProperty);
    } 
    if (!includedModelKey)
      vmomiProperties.add("@modelKey"); 
    return vmomiProperties.<String>toArray(new String[vmomiProperties.size()]);
  }
  
  private static String convertProperty(String property) {
    return VmomiProperty.toVmomiProperty(property);
  }
  
  private static SortCriterion[] convertSortCriteria(List<SortCriterion> criteria) {
    assert criteria != null;
    if (criteria.isEmpty())
      return null; 
    criteria = excludeSortByType(criteria);
    if (criteria.isEmpty())
      return new SortCriterion[] { stubSortForTotalCountQuery() }; 
    SortCriterion[] vmomiSortCriteria = new SortCriterion[criteria.size()];
    int i = 0;
    for (SortCriterion criterion : criteria)
      vmomiSortCriteria[i++] = convertSortCriterion(criterion); 
    return vmomiSortCriteria;
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
  
  private static SortCriterion convertSortCriterion(SortCriterion criterion) {
    assert criterion != null;
    assert criterion.getProperty() != null;
    assert criterion.getProperty().length() > 0;
    assert criterion.getSortDirection() != null;
    SortCriterion vmomiSort = new SortCriterion();
    vmomiSort.setProperty(convertProperty(criterion.getProperty()));
    vmomiSort.setSortDirection(convertSortDirection(criterion
          .getSortDirection()).name());
    vmomiSort.setIgnoreCase(criterion.isIgnoreCase() ? Boolean.TRUE : null);
    return vmomiSort;
  }
  
  private static SortCriterion.SortDirection convertSortDirection(SortCriterion.SortDirection sortDirection) {
    assert sortDirection != null;
    switch (sortDirection) {
      case ASCENDING:
        return SortCriterion.SortDirection.ASCENDING;
      case DESCENDING:
        return SortCriterion.SortDirection.DESCENDING;
    } 
    throw new IllegalArgumentException(String.format("Unsupported sort direction: '%s'", new Object[] { sortDirection
            .name() }));
  }
  
  private static Integer convertOffset(Query query) {
    assert query.getOffset() >= 0;
    int offset = query.getOffset();
    if (offset == 0)
      return null; 
    assert query.getSortCriteria() != null;
    assert query.getSortCriteria().size() > 0;
    return Integer.valueOf(offset);
  }
  
  private static Integer convertLimit(Query query) {
    int limit = query.getLimit();
    if (limit < 0)
      return null; 
    assert query.getSortCriteria() != null;
    assert query.getLimit() == 0 || query.getSortCriteria().size() > 0;
    return Integer.valueOf(limit);
  }
  
  private static SortCriterion stubSortForTotalCountQuery() {
    SortCriterion sortCriterion = new SortCriterion();
    sortCriterion.setProperty("@modelKey");
    return sortCriterion;
  }
}
