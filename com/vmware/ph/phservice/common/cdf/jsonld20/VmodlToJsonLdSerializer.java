package com.vmware.ph.phservice.common.cdf.jsonld20;

import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import com.vmware.ph.phservice.common.internal.DateUtil;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.ComplexTypeField;
import com.vmware.vim.vmomi.core.types.DataObjectType;
import com.vmware.vim.vmomi.core.types.VmodlArrayType;
import com.vmware.vim.vmomi.core.types.VmodlEnumType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class VmodlToJsonLdSerializer {
  public static final String TYPE_ATTRIBUTE = "@type";
  
  public static final String ID_ATTRIBUTE = "@id";
  
  public static final String MO_ID_ATTRIBUTE = "moId";
  
  public static final String NAME_ATTRIBUTE = "name";
  
  private static final String VALUE_ATTRIBUTE = "@value";
  
  private static final String DECLARED_TYPE_ATTRIBUTE = "pa__declared_type";
  
  private static final String DATETIME_TYPE_VAL = "datetime";
  
  private static final String DO_DYNAMIC_TYPE_PROP = "dynamicType";
  
  private static final String DO_DYNAMIC_PROPERTY_PROP = "dynamicProperty";
  
  private final VmodlTypeResolver _vmodlTypeResolver;
  
  private final MoRefSerializer _moRefSerializer;
  
  private final boolean _useWsdlTypes;
  
  private boolean _serializeCalendarAsObject = false;
  
  private boolean _serializeCalendarInSeconds = false;
  
  private boolean _serializeNullValues = false;
  
  public VmodlToJsonLdSerializer(VmodlTypeMap vmodlTypeMap) {
    this(vmodlTypeMap, true, null);
  }
  
  public VmodlToJsonLdSerializer(VmodlTypeMap vmodlTypeMap, List<String> vmodlPackages) {
    this(vmodlTypeMap, false, vmodlPackages);
  }
  
  private VmodlToJsonLdSerializer(VmodlTypeMap vmodlTypeMap, boolean useWsdlTypes, List<String> vmodlPackages) {
    this._vmodlTypeResolver = new VmodlTypeResolver(vmodlTypeMap, vmodlPackages);
    this._moRefSerializer = new MoRefSerializer(this._vmodlTypeResolver, useWsdlTypes);
    this._useWsdlTypes = useWsdlTypes;
  }
  
  public void setSerializeCalendarAsObject(boolean serializeAsObject) {
    this._serializeCalendarAsObject = serializeAsObject;
  }
  
  public void setSerializeCalendarInSeconds(boolean serializeCalendarInSeconds) {
    this._serializeCalendarInSeconds = serializeCalendarInSeconds;
  }
  
  public void setSerializeNullValues(boolean serializeNullValues) {
    this._serializeNullValues = serializeNullValues;
  }
  
  public Object serialize(Object vmodlObject) {
    return serialize(vmodlObject, null);
  }
  
  public JSONObject serialize(ManagedObjectReference moRef, Map<String, Object> propertyPathsToValue, char pathSeparator) {
    JSONObject result = serializeManagedObject(moRef);
    serializeProperties(result, propertyPathsToValue, pathSeparator);
    return result;
  }
  
  public JSONObject serialize(String type, String id, Map<String, Object> propertyPathsToValue, char pathSeparator) {
    JSONObject result = new JSONObject();
    result.put("@type", type);
    result.put("@id", id);
    serializeProperties(result, propertyPathsToValue, pathSeparator);
    return result;
  }
  
  public Object serialize(Object vmodlObject, VmodlType declaredType) {
    if (vmodlObject == null)
      return null; 
    if (vmodlObject.getClass().isArray())
      return serializeArray(vmodlObject, declaredType); 
    if (vmodlObject instanceof DataObject)
      return serializeDataObject((DataObject)vmodlObject, declaredType); 
    if (vmodlObject instanceof ManagedObjectReference)
      return serializeMoRef((ManagedObjectReference)vmodlObject); 
    if (vmodlObject instanceof ManagedObject)
      return serializeManagedObject(((ManagedObject)vmodlObject)._getRef()); 
    if (vmodlObject instanceof Calendar) {
      Calendar calendar = (Calendar)vmodlObject;
      if (this._serializeCalendarAsObject)
        return serializeCalendarAsObject(calendar); 
      if (this._serializeCalendarInSeconds)
        return Long.valueOf(calendar.getTimeInMillis() / 1000L); 
      return Long.valueOf(calendar.getTimeInMillis());
    } 
    return vmodlObject;
  }
  
  public Object deserialize(Object jsonObject) {
    return deserialize(jsonObject, null);
  }
  
  public Object deserialize(Object json, VmodlType declaredType) {
    if (json == null || json.equals(JSONObject.NULL))
      return null; 
    if (json instanceof JSONArray)
      return deserializeArray((JSONArray)json, declaredType); 
    if (json instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject)json;
      String objectTypeName = jsonObject.getString("@type");
      if ("datetime".equals(objectTypeName)) {
        long timeStampValue = convertToMillisIfNecessary(jsonObject.getLong("@value"));
        Calendar calendar = DateUtil.createUtcCalendar();
        calendar.setTimeInMillis(timeStampValue);
        return calendar;
      } 
      VmodlType vmodlType = getVmodlType(objectTypeName);
      VmodlType.Kind vmodlTypeKind = vmodlType.getKind();
      if (vmodlTypeKind == VmodlType.Kind.DATA_OBJECT)
        return deserializeDataObject(jsonObject, declaredType); 
      if (vmodlTypeKind == VmodlType.Kind.MOREF)
        return deserializeMoRef(jsonObject); 
      if (vmodlTypeKind == VmodlType.Kind.MANAGED_OBJECT)
        return deserializeManagedObject(jsonObject); 
    } 
    if (declaredType != null) {
      if (declaredType.getKind() == VmodlType.Kind.ENUM)
        return ((VmodlEnumType)declaredType).getEnum(json.toString()); 
      if (declaredType.getKind() == VmodlType.Kind.DATETIME) {
        Calendar calendar = DateUtil.createUtcCalendar();
        calendar.setTimeInMillis(((Long)json).longValue());
        return calendar;
      } 
    } 
    return json;
  }
  
  private long convertToMillisIfNecessary(long timeStampValue) {
    int minDigitsForTimestampInMillis = 12;
    if (String.valueOf(timeStampValue).length() < 12)
      timeStampValue *= 1000L; 
    return timeStampValue;
  }
  
  private JSONArray serializeArray(Object vmodlObject, VmodlType declaredType) {
    assert vmodlObject.getClass().isArray();
    Object[] vmodlArray = convertObjectToObjectArray(vmodlObject);
    VmodlType arrElementType = null;
    if (declaredType != null && declaredType instanceof VmodlArrayType) {
      VmodlArrayType arrType = (VmodlArrayType)declaredType;
      arrElementType = arrType.getComponentType();
    } 
    JSONArray jsonArray = new JSONArray();
    for (Object vmodlObjectInArray : vmodlArray) {
      Object serializedObject = serialize(vmodlObjectInArray, arrElementType);
      jsonArray.put(serializedObject);
    } 
    return jsonArray;
  }
  
  private JSONObject serializeDataObject(DataObject dataObject, VmodlType declaredType) {
    JSONObject j = new JSONObject();
    VmodlType vmodlType = getDataObjectVmodlType(dataObject, declaredType, this._vmodlTypeResolver);
    String objectTypeName = getObjectTypeName(vmodlType);
    j.put("@type", objectTypeName);
    if (null != declaredType) {
      String declaredInParentAs = getObjectTypeName(declaredType);
      if (!objectTypeName.equals(declaredInParentAs))
        j.put("pa__declared_type", declaredInParentAs); 
    } 
    ComplexTypeField[] properties = ((DataObjectType)vmodlType).getProperties();
    for (ComplexTypeField property : properties) {
      if (!"dynamicType".equals(property.getName()) && 
        !"dynamicProperty".equals(property.getName())) {
        String fieldName = property.getName();
        Object fieldValue = property.get(dataObject);
        VmodlType fieldType = property.getType();
        if (fieldValue == null && this._serializeNullValues) {
          j.put(fieldName, JSONObject.NULL);
        } else {
          Object serializedValue = serialize(fieldValue, fieldType);
          j.put(fieldName, serializedValue);
        } 
      } 
    } 
    return j;
  }
  
  private static VmodlType getDataObjectVmodlType(DataObject dataObject, VmodlType declaredType, VmodlTypeResolver typeResolver) {
    Class<? extends DataObject> dataObjectActualClass = (Class)dataObject.getClass();
    VmodlType vmodlType = typeResolver.getVmodlTypeForClass(dataObjectActualClass);
    if (vmodlType == null) {
      Class<?>[] interfaces = dataObjectActualClass.getInterfaces();
      if (interfaces != null && interfaces.length == 1) {
        Class<?> interfaceClass = interfaces[0];
        vmodlType = typeResolver.getVmodlTypeForClass(interfaceClass);
      } 
    } 
    if (null != declaredType) {
      Class<?> declaredClass = declaredType.getTypeClass();
      if (!declaredClass.isAssignableFrom(dataObjectActualClass))
        throw new Bug("I was expecting " + dataObjectActualClass + " to be equal to, or child of " + declaredClass); 
    } 
    return (vmodlType != null) ? vmodlType : declaredType;
  }
  
  private JSONObject serializeMoRef(ManagedObjectReference moRef) {
    JSONObject jAttribute = new JSONObject();
    VmodlType vmodlType = this._vmodlTypeResolver.getVmodlTypeForClass(ManagedObjectReference.class);
    String typeName = getObjectTypeName(vmodlType);
    jAttribute.put("@type", typeName);
    String id = this._moRefSerializer.serializeMoRefToString(moRef);
    jAttribute.put("@id", id);
    return jAttribute;
  }
  
  private JSONObject serializeManagedObject(ManagedObjectReference moRef) {
    JSONObject jAttribute = new JSONObject();
    VmodlType vmodlType = this._vmodlTypeResolver.getVmodlTypeForWsdlName(moRef.getType());
    String typeName = getObjectTypeName(vmodlType);
    jAttribute.put("@type", typeName);
    String id = this._moRefSerializer.serializeMoRefToString(moRef);
    jAttribute.put("@id", id);
    return jAttribute;
  }
  
  private JSONObject serializeCalendarAsObject(Calendar calendar) {
    long calendarTime = this._serializeCalendarInSeconds ? TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis()) : calendar.getTimeInMillis();
    JSONObject result = new JSONObject();
    result.put("@type", "datetime");
    result.put("@value", calendarTime);
    return result;
  }
  
  Object deserializeArray(JSONArray jsonArray, VmodlType declaredType) {
    if (jsonArray == null || jsonArray.length() == 0)
      return null; 
    List<Object> deserializedObjects = new ArrayList();
    Iterator<Object> objects = jsonArray.iterator();
    while (objects.hasNext())
      deserializedObjects.add(deserialize(objects.next())); 
    Object firstObject = jsonArray.get(0);
    Class<?> resultClass = null;
    boolean isPrimitive = false;
    if (firstObject instanceof JSONObject) {
      JSONObject firstJsonObject = (JSONObject)firstObject;
      String objectTypeName = firstJsonObject.getString("@type");
      if (declaredType != null && declaredType instanceof VmodlArrayType) {
        VmodlArrayType arrType = (VmodlArrayType)declaredType;
        VmodlType arrElementType = arrType.getComponentType();
        resultClass = arrElementType.getTypeClass();
      } else {
        resultClass = getVmodlType(objectTypeName).getTypeClass();
      } 
    } else {
      if (declaredType != null && declaredType instanceof VmodlArrayType) {
        VmodlArrayType arrType = (VmodlArrayType)declaredType;
        VmodlType arrElementType = arrType.getComponentType();
        resultClass = arrElementType.getTypeClass();
      } else {
        resultClass = firstObject.getClass();
      } 
      isPrimitive = (ClassUtils.wrapperToPrimitive(resultClass) != null);
    } 
    Object arrayObjectToSet = convertObjectArrayToClazzArray(deserializedObjects
        .toArray(), resultClass);
    if (isPrimitive)
      arrayObjectToSet = convertBoxedArrayToPrimitivesArray(arrayObjectToSet); 
    return arrayObjectToSet;
  }
  
  DataObject deserializeDataObject(JSONObject jsonObject, VmodlType declaredType) {
    String objectTypeName = jsonObject.getString("@type");
    VmodlType vmodlType = getVmodlType(objectTypeName);
    DataObject dataObject = (DataObject)((DataObjectType)vmodlType).newInstance();
    ComplexTypeField[] properties = ((DataObjectType)vmodlType).getProperties();
    for (ComplexTypeField property : properties) {
      String fieldName = property.getName();
      if (!"dynamicType".equals(property.getName()))
        if (jsonObject.has(fieldName)) {
          Object fieldValue = jsonObject.get(fieldName);
          Object deserializedValue = deserialize(fieldValue, property.getType());
          if (property.getType().getKind() == VmodlType.Kind.LONG) {
            property.set(dataObject, Long.valueOf(deserializedValue.toString()));
          } else if (property.getType().getKind() == VmodlType.Kind.DOUBLE) {
            property.set(dataObject, Double.valueOf(deserializedValue.toString()));
          } else {
            property.set(dataObject, deserializedValue);
          } 
        }  
    } 
    return dataObject;
  }
  
  ManagedObjectReference deserializeMoRef(JSONObject jsonObject) {
    String id, objectTypeName;
    if (jsonObject.has("@id")) {
      id = jsonObject.getString("@id");
      objectTypeName = MoRefSerializer.getTypeNameFromMoRefString(id);
    } else {
      id = jsonObject.getString("moId");
      objectTypeName = jsonObject.optString("@type");
    } 
    VmodlType vmodlType = getVmodlType(objectTypeName);
    return this._moRefSerializer.deserializeMoRefFromString(id, vmodlType);
  }
  
  ManagedObjectReference deserializeManagedObject(JSONObject jsonObject) {
    return deserializeMoRef(jsonObject);
  }
  
  void serializeProperties(JSONObject rootObject, Map<String, Object> propertyPathsToValue, char pathSeparator) {
    for (Map.Entry<String, Object> entry : propertyPathsToValue.entrySet()) {
      String propertyPath = entry.getKey();
      Object propertyValue = entry.getValue();
      JSONObject holder = JSONObjectUtil.getOrCreateHolder(rootObject, propertyPath, pathSeparator);
      int lastIndexOf = propertyPath.lastIndexOf(pathSeparator);
      String propertyName = (lastIndexOf == -1) ? propertyPath : propertyPath.substring(lastIndexOf + 1);
      Object serializedPropertyValue = serialize(propertyValue);
      holder.put(propertyName, serializedPropertyValue);
    } 
  }
  
  static Object[] convertObjectToObjectArray(Object array) {
    Character[] arrayOfCharacter;
    Object[] arrO = new Object[0];
    try {
      arrO = (Object[])array;
    } catch (ClassCastException e) {
      if ((new short[0]).getClass().equals(array.getClass())) {
        Short[] arrayOfShort = ArrayUtils.toObject((short[])array);
      } else if ((new int[0]).getClass().equals(array.getClass())) {
        Integer[] arrayOfInteger = ArrayUtils.toObject((int[])array);
      } else if ((new long[0]).getClass().equals(array.getClass())) {
        Long[] arrayOfLong = ArrayUtils.toObject((long[])array);
      } else if ((new float[0]).getClass().equals(array.getClass())) {
        Float[] arrayOfFloat = ArrayUtils.toObject((float[])array);
      } else if ((new double[0]).getClass().equals(array.getClass())) {
        Double[] arrayOfDouble = ArrayUtils.toObject((double[])array);
      } else if ((new boolean[0]).getClass().equals(array.getClass())) {
        Boolean[] arrayOfBoolean = ArrayUtils.toObject((boolean[])array);
      } else if ((new byte[0]).getClass().equals(array.getClass())) {
        Byte[] arrayOfByte = ArrayUtils.toObject((byte[])array);
      } else if ((new char[0]).getClass().equals(array.getClass())) {
        arrayOfCharacter = ArrayUtils.toObject((char[])array);
      } else {
        throw new Bug("ARRAY serialization for class " + array
            .getClass() + " is not implemented. The following data will not be serialized (Is it an array?): " + array);
      } 
    } 
    return (Object[])arrayOfCharacter;
  }
  
  static Object convertBoxedArrayToPrimitivesArray(Object array) {
    Object arrO;
    if ((new Short[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Short[])array);
    } else if ((new Integer[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Integer[])array);
    } else if ((new Long[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Long[])array);
    } else if ((new Float[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Float[])array);
    } else if ((new Double[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Double[])array);
    } else if ((new Boolean[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Boolean[])array);
    } else if ((new Byte[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Byte[])array);
    } else if ((new Character[0]).getClass().equals(array.getClass())) {
      arrO = ArrayUtils.toPrimitive((Character[])array);
    } else if ((new String[0]).getClass().equals(array.getClass())) {
      arrO = array;
    } else {
      throw new Bug("ARRAY deserialization for class " + array
          .getClass() + " is not implemented. The following data will not be deserialized (Is it an array?): " + array);
    } 
    return arrO;
  }
  
  private static Object convertObjectArrayToClazzArray(Object[] array, Class<?> clazz) {
    Object newArray = Array.newInstance(clazz, array.length);
    for (int i = 0; i < array.length; i++)
      Array.set(newArray, i, array[i]); 
    return newArray;
  }
  
  private String getObjectTypeName(VmodlType objectVmodlType) {
    if (this._useWsdlTypes)
      return this._vmodlTypeResolver.getWsdlNameForVmodlType(objectVmodlType); 
    return this._vmodlTypeResolver.getPackageNameForVmodlType(objectVmodlType);
  }
  
  private VmodlType getVmodlType(String objectTypeName) {
    VmodlType vmodlType = null;
    if (this._useWsdlTypes) {
      vmodlType = this._vmodlTypeResolver.getVmodlTypeForWsdlName(objectTypeName);
    } else {
      vmodlType = this._vmodlTypeResolver.getVmodlTypeForPackageName(objectTypeName);
    } 
    return vmodlType;
  }
}
