package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

final class InvSvcTagModelPropertyProvider implements PropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  public static Map<String, QuerySchema.PropertyInfo> getProperties() {
    Map<String, QuerySchema.PropertyInfo> properties = new LinkedHashMap<>();
    properties.put("inventoryservice:InventoryServiceTag/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceTag/description", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceTag/category", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    properties.put("inventoryservice:InventoryServiceTag/categoryName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    return Collections.unmodifiableMap(properties);
  }
  
  InvSvcTagModelPropertyProvider(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
  }
  
  public List<List<Object>> get(List<?> modelKeys, List<String> properties) {
    assert modelKeys != null;
    assert properties != null;
    List<String> ids = new ArrayList<>(modelKeys.size());
    for (Object modelKey : modelKeys) {
      String id = toTagId(modelKey);
      ids.add(id);
    } 
    List<TagModel> tagModels = this._tagging.getTags(ids);
    if (properties.contains("inventoryservice:InventoryServiceCategory/name")) {
      Set<String> categoryIds = new HashSet<>();
      for (TagModel tagModel : tagModels)
        categoryIds.add(tagModel.getCategoryId()); 
      this._tagging.getCategories(new ArrayList<>(categoryIds));
    } 
    return getItems(tagModels, properties);
  }
  
  public List<List<Object>> list(List<String> properties) {
    assert properties != null;
    List<TagModel> tagModels = this._tagging.getAllTags();
    if (properties.contains("inventoryservice:InventoryServiceCategory/name"))
      this._tagging.getAllCategories(); 
    return getItems(tagModels, properties);
  }
  
  private List<List<Object>> getItems(List<TagModel> tagModels, List<String> properties) {
    assert tagModels != null;
    assert properties != null;
    List<List<Object>> items = new ArrayList<>(tagModels.size());
    for (TagModel tagModel : tagModels) {
      List<Object> values = new ArrayList(properties.size());
      for (String property : properties) {
        Object value = getProperty(tagModel, property);
        values.add(value);
      } 
      items.add(values);
    } 
    return items;
  }
  
  private Object getProperty(TagModel tagModel, String property) {
    String tagId;
    String categoryId;
    assert tagModel != null;
    assert property != null;
    switch (property) {
      case "@modelKey":
        tagId = tagModel.getId();
        return InvSvcTaggingIdConverter.taggingIdToMor(tagId);
      case "@type":
        return "inventoryservice:InventoryServiceTag";
      case "inventoryservice:InventoryServiceTag/name":
        return tagModel.getName();
      case "inventoryservice:InventoryServiceTag/description":
        return tagModel.getDescription();
      case "inventoryservice:InventoryServiceTag/category":
        categoryId = tagModel.getCategoryId();
        return InvSvcTaggingIdConverter.taggingIdToMor(categoryId);
      case "inventoryservice:InventoryServiceTag/categoryName":
        return getCategoryName(tagModel);
    } 
    throw new IllegalArgumentException("Unknown property : " + property);
  }
  
  private String getCategoryName(TagModel tagModel) {
    assert tagModel != null;
    String categoryId = tagModel.getCategoryId();
    CategoryModel categoryModel = this._tagging.getCategory(categoryId);
    if (categoryModel == null)
      return null; 
    return categoryModel.getName();
  }
  
  private static String toTagId(Object key) {
    Validate.notNull(key, "Null key for model inventoryservice:InventoryServiceTag");
    if (!(key instanceof ManagedObjectReference))
      throw new IllegalArgumentException(String.format("Key for model '%s' must be a ManagedObjectReference and not %s: <%s>", new Object[] { "inventoryservice:InventoryServiceTag", key
              
              .getClass().getCanonicalName(), key })); 
    ManagedObjectReference ref = (ManagedObjectReference)key;
    return InvSvcTaggingIdConverter.taggingMorToId(ref);
  }
}
