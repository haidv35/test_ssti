package com.vmware.ph.phservice.common.vmomi;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.ComplexType;
import com.vmware.vim.vmomi.core.types.ComplexTypeField;
import com.vmware.vim.vmomi.core.types.DataObjectType;
import com.vmware.vim.vmomi.core.types.ManagedMethod;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.ManagedProperty;
import com.vmware.vim.vmomi.core.types.VmodlField;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VmodlUtil {
  public static final String PROPERTY_PATH_SEPARATOR = ".";
  
  private static final int DEFAULT_REFLECTION_LEVEL = 4;
  
  private static final Log _log = LogFactory.getLog(VmodlUtil.class);
  
  public static List<VmodlType> getVmodlTypesAssignableFromClass(Class<?> typeClass, VmodlTypeMap vmodlTypeMap) {
    List<VmodlType> resultVmodlTypes = new ArrayList<>();
    for (VmodlType vmodlType : vmodlTypeMap.getVmodlTypes()) {
      if (typeClass.isAssignableFrom(vmodlType.getTypeClass()))
        resultVmodlTypes.add(vmodlType); 
    } 
    return resultVmodlTypes;
  }
  
  public static Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForRelatedManagedObjects(List<VmodlType> startManagedObjectVmodlTypes) {
    Map<VmodlType, Pair<VmodlType, String>> managedObjectTypeToManagedEntityRule = new LinkedHashMap<>();
    for (VmodlType startManagedObjectVmodlType : startManagedObjectVmodlTypes) {
      managedObjectTypeToManagedEntityRule.put(startManagedObjectVmodlType, null);
      Map<VmodlType, String> managedObjectTypeToPath = getRelatedManagedObjectVmodlTypesAndPropertyPaths((ManagedObjectType)startManagedObjectVmodlType, 4);
      for (Map.Entry<VmodlType, String> entry : managedObjectTypeToPath.entrySet()) {
        VmodlType moType = entry.getKey();
        String path = entry.getValue();
        if (!startManagedObjectVmodlTypes.contains(moType))
          managedObjectTypeToManagedEntityRule.put(moType, new Pair(startManagedObjectVmodlType, path)); 
      } 
    } 
    return managedObjectTypeToManagedEntityRule;
  }
  
  public static Map<VmodlType, Pair<VmodlType, String>> getRetrievalRulesForDataObjectRelatedManagedObjects(VmodlType startManagedObjectVmodlType, String dataObjectPropertyPath, DataObject dataObject, VmodlTypeMap vmodlTypeMap) {
    Field[] dataObjectProperties = dataObject.getClass().getFields();
    Map<VmodlType, Pair<VmodlType, String>> managedObjectTypeToRetrievalRule = new LinkedHashMap<>();
    for (Field dataObjectProperty : dataObjectProperties) {
      try {
        Object dataObjectPropertyValue = dataObjectProperty.get(dataObject);
        if (dataObjectPropertyValue instanceof ManagedObjectReference) {
          String dataObjectFieldWsdlName = ((ManagedObjectReference)dataObjectPropertyValue).getType();
          VmodlType moRefVmodlType = vmodlTypeMap.getVmodlType(dataObjectFieldWsdlName);
          managedObjectTypeToRetrievalRule.put(moRefVmodlType, new Pair(startManagedObjectVmodlType, dataObjectPropertyPath + "." + dataObjectProperty



                
                .getName()));
        } 
      } catch (IllegalArgumentException|IllegalAccessException e) {
        _log.debug(
            String.format("Failed to acquire value for field '%s' for MO '%s'.", new Object[] { dataObjectProperty.getName(), dataObject.getClass() }), e);
      } 
    } 
    return managedObjectTypeToRetrievalRule;
  }
  
  public static List<VmodlType> getManagedObjectReferenceVmodlTypesInParentTypeProperties(ComplexType parentVmodlType) {
    List<VmodlType> vmodlTypes = new LinkedList<>();
    for (ComplexTypeField declaredProperty : parentVmodlType.getDeclaredProperties()) {
      VmodlType propertyVmodlType = declaredProperty.getType();
      VmodlType.Kind propertyVmodlKind = propertyVmodlType.getKind();
      if (propertyVmodlKind == VmodlType.Kind.MOREF) {
        ManagedObjectType managedObjectType = (ManagedObjectType)declaredProperty.getManagedObjectType();
        vmodlTypes.add(managedObjectType);
      } 
    } 
    return vmodlTypes;
  }
  
  public static Map<VmodlType, String> getRelatedManagedObjectVmodlTypesAndPropertyPaths(ManagedObjectType startManagedObjectVmodlType, int maxLevel) {
    return getRelatedManagedObjectTypeAndPropertyPathsInt((VmodlType)startManagedObjectVmodlType, null, 0, maxLevel);
  }
  
  public static List<ManagedMethod> getManagedMethods(ManagedObjectType managedObjectType, VmodlVersion vmodlVersion) {
    List<ManagedMethod> managedMethods = new LinkedList<>();
    ManagedMethod[] objectMethods = null;
    try {
      objectMethods = managedObjectType.getMethods(false);
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        String msg = String.format("Error happened when processing all methods for %s", new Object[] { managedObjectType.getTypeName().toString() });
        _log.debug(msg, e);
      } 
    } 
    if (objectMethods == null)
      return Collections.emptyList(); 
    for (ManagedMethod managedMethod : objectMethods) {
      boolean isMethodSupported = (vmodlVersion == null || vmodlVersion.isCompatible(managedMethod.getVersion()));
      if (isMethodSupported)
        managedMethods.add(managedMethod); 
    } 
    return managedMethods;
  }
  
  public static List<String> getProperties(VmodlType vmodlType, String parentProperty, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return getPropertiesInt(vmodlType, parentProperty, 1, 4, vmodlTypeMap, vmodlVersion);
  }
  
  public static List<String> getProperties(VmodlType vmodlType, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return getPropertiesInt(vmodlType, null, 1, 4, vmodlTypeMap, vmodlVersion);
  }
  
  public static List<String> getProperties(VmodlType vmodlType, int maxLevel, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return getPropertiesInt(vmodlType, null, 1, maxLevel, vmodlTypeMap, vmodlVersion);
  }
  
  private static Map<VmodlType, String> getRelatedManagedObjectTypeAndPropertyPathsInt(VmodlType vmodlType, String parentPropertyPath, int currentLevel, int maxLevel) {
    ComplexTypeField[] arrayOfComplexTypeField;
    Map<VmodlType, String> resultProperties = new LinkedHashMap<>();
    if (currentLevel == maxLevel)
      return resultProperties; 
    VmodlField[] propertyFields = null;
    if (vmodlType instanceof ManagedObjectType) {
      ManagedObjectType moType = (ManagedObjectType)vmodlType;
      ManagedProperty[] arrayOfManagedProperty = moType.getManagedProperties();
    } else if (vmodlType instanceof DataObjectType) {
      DataObjectType doType = (DataObjectType)vmodlType;
      arrayOfComplexTypeField = doType.getProperties();
    } 
    if (arrayOfComplexTypeField == null)
      return resultProperties; 
    for (ComplexTypeField complexTypeField : arrayOfComplexTypeField) {
      String propertyPath = complexTypeField.getName();
      if (parentPropertyPath != null)
        propertyPath = parentPropertyPath + "." + propertyPath; 
      VmodlType propertyVmodlType = null;
      if (complexTypeField instanceof ComplexTypeField)
        try {
          propertyVmodlType = complexTypeField.getManagedObjectType();
        } catch (Exception exception) {} 
      if (propertyVmodlType == null)
        propertyVmodlType = complexTypeField.isLink() ? complexTypeField.getLinkType() : complexTypeField.getType(); 
      if (propertyVmodlType instanceof ManagedObjectType) {
        resultProperties.put(propertyVmodlType, propertyPath);
      } else {
        Map<VmodlType, String> subProperies = getRelatedManagedObjectTypeAndPropertyPathsInt(propertyVmodlType, propertyPath, currentLevel + 1, maxLevel);
        resultProperties.putAll(subProperies);
      } 
    } 
    return resultProperties;
  }
  
  private static List<String> getPropertiesInt(VmodlType vmodlType, String parentProperty, int currentLevel, int maxLevel, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    List<String> resultProperties = new LinkedList<>();
    try {
      ComplexTypeField[] arrayOfComplexTypeField;
      VmodlField[] propertyFields = null;
      if (vmodlType instanceof ManagedObjectType) {
        ManagedObjectType moType = (ManagedObjectType)vmodlType;
        ManagedProperty[] arrayOfManagedProperty = moType.getManagedProperties();
      } else if (vmodlType instanceof DataObjectType) {
        DataObjectType daType = (DataObjectType)vmodlType;
        arrayOfComplexTypeField = daType.getProperties();
      } 
      if (arrayOfComplexTypeField == null)
        return Collections.emptyList(); 
      for (ComplexTypeField complexTypeField : arrayOfComplexTypeField) {
        if (vmodlVersion == null || vmodlVersion
          .isCompatible(complexTypeField.getVersion())) {
          String propertyName = complexTypeField.getName();
          if (parentProperty != null)
            propertyName = parentProperty + "/" + propertyName; 
          resultProperties.add(propertyName);
          if (currentLevel < maxLevel) {
            VmodlType propertyVmodlType = complexTypeField.isLink() ? complexTypeField.getLinkType() : complexTypeField.getType();
            List<String> subProperies = getPropertiesInt(propertyVmodlType, propertyName, currentLevel + 1, maxLevel, vmodlTypeMap, vmodlVersion);
            resultProperties.addAll(subProperies);
          } 
        } 
      } 
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        String msg = String.format("Error happened when processing all properties for %s", new Object[] { vmodlType.getTypeName().toString() });
        _log.debug(msg, e);
      } 
    } 
    Collections.sort(resultProperties);
    return resultProperties;
  }
}
