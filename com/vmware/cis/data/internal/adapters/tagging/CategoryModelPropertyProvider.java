package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.tagging.CategoryModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

final class CategoryModelPropertyProvider implements PropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  public static Map<String, QuerySchema.PropertyInfo> getProperties() {
    Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>();
    properties.put("com.vmware.cis.tagging.CategoryModel/id", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    properties.put("com.vmware.cis.tagging.CategoryModel/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.CategoryModel/description", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.CategoryModel/cardinality", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("com.vmware.cis.tagging.CategoryModel/associableTypes", QuerySchema.PropertyInfo.forNonFilterableProperty());
    properties.put("com.vmware.cis.tagging.CategoryModel/usedBy", QuerySchema.PropertyInfo.forNonFilterableProperty());
    properties.put("com.vmware.cis.tagging.CategoryModel/tags", QuerySchema.PropertyInfo.forNonFilterableProperty());
    return Collections.unmodifiableMap(properties);
  }
  
  CategoryModelPropertyProvider(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
  }
  
  public List<List<Object>> get(List<?> modelKeys, List<String> properties) {
    assert modelKeys != null;
    assert properties != null;
    List<String> ids = new ArrayList<>(modelKeys.size());
    for (Object modelKey : modelKeys) {
      String id = toCategoryId(modelKey);
      ids.add(id);
    } 
    List<CategoryModel> categoryModels = this._tagging.getCategories(ids);
    return getItems(categoryModels, properties);
  }
  
  public List<List<Object>> list(List<String> properties) {
    assert properties != null;
    List<CategoryModel> categoryModels = this._tagging.getAllCategories();
    return getItems(categoryModels, properties);
  }
  
  private List<List<Object>> getItems(List<CategoryModel> categoryModels, List<String> properties) {
    assert categoryModels != null;
    assert properties != null;
    List<List<Object>> items = new ArrayList<>(categoryModels.size());
    for (CategoryModel categoryModel : categoryModels) {
      List<Object> values = new ArrayList(properties.size());
      for (String property : properties) {
        Object value = getProperty(categoryModel, property);
        values.add(value);
      } 
      items.add(values);
    } 
    return items;
  }
  
  private Object getProperty(CategoryModel categoryModel, String property) {
    assert categoryModel != null;
    assert property != null;
    switch (property) {
      case "@modelKey":
        return categoryModel.getId();
      case "@type":
        return "com.vmware.cis.tagging.Category";
      case "com.vmware.cis.tagging.CategoryModel/id":
        return categoryModel.getId();
      case "com.vmware.cis.tagging.CategoryModel/name":
        return categoryModel.getName();
      case "com.vmware.cis.tagging.CategoryModel/description":
        return categoryModel.getDescription();
      case "com.vmware.cis.tagging.CategoryModel/cardinality":
        return categoryModel.getCardinality().name();
      case "com.vmware.cis.tagging.CategoryModel/associableTypes":
        return categoryModel.getAssociableTypes();
      case "com.vmware.cis.tagging.CategoryModel/usedBy":
        return categoryModel.getUsedBy();
      case "com.vmware.cis.tagging.CategoryModel/tags":
        return this._tagging.listTagsForCategory(categoryModel.getId());
    } 
    throw new IllegalArgumentException("Unknown property : " + property);
  }
  
  private static String toCategoryId(Object key) {
    Validate.notNull(key, "Null key for model com.vmware.cis.tagging.CategoryModel");
    if (!(key instanceof String))
      throw new IllegalArgumentException(String.format("Key for model '%s' must be a java.lang.String and not %s: <%s>", new Object[] { "com.vmware.cis.tagging.CategoryModel", key
              
              .getClass().getCanonicalName(), key })); 
    return (String)key;
  }
}
