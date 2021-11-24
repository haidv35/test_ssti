package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InvSvcTagAssociationDataProviderConnection implements DataProvider {
  private static final QuerySchema _schema;
  
  private final DataProvider _provider;
  
  private final TaggableEntityReferenceConverter _entityConverter;
  
  private final String _serverGuid;
  
  static {
    Map<String, QuerySchema.PropertyInfo> props = new HashMap<>();
    props.put("inventoryservice:InventoryServiceTagAssociation/tag", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    props.put("inventoryservice:InventoryServiceTagAssociation/entity", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    _schema = QuerySchema.forProperties(props);
  }
  
  public InvSvcTagAssociationDataProviderConnection(DataProvider provider, TaggableEntityReferenceConverter entityConverter, String serverGuid) {
    assert provider != null;
    assert entityConverter != null;
    assert serverGuid != null;
    this._provider = provider;
    this._entityConverter = entityConverter;
    this._serverGuid = serverGuid;
  }
  
  public ResultSet executeQuery(Query query) {
    assert query != null;
    if (skip(query))
      return this._provider.executeQuery(query); 
    Query convertedQuery = convertQuery(query, this._entityConverter);
    ResultSet resultSet = this._provider.executeQuery(convertedQuery);
    ResultSet convertedResult = convertResultSet(query.getProperties(), resultSet, this._entityConverter, this._serverGuid);
    return convertedResult;
  }
  
  private static Query convertQuery(Query query, TaggableEntityReferenceConverter entityConverter) {
    assert query != null;
    assert entityConverter != null;
    Collection<String> convertedModels = convertModels(query.getResourceModels());
    List<String> convertedProperties = convertProperties(query.getProperties());
    Filter convertedFilter = convertFilter(query.getFilter(), entityConverter);
    List<SortCriterion> convertedSortCriteria = convertSortCriteria(query.getSortCriteria());
    return QueryCopy.copyAndSelect(query, convertedProperties)
      .from(convertedModels)
      .where(convertedFilter)
      .orderBy(convertedSortCriteria)
      .build();
  }
  
  private static Collection<String> convertModels(Collection<String> models) {
    assert models != null;
    if (models.size() > 1)
      throw new IllegalArgumentException("Multiple models in query: " + models); 
    String model = models.iterator().next();
    if (!"inventoryservice:InventoryServiceTagAssociation".equals(model))
      throw new IllegalArgumentException("Unknown model: " + model); 
    return Arrays.asList(new String[] { "com.vmware.cis.tagging.TagAssociationModel" });
  }
  
  private static Filter convertFilter(Filter filter, TaggableEntityReferenceConverter entityConverter) {
    if (filter == null)
      return null; 
    List<PropertyPredicate> convertedPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      PropertyPredicate convertedPredicate = convertPredicate(predicate, entityConverter);
      convertedPredicates.add(convertedPredicate);
    } 
    return new Filter(convertedPredicates, filter.getOperator());
  }
  
  private static PropertyPredicate convertPredicate(PropertyPredicate predicate, TaggableEntityReferenceConverter entityConverter) {
    Object<?> object;
    Object convertedComparableValue;
    Collection<?> comparableList;
    assert predicate != null;
    assert entityConverter != null;
    switch (predicate.getOperator()) {
      case IN:
      case NOT_IN:
        assert predicate.getComparableValue() instanceof Collection;
        comparableList = (Collection)predicate.getComparableValue();
        object = (Object<?>)convertComparableList(predicate.getProperty(), comparableList, entityConverter);
        break;
      default:
        convertedComparableValue = convertComparableValue(predicate.getProperty(), predicate
            .getComparableValue(), entityConverter);
        break;
    } 
    String convertedProperty = convertProperty(predicate.getProperty());
    return new PropertyPredicate(convertedProperty, predicate
        .getOperator(), convertedComparableValue, predicate
        
        .isIgnoreCase());
  }
  
  private static List<SortCriterion> convertSortCriteria(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    if (sortCriteria.isEmpty())
      return sortCriteria; 
    List<SortCriterion> convertedSortCriteria = new ArrayList<>(sortCriteria.size());
    for (SortCriterion sortCriterion : sortCriteria) {
      String convertedProperty = convertProperty(sortCriterion.getProperty());
      SortCriterion convertedSortCriterion = new SortCriterion(convertedProperty, sortCriterion.getSortDirection(), sortCriterion.isIgnoreCase());
      convertedSortCriteria.add(convertedSortCriterion);
    } 
    return convertedSortCriteria;
  }
  
  private static List<String> convertProperties(List<String> properties) {
    assert properties != null;
    List<String> convertedProperties = new ArrayList<>(properties.size());
    for (String property : properties) {
      String convertedProperty = convertProperty(property);
      convertedProperties.add(convertedProperty);
    } 
    return convertedProperties;
  }
  
  private static String convertProperty(String property) {
    assert property != null;
    switch (property) {
      case "inventoryservice:InventoryServiceTagAssociation/tag":
        return "com.vmware.cis.tagging.TagAssociationModel/tagId";
      case "inventoryservice:InventoryServiceTagAssociation/entity":
        return "com.vmware.cis.tagging.TagAssociationModel/objectId";
    } 
    throw new IllegalArgumentException("Unknown property: " + property);
  }
  
  private static Collection<?> convertComparableList(String property, Collection<?> comparableList, TaggableEntityReferenceConverter entityConverter) {
    assert property != null;
    assert comparableList != null;
    assert entityConverter != null;
    List<Object> convertedComparableList = new ArrayList(comparableList.size());
    for (Object comparableValue : comparableList) {
      Object convertedComparableValue = convertComparableValue(property, comparableValue, entityConverter);
      convertedComparableList.add(convertedComparableValue);
    } 
    return convertedComparableList;
  }
  
  private static Object convertComparableValue(String property, Object comparableValue, TaggableEntityReferenceConverter entityConverter) {
    ManagedObjectReference tag;
    assert property != null;
    assert comparableValue != null;
    assert entityConverter != null;
    switch (property) {
      case "inventoryservice:InventoryServiceTagAssociation/tag":
        assert comparableValue instanceof ManagedObjectReference;
        tag = (ManagedObjectReference)comparableValue;
        return InvSvcTaggingIdConverter.taggingMorToId(tag);
      case "inventoryservice:InventoryServiceTagAssociation/entity":
        return entityConverter.convertComparableValue(comparableValue);
    } 
    throw new IllegalArgumentException("Unknown property: " + property);
  }
  
  private static ResultSet convertResultSet(List<String> originalProperties, ResultSet resultSet, TaggableEntityReferenceConverter entityConverter, String serverGuid) {
    assert originalProperties != null;
    assert resultSet != null;
    assert entityConverter != null;
    assert originalProperties.size() == resultSet.getProperties().size();
    assert serverGuid != null;
    ResultSet.Builder convertedBuilder = ResultSet.Builder.properties(originalProperties);
    List<String> properties = resultSet.getProperties();
    for (ResourceItem item : resultSet.getItems()) {
      List<Object> propertyValues = item.getPropertyValues();
      List<Object> convertedPropertyValues = new ArrayList(propertyValues.size());
      for (int index = 0; index < properties.size(); index++) {
        String property = properties.get(index);
        Object propertyValue = propertyValues.get(index);
        Object convertedPropertyValue = convertPropertyValue(property, propertyValue, entityConverter, serverGuid);
        convertedPropertyValues.add(convertedPropertyValue);
      } 
      convertedBuilder.item(item.getKey(), convertedPropertyValues);
    } 
    convertedBuilder.totalCount(resultSet.getTotalCount());
    return convertedBuilder.build();
  }
  
  private static Object convertPropertyValue(String property, Object propertyValue, TaggableEntityReferenceConverter entityConverter, String serverGuid) {
    String tag;
    DynamicID entity;
    assert property != null;
    assert propertyValue != null;
    assert entityConverter != null;
    assert serverGuid != null;
    switch (property) {
      case "com.vmware.cis.tagging.TagAssociationModel/tagId":
        assert propertyValue instanceof String;
        tag = (String)propertyValue;
        return InvSvcTaggingIdConverter.taggingIdToMor(tag);
      case "com.vmware.cis.tagging.TagAssociationModel/objectId":
        assert propertyValue instanceof DynamicID;
        entity = (DynamicID)propertyValue;
        return entityConverter.convertPropertyValue(entity, serverGuid);
    } 
    throw new IllegalArgumentException("Unknown property: " + property);
  }
  
  private static boolean skip(Query query) {
    assert query != null;
    return !query.getResourceModels().contains("inventoryservice:InventoryServiceTagAssociation");
  }
  
  public QuerySchema getSchema() {
    QuerySchema baseSchema = this._provider.getSchema();
    return SchemaUtil.merge(baseSchema, _schema);
  }
}
