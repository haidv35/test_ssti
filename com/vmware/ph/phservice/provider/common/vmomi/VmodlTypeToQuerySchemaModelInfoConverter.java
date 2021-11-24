package com.vmware.ph.phservice.provider.common.vmomi;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MethodNameToQueryPropertyConverter;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.vmomi.core.types.ManagedMethod;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VmodlTypeToQuerySchemaModelInfoConverter {
  public static List<VmodlType> getAllDataObjectVmodlTypesInPackage(VmodlTypeMap vmodlTypeMap, String vmodlPackageName) {
    List<VmodlType> dataObjectTypes = new LinkedList<>();
    List<VmodlType> vmodlTypes = vmodlTypeMap.getVmodlTypes();
    for (VmodlType vmodlType : vmodlTypes) {
      if (DataObject.class.isAssignableFrom(vmodlType.getTypeClass()) && vmodlType
        .getTypeClass().getCanonicalName().contains(vmodlPackageName))
        dataObjectTypes.add(vmodlType); 
    } 
    return dataObjectTypes;
  }
  
  public static List<VmodlType> getDataObjectVmodlTypesForVmodlClasses(VmodlTypeMap vmodlTypeMap, List<Class<? extends DataObject>> vmodlTypeClasses) {
    List<VmodlType> dataObjectTypes = new LinkedList<>();
    for (Class<?> vmodlTypeClass : vmodlTypeClasses) {
      VmodlType vmodlType = vmodlTypeMap.getLoadedVmodlType(vmodlTypeClass);
      dataObjectTypes.add(vmodlType);
    } 
    return dataObjectTypes;
  }
  
  public static Map<String, QuerySchema.ModelInfo> convertVmodlTypesToClassNameModelInfos(Collection<VmodlType> vmodlTypes, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return convertVmodlTypesToModelInfos(vmodlTypes, vmodlTypeMap, vmodlVersion, true);
  }
  
  public static Map<String, QuerySchema.ModelInfo> convertVmodlTypesToWsdlNameModelInfos(Collection<VmodlType> vmodlTypes, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return convertVmodlTypesToModelInfos(vmodlTypes, vmodlTypeMap, vmodlVersion, false);
  }
  
  public static Map<String, QuerySchema.ModelInfo> convertManagedObjectTypeMethodsToWsdlNameModelInfos(Collection<ManagedObjectType> managedObjectTypes, VmodlVersion vmodlVersion) {
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    for (ManagedObjectType managedObjectType : managedObjectTypes) {
      List<ManagedMethod> managedObjectMethods = VmodlUtil.getManagedMethods(managedObjectType, vmodlVersion);
      QuerySchema.ModelInfo methodsModelInfo = convertManagedObjectMethodsToModelInfo(managedObjectMethods);
      models.put(managedObjectType.getWsdlName(), methodsModelInfo);
    } 
    return models;
  }
  
  private static Map<String, QuerySchema.ModelInfo> convertVmodlTypesToModelInfos(Collection<VmodlType> vmodlTypes, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion, boolean useTypeClassNames) {
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    for (VmodlType vmodlType : vmodlTypes) {
      List<String> propertyNames = VmodlUtil.getProperties(vmodlType, 4, vmodlTypeMap, vmodlVersion);
      QuerySchema.ModelInfo queryModelInfo = convertPropertyNamesToModelInfo(propertyNames);
      if (useTypeClassNames) {
        models.put(vmodlType.getTypeClass().getSimpleName(), queryModelInfo);
        continue;
      } 
      models.put(vmodlType.getWsdlName(), queryModelInfo);
    } 
    return models;
  }
  
  public static QuerySchema.ModelInfo convertPropertyNamesToModelInfo(List<String> propertyNames) {
    Map<String, QuerySchema.PropertyInfo> modelInfoProperties = new TreeMap<>();
    for (String propertyName : propertyNames)
      modelInfoProperties.put(propertyName, QuerySchema.PropertyInfo.forNonFilterableProperty()); 
    return new QuerySchema.ModelInfo(modelInfoProperties);
  }
  
  private static QuerySchema.ModelInfo convertManagedObjectMethodsToModelInfo(List<ManagedMethod> managedObjectMethods) {
    Map<String, QuerySchema.PropertyInfo> modelInfoProperties = new HashMap<>();
    for (ManagedMethod managedMethod : managedObjectMethods) {
      boolean isMethodSuitableForModelProperty = (managedMethod.getResult() != null && (managedMethod.getParameters()).length == 0);
      if (isMethodSuitableForModelProperty) {
        String methodName = managedMethod.getName();
        String methodQueryPropery = MethodNameToQueryPropertyConverter.toMethodQueryProperty(methodName);
        modelInfoProperties.put(methodQueryPropery, QuerySchema.PropertyInfo.forNonFilterableProperty());
      } 
    } 
    return new QuerySchema.ModelInfo(modelInfoProperties);
  }
  
  public static QuerySchema.ModelInfo convertVmodlClassesPropertiesToModelInfo(List<Class<? extends DataObject>> vmodlClasses, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    return convertVmodlClassesPropertiesToModelInfo(vmodlClasses, null, vmodlTypeMap, vmodlVersion);
  }
  
  public static QuerySchema.ModelInfo convertVmodlClassesPropertiesToModelInfo(List<Class<? extends DataObject>> vmodlClasses, String parentProperty, VmodlTypeMap vmodlTypeMap, VmodlVersion vmodlVersion) {
    Set<String> modelInfoProperties = new LinkedHashSet<>();
    for (Class<?> vmodlClass : vmodlClasses) {
      List<String> vmodlClassProperties = VmodlUtil.getProperties(vmodlTypeMap
          .getLoadedVmodlType(vmodlClass), parentProperty, vmodlTypeMap, vmodlVersion);
      modelInfoProperties.addAll(vmodlClassProperties);
    } 
    QuerySchema.ModelInfo modelInfo = convertPropertyNamesToModelInfo(new ArrayList<>(modelInfoProperties));
    return modelInfo;
  }
}
