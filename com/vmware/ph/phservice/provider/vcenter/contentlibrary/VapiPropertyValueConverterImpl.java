package com.vmware.ph.phservice.provider.vcenter.contentlibrary;

import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.ph.phservice.common.vapi.VapiTypeProvider;
import com.vmware.ph.phservice.common.vapi.util.VapiUriSchemeUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.vapi.bindings.ApiEnumeration;
import com.vmware.vapi.bindings.type.OptionalType;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.bindings.type.Type;
import com.vmware.vapi.data.BlobValue;
import com.vmware.vapi.data.BooleanValue;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.DoubleValue;
import com.vmware.vapi.data.IntegerValue;
import com.vmware.vapi.data.SecretValue;
import com.vmware.vapi.data.StringValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vapi.internal.bindings.TypeConverter;
import com.vmware.vapi.internal.bindings.TypeConverterImpl;
import com.vmware.vapi.internal.bindings.convert.ConverterFactory;
import com.vmware.vapi.internal.bindings.convert.NameToTypeResolver;
import com.vmware.vapi.internal.bindings.convert.impl.DefaultConverterFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VapiPropertyValueConverterImpl implements VapiPropertyValueConverter {
  private final VapiTypeProvider _vapiTypeProvider;
  
  private TypeConverter _vapiTypeConverter;
  
  public VapiPropertyValueConverterImpl(VapiTypeProvider vapiTypeProvider) {
    this._vapiTypeProvider = vapiTypeProvider;
    initTypeConverter();
  }
  
  public Object fromVapiResultDataValue(String property, DataValue resourceTypeValue, DataValue dataValue, String serverGuid) {
    validateResourceType(resourceTypeValue, property);
    if (dataValue == null)
      return null; 
    Object vapiRsult = null;
    if (property.equals("@modelKey")) {
      validateStringValue(dataValue, property);
      vapiRsult = VapiUriSchemeUtil.createUri(((StringValue)resourceTypeValue).getValue(), ((StringValue)dataValue)
          .getValue());
    } else {
      StructType structType;
      Type propertyType = fixOptionals(getVapiTypeOfProperty(property), dataValue);
      if (propertyType == null && dataValue instanceof StructValue) {
        StructValue structValue = (StructValue)dataValue;
        structType = this._vapiTypeProvider.getResourceModelType(structValue.getName());
      } 
      if (structType != null) {
        vapiRsult = toBinding(dataValue, (Type)structType, property);
      } else {
        vapiRsult = fromUntypedDataValue(dataValue);
      } 
    } 
    return vapiRsult;
  }
  
  public DataValue toVapiComparableValue(String property, Object comparableValue) {
    if (comparableValue instanceof Integer) {
      Integer integerValue = (Integer)comparableValue;
      return (DataValue)new IntegerValue(integerValue.longValue());
    } 
    if (comparableValue instanceof Long)
      return (DataValue)new IntegerValue(((Long)comparableValue).longValue()); 
    if (comparableValue instanceof Boolean)
      return (DataValue)BooleanValue.getInstance(((Boolean)comparableValue).booleanValue()); 
    if (comparableValue instanceof String)
      return (DataValue)new StringValue((String)comparableValue); 
    if (comparableValue instanceof Float) {
      Float floatValue = (Float)comparableValue;
      return (DataValue)new DoubleValue(floatValue.doubleValue());
    } 
    if (comparableValue instanceof Double)
      return (DataValue)new DoubleValue(((Double)comparableValue).doubleValue()); 
    if (comparableValue instanceof ApiEnumeration)
      return (DataValue)new StringValue(((ApiEnumeration)comparableValue).name()); 
    if (comparableValue instanceof URI && 
      VapiUriSchemeUtil.isVapiUri((URI)comparableValue)) {
      URI uri = (URI)comparableValue;
      String key = VapiUriSchemeUtil.getId(uri);
      return (DataValue)new StringValue(key);
    } 
    throw new IllegalArgumentException(
        String.format("Unsupported comparable value of type %s: %s", new Object[] { comparableValue.getClass().getCanonicalName(), comparableValue }));
  }
  
  public List<DataValue> toVapiComparableList(String property, Collection<?> comparableCollection) {
    List<DataValue> vapiComparableList = new ArrayList<>(comparableCollection.size());
    for (Object comparableElement : comparableCollection)
      vapiComparableList.add(toVapiComparableValue(property, comparableElement)); 
    return vapiComparableList;
  }
  
  public boolean isTypeRequired() {
    return true;
  }
  
  private void initTypeConverter() {
    this._vapiTypeConverter = (TypeConverter)new TypeConverterImpl((ConverterFactory)new DefaultConverterFactory(new NameToTypeResolver() {
            public Type resolve(String name) {
              return (Type)VapiPropertyValueConverterImpl.this._vapiTypeProvider.getResourceModelType(name);
            }
          }));
  }
  
  private static void validateResourceType(DataValue resourceTypeValue, String property) {
    if (resourceTypeValue == null)
      throw new IllegalArgumentException("The resourceType of " + property + " property must not be null!"); 
    if (!(resourceTypeValue instanceof StringValue))
      throw new IllegalArgumentException(String.format("Resource type of property '%s' must be a StringValue and not %s: %s", new Object[] { property, resourceTypeValue
              
              .getClass().getCanonicalName(), resourceTypeValue })); 
  }
  
  private static void validateStringValue(DataValue value, String property) {
    if (!(value instanceof StringValue))
      throw new IllegalArgumentException(
          String.format("Property '%s' must be a StringValue and not %s: %s", new Object[] { property, value.getClass().getCanonicalName(), value })); 
  }
  
  private Object toBinding(DataValue dataValue, Type bindingType, String property) {
    try {
      return this._vapiTypeConverter.convertToJava(dataValue, bindingType);
    } catch (RuntimeException ex) {
      throw new IllegalStateException("Could not convert value of property " + property, ex);
    } 
  }
  
  private Object fromUntypedDataValue(DataValue dataValue) {
    if (dataValue instanceof StringValue)
      return ((StringValue)dataValue).getValue(); 
    if (dataValue instanceof IntegerValue)
      return Long.valueOf(((IntegerValue)dataValue).getValue()); 
    if (dataValue instanceof BooleanValue)
      return Boolean.valueOf(((BooleanValue)dataValue).getValue()); 
    if (dataValue instanceof SecretValue)
      return ((SecretValue)dataValue).getValue(); 
    if (dataValue instanceof DoubleValue)
      return Double.valueOf(((DoubleValue)dataValue).getValue()); 
    if (dataValue instanceof BlobValue)
      return ((BlobValue)dataValue).getValue(); 
    return dataValue;
  }
  
  private Type fixOptionals(Type bindingType, DataValue dataValue) {
    if (bindingType == null)
      return null; 
    if (bindingType instanceof OptionalType && !(dataValue instanceof com.vmware.vapi.data.OptionalValue))
      return ((OptionalType)bindingType).getElementType(); 
    return bindingType;
  }
  
  private Type getVapiTypeOfProperty(String property) {
    assert property != null;
    if (QuerySchemaUtil.isQueryPropertySpecialProperty(property))
      return null; 
    QualifiedProperty qualifiedProperty = QualifiedProperty.forQualifiedName(property);
    String modelCanonicalName = toCanonicalName(qualifiedProperty
        .getResourceModel());
    StructType modelType = this._vapiTypeProvider.getResourceModelType(modelCanonicalName);
    if (modelType == null)
      return null; 
    Type propertyType = VapiPropertyTypeResolver.resolvePropertyType(modelType, qualifiedProperty
        .getSimpleProperty());
    return propertyType;
  }
  
  private static String toCanonicalName(String mixedCaseName) {
    StringBuilder canonical = new StringBuilder();
    char prev = Character.MIN_VALUE;
    for (int i = 0; i < mixedCaseName.length(); i++) {
      char ch = mixedCaseName.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (Character.isLowerCase(prev))
          canonical.append('_'); 
        canonical.append(Character.toLowerCase(ch));
      } else {
        canonical.append(ch);
      } 
      prev = ch;
    } 
    return canonical.toString();
  }
}
