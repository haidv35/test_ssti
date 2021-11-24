package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.tagging.BatchTypes;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.std.DynamicID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TaggingFacadeCache implements LenientTaggingFacade {
  private static final TagModel NO_SUCH_TAG = new TagModel();
  
  private static final CategoryModel NO_SUCH_CATEGORY = new CategoryModel();
  
  private final LenientTaggingFacade _tagging;
  
  private final Map<String, TagModel> _cachedTagById;
  
  private List<TagModel> _cachedAllTags;
  
  private final Map<String, CategoryModel> _cachedCategoryById;
  
  private List<CategoryModel> _cachedAllCategories;
  
  TaggingFacadeCache(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
    this._cachedTagById = new HashMap<>();
    this._cachedAllTags = null;
    this._cachedCategoryById = new HashMap<>();
    this._cachedAllCategories = null;
  }
  
  public List<String> listTags() {
    return this._tagging.listTags();
  }
  
  public TagModel getTag(String tagId) {
    assert tagId != null;
    TagModel cached = this._cachedTagById.get(tagId);
    if (cached == NO_SUCH_TAG)
      return null; 
    if (cached != null)
      return cached; 
    TagModel tagModel = this._tagging.getTag(tagId);
    if (tagModel == null) {
      this._cachedTagById.put(tagId, NO_SUCH_TAG);
    } else {
      this._cachedTagById.put(tagId, tagModel);
    } 
    return tagModel;
  }
  
  public List<TagModel> getAllTags() {
    if (this._cachedAllTags != null)
      return this._cachedAllTags; 
    this._cachedAllTags = this._tagging.getAllTags();
    for (TagModel tagModel : this._cachedAllTags)
      this._cachedTagById.put(tagModel.getId(), tagModel); 
    return this._cachedAllTags;
  }
  
  public List<TagModel> getTags(List<String> tagIds) {
    List<String> noncachedIds = new ArrayList<>(tagIds.size());
    Map<String, TagModel> modelById = new HashMap<>(tagIds.size());
    for (String tagId : tagIds) {
      TagModel cached = this._cachedTagById.get(tagId);
      if (cached == null) {
        noncachedIds.add(tagId);
        continue;
      } 
      if (cached != NO_SUCH_TAG)
        modelById.put(tagId, cached); 
    } 
    if (!noncachedIds.isEmpty()) {
      List<TagModel> noncachedModels = this._tagging.getTags(noncachedIds);
      for (TagModel model : noncachedModels) {
        modelById.put(model.getId(), model);
        this._cachedTagById.put(model.getId(), model);
      } 
    } 
    List<TagModel> models = new ArrayList<>(tagIds.size());
    for (String tagId : tagIds) {
      TagModel model = modelById.get(tagId);
      if (model != null)
        models.add(model); 
    } 
    return models;
  }
  
  public List<String> findTagsByName(String tagNamePattern) {
    return this._tagging.findTagsByName(tagNamePattern);
  }
  
  public List<String> listCategories() {
    return this._tagging.listCategories();
  }
  
  public CategoryModel getCategory(String categoryId) {
    assert categoryId != null;
    CategoryModel cached = this._cachedCategoryById.get(categoryId);
    if (cached == NO_SUCH_CATEGORY)
      return null; 
    if (cached != null)
      return cached; 
    CategoryModel categoryModel = this._tagging.getCategory(categoryId);
    if (categoryModel == null) {
      this._cachedCategoryById.put(categoryId, NO_SUCH_CATEGORY);
    } else {
      this._cachedCategoryById.put(categoryId, categoryModel);
    } 
    return categoryModel;
  }
  
  public List<CategoryModel> getAllCategories() {
    if (this._cachedAllCategories != null)
      return this._cachedAllCategories; 
    this._cachedAllCategories = this._tagging.getAllCategories();
    for (CategoryModel categoryModel : this._cachedAllCategories)
      this._cachedCategoryById.put(categoryModel.getId(), categoryModel); 
    return this._cachedAllCategories;
  }
  
  public List<CategoryModel> getCategories(List<String> categoryIds) {
    assert categoryIds != null;
    List<String> noncachedIds = new ArrayList<>(categoryIds.size());
    Map<String, CategoryModel> modelById = new HashMap<>(categoryIds.size());
    for (String categoryId : categoryIds) {
      CategoryModel model = this._cachedCategoryById.get(categoryId);
      if (model == null) {
        noncachedIds.add(categoryId);
        continue;
      } 
      if (model != NO_SUCH_CATEGORY)
        modelById.put(categoryId, model); 
    } 
    if (!noncachedIds.isEmpty()) {
      List<CategoryModel> noncachedModels = this._tagging.getCategories(noncachedIds);
      for (CategoryModel model : noncachedModels) {
        modelById.put(model.getId(), model);
        this._cachedCategoryById.put(model.getId(), model);
      } 
    } 
    List<CategoryModel> models = new ArrayList<>(categoryIds.size());
    for (String tagId : categoryIds) {
      CategoryModel model = modelById.get(tagId);
      if (model != null)
        models.add(model); 
    } 
    return models;
  }
  
  public List<String> listTagsForCategory(String categoryId) {
    assert categoryId != null;
    return this._tagging.listTagsForCategory(categoryId);
  }
  
  public List<String> listAttachedTags(List<DynamicID> objectIds) {
    assert objectIds != null;
    return this._tagging.listAttachedTags(objectIds);
  }
  
  public List<DynamicID> listAttachedObjects(List<String> tagIds) {
    assert tagIds != null;
    return this._tagging.listAttachedObjects(tagIds);
  }
  
  public List<BatchTypes.TagToObjects> listAllAttachedObjectsOnTags() {
    return this._tagging.listAllAttachedObjectsOnTags();
  }
  
  public List<BatchTypes.TagToObjects> listAttachedObjectsOnTags(List<String> tagIds) {
    assert tagIds != null;
    return this._tagging.listAttachedObjectsOnTags(tagIds);
  }
  
  public List<BatchTypes.ObjectToTags> listAttachedTagsOnObjects(List<DynamicID> objectIds) {
    assert objectIds != null;
    return this._tagging.listAttachedTagsOnObjects(objectIds);
  }
}
