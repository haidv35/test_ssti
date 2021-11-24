package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.Validate;

final class RelationshipQuery {
  private final Set<String> _selectProperties;
  
  private final String _filterProperty;
  
  private final Set<String> _resourceModels;
  
  private final RelationshipJoin _relationshipJoin;
  
  RelationshipQuery(LinkedHashSet<String> targetSelectProperties, String sourceJoinProperty, String targetJoinProperty) {
    Validate.notEmpty(targetSelectProperties);
    Validate.notEmpty(sourceJoinProperty);
    Validate.notEmpty(targetJoinProperty);
    Set<String> selectProperties = new LinkedHashSet<>(targetSelectProperties);
    selectProperties.add(targetJoinProperty);
    Set<String> resourceModels = new LinkedHashSet<>();
    for (String targetSelectProperty : targetSelectProperties) {
      if (!PropertyUtil.isSpecialProperty(targetSelectProperty)) {
        QualifiedProperty qTargetSelectProperty = QualifiedProperty.forQualifiedName(targetSelectProperty);
        resourceModels.add(qTargetSelectProperty.getResourceModel());
      } 
    } 
    if (!PropertyUtil.isSpecialProperty(targetJoinProperty)) {
      QualifiedProperty qTargetJoinProperty = QualifiedProperty.forQualifiedName(targetJoinProperty);
      resourceModels.add(qTargetJoinProperty.getResourceModel());
    } 
    this._selectProperties = Collections.unmodifiableSet(selectProperties);
    this._filterProperty = PropertyUtil.isModelKey(targetJoinProperty) ? "@modelKey" : targetJoinProperty;
    this._resourceModels = Collections.unmodifiableSet(resourceModels);
    this._relationshipJoin = new RelationshipJoin(sourceJoinProperty, targetJoinProperty);
  }
  
  public RelationshipJoin getRelationshipJoin() {
    return this._relationshipJoin;
  }
  
  public Query buildQueryForJoin(ResultSet joinResult, SortCriterion relatedPropertySortCriterion) {
    if (joinResult == null)
      return null; 
    List<Object> joinPropertyValues = ResultSetUtil.extractNotNullPropertyValues(joinResult, this._relationshipJoin
        .getSourceJoinProperty());
    if (joinPropertyValues.isEmpty())
      return null; 
    PropertyPredicate predicate = new PropertyPredicate(this._filterProperty, PropertyPredicate.ComparisonOperator.IN, joinPropertyValues);
    return buildQuery(new Filter(Arrays.asList(new PropertyPredicate[] { predicate }, )), buildSortCriteria(this._selectProperties.iterator().next(), relatedPropertySortCriterion));
  }
  
  public Query buildQueryForFilter(Filter relatedPropertyFilter, SortCriterion relatedPropertySortCriterion) {
    Validate.notNull(relatedPropertyFilter);
    List<PropertyPredicate> filterPredicates = relatedPropertyFilter.getCriteria();
    List<PropertyPredicate> relatedFilterPredicates = new ArrayList<>(filterPredicates.size());
    for (PropertyPredicate filterPredicate : filterPredicates)
      relatedFilterPredicates.add(new PropertyPredicate(this._filterProperty, filterPredicate
            
            .getOperator(), filterPredicate
            .getComparableValue(), filterPredicate
            .isIgnoreCase())); 
    Filter relatedTargetPropertyFilter = new Filter(relatedFilterPredicates, relatedPropertyFilter.getOperator());
    return buildQuery(relatedTargetPropertyFilter, 
        buildSortCriteria(this._filterProperty, relatedPropertySortCriterion));
  }
  
  private Query buildQuery(Filter filter, List<SortCriterion> sortCriteria) {
    assert filter != null;
    assert sortCriteria != null;
    Set<String> selectProperties = new LinkedHashSet<>(this._selectProperties.size());
    for (String selectProperty : this._selectProperties) {
      if (PropertyUtil.isModelKey(selectProperty)) {
        selectProperties.add("@modelKey");
        continue;
      } 
      selectProperties.add(selectProperty);
    } 
    Query.Builder queryBuilder = Query.Builder.select(new ArrayList<>(selectProperties)).from(new ArrayList<>(this._resourceModels)).where(filter).orderBy(sortCriteria);
    return queryBuilder.build();
  }
  
  private static List<SortCriterion> buildSortCriteria(String targetModelProperty, SortCriterion relatedPropertySortCriterion) {
    List<SortCriterion> relatedPropertySortCriteria = new ArrayList<>(1);
    if (relatedPropertySortCriterion != null)
      relatedPropertySortCriteria.add(new SortCriterion(targetModelProperty, relatedPropertySortCriterion
            .getSortDirection(), relatedPropertySortCriterion
            .isIgnoreCase())); 
    return relatedPropertySortCriteria;
  }
  
  public static final class RelationshipJoin {
    private final String _sourceJoinProperty;
    
    private final String _targetJoinProperty;
    
    RelationshipJoin(String sourceJoinProperty, String targetJoinProperty) {
      Validate.notEmpty(sourceJoinProperty);
      Validate.notEmpty(targetJoinProperty);
      this._sourceJoinProperty = sourceJoinProperty;
      this._targetJoinProperty = targetJoinProperty;
    }
    
    public String getSourceJoinProperty() {
      return this._sourceJoinProperty;
    }
    
    public String getTargetJoinProperty() {
      return this._targetJoinProperty;
    }
  }
}
