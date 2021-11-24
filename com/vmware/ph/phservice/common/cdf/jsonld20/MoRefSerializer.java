package com.vmware.ph.phservice.common.cdf.jsonld20;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.List;

public class MoRefSerializer {
  private final VmodlTypeResolver _vmodlTypeResolver;
  
  private final boolean _useWsdlTypes;
  
  public MoRefSerializer(VmodlTypeMap vmodlTypeMap, List<String> vmodlPackages, boolean useWsdlTypes) {
    this(new VmodlTypeResolver(vmodlTypeMap, vmodlPackages), useWsdlTypes);
  }
  
  public MoRefSerializer(VmodlTypeResolver vmodlTypeResolver, boolean useWsdlTypes) {
    this._vmodlTypeResolver = vmodlTypeResolver;
    this._useWsdlTypes = useWsdlTypes;
  }
  
  public String serializeMoRefToString(ManagedObjectReference moRef) {
    String moRefTypeName = moRef.getType();
    if (!this._useWsdlTypes) {
      VmodlType wsdlNameVmodlType = this._vmodlTypeResolver.getVmodlTypeForWsdlName(moRefTypeName);
      moRefTypeName = this._vmodlTypeResolver.getPackageNameForVmodlType(wsdlNameVmodlType);
    } 
    String moRefString = moRefTypeName + ":" + moRef.getValue() + ":" + moRef.getServerGuid();
    return moRefString;
  }
  
  public ManagedObjectReference deserializeMoRefFromString(String moRefString, VmodlType vmodlType) {
    String[] moRefComponents = getMoRefComponentsFromMoRefString(moRefString);
    if (moRefComponents.length == 0)
      throw new IllegalArgumentException("The ManagedObjectReference string must not be empty."); 
    String moRefType = null;
    if (vmodlType != null)
      moRefType = this._vmodlTypeResolver.getWsdlNameForVmodlType(vmodlType); 
    String moRefId = null;
    String serverGuid = null;
    if (moRefComponents.length == 1) {
      moRefId = moRefComponents[0];
    } else {
      if (moRefType == null) {
        moRefType = moRefComponents[0];
        VmodlType packageNameVmodlType = this._vmodlTypeResolver.getVmodlTypeForPackageName(moRefType);
        if (packageNameVmodlType != null)
          moRefType = this._vmodlTypeResolver.getWsdlNameForVmodlType(packageNameVmodlType); 
      } 
      moRefId = moRefComponents[1];
      if (moRefComponents.length > 2 && !moRefComponents[2].equals("null"))
        serverGuid = moRefComponents[2]; 
    } 
    if (moRefType == null)
      throw new IllegalArgumentException("Missing valid ManagedObjectReference type name."); 
    ManagedObjectReference moRef = new ManagedObjectReference(moRefType, moRefId, serverGuid);
    return moRef;
  }
  
  public static String getTypeNameFromMoRefString(String moRefString) {
    String[] moRefComponents = getMoRefComponentsFromMoRefString(moRefString);
    String typeName = null;
    if (moRefComponents.length > 1)
      typeName = moRefComponents[0]; 
    return typeName;
  }
  
  private static String[] getMoRefComponentsFromMoRefString(String moRefString) {
    String[] moRefComponents = moRefString.split(":");
    return moRefComponents;
  }
}
