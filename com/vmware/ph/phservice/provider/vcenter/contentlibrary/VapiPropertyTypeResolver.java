package com.vmware.ph.phservice.provider.vcenter.contentlibrary;

import com.vmware.vapi.bindings.type.ListType;
import com.vmware.vapi.bindings.type.OptionalType;
import com.vmware.vapi.bindings.type.SetType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.bindings.type.TypeReference;
import org.apache.commons.lang.Validate;

public class VapiPropertyTypeResolver {
  public static Type resolvePropertyType(StructType structType, String propertyPath) {
    Validate.notNull(structType);
    Validate.notEmpty(propertyPath);
    String[] namesTokens = propertyPath.split(
        Character.toString('/'));
    if (namesTokens.length == 0)
      throw new IllegalArgumentException(
          String.format("Invalid property path '%s': contains no simple property names.", new Object[] { propertyPath })); 
    StructType typeOfContainingStruct = structType;
    int indexOfLastFieldName = namesTokens.length - 1;
    for (int i = 0; i < indexOfLastFieldName; i++) {
      String fieldName = namesTokens[i];
      typeOfContainingStruct = resolveStructTypeOfFieldBySimpleName(typeOfContainingStruct, fieldName);
      if (typeOfContainingStruct == null)
        return null; 
    } 
    String lastFieldName = namesTokens[indexOfLastFieldName];
    Type result = resolveTypeOfFieldBySimpleName(typeOfContainingStruct, lastFieldName);
    if (result instanceof TypeReference) {
      TypeReference<?> ref = (TypeReference)result;
      result = ref.resolve();
    } 
    return result;
  }
  
  private static Type resolveTypeOfFieldBySimpleName(StructType typeOfContainingStruct, String fieldName) {
    Type fieldType = typeOfContainingStruct.getFieldByJavaName(fieldName);
    if (fieldType == null)
      return null; 
    return fieldType;
  }
  
  private static StructType resolveStructTypeOfFieldBySimpleName(StructType typeOfContainingStruct, String fieldName) {
    Type fieldType = resolveTypeOfFieldBySimpleName(typeOfContainingStruct, fieldName);
    if (fieldType == null)
      return null; 
    StructType result = null;
    while (result == null) {
      if (fieldType instanceof StructType) {
        result = (StructType)fieldType;
        continue;
      } 
      if (fieldType instanceof OptionalType) {
        OptionalType optionalType = (OptionalType)fieldType;
        fieldType = optionalType.getElementType();
        continue;
      } 
      if (fieldType instanceof ListType) {
        ListType listType = (ListType)fieldType;
        fieldType = listType.getElementType();
        continue;
      } 
      if (fieldType instanceof SetType) {
        SetType setType = (SetType)fieldType;
        fieldType = setType.getElementType();
        continue;
      } 
      if (fieldType instanceof TypeReference) {
        TypeReference<?> ref = (TypeReference)fieldType;
        fieldType = ref.resolve();
        continue;
      } 
      throw new IllegalArgumentException(
          String.format("Field '%s' of structure '%s' cannot be resolved to a structure type", new Object[] { fieldName, typeOfContainingStruct.getName() }));
    } 
    return result;
  }
}
