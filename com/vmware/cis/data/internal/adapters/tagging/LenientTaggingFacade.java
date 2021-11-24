package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.tagging.BatchTypes;
import com.vmware.cis.tagging.CategoryModel;
import com.vmware.cis.tagging.TagModel;
import com.vmware.vapi.std.DynamicID;
import java.util.List;

interface LenientTaggingFacade {
  List<String> listTags();
  
  TagModel getTag(String paramString);
  
  List<TagModel> getAllTags();
  
  List<TagModel> getTags(List<String> paramList);
  
  List<String> findTagsByName(String paramString);
  
  List<String> listCategories();
  
  CategoryModel getCategory(String paramString);
  
  List<CategoryModel> getAllCategories();
  
  List<CategoryModel> getCategories(List<String> paramList);
  
  List<String> listTagsForCategory(String paramString);
  
  List<String> listAttachedTags(List<DynamicID> paramList);
  
  List<DynamicID> listAttachedObjects(List<String> paramList);
  
  List<BatchTypes.TagToObjects> listAllAttachedObjectsOnTags();
  
  List<BatchTypes.TagToObjects> listAttachedObjectsOnTags(List<String> paramList);
  
  List<BatchTypes.ObjectToTags> listAttachedTagsOnObjects(List<DynamicID> paramList);
}
