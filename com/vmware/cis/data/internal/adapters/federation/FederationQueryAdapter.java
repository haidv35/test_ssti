package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FederationQueryAdapter {
  private static final Logger _logger = LoggerFactory.getLogger(FederationQueryAdapter.class);
  
  private static final List<SortCriterion> SORT_BY_KEY = Collections.singletonList(new SortCriterion("@modelKey"));
  
  private static final Filter FILTER_MATCH_NONE = new Filter(
      Collections.singletonList(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, "no-such-key")));
  
  private final QueryRouter _queryRouter;
  
  private final FederationQuerySchemaLookup _schemaLookup;
  
  public FederationQueryAdapter(QueryRouter queryRouter, FederationQuerySchemaLookup schemaLookup) {
    assert queryRouter != null;
    assert schemaLookup != null;
    this._queryRouter = queryRouter;
    this._schemaLookup = schemaLookup;
  }
  
  public void validateQuery(Query query) {
    assert query != null;
    for (String property : query.getProperties()) {
      if (!isSelectableAnywhere(property))
        throw new IllegalArgumentException("Cannot select property: " + property); 
    } 
    if (query.getFilter() != null)
      for (PropertyPredicate predicate : query.getFilter().getCriteria()) {
        if (!isFilterableAnywhere(predicate))
          throw new IllegalArgumentException("Cannot filter by non-filterable property: " + predicate
              .getProperty()); 
      }  
    if (!query.getSortCriteria().isEmpty())
      for (SortCriterion sortCriterion : query.getSortCriteria()) {
        String property = sortCriterion.getProperty();
        if (!isSortableAnywhere(property))
          throw new IllegalArgumentException("Cannot sort by non-filterable property: " + property); 
      }  
  }
  
  public Query adaptQuery(Query query, String nodeId) {
    assert query != null;
    assert !StringUtils.isEmpty(nodeId);
    Query routedQuery = this._queryRouter.route(query, nodeId);
    if (routedQuery == null)
      return null; 
    if (!requiresConversion(routedQuery, nodeId))
      return routedQuery; 
    Filter newFilter = adaptFilter(routedQuery.getFilter(), nodeId);
    if (FILTER_MATCH_NONE.equals(newFilter))
      return null; 
    List<String> newSelect = adaptSelect(routedQuery.getProperties(), nodeId);
    List<SortCriterion> newSort = adaptSort(routedQuery.getSortCriteria(), nodeId);
    Collection<String> supportedModels = getSupportedModels(routedQuery.getResourceModels(), nodeId);
    if (supportedModels.isEmpty())
      return null; 
    Collection<String> newModels = adaptModels(newSelect, newFilter, newSort, supportedModels);
    if (newSort.isEmpty() && (routedQuery.getOffset() > 0 || routedQuery.getLimit() > 0))
      newSort = SORT_BY_KEY; 
    return QueryCopy.copyAndSelect(routedQuery, newSelect)
      .from(newModels)
      .where(newFilter)
      .orderBy(newSort)
      .build();
  }
  
  private Collection<String> adaptModels(List<String> properties, Filter filter, List<SortCriterion> sortCriteria, Collection<String> resourceModels) {
    if (resourceModels.size() == 1)
      return resourceModels; 
    Collection<String> selectedModels = QueryQualifier.getFromClause(properties, filter, sortCriteria);
    return selectedModels.isEmpty() ? resourceModels : selectedModels;
  }
  
  private List<String> adaptSelect(List<String> properties, String nodeId) {
    if (properties.isEmpty())
      return properties; 
    List<String> newProperties = new ArrayList<>(properties.size());
    for (String property : properties) {
      if (isSelectable(property, nodeId))
        newProperties.add(property); 
    } 
    return newProperties;
  }
  
  private Filter adaptFilter(Filter filter, String nodeId) {
    if (filter == null)
      return filter; 
    List<PropertyPredicate> predicates = filter.getCriteria();
    LogicalOperator operator = filter.getOperator();
    List<PropertyPredicate> newPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (isFilterable(predicate, nodeId)) {
        newPredicates.add(predicate);
        continue;
      } 
      _logger.debug("Skip query predicate that filters by non-filterable property '{}' on node id '{}'.", predicate
          .getProperty(), nodeId);
    } 
    if (operator.equals(LogicalOperator.AND) && newPredicates.size() < predicates.size())
      return FILTER_MATCH_NONE; 
    if (newPredicates.isEmpty())
      return FILTER_MATCH_NONE; 
    return new Filter(newPredicates, operator);
  }
  
  private List<SortCriterion> adaptSort(List<SortCriterion> sortCriteria, String nodeId) {
    if (sortCriteria.isEmpty())
      return sortCriteria; 
    List<SortCriterion> newSortCriteria = new ArrayList<>(sortCriteria.size());
    for (SortCriterion sortCriterion : sortCriteria) {
      String property = sortCriterion.getProperty();
      if (isSortable(property, nodeId)) {
        newSortCriteria.add(sortCriterion);
        continue;
      } 
      _logger.debug("Skip query sort criteria that orders by non-sortable property '{}' on node id '{}'.", property, nodeId);
    } 
    return newSortCriteria;
  }
  
  private boolean requiresConversion(Query query, String nodeId) {
    for (String property : query.getProperties()) {
      if (!isSelectable(property, nodeId))
        return true; 
    } 
    if (query.getFilter() != null)
      for (PropertyPredicate predicate : query.getFilter().getCriteria()) {
        if (!isFilterable(predicate, nodeId))
          return true; 
      }  
    if (!query.getSortCriteria().isEmpty())
      for (SortCriterion sortCriterion : query.getSortCriteria()) {
        String property = sortCriterion.getProperty();
        if (!isSortable(property, nodeId))
          return true; 
      }  
    for (String resourceModel : query.getResourceModels()) {
      if (!this._schemaLookup.isModelSupported(resourceModel, nodeId))
        return true; 
    } 
    return false;
  }
  
  private boolean isSelectable(String property, String nodeId) {
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    return this._schemaLookup.isSelectable(qualifiedProperty.getResourceModel(), qualifiedProperty
        .getSimpleProperty(), nodeId);
  }
  
  private boolean isSelectableAnywhere(String property) {
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    return this._schemaLookup.isSelectableAnywhere(qualifiedProperty.getResourceModel(), qualifiedProperty
        .getSimpleProperty());
  }
  
  private boolean isFilterable(PropertyPredicate predicate, String nodeId) {
    String property = predicate.getProperty();
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    String model = qualifiedProperty.getResourceModel();
    String propertyPath = qualifiedProperty.getSimpleProperty();
    switch (predicate.getOperator()) {
      case UNSET:
        return this._schemaLookup.isFilterableByUnset(model, propertyPath, nodeId);
    } 
    return this._schemaLookup.isFilterable(model, propertyPath, nodeId);
  }
  
  private boolean isFilterableAnywhere(PropertyPredicate predicate) {
    String property = predicate.getProperty();
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    String model = qualifiedProperty.getResourceModel();
    String propertyPath = qualifiedProperty.getSimpleProperty();
    switch (predicate.getOperator()) {
      case UNSET:
        return this._schemaLookup.isFilterableByUnsetAnywhere(model, propertyPath);
    } 
    return this._schemaLookup.isFilterableAnywhere(model, propertyPath);
  }
  
  private boolean isSortable(String property, String nodeId) {
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    return this._schemaLookup.isSortable(qualifiedProperty.getResourceModel(), qualifiedProperty
        .getSimpleProperty(), nodeId);
  }
  
  private boolean isSortableAnywhere(String property) {
    if (PropertyUtil.isSpecialProperty(property) || PropertyUtil.isInstanceUuid(property))
      return true; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    return this._schemaLookup.isSortableAnywhere(qualifiedProperty.getResourceModel(), qualifiedProperty
        .getSimpleProperty());
  }
  
  private Collection<String> getSupportedModels(Collection<String> resourceModels, String nodeId) {
    List<String> supportedModels = new ArrayList<>();
    for (String model : resourceModels) {
      if (this._schemaLookup.isModelSupported(model, nodeId))
        supportedModels.add(model); 
    } 
    return supportedModels;
  }
}
