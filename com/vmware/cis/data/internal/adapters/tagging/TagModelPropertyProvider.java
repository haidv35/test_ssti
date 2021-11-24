package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.TagModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

final class TagModelPropertyProvider implements PropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  public static Map<String, QuerySchema.PropertyInfo> getProperties() {
    Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>();
    properties.put("com.vmware.cis.tagging.TagModel/id", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    properties.put("com.vmware.cis.tagging.TagModel/categoryId", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    properties.put("com.vmware.cis.tagging.TagModel/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.TagModel/description", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.TagModel/categoryName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.TagModel/usedBy", QuerySchema.PropertyInfo.forNonFilterableProperty());
    return Collections.unmodifiableMap(properties);
  }
  
  TagModelPropertyProvider(LenientTaggingFacade tagging) {
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
    if (properties.contains("com.vmware.cis.tagging.TagModel/categoryName")) {
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
    if (properties.contains("com.vmware.cis.tagging.TagModel/categoryName"))
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
    assert tagModel != null;
    assert property != null;
    switch (property) {
      case "@modelKey":
        return tagModel.getId();
      case "@type":
        return "com.vmware.cis.tagging.Tag";
      case "com.vmware.cis.tagging.TagModel/id":
        return tagModel.getId();
      case "com.vmware.cis.tagging.TagModel/categoryId":
        return tagModel.getCategoryId();
      case "com.vmware.cis.tagging.TagModel/name":
        return tagModel.getName();
      case "com.vmware.cis.tagging.TagModel/description":
        return tagModel.getDescription();
      case "com.vmware.cis.tagging.TagModel/usedBy":
        return tagModel.getUsedBy();
      case "com.vmware.cis.tagging.TagModel/categoryName":
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
    Validate.notNull(key, "Null key for model com.vmware.cis.tagging.TagModel");
    if (!(key instanceof String))
      throw new IllegalArgumentException(String.format("Key for model '%s' must be a java.lang.String and not %s: <%s>", new Object[] { "com.vmware.cis.tagging.TagModel", key
              
              .getClass().getCanonicalName(), key })); 
    return (String)key;
  }
}
