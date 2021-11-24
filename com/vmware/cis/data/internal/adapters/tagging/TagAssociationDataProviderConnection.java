package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.cis.tagging.BatchTypes;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.std.DynamicID;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;

public final class TagAssociationDataProviderConnection implements DataProvider {
  private static final String TAG_ASSOCIATION_KEY = "NOT_USED";
  
  private static final QuerySchema _schema;
  
  private final LenientTaggingFacade _tagging;
  
  private final URI _taggingVapiUri;
  
  static {
    Map<String, QuerySchema.PropertyInfo> props = new HashMap<>();
    props.put("com.vmware.cis.tagging.TagAssociationModel/tagId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    props.put("com.vmware.cis.tagging.TagAssociationModel/objectId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    _schema = QuerySchema.forProperties(props);
  }
  
  public TagAssociationDataProviderConnection(ApiProvider apiProvider, URI taggingVapiUri) {
    Validate.notNull(apiProvider);
    Validate.notNull(taggingVapiUri);
    this._tagging = new TaggingFacadePerfLogging(new TaggingFacadeImpl(apiProvider));
    this._taggingVapiUri = taggingVapiUri;
  }
  
  public QuerySchema getSchema() {
    return _schema;
  }
  
  public ResultSet executeQuery(Query query) {
    validateQuery(query);
    Collection<TagAssociationInfo> associations = collectAssociationsFor(query);
    List<List<Object>> collectedItems = toItems(associations, query.getProperties());
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(query.getProperties());
    for (List<Object> collectedItem : collectedItems)
      resultBuilder.item("NOT_USED", collectedItem); 
    ResultSet result = resultBuilder.build();
    return result;
  }
  
  private void validateQuery(Query query) {
    Validate.notNull(query);
    if (query.getResourceModels().size() > 1)
      throw new IllegalArgumentException("Multiple models in tag association query: " + query
          .getResourceModels()); 
    String model = query.getResourceModels().iterator().next();
    if (!"com.vmware.cis.tagging.TagAssociationModel".equals(model))
      throw new IllegalArgumentException("Unknown model: " + model); 
    if (query.getWithTotalCount())
      throw new IllegalArgumentException("Total count is not supported"); 
    if (!query.getSortCriteria().isEmpty())
      throw new IllegalArgumentException("Ordering is not supported"); 
    List<String> selectedProperties = query.getProperties();
    Validate.notEmpty(selectedProperties, "Must select at least one property");
    for (String property : query.getProperties()) {
      if ("com.vmware.cis.tagging.TagAssociationModel/tagId".equals(property) || "com.vmware.cis.tagging.TagAssociationModel/objectId"
        .equals(property))
        continue; 
      throw new IllegalArgumentException("Cannot select property: " + property);
    } 
  }
  
  private Collection<TagAssociationInfo> collectAssociationsFor(Query query) {
    assert query != null;
    Filter filter = query.getFilter();
    List<String> selectedProperties = query.getProperties();
    if (filter == null)
      return collectAllAssociations(selectedProperties); 
    if (isFilterByTagId(filter)) {
      PropertyPredicate predicate = filter.getCriteria().iterator().next();
      Collection<Object> comparableValues = getComparableValues(predicate);
      List<String> tagIds = toTagIds(comparableValues);
      return collectAssociationsByTagId(tagIds, selectedProperties);
    } 
    if (isFilterByObjectId(filter)) {
      PropertyPredicate predicate = filter.getCriteria().iterator().next();
      Collection<Object> comparableValues = getComparableValues(predicate);
      List<DynamicID> objectIds = toObjectIds(comparableValues);
      return collectAssociationsByObjectId(objectIds, selectedProperties);
    } 
    if (isFilterByTagIdAndObjectId(filter)) {
      String tagId = getTagId(filter);
      DynamicID objectId = getObjectId(filter);
      return collectAssociationsByTagIdAndObjectId(tagId, objectId);
    } 
    throw new IllegalArgumentException("Unsupported filter: " + filter);
  }
  
  private Collection<TagAssociationInfo> collectAllAssociations(List<String> selectedProperties) {
    assert selectedProperties != null;
    List<BatchTypes.TagToObjects> tagsToObjects = this._tagging.listAllAttachedObjectsOnTags();
    boolean selectTagId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/tagId");
    boolean selectObjectId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/objectId");
    if (selectTagId && selectObjectId)
      return fromTagToObjects(tagsToObjects); 
    if (selectTagId)
      throw new IllegalArgumentException("Cannot select only tagId without filter clause."); 
    assert selectObjectId;
    throw new IllegalArgumentException("Cannot select only objectId without filter clause.");
  }
  
  private Collection<TagAssociationInfo> collectAssociationsByTagId(List<String> tagIds, List<String> selectedProperties) {
    assert tagIds != null;
    assert selectedProperties != null;
    boolean selectTagId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/tagId");
    boolean selectObjectId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/objectId");
    if (selectTagId && selectObjectId) {
      List<BatchTypes.TagToObjects> tagsToObjects = this._tagging.listAttachedObjectsOnTags(tagIds);
      return fromTagToObjects(tagsToObjects);
    } 
    assert selectObjectId;
    List<DynamicID> objectIds = this._tagging.listAttachedObjects(tagIds);
    return fromObjectIds(objectIds);
  }
  
  private Collection<TagAssociationInfo> collectAssociationsByObjectId(List<DynamicID> objectIds, List<String> selectedProperties) {
    assert objectIds != null;
    assert selectedProperties != null;
    boolean selectTagId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/tagId");
    boolean selectObjectId = selectedProperties.contains("com.vmware.cis.tagging.TagAssociationModel/objectId");
    if (selectTagId && selectObjectId) {
      List<BatchTypes.ObjectToTags> objectsToTags = this._tagging.listAttachedTagsOnObjects(objectIds);
      return fromObjectToTags(objectsToTags);
    } 
    assert selectTagId;
    List<String> tagIds = this._tagging.listAttachedTags(objectIds);
    return fromTagIds(tagIds);
  }
  
  private Collection<TagAssociationInfo> collectAssociationsByTagIdAndObjectId(String tagId, DynamicID objectId) {
    assert tagId != null;
    assert objectId != null;
    List<String> tagIds = this._tagging.listAttachedTags(
        Collections.singletonList(objectId));
    if (!tagIds.contains(tagId))
      return Collections.emptyList(); 
    TagAssociationInfo association = new TagAssociationInfo(tagId, objectId);
    return Collections.singletonList(association);
  }
  
  private static boolean isFilterByTagId(Filter filter) {
    assert filter != null;
    if (filter.getCriteria().size() != 1)
      return false; 
    PropertyPredicate predicate = filter.getCriteria().iterator().next();
    return "com.vmware.cis.tagging.TagAssociationModel/tagId".equals(predicate.getProperty());
  }
  
  private static boolean isFilterByObjectId(Filter filter) {
    assert filter != null;
    if (filter.getCriteria().size() != 1)
      return false; 
    PropertyPredicate predicate = filter.getCriteria().iterator().next();
    return "com.vmware.cis.tagging.TagAssociationModel/objectId".equals(predicate.getProperty());
  }
  
  private static boolean isFilterByTagIdAndObjectId(Filter filter) {
    assert filter != null;
    if (filter.getCriteria().size() != 2)
      return false; 
    if (!LogicalOperator.AND.equals(filter.getOperator()))
      return false; 
    Iterator<PropertyPredicate> it = filter.getCriteria().iterator();
    PropertyPredicate first = it.next();
    PropertyPredicate second = it.next();
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(first.getOperator()) || 
      !PropertyPredicate.ComparisonOperator.EQUAL.equals(second.getOperator()))
      return false; 
    if ("com.vmware.cis.tagging.TagAssociationModel/tagId".equals(first.getProperty()) && "com.vmware.cis.tagging.TagAssociationModel/objectId"
      .equals(second.getProperty()))
      return true; 
    if ("com.vmware.cis.tagging.TagAssociationModel/tagId".equals(second.getProperty()) && "com.vmware.cis.tagging.TagAssociationModel/objectId"
      .equals(first.getProperty()))
      return true; 
    return false;
  }
  
  private static String getTagId(Filter filter) {
    assert filter != null;
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if ("com.vmware.cis.tagging.TagAssociationModel/tagId".equals(predicate.getProperty())) {
        if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()))
          throw new IllegalArgumentException("Unsupported comparison operator: " + predicate
              
              .getOperator()); 
        return toTagId(predicate.getComparableValue());
      } 
    } 
    throw new IllegalArgumentException("Cannot find predicate on tagId in filter: " + filter);
  }
  
  private static DynamicID getObjectId(Filter filter) {
    assert filter != null;
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if ("com.vmware.cis.tagging.TagAssociationModel/objectId".equals(predicate.getProperty())) {
        if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()))
          throw new IllegalArgumentException("Unsupported comparison operator: " + predicate
              
              .getOperator()); 
        return toObjectId(predicate.getComparableValue());
      } 
    } 
    throw new IllegalArgumentException("Cannot find predicate on objectId in filter: " + filter);
  }
  
  private static Collection<Object> getComparableValues(PropertyPredicate predicate) {
    assert predicate != null;
    if (PropertyPredicate.ComparisonOperator.IN.equals(predicate.getOperator())) {
      assert predicate.getComparableValue() instanceof Collection;
      Collection<Object> comparableValues = (Collection<Object>)predicate.getComparableValue();
      return comparableValues;
    } 
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(predicate.getOperator()))
      throw new IllegalArgumentException("Unsupported comparison operator: " + predicate
          .getOperator()); 
    return Collections.singletonList(predicate.getComparableValue());
  }
  
  private static List<String> toTagIds(Collection<Object> comparableValues) {
    assert comparableValues != null;
    List<String> tagIds = new ArrayList<>(comparableValues.size());
    for (Object comparableValue : comparableValues) {
      String tagId = toTagId(comparableValue);
      tagIds.add(tagId);
    } 
    return tagIds;
  }
  
  private static String toTagId(Object comparableValue) {
    assert comparableValue != null;
    String tagId = typedComparableValue("com.vmware.cis.tagging.TagAssociationModel/tagId", String.class, comparableValue);
    return tagId;
  }
  
  private static List<DynamicID> toObjectIds(Collection<Object> comparableValues) {
    assert comparableValues != null;
    List<DynamicID> objectIds = new ArrayList<>(comparableValues.size());
    for (Object comparableValue : comparableValues) {
      DynamicID objectId = toObjectId(comparableValue);
      objectIds.add(objectId);
    } 
    return objectIds;
  }
  
  private static DynamicID toObjectId(Object comparableValue) {
    assert comparableValue != null;
    DynamicID objectId = typedComparableValue("com.vmware.cis.tagging.TagAssociationModel/objectId", DynamicID.class, comparableValue);
    return objectId;
  }
  
  private static <T> T typedComparableValue(@Nonnull String filterableProperty, @Nonnull Class<T> expectedClass, @Nonnull Object comparableValue) {
    if (expectedClass.isInstance(comparableValue))
      return expectedClass.cast(comparableValue); 
    throw new IllegalArgumentException(String.format("Invalid comparable value for property '%s' - expected %s but found %s", new Object[] { filterableProperty, expectedClass, comparableValue

            
            .getClass() }));
  }
  
  private static Collection<TagAssociationInfo> fromTagIds(Collection<String> tagIds) {
    assert tagIds != null;
    List<TagAssociationInfo> associations = new ArrayList<>(tagIds.size());
    for (String tagId : tagIds) {
      TagAssociationInfo association = new TagAssociationInfo(tagId, null);
      associations.add(association);
    } 
    return associations;
  }
  
  private static Collection<TagAssociationInfo> fromObjectIds(Collection<DynamicID> objectIds) {
    assert objectIds != null;
    List<TagAssociationInfo> associations = new ArrayList<>(objectIds.size());
    for (DynamicID objectId : objectIds) {
      TagAssociationInfo association = new TagAssociationInfo(null, objectId);
      associations.add(association);
    } 
    return associations;
  }
  
  private static Collection<TagAssociationInfo> fromObjectToTags(Collection<BatchTypes.ObjectToTags> objectsToTags) {
    assert objectsToTags != null;
    List<TagAssociationInfo> associations = new ArrayList<>(objectsToTags.size());
    for (BatchTypes.ObjectToTags objectToTags : objectsToTags) {
      DynamicID objectId = objectToTags.getObjectId();
      for (String tagId : objectToTags.getTagIds()) {
        TagAssociationInfo association = new TagAssociationInfo(tagId, objectId);
        associations.add(association);
      } 
    } 
    return associations;
  }
  
  private static List<TagAssociationInfo> fromTagToObjects(Collection<BatchTypes.TagToObjects> tagsToObjects) {
    assert tagsToObjects != null;
    List<TagAssociationInfo> associations = new ArrayList<>(tagsToObjects.size());
    for (BatchTypes.TagToObjects tagToObjects : tagsToObjects) {
      String tagId = tagToObjects.getTagId();
      for (DynamicID objectId : tagToObjects.getObjectIds()) {
        TagAssociationInfo association = new TagAssociationInfo(tagId, objectId);
        associations.add(association);
      } 
    } 
    return associations;
  }
  
  private static List<List<Object>> toItems(Collection<TagAssociationInfo> associations, List<String> selectedProperties) {
    assert associations != null;
    assert selectedProperties != null;
    List<List<Object>> items = new ArrayList<>(associations.size());
    for (TagAssociationInfo association : associations) {
      List<Object> item = toItem(association, selectedProperties);
      items.add(item);
    } 
    return items;
  }
  
  private static List<Object> toItem(TagAssociationInfo association, List<String> selectedProperties) {
    assert association != null;
    assert selectedProperties != null;
    List<Object> values = new ArrayList();
    for (String property : selectedProperties) {
      Object value = getPropertyValue(association, property);
      values.add(value);
    } 
    return values;
  }
  
  private static Object getPropertyValue(TagAssociationInfo association, String property) {
    assert association != null;
    assert property != null;
    switch (property) {
      case "com.vmware.cis.tagging.TagAssociationModel/tagId":
        assert association.tagId != null;
        return association.tagId;
      case "com.vmware.cis.tagging.TagAssociationModel/objectId":
        assert association.objectId != null;
        return association.objectId;
    } 
    throw new IllegalArgumentException("Unknown property requested: " + property);
  }
  
  private static final class TagAssociationInfo {
    final String tagId;
    
    final DynamicID objectId;
    
    TagAssociationInfo(String tagId, DynamicID objectId) {
      assert tagId != null || objectId != null;
      this.tagId = tagId;
      this.objectId = objectId;
    }
  }
  
  public String toString() {
    return "TagAssociationDataProviderConnection(url=" + this._taggingVapiUri + ")";
  }
}
