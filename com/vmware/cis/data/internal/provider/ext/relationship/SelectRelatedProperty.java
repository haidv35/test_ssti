package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

final class SelectRelatedProperty {
  public static List<String> resolveRelatedPropertiesInSelect(List<String> properties, boolean doClientPaging, Map<String, RelatedPropertyDescriptor> relatedSelectProperties, Set<RelatedPropertyDescriptor> relatedFilterPropertiesInSort, RelatedPropertyLookup relatedPropertyLookup) {
    assert properties != null;
    assert relatedSelectProperties != null;
    assert relatedFilterPropertiesInSort != null;
    assert relatedPropertyLookup != null;
    relatedSelectProperties.putAll(relatedPropertyLookup
        .getRelatedPropertyDescriptors(properties));
    if (relatedSelectProperties.isEmpty() && relatedFilterPropertiesInSort.isEmpty())
      return properties; 
    Set<String> extendedProperties = new LinkedHashSet<>();
    if (doClientPaging)
      extendedProperties.add("@modelKey"); 
    for (String property : properties) {
      RelatedPropertyDescriptor relatedPropertyDescriptor = relatedSelectProperties.get(property);
      if (relatedPropertyDescriptor != null) {
        extendedProperties.add(relatedPropertyDescriptor.getSourceModelProperty());
        continue;
      } 
      if (!doClientPaging)
        extendedProperties.add(property); 
    } 
    for (RelatedPropertyDescriptor relatedFilterProperty : relatedFilterPropertiesInSort)
      extendedProperties.add(relatedFilterProperty.getSourceModelProperty()); 
    return new ArrayList<>(extendedProperties);
  }
  
  public static List<String> getSourceModelPropertiesForRelatedSelectProperties(Collection<RelatedPropertyDescriptor> relatedSelectProperties) {
    assert relatedSelectProperties != null;
    if (relatedSelectProperties.isEmpty())
      return Collections.emptyList(); 
    Set<String> sourceModelProperties = new LinkedHashSet<>();
    for (RelatedPropertyDescriptor relatedSelectProperty : relatedSelectProperties)
      sourceModelProperties.add(relatedSelectProperty.getSourceModelProperty()); 
    return new ArrayList<>(sourceModelProperties);
  }
  
  public static ResultSet addRelatedPropertiesToResult(ResultSet result, Query initialQuery, Map<String, RelatedPropertyDescriptor> relatedSelectProperties, Map<String, SortCriterion> relatedSelectPropertySortCriteria, boolean doClientPaging, DataProvider connection) {
    assert result != null;
    assert initialQuery != null;
    assert relatedSelectProperties != null;
    assert relatedSelectPropertySortCriteria != null;
    assert connection != null;
    if (relatedSelectProperties.isEmpty())
      return result; 
    List<List<Integer>> reorderItemsIndices = new ArrayList<>(0);
    List<RelatedPropertyResultInfo> relatedPropertyResults = new ArrayList<>();
    for (RelatedPropertyDescriptor relatedPropertyDescriptor : relatedSelectProperties.values()) {
      RelatedPropertyResultInfo relatedPropertyResult = getRelatedPropertyValues(relatedPropertyDescriptor, result, relatedSelectPropertySortCriteria
          
          .get(relatedPropertyDescriptor.getName()), connection);
      relatedPropertyResults.add(relatedPropertyResult);
      if (!relatedPropertyResult.getRelatedPropertyValuePermutation().isEmpty())
        reorderItemsIndices.add(relatedPropertyResult.getRelatedPropertyValuePermutation()); 
    } 
    ResultSet enrichedResult = mergeResults(result, 
        getResultPropertiesToRemove(result, initialQuery, doClientPaging), relatedPropertyResults);
    if (!reorderItemsIndices.isEmpty()) {
      assert reorderItemsIndices.size() == 1;
      enrichedResult = ResultSetUtil.reorderResultByIndices(enrichedResult, reorderItemsIndices.get(0));
    } 
    return enrichedResult;
  }
  
  private static RelatedPropertyResultInfo getRelatedPropertyValues(RelatedPropertyDescriptor relatedPropertyDescriptor, ResultSet result, SortCriterion relatedPropertySortCriterion, DataProvider connection) {
    List<Object> targetPropertyValues;
    assert relatedPropertyDescriptor != null;
    List<RelationshipHop> relationshipHops = new ArrayList<>();
    List<RelationshipQuery> relationshipQueries = RelationshipQueryFactory.createRelationshipQueriesForSelect(relatedPropertyDescriptor, relationshipHops);
    RelationshipHop lastRelationshipHop = relationshipHops.get(relationshipHops.size() - 1);
    String targetModelProperty = lastRelationshipHop.getSourceModelProperty();
    List<Integer> reorderedItemsIndices = new ArrayList<>();
    ResultSet relatedResult = RelationshipQueryExecutor.executeRelationshipQueriesForSelect(relatedPropertyDescriptor
        .getName(), relationshipQueries, result, relatedPropertySortCriterion, reorderedItemsIndices, connection);
    String relatedPropertyName = relatedPropertyDescriptor.getName();
    if (relatedResult != null && !relatedResult.getItems().isEmpty()) {
      targetPropertyValues = extractRelatedPropertyValues(relatedResult, targetModelProperty, relatedPropertyName, relatedPropertyDescriptor
          
          .getType());
    } else {
      targetPropertyValues = Collections.nCopies(result.getItems().size(), null);
    } 
    return new RelatedPropertyResultInfo(relatedPropertyName, targetPropertyValues, reorderedItemsIndices);
  }
  
  private static List<Object> extractRelatedPropertyValues(ResultSet relatedResult, String targetModelProperty, String relatedPropertyName, Class<?> relatedPropertyType) {
    List<Object> targetPropertyValues;
    assert relatedResult != null;
    assert !relatedResult.getItems().isEmpty();
    String tgtModelProperty = PropertyUtil.isModelKey(targetModelProperty) ? "@modelKey" : targetModelProperty;
    int tgtPropertyIndex = relatedResult.getProperties().lastIndexOf(tgtModelProperty);
    if (tgtPropertyIndex < 0)
      throw new IllegalArgumentException(String.format("The given property [%s] is not among the result properties!", new Object[] { tgtModelProperty })); 
    List<Object> tgtPropertyValues = ResultSetAnalyzer.gatherPropertyValuesByIndexOrdered(relatedResult, tgtPropertyIndex);
    if (PropertyUtil.isModelKey(targetModelProperty)) {
      int tgtTypePropertyIndex = relatedResult.getProperties().lastIndexOf("@type");
      if (tgtTypePropertyIndex < 0)
        throw new IllegalArgumentException("The result does not contain a @type property, needed for a @modelKey conversion!"); 
      List<Object> tgtResourceTypes = ResultSetAnalyzer.gatherPropertyValuesByIndexOrdered(relatedResult, tgtTypePropertyIndex);
      targetPropertyValues = ModelKeyConverter.convertModelKeyToReferences(relatedPropertyName, relatedPropertyType, tgtPropertyValues, tgtResourceTypes);
    } else {
      targetPropertyValues = new ArrayList(tgtPropertyValues.size());
      for (Object tgtPropertyValue : tgtPropertyValues) {
        Object extractedValue = extractRelatedPropertyValue(tgtPropertyValue, relatedPropertyName, relatedPropertyType);
        targetPropertyValues.add(extractedValue);
      } 
    } 
    return targetPropertyValues.isEmpty() ? 
      Collections.<Object>nCopies(relatedResult.getItems().size(), null) : targetPropertyValues;
  }
  
  private static Object extractRelatedPropertyValue(Object wrappedPropertyValue, String relatedPropertyName, Class<?> relatedPropertyType) {
    if (wrappedPropertyValue == null)
      return null; 
    if (!(wrappedPropertyValue instanceof Collection))
      return wrappedPropertyValue; 
    Collection<?> valueCollection = (Collection)wrappedPropertyValue;
    if (relatedPropertyType.isArray()) {
      boolean deduplicateArrayElements = false;
      if (ModelKeyConverter.isArrayIdentifier(relatedPropertyType))
        deduplicateArrayElements = true; 
      return extractArrayPropertyValue(valueCollection, relatedPropertyType
          .getComponentType(), deduplicateArrayElements);
    } 
    if (valueCollection.isEmpty())
      return null; 
    if (valueCollection.size() > 1) {
      String msg = String.format("Related property '%s' expects only one value but received: %s", new Object[] { relatedPropertyName, valueCollection });
      throw new IllegalArgumentException(msg);
    } 
    return valueCollection.iterator().next();
  }
  
  private static Object extractArrayPropertyValue(Collection<?> valueCollection, Class<?> arrayPropertyType, boolean deduplicateArrayElements) {
    if (deduplicateArrayElements && valueCollection.size() > 1)
      valueCollection = new LinkedHashSet(valueCollection); 
    Object arrayValue = Array.newInstance(arrayPropertyType, valueCollection
        .size());
    int i = 0;
    for (Object value : valueCollection)
      Array.set(arrayValue, i++, value); 
    return arrayValue;
  }
  
  private static int[] getResultPropertiesToRemove(ResultSet enrichedResult, Query originalQuery, boolean doClientPaging) {
    Set<Integer> propertiesToRemoveSet = new HashSet<>();
    List<String> resultProperties = enrichedResult.getProperties();
    List<String> queryProperties = originalQuery.getProperties();
    int i = 0;
    for (String resultProperty : resultProperties) {
      if (!queryProperties.contains(resultProperty))
        if (!PropertyUtil.isModelKey(resultProperty) || !doClientPaging)
          propertiesToRemoveSet.add(Integer.valueOf(i));  
      i++;
    } 
    Integer[] resultPropertiesToRemove = propertiesToRemoveSet.<Integer>toArray(new Integer[propertiesToRemoveSet.size()]);
    Arrays.sort(resultPropertiesToRemove, Collections.reverseOrder());
    return ArrayUtils.toPrimitive(resultPropertiesToRemove);
  }
  
  private static ResultSet mergeResults(ResultSet initialResult, int[] resultPropertiesToRemove, List<RelatedPropertyResultInfo> relatedPropertyResults) {
    assert resultPropertiesToRemove != null;
    assert relatedPropertyResults != null;
    if (relatedPropertyResults.isEmpty())
      return initialResult; 
    List<String> extendedProperties = new ArrayList<>(initialResult.getProperties());
    for (int resultPropertyIndexToRemove : resultPropertiesToRemove)
      extendedProperties.remove(resultPropertyIndexToRemove); 
    for (RelatedPropertyResultInfo relatedPropertyResult : relatedPropertyResults)
      extendedProperties.add(relatedPropertyResult.getRelatedPropertyName()); 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(extendedProperties);
    List<ResourceItem> initialResourceItems = initialResult.getItems();
    for (int r = 0; r < initialResourceItems.size(); r++) {
      ResourceItem resourceItem = initialResourceItems.get(r);
      List<Object> propertyValues = new ArrayList(resourceItem.getPropertyValues());
      for (int resultPropertyIndexToRemove : resultPropertiesToRemove)
        propertyValues.remove(resultPropertyIndexToRemove); 
      propertyValues.addAll(
          getRelatedPropertyValuesForResourceItem(r, relatedPropertyResults));
      resultBuilder.item(resourceItem.getKey(), propertyValues);
    } 
    return resultBuilder.totalCount(initialResult.getTotalCount()).build();
  }
  
  private static List<Object> getRelatedPropertyValuesForResourceItem(int resourceItemIndex, List<RelatedPropertyResultInfo> relatedPropertyResults) {
    List<Object> propertyValuesForResourceItem = new ArrayList(relatedPropertyResults.size());
    for (RelatedPropertyResultInfo relatedPropertyResult : relatedPropertyResults) {
      if (relatedPropertyResult.getRelatedPropertyValues().isEmpty()) {
        propertyValuesForResourceItem.add(null);
        continue;
      } 
      Object propertyValue = relatedPropertyResult.getRelatedPropertyValues().get(resourceItemIndex);
      propertyValuesForResourceItem.add(propertyValue);
    } 
    return propertyValuesForResourceItem;
  }
  
  private static final class RelatedPropertyResultInfo {
    private final String _relatedPropertyName;
    
    private final List<Object> _relatedPropertyValues;
    
    private final List<Integer> _relatedPropertyValuePermutation;
    
    RelatedPropertyResultInfo(String relatedPropertyName, List<Object> relatedPropertyValues, List<Integer> relatedPropertyValuePermutation) {
      Validate.notEmpty(relatedPropertyName);
      Validate.notNull(relatedPropertyValues);
      Validate.notNull(relatedPropertyValuePermutation);
      this._relatedPropertyName = relatedPropertyName;
      this._relatedPropertyValuePermutation = relatedPropertyValuePermutation;
      this._relatedPropertyValues = relatedPropertyValues;
    }
    
    public String getRelatedPropertyName() {
      return this._relatedPropertyName;
    }
    
    public List<Object> getRelatedPropertyValues() {
      return this._relatedPropertyValues;
    }
    
    public List<Integer> getRelatedPropertyValuePermutation() {
      return this._relatedPropertyValuePermutation;
    }
  }
}
