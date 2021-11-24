package com.vmware.ph.phservice.provider.common.vmomi;

import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public final class VmodlQueryContextUtil {
  public static List<ManagedObjectReference> getMoRefsFromContext(QueryContext queryContext, String objectKey) {
    List<ManagedObjectReference> moRefs = Collections.emptyList();
    try {
      List<KeyAnyValue> objectIdToMoRefs = getObjectsFromContext(queryContext, objectKey, KeyAnyValue.class);
      moRefs = new ArrayList<>(objectIdToMoRefs.size());
      for (KeyAnyValue objectIdToMoRef : objectIdToMoRefs)
        moRefs.add((ManagedObjectReference)objectIdToMoRef.getValue()); 
    } catch (IllegalStateException e) {
      moRefs = getObjectsFromContext(queryContext, objectKey, ManagedObjectReference.class);
    } 
    return moRefs;
  }
  
  public static Map<ManagedObjectReference, String> getMoRefToObjectIdFromContext(QueryContext queryContext, String objectKey) {
    Map<ManagedObjectReference, String> moRefToObjectId = new LinkedHashMap<>();
    try {
      List<KeyAnyValue> keyAnyValues = getObjectsFromContext(queryContext, objectKey, KeyAnyValue.class);
      for (KeyAnyValue keyAnyValue : keyAnyValues) {
        String objectId = keyAnyValue.getKey();
        ManagedObjectReference objectMoRef = (ManagedObjectReference)keyAnyValue.getValue();
        moRefToObjectId.put(objectMoRef, objectId);
      } 
    } catch (IllegalStateException e) {
      List<ManagedObjectReference> moRefs = getObjectsFromContext(queryContext, objectKey, ManagedObjectReference.class);
      for (ManagedObjectReference moRef : moRefs)
        moRefToObjectId.put(moRef, null); 
    } 
    return moRefToObjectId;
  }
  
  private static <T> List<T> getObjectsFromContext(QueryContext queryContext, String objectKey, @Nonnull Class<T> clazz) {
    if (queryContext == null || queryContext.isEmpty())
      return Collections.emptyList(); 
    return queryContext.getObjects(objectKey, clazz);
  }
}
