package com.vmware.ph.phservice.provider.common.vmomi;

import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class VmomiDataProviderUtil {
  public static URI createManagedObjectModelKey(ManagedObject managedObject) {
    ManagedObjectReference moRef = managedObject._getRef();
    URI modelKey = DataProviderUtil.createModelKey(moRef.getType(), moRef.getServerGuid());
    return modelKey;
  }
  
  public static List<Object> getPropertyValuesForPropertiesOfKind(Object object, VmodlTypeMap vmodlTypeMap, VmodlType.Kind vmodlKind) {
    List<Object> propertyValues = new LinkedList();
    Field[] declaredFields = object.getClass().getDeclaredFields();
    for (Field declaredField : declaredFields) {
      VmodlType fieldVmodlType = vmodlTypeMap.getVmodlType(declaredField.getType());
      if (fieldVmodlType.getKind() == vmodlKind) {
        Object propertyValue = DataProviderUtil.getPropertyValue(object, declaredField.getName());
        propertyValues.add(propertyValue);
      } 
    } 
    return propertyValues;
  }
}
