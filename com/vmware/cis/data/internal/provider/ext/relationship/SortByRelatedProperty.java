package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

final class SortByRelatedProperty {
  public static Map<String, RelatedPropertyDescriptor> getRelatedPropertiesInSort(Query query, RelatedPropertyLookup relatedPropertyLookup) {
    assert query != null;
    assert relatedPropertyLookup != null;
    List<SortCriterion> sortCriteria = query.getSortCriteria();
    if (sortCriteria.isEmpty())
      return Collections.emptyMap(); 
    Set<String> sortProperties = QueryClauseAnalyzer.gatherPropertiesFromSort(sortCriteria);
    Map<String, RelatedPropertyDescriptor> relatedSortProperties = relatedPropertyLookup.getRelatedPropertyDescriptors(new ArrayList<>(sortProperties));
    validateSortByRelatedProperties(query, relatedSortProperties);
    return relatedSortProperties;
  }
  
  public static List<SortCriterion> getSortWithoutRelatedProperties(List<SortCriterion> sortCriteria, Map<String, RelatedPropertyDescriptor> relatedSortProperties) {
    assert sortCriteria != null;
    assert relatedSortProperties != null;
    if (sortCriteria.isEmpty())
      return Collections.emptyList(); 
    if (relatedSortProperties.isEmpty())
      return sortCriteria; 
    List<SortCriterion> realSortCriteria = new ArrayList<>();
    for (SortCriterion sortCriterion : sortCriteria) {
      if (!relatedSortProperties.containsKey(sortCriterion.getProperty()))
        realSortCriteria.add(sortCriterion); 
    } 
    return realSortCriteria;
  }
  
  public static Map<String, SortCriterion> getSortCriteriaForRelatedPropertiesInFilter(List<SortCriterion> sortCriteria, Set<String> relatedSortProperties, Set<String> relatedFilterProperties) {
    assert sortCriteria != null;
    assert relatedSortProperties != null;
    assert relatedFilterProperties != null;
    if (sortCriteria.isEmpty() || relatedFilterProperties.isEmpty())
      return Collections.emptyMap(); 
    Map<String, SortCriterion> relatedPropertiesSortCriteria = getSortCriteriaForRelatedProperties(sortCriteria, relatedSortProperties);
    relatedPropertiesSortCriteria.keySet().retainAll(relatedFilterProperties);
    return relatedPropertiesSortCriteria;
  }
  
  public static Map<String, SortCriterion> getSortCriteriaForRelatedPropertiesInSelect(List<SortCriterion> sortCriteria, Set<String> relatedSelectProperties, Set<String> relatedSortProperties, Set<RelatedPropertyDescriptor> relatedSortPropertiesInFilter) {
    assert sortCriteria != null;
    assert relatedSelectProperties != null;
    assert relatedSortProperties != null;
    assert relatedSortPropertiesInFilter != null;
    if (sortCriteria.isEmpty() || relatedSelectProperties.isEmpty())
      return Collections.emptyMap(); 
    Set<String> relatedFilterProperties = new LinkedHashSet<>();
    for (RelatedPropertyDescriptor relatedFilterProperty : relatedSortPropertiesInFilter)
      relatedFilterProperties.add(relatedFilterProperty.getName()); 
    Map<String, SortCriterion> relatedPropertiesSortCriteria = getSortCriteriaForRelatedProperties(sortCriteria, relatedSortProperties);
    relatedPropertiesSortCriteria.keySet().retainAll(relatedSelectProperties);
    relatedPropertiesSortCriteria.keySet().removeAll(relatedFilterProperties);
    return relatedPropertiesSortCriteria;
  }
  
  public static ResultSet reorderResultByFilter(ResultSet result, Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder, Set<String> queryProperties, List<SortCriterion> sortCriteria) {
    assert result != null;
    assert relatedFilterPropertiesOrder != null;
    assert queryProperties != null;
    assert sortCriteria != null;
    if (result.getItems().isEmpty() || relatedFilterPropertiesOrder.isEmpty())
      return result; 
    assert relatedFilterPropertiesOrder.size() == 1;
    RelatedPropertyDescriptor relatedFilterProperty = relatedFilterPropertiesOrder.keySet().iterator().next();
    List<Object> orderedPropertyValues = relatedFilterPropertiesOrder.get(relatedFilterProperty);
    String srcModelProperty = relatedFilterProperty.getSourceModelProperty();
    srcModelProperty = PropertyUtil.isModelKey(srcModelProperty) ? "@modelKey" : srcModelProperty;
    SortCriterion.SortDirection sortDirection = SortCriterion.SortDirection.ASCENDING;
    for (SortCriterion sortCriterion : sortCriteria) {
      if (srcModelProperty.equals(sortCriterion.getProperty())) {
        sortDirection = sortCriterion.getSortDirection();
        break;
      } 
    } 
    ResultSet reorderedResultSet = ResultSetUtil.reorderResultByPropertyValuesOrder(result, srcModelProperty, orderedPropertyValues, SortCriterion.SortDirection.ASCENDING
        .equals(sortDirection));
    if (!queryProperties.contains(srcModelProperty))
      reorderedResultSet = ResultSetUtil.removePropertyFromResultSet(reorderedResultSet, srcModelProperty); 
    return reorderedResultSet;
  }
  
  private static Map<String, SortCriterion> getSortCriteriaForRelatedProperties(List<SortCriterion> sortCriteria, Set<String> relatedSortProperties) {
    assert sortCriteria != null;
    assert relatedSortProperties != null;
    if (sortCriteria.isEmpty() || relatedSortProperties.isEmpty())
      return Collections.emptyMap(); 
    Map<String, SortCriterion> sortCriteriaByRelatedProperty = new LinkedHashMap<>();
    for (SortCriterion sortCriterion : sortCriteria) {
      String sortProperty = sortCriterion.getProperty();
      if (relatedSortProperties.contains(sortProperty))
        sortCriteriaByRelatedProperty.put(sortProperty, sortCriterion); 
    } 
    return sortCriteriaByRelatedProperty;
  }
  
  private static void validateSortByRelatedProperties(Query query, Map<String, RelatedPropertyDescriptor> relatedSortProperties) {
    assert query != null;
    assert relatedSortProperties != null;
    if (relatedSortProperties.isEmpty())
      return; 
    Set<String> relatedSortPropertiesSet = new HashSet<>(relatedSortProperties.keySet());
    if (relatedSortPropertiesSet.size() > 1)
      throw new UnsupportedOperationException("Sort by more than one related property is not supported!"); 
    Set<String> sortProperties = QueryClauseAnalyzer.gatherPropertiesFromSort(query.getSortCriteria());
    Validate.isTrue(relatedSortPropertiesSet.containsAll(sortProperties), "Sort by related and real property at the same time in one query is not supported!");
    List<String> queryProperties = query.getProperties();
    Set<String> filterProperties = QueryClauseAnalyzer.gatherPropertiesFromFilter(query.getFilter());
    relatedSortPropertiesSet.removeAll(queryProperties);
    relatedSortPropertiesSet.removeAll(filterProperties);
    Validate.isTrue(relatedSortPropertiesSet.isEmpty(), 
        String.format("The given related sort properties [%s] must be part of either select or filter clauses!", new Object[] { relatedSortPropertiesSet }));
    for (RelatedPropertyDescriptor relatedSortProperty : relatedSortProperties.values()) {
      Validate.isTrue(!relatedSortProperty.getType().isArray(), 
          String.format("Not possible to sort by an array property '%s'!", new Object[] { relatedSortProperty.getName() }));
    } 
  }
}
