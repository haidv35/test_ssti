package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.internal.adapters.util.vapi.VapiOsgiAwareStubFactory;
import com.vmware.cis.tagging.Batch;
import com.vmware.cis.tagging.BatchTypes;
import com.vmware.cis.tagging.Category;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.Tag;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vapi.std.DynamicID;
import com.vmware.vapi.std.errors.NotFound;
import com.vmware.vapi.std.errors.Unauthorized;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TaggingFacadeImpl implements LenientTaggingFacade {
  private final Tag _tagService;
  
  private final Category _categoryService;
  
  private final Batch _batchService;
  
  TaggingFacadeImpl(ApiProvider apiProvider) {
    assert apiProvider != null;
    VapiOsgiAwareStubFactory stubFactory = new VapiOsgiAwareStubFactory(apiProvider);
    this._tagService = stubFactory.<Tag>createStub(Tag.class);
    this._categoryService = stubFactory.<Category>createStub(Category.class);
    this._batchService = stubFactory.<Batch>createStub(Batch.class);
  }
  
  public List<String> listTags() {
    return this._tagService.list();
  }
  
  public TagModel getTag(String tagId) {
    assert tagId != null;
    try {
      return this._tagService.get(tagId);
    } catch (NotFound notFound) {
      return null;
    } catch (Unauthorized unauthorized) {
      return null;
    } 
  }
  
  public List<TagModel> getAllTags() {
    return this._batchService.getAllTags();
  }
  
  public List<TagModel> getTags(List<String> tagIds) {
    List<TagModel> unordered = this._batchService.getTags(tagIds);
    Map<String, TagModel> modelById = new HashMap<>(unordered.size());
    for (TagModel model : unordered)
      modelById.put(model.getId(), model); 
    List<TagModel> ordered = new ArrayList<>(unordered.size());
    for (String id : tagIds) {
      TagModel model = modelById.get(id);
      if (model != null)
        ordered.add(model); 
    } 
    return ordered;
  }
  
  public List<String> findTagsByName(String tagNamePattern) {
    assert tagNamePattern != null;
    return this._batchService.findTagsByName(tagNamePattern);
  }
  
  public List<String> listCategories() {
    return this._categoryService.list();
  }
  
  public CategoryModel getCategory(String categoryId) {
    assert categoryId != null;
    try {
      return this._categoryService.get(categoryId);
    } catch (NotFound notFound) {
      return null;
    } catch (Unauthorized unauthorized) {
      return null;
    } 
  }
  
  public List<CategoryModel> getAllCategories() {
    return this._batchService.getAllCategories();
  }
  
  public List<CategoryModel> getCategories(List<String> categoryIds) {
    List<CategoryModel> unordered = this._batchService.getCategories(categoryIds);
    Map<String, CategoryModel> modelById = new HashMap<>(unordered.size());
    for (CategoryModel model : unordered)
      modelById.put(model.getId(), model); 
    List<CategoryModel> ordered = new ArrayList<>(unordered.size());
    for (String id : categoryIds) {
      CategoryModel model = modelById.get(id);
      if (model != null)
        ordered.add(model); 
    } 
    return ordered;
  }
  
  public List<String> listTagsForCategory(String categoryId) {
    assert categoryId != null;
    try {
      return this._tagService.listTagsForCategory(categoryId);
    } catch (NotFound notFound) {
      return null;
    } catch (Unauthorized unauthorized) {
      return null;
    } 
  }
  
  public List<String> listAttachedTags(List<DynamicID> objectIds) {
    assert objectIds != null;
    try {
      return this._batchService.listAttachedTags(objectIds);
    } catch (Unauthorized unauthorized) {
      return Collections.emptyList();
    } 
  }
  
  public List<DynamicID> listAttachedObjects(List<String> tagIds) {
    assert tagIds != null;
    try {
      return this._batchService.listAttachedObjects(tagIds);
    } catch (NotFound notFound) {
      return Collections.emptyList();
    } catch (Unauthorized unauthorized) {
      return Collections.emptyList();
    } 
  }
  
  public List<BatchTypes.TagToObjects> listAllAttachedObjectsOnTags() {
    try {
      return this._batchService.listAllAttachedObjectsOnTags();
    } catch (Unauthorized unauthorized) {
      return Collections.emptyList();
    } 
  }
  
  public List<BatchTypes.TagToObjects> listAttachedObjectsOnTags(List<String> tagIds) {
    assert tagIds != null;
    try {
      return this._batchService.listAttachedObjectsOnTags(tagIds);
    } catch (NotFound notFound) {
      return Collections.emptyList();
    } catch (Unauthorized unauthorized) {
      return Collections.emptyList();
    } 
  }
  
  public List<BatchTypes.ObjectToTags> listAttachedTagsOnObjects(List<DynamicID> objectIds) {
    assert objectIds != null;
    try {
      return this._batchService.listAttachedTagsOnObjects(objectIds);
    } catch (NotFound notFound) {
      return Collections.emptyList();
    } catch (Unauthorized unauthorized) {
      return Collections.emptyList();
    } 
  }
}
