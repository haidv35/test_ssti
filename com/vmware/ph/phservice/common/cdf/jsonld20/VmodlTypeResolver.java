package com.vmware.ph.phservice.common.cdf.jsonld20;

import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VmodlTypeResolver {
  private static final String VMODL_CORE_TYPE_PACKAGE = "com.vmware.vim.binding.vmodl";
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final List<String> _vmodlPackages;
  
  public VmodlTypeResolver(VmodlTypeMap vmodlTypeMap) {
    this(vmodlTypeMap, null);
  }
  
  public VmodlTypeResolver(VmodlTypeMap vmodlTypeMap, List<String> vmodlPackages) {
    this
      ._vmodlTypeMap = Objects.<VmodlTypeMap>requireNonNull(vmodlTypeMap, "The VMODL type map must be specified!");
    this._vmodlPackages = new ArrayList<>();
    if (vmodlPackages != null)
      this._vmodlPackages.addAll(vmodlPackages); 
    if (!this._vmodlPackages.contains("com.vmware.vim.binding.vmodl"))
      this._vmodlPackages.add("com.vmware.vim.binding.vmodl"); 
  }
  
  public VmodlType getVmodlTypeForWsdlName(String wsdlTypeName) {
    if (wsdlTypeName == null)
      return null; 
    VmodlType vmodlType = this._vmodlTypeMap.getVmodlType(wsdlTypeName);
    return vmodlType;
  }
  
  public VmodlType getVmodlTypeForPackageName(String packageTypeName) {
    if (packageTypeName == null)
      return null; 
    packageTypeName = convertPackageTypeNameToValidClassName(packageTypeName);
    for (String vmodlPackage : this._vmodlPackages) {
      String vmodlPackagePrefix = getVmodlPackagePrefix(vmodlPackage);
      String vmodlRawPackage = vmodlPackage.substring(vmodlPackagePrefix.length());
      if (vmodlRawPackage.startsWith("."))
        vmodlRawPackage = vmodlRawPackage.substring(1); 
      if (packageTypeName.startsWith(vmodlRawPackage)) {
        String inputVmodlClassName = vmodlPackagePrefix + "." + packageTypeName;
        try {
          Class<?> vmodlTypeClass = Class.forName(inputVmodlClassName);
          VmodlType result = this._vmodlTypeMap.getVmodlType(vmodlTypeClass);
          if (result != null)
            return result; 
        } catch (ClassNotFoundException classNotFoundException) {}
      } 
    } 
    return null;
  }
  
  public VmodlType getVmodlTypeForClass(Class<?> clazz) {
    if (clazz == null)
      return null; 
    VmodlType vmodlType = this._vmodlTypeMap.getVmodlType(clazz);
    return vmodlType;
  }
  
  public String getPackageNameForVmodlType(VmodlType vmodlType) {
    if (vmodlType == null)
      return null; 
    String className = vmodlType.getTypeName().getName();
    String packageName = null;
    for (String vmodlPackage : this._vmodlPackages) {
      if (className.startsWith(vmodlPackage)) {
        String vmodlPackagePrefix = getVmodlPackagePrefix(vmodlPackage);
        String rawClassName = className.substring(vmodlPackagePrefix.length() + 1);
        packageName = rawClassName.replaceAll("\\$", ".");
      } 
    } 
    return packageName;
  }
  
  public String getWsdlNameForVmodlType(VmodlType vmodlType) {
    if (vmodlType == null)
      return null; 
    return vmodlType.getWsdlName();
  }
  
  public boolean isTypeNameWsdlName(String typeName) {
    if (typeName == null)
      return false; 
    return (this._vmodlTypeMap.getVmodlType(typeName) != null);
  }
  
  public boolean isTypeNamePackageName(String typeName) {
    if (typeName == null)
      return false; 
    return (getVmodlTypeForPackageName(typeName) != null);
  }
  
  private static String getVmodlPackagePrefix(String vmodlPackage) {
    String vmodlPackagePrefix = vmodlPackage.replaceFirst("[.]binding.*", ".binding");
    return vmodlPackagePrefix;
  }
  
  private static String convertPackageTypeNameToValidClassName(String packageTypeName) {
    String[] packageTypeNameSegments = packageTypeName.split("\\.");
    StringBuilder packageTypeNameAsClassBuilder = new StringBuilder();
    boolean isClassNameFound = false;
    for (String segment : packageTypeNameSegments) {
      if (packageTypeNameAsClassBuilder.length() > 0)
        if (isClassNameFound) {
          packageTypeNameAsClassBuilder.append("$");
        } else {
          packageTypeNameAsClassBuilder.append(".");
        }  
      packageTypeNameAsClassBuilder.append(segment);
      if (Character.isUpperCase(segment.codePointAt(0)))
        isClassNameFound = true; 
    } 
    return packageTypeNameAsClassBuilder.toString();
  }
}
