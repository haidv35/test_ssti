package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AggregatedModelQueryConverter {
  private final String _aggregatedModel;
  
  private final String _childModel;
  
  private final AggregatedModelPropertyConverter _propertyConverter;
  
  public AggregatedModelQueryConverter(String aggregatedModel, String childModel, Set<String> childModelPropertiesNonQualified) {
    assert aggregatedModel != null;
    assert childModel != null;
    assert childModelPropertiesNonQualified != null;
    this._aggregatedModel = aggregatedModel;
    this._childModel = childModel;
    this._propertyConverter = new AggregatedModelPropertyConverter(aggregatedModel, childModel, childModelPropertiesNonQualified);
  }
  
  public Query toChildQuery(Query query) {
    assert query != null;
    Filter filter = toChildFilter(query.getFilter());
    if (filter == null && query.getFilter() != null)
      return null; 
    List<String> select = toChildSelect(query.getProperties());
    validateSelectNoDuplicates(select);
    if (!query.getWithTotalCount() && select.isEmpty() && 
      !query.getProperties().contains("@modelKey"))
      return null; 
    List<SortCriterion> sort = toChildSort(query.getSortCriteria());
    Collection<String> models = this._propertyConverter.toChildModels(query
        .getResourceModels());
    return QueryCopy.copyAndSelect(query, select)
      .from(models)
      .where(filter)
      .orderBy(sort)





      
      .offset(0)
      .limit(getLimitForAggregation(query.getOffset(), query.getLimit()))
      .build();
  }
  
  private List<String> toChildSelect(List<String> aggregatedSelect) {
    assert aggregatedSelect != null;
    List<String> selectedProperties = new ArrayList<>();
    for (String property : aggregatedSelect) {
      String childProperty = this._propertyConverter.toChildProperty(property);
      addIfNonNull(selectedProperties, childProperty);
    } 
    return selectedProperties;
  }
  
  private void validateSelectNoDuplicates(List<String> childSelect) {
    Set<String> selectedSet = new HashSet<>(childSelect.size());
    for (String childProperty : childSelect) {
      if (selectedSet.contains(childProperty))
        throw new UnsupportedOperationException(String.format("Selected duplicate property '%s' in query for aggregated model '%s'", new Object[] { childProperty, this._aggregatedModel })); 
      selectedSet.add(childProperty);
    } 
  }
  
  private Filter toChildFilter(Filter filter) {
    if (filter == null)
      return null; 
    assert filter.getOperator() != null;
    assert filter.getCriteria() != null;
    assert !filter.getCriteria().isEmpty();
    switch (filter.getOperator()) {
      case AND:
        return toChildConjunction(filter.getCriteria());
      case OR:
        return toChildDisjunction(filter.getCriteria());
    } 
    throw new UnsupportedOperationException("Unsupported logical operator: " + filter
        .getOperator());
  }
  
  private Filter toChildConjunction(List<PropertyPredicate> predicates) {
    assert predicates != null;
    assert !predicates.isEmpty();
    List<PropertyPredicate> childPredicates = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      PropertyPredicate childPredicate = toChildPredicate(predicate);
      if (childPredicate == null)
        return null; 
      childPredicates.add(childPredicate);
    } 
    return new Filter(childPredicates, LogicalOperator.AND);
  }
  
  private Filter toChildDisjunction(List<PropertyPredicate> predicates) {
    assert predicates != null;
    assert !predicates.isEmpty();
    List<PropertyPredicate> childPredicates = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      PropertyPredicate childPredicate = toChildPredicate(predicate);
      addIfNonNull(childPredicates, childPredicate);
    } 
    if (childPredicates.isEmpty())
      return null; 
    return new Filter(childPredicates, LogicalOperator.OR);
  }
  
  private PropertyPredicate toChildPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    String property = this._propertyConverter.toChildProperty(predicate
        .getProperty());
    if (property == null)
      return null; 
    return new PropertyPredicate(property, predicate.getOperator(), predicate
        .getComparableValue(), predicate.isIgnoreCase());
  }
  
  private List<SortCriterion> toChildSort(List<SortCriterion> criteria) {
    assert criteria != null;
    List<SortCriterion> childSort = new ArrayList<>(criteria.size());
    for (SortCriterion criterion : criteria) {
      SortCriterion childCriterion = toChildSortCriterion(criterion);
      addIfNonNull(childSort, childCriterion);
    } 
    if (childSort.isEmpty() && !criteria.isEmpty())
      throw new UnsupportedOperationException(String.format("No sort criteria applicable to model '%s' in query for aggregated model '%s'", new Object[] { this._childModel, this._aggregatedModel })); 
    return childSort;
  }
  
  private SortCriterion toChildSortCriterion(SortCriterion criterion) {
    assert criterion != null;
    String property = this._propertyConverter.toChildProperty(criterion
        .getProperty());
    if (property == null)
      return null; 
    return new SortCriterion(property, criterion
        .getSortDirection(), criterion.isIgnoreCase());
  }
  
  private static <T> void addIfNonNull(List<T> list, T element) {
    assert list != null;
    if (element == null)
      return; 
    list.add(element);
  }
  
  private static int getLimitForAggregation(int originalOffset, int originalLimit) {
    if (originalLimit == 0)
      return 0; 
    if (originalLimit < 0)
      return -1; 
    return originalOffset + originalLimit;
  }
}
