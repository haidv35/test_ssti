package com.vmware.cis.data.internal.provider.ext.search;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.UnqualifiedProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public final class SearchModelDescriptor {
  public static final String SEARCH_TERM = "searchTerm";
  
  public static final String TAGS = "tags";
  
  private final String _modelName;
  
  private final Collection<SearchChildModelDescriptor> _childModels;
  
  private final QuerySchema.ModelInfo _modelInfo;
  
  public SearchModelDescriptor(@Nonnull String modelName, @Nonnull Collection<SearchChildModelDescriptor> childModels) {
    assert !modelName.isEmpty();
    assert !childModels.isEmpty();
    this._modelName = modelName;
    this._childModels = childModels;
    this._modelInfo = createModelInfo(childModels);
  }
  
  public String getModelName() {
    return this._modelName;
  }
  
  public QuerySchema.ModelInfo getModelInfo() {
    return this._modelInfo;
  }
  
  public QuerySchema addModel(QuerySchema schema) {
    assert schema != null;
    Map<String, QuerySchema.ModelInfo> infoByModel = new LinkedHashMap<>(schema.getModels());
    infoByModel.put(this._modelName, this._modelInfo);
    return QuerySchema.forModels(infoByModel);
  }
  
  public boolean isSearchQuery(Query query) {
    assert query != null;
    if (query.getResourceModels().size() != 1)
      return false; 
    String model = query.getResourceModels().iterator().next();
    return this._modelName.equals(model);
  }
  
  public Collection<Query> toChildQueries(Query searchQuery) {
    assert searchQuery != null;
    validateQuery(searchQuery);
    String searchTerm = getSearchTerm(searchQuery);
    List<String> unqualifiedSelect = unqualify(searchQuery.getProperties());
    int limit = searchQuery.getLimit();
    List<Query> childQueries = new ArrayList<>(this._childModels.size());
    for (SearchChildModelDescriptor childDesc : this._childModels) {
      Query childQuery = childDesc.createQuery(searchTerm, unqualifiedSelect, limit);
      childQueries.add(childQuery);
    } 
    return childQueries;
  }
  
  public ResultSet toAggregatedResult(Collection<ResultSet> childResults, Query searchQuery) {
    assert childResults != null;
    assert childResults.size() == this._childModels.size();
    assert searchQuery != null;
    assert searchQuery.getWithTotalCount();
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(searchQuery.getProperties());
    List<String> unqualifiedSelect = unqualify(searchQuery.getProperties());
    int limit = searchQuery.getLimit();
    aggregateResultItems(childResults, unqualifiedSelect, limit, resultBuilder);
    int totalCount = accumulateTotalCount(childResults);
    resultBuilder.totalCount(Integer.valueOf(totalCount));
    return resultBuilder.build();
  }
  
  private static QuerySchema.ModelInfo createModelInfo(Collection<SearchChildModelDescriptor> childModels) {
    assert childModels != null;
    Map<String, QuerySchema.PropertyInfo> infoByProp = new LinkedHashMap<>();
    for (SearchChildModelDescriptor childModel : childModels) {
      for (String prop : childModel.getUnqualifiedProperties()) {
        if (PropertyUtil.isSpecialProperty(prop))
          continue; 
        String root = UnqualifiedProperty.getRootProperty(prop);
        infoByProp.put(root, QuerySchema.PropertyInfo.forNonFilterableProperty());
      } 
    } 
    infoByProp.put("searchTerm", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    infoByProp.put("tags", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    return new QuerySchema.ModelInfo(infoByProp);
  }
  
  private void validateQuery(Query query) {
    assert query != null;
    Filter filter = query.getFilter();
    validateSelect(query.getProperties());
    validateFilter(filter);
    validateSort(query.getSortCriteria());
    if (query.getLimit() <= 0)
      throw new UnsupportedOperationException("Must use positive limit for search model " + this._modelName); 
    if (query.getOffset() > 0)
      throw new UnsupportedOperationException("Cannot use offset for search model " + this._modelName); 
    if (!query.getWithTotalCount())
      throw new UnsupportedOperationException("Total count must always be requested for search model " + this._modelName); 
  }
  
  private void validateSelect(Collection<String> select) {
    assert select != null;
    for (String property : select) {
      if (property.endsWith("searchTerm"))
        throw new UnsupportedOperationException(String.format("Cannot retrieve property '%s' of search model '%s'", new Object[] { "searchTerm", this._modelName })); 
    } 
  }
  
  private void validateFilter(Filter filter) {
    if (filter == null)
      throw new UnsupportedOperationException("Filter is required for search model " + this._modelName); 
    if (!filter.getOperator().equals(LogicalOperator.AND))
      throw new UnsupportedOperationException(String.format("Unsupported logical operator %s for search model %s", new Object[] { filter
              
              .getOperator(), this._modelName })); 
    validatePredicates(filter.getCriteria());
  }
  
  private void validatePredicates(List<PropertyPredicate> predicates) {
    assert predicates != null;
    assert !predicates.isEmpty();
    boolean foundSearchTermPredicate = false;
    for (PropertyPredicate predicate : predicates) {
      assert predicate != null;
      String property = predicate.getProperty();
      if (property.endsWith("searchTerm")) {
        validateSearchTermPredicate(predicate);
        if (foundSearchTermPredicate)
          throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by multiple %s predicates.", new Object[] { this._modelName, "searchTerm" })); 
        foundSearchTermPredicate = true;
        continue;
      } 
      if (property.endsWith("tags")) {
        validateTagsPredicate(predicate);
        continue;
      } 
      throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by property '%s'", new Object[] { this._modelName, property }));
    } 
    if (!foundSearchTermPredicate)
      throw new UnsupportedOperationException(String.format("Predicate by %s is manditory for filtering '%s' model.", new Object[] { "searchTerm", this._modelName })); 
  }
  
  private void validateSearchTermPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()))
      throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by property '%s' using operator %s", new Object[] { this._modelName, predicate


              
              .getProperty(), predicate
              .getOperator().name() })); 
    assert predicate.getComparableValue() != null;
    if (!(predicate.getComparableValue() instanceof String))
      throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by property '%s' using comparable value of class %s", new Object[] { this._modelName, predicate


              
              .getProperty(), predicate
              .getComparableValue().getClass().getCanonicalName() })); 
  }
  
  private void validateTagsPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    assert predicate.getProperty().endsWith("tags");
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()) && 
      !PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(predicate.getOperator()))
      throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by property '%s' using operator %s", new Object[] { this._modelName, predicate

              
              .getProperty(), predicate
              .getOperator().name() })); 
    assert predicate.getComparableValue() != null;
    if (!(predicate.getComparableValue() instanceof String))
      throw new UnsupportedOperationException(String.format("Cannot filter search model '%s' by property '%s' using comparable value of class %s", new Object[] { this._modelName, predicate

              
              .getProperty(), predicate
              .getComparableValue().getClass().getCanonicalName() })); 
  }
  
  private List<SortCriterion> validateSort(List<SortCriterion> aggrSort) {
    assert aggrSort != null;
    for (SortCriterion sortCriterion : aggrSort) {
      String property = sortCriterion.getProperty();
      if (!"@modelKey".equals(property))
        throw new UnsupportedOperationException(String.format("Cannot order search model '%s' by property '%s'", new Object[] { this._modelName, property })); 
      if (!SortCriterion.SortDirection.ASCENDING.equals(sortCriterion.getSortDirection()))
        throw new UnsupportedOperationException(String.format("Cannot order search model '%s' by property '%s' in %s order", new Object[] { this._modelName, property, sortCriterion
                
                .getSortDirection() })); 
    } 
    return aggrSort;
  }
  
  private static String getSearchTerm(Query query) {
    assert query != null;
    assert query.getFilter() != null;
    assert query.getFilter().getCriteria().size() > 0;
    List<PropertyPredicate> predicates = query.getFilter().getCriteria();
    for (PropertyPredicate predicate : predicates) {
      assert predicate != null;
      if (predicate.getProperty().endsWith("searchTerm")) {
        assert predicate.getComparableValue() instanceof String;
        return (String)predicate.getComparableValue();
      } 
    } 
    throw new UnsupportedOperationException(
        String.format("No %s predicate found.", new Object[] { "searchTerm" }));
  }
  
  private static List<String> unqualify(List<String> qualified) {
    assert qualified != null;
    List<String> unqualified = new ArrayList<>(qualified.size());
    for (String qualifiedProp : qualified) {
      String unqualifiedProp = unqualify(qualifiedProp);
      unqualified.add(unqualifiedProp);
    } 
    return unqualified;
  }
  
  private static String unqualify(String qualified) {
    assert qualified != null;
    if (PropertyUtil.isSpecialProperty(qualified))
      return qualified; 
    return QualifiedProperty.forQualifiedName(qualified).getSimpleProperty();
  }
  
  private void aggregateResultItems(Collection<ResultSet> childResults, Collection<String> unqualifiedSelect, int limit, ResultSet.Builder resultBuilder) {
    assert childResults != null;
    assert childResults.size() == this._childModels.size();
    assert unqualifiedSelect != null;
    assert limit > 0;
    assert resultBuilder != null;
    int itemCount = 0;
    Iterator<SearchChildModelDescriptor> descIt = this._childModels.iterator();
    for (ResultSet childResult : childResults) {
      SearchChildModelDescriptor desc = descIt.next();
      for (ResourceItem childItem : childResult.getItems()) {
        List<Object> values = desc.reorderPropertyValues(unqualifiedSelect, childItem);
        resultBuilder.item(childItem.getKey(), values);
        itemCount++;
        if (itemCount >= limit)
          return; 
      } 
    } 
  }
  
  private int accumulateTotalCount(Collection<ResultSet> childResults) {
    assert childResults != null;
    int totalCount = 0;
    for (ResultSet childResult : childResults)
      totalCount += childResult.getTotalCount().intValue(); 
    return totalCount;
  }
}
