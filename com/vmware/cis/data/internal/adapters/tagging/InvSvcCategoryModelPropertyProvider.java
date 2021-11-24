package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

final class InvSvcCategoryModelPropertyProvider implements PropertyProvider {
  private final LenientTaggingFacade _tagging;
  
  public static Map<String, QuerySchema.PropertyInfo> getProperties() {
    Map<String, QuerySchema.PropertyInfo> properties = new LinkedHashMap<>();
    properties.put("inventoryservice:InventoryServiceCategory/name", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceCategory/description", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceCategory/multipleCardinality", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    properties.put("inventoryservice:InventoryServiceCategory/associableEntityType", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceCategory/associableEntityTypeName", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.STRING));
    properties.put("inventoryservice:InventoryServiceCategory/associableWithAllEntityTypes", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.BOOLEAN));
    properties.put("inventoryservice:InventoryServiceCategory/childTags", QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.ID));
    return Collections.unmodifiableMap(properties);
  }
  
  InvSvcCategoryModelPropertyProvider(LenientTaggingFacade tagging) {
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
    String id;
    Collection<String> ids;
    Collection<?> associableTypes;
    Object[] associableTypesArray;
    assert categoryModel != null;
    assert property != null;
    switch (property) {
      case "@modelKey":
        id = categoryModel.getId();
        return InvSvcTaggingIdConverter.taggingIdToMor(id);
      case "@type":
        return "inventoryservice:InventoryServiceCategory";
      case "inventoryservice:InventoryServiceCategory/name":
        return categoryModel.getName();
      case "inventoryservice:InventoryServiceCategory/description":
        return categoryModel.getDescription();
      case "inventoryservice:InventoryServiceCategory/multipleCardinality":
        return Boolean.valueOf(CategoryModel.Cardinality.MULTIPLE.equals(categoryModel.getCardinality()));
      case "inventoryservice:InventoryServiceCategory/childTags":
        ids = this._tagging.listTagsForCategory(categoryModel.getId());
        return InvSvcTaggingIdConverter.taggingIdsToMorArray(ids);
      case "inventoryservice:InventoryServiceCategory/associableEntityType":
      case "inventoryservice:InventoryServiceCategory/associableEntityTypeName":
        associableTypes = categoryModel.getAssociableTypes();
        associableTypesArray = associableTypes.isEmpty() ? null : associableTypes.toArray();
        return associableTypesArray;
      case "inventoryservice:InventoryServiceCategory/associableWithAllEntityTypes":
        return new Object[] { Boolean.valueOf(categoryModel.getAssociableTypes().isEmpty()) };
    } 
    throw new IllegalArgumentException("Unknown property : " + property);
  }
  
  private static String toCategoryId(Object key) {
    Validate.notNull(key, "Null key for model inventoryservice:InventoryServiceCategory");
    if (!(key instanceof ManagedObjectReference))
      throw new IllegalArgumentException(String.format("Key for model '%s' must be a ManagedObjectReference and not %s: <%s>", new Object[] { "inventoryservice:InventoryServiceCategory", key
              
              .getClass().getCanonicalName(), key })); 
    ManagedObjectReference ref = (ManagedObjectReference)key;
    return InvSvcTaggingIdConverter.taggingMorToId(ref);
  }
}
