package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.tagging.BatchTypes;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.std.DynamicID;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TaggingFacadePerfLogging implements LenientTaggingFacade {
  private static final Logger _logger = LoggerFactory.getLogger(TaggingFacadePerfLogging.class);
  
  private final LenientTaggingFacade _tagging;
  
  TaggingFacadePerfLogging(LenientTaggingFacade tagging) {
    assert tagging != null;
    this._tagging = tagging;
  }
  
  public List<String> listTags() {
    long begint = System.currentTimeMillis();
    List<String> ids = this._tagging.listTags();
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Tag.list() returned {} ids in {} ms", Integer.valueOf(ids.size()), Long.valueOf(totalt));
    return ids;
  }
  
  public TagModel getTag(String tagId) {
    return this._tagging.getTag(tagId);
  }
  
  public List<TagModel> getAllTags() {
    long begint = System.currentTimeMillis();
    List<TagModel> models = this._tagging.getAllTags();
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.getAllTags() returned {} tags in {} ms", Integer.valueOf(models.size()), Long.valueOf(totalt));
    return models;
  }
  
  public List<TagModel> getTags(List<String> tagIds) {
    long begint = System.currentTimeMillis();
    List<TagModel> models = this._tagging.getTags(tagIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.getTags() returned {} tags in {} ms", Integer.valueOf(models.size()), Long.valueOf(totalt));
    return models;
  }
  
  public List<String> findTagsByName(String tagNamePattern) {
    long begint = System.currentTimeMillis();
    List<String> ids = this._tagging.findTagsByName(tagNamePattern);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.findTagsByName() returned {} ids in {} ms", Integer.valueOf(ids.size()), Long.valueOf(totalt));
    return ids;
  }
  
  public List<String> listCategories() {
    long begint = System.currentTimeMillis();
    List<String> ids = this._tagging.listCategories();
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Category.list() returned {} ids in {} ms", Integer.valueOf(ids.size()), Long.valueOf(totalt));
    return ids;
  }
  
  public CategoryModel getCategory(String categoryId) {
    return this._tagging.getCategory(categoryId);
  }
  
  public List<CategoryModel> getAllCategories() {
    long begint = System.currentTimeMillis();
    List<CategoryModel> models = this._tagging.getAllCategories();
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.getAllCategories() returned {} categories in {} ms", Integer.valueOf(models.size()), Long.valueOf(totalt));
    return models;
  }
  
  public List<CategoryModel> getCategories(List<String> categoryIds) {
    long begint = System.currentTimeMillis();
    List<CategoryModel> models = this._tagging.getCategories(categoryIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.getCategories() returned {} categories in {} ms", Integer.valueOf(models.size()), Long.valueOf(totalt));
    return models;
  }
  
  public List<String> listTagsForCategory(String categoryId) {
    return this._tagging.listTagsForCategory(categoryId);
  }
  
  public List<String> listAttachedTags(List<DynamicID> objectIds) {
    long begint = System.currentTimeMillis();
    List<String> ids = this._tagging.listAttachedTags(objectIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.listAttachedTags() returned {} ids in {} ms", Integer.valueOf(ids.size()), Long.valueOf(totalt));
    return ids;
  }
  
  public List<DynamicID> listAttachedObjects(List<String> tagIds) {
    long begint = System.currentTimeMillis();
    List<DynamicID> ids = this._tagging.listAttachedObjects(tagIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.listAttachedObjects() returned {} ids in {} ms", Integer.valueOf(ids.size()), Long.valueOf(totalt));
    return ids;
  }
  
  public List<BatchTypes.TagToObjects> listAllAttachedObjectsOnTags() {
    long begint = System.currentTimeMillis();
    List<BatchTypes.TagToObjects> tagsToObjects = this._tagging.listAllAttachedObjectsOnTags();
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.listAllAttachedObjectsOnTags() returned {} TagToObjects in {} ms", Integer.valueOf(tagsToObjects.size()), Long.valueOf(totalt));
    return tagsToObjects;
  }
  
  public List<BatchTypes.TagToObjects> listAttachedObjectsOnTags(List<String> tagIds) {
    long begint = System.currentTimeMillis();
    List<BatchTypes.TagToObjects> tagsToObjects = this._tagging.listAttachedObjectsOnTags(tagIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.listAttachedObjectsOnTags() returned {} TagToObjects in {} ms", Integer.valueOf(tagsToObjects.size()), Long.valueOf(totalt));
    return tagsToObjects;
  }
  
  public List<BatchTypes.ObjectToTags> listAttachedTagsOnObjects(List<DynamicID> objectIds) {
    long begint = System.currentTimeMillis();
    List<BatchTypes.ObjectToTags> objectsToTags = this._tagging.listAttachedTagsOnObjects(objectIds);
    long totalt = System.currentTimeMillis() - begint;
    _logger.debug("Batch.listAttachedTagsOnObjects() returned {} ObjectToTags in {} ms", Integer.valueOf(objectsToTags.size()), Long.valueOf(totalt));
    return objectsToTags;
  }
}
