package com.vmware.cis.data.internal.adapters.vapi.impl;

import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.ph.phservice.common.vapi.util.VapiResultUtil;
import com.vmware.vapi.data.BooleanValue;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.DoubleValue;
import com.vmware.vapi.data.IntegerValue;
import com.vmware.vapi.data.StringValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DefaultVapiPropertyValueConverter implements VapiPropertyValueConverter {
  public Object fromVapiResultDataValue(String property, DataValue resourceTypeValue, DataValue dataValue, String serverGuid) {
    assert property != null;
    assert dataValue != null;
    if (PropertyUtil.isModelKey(property)) {
      String resourceName = (resourceTypeValue != null) ? resourceTypeValue.toString() : "";
      if (resourceName.indexOf('.') != -1)
        return VapiResultUtil.createModelKey(resourceName, dataValue, serverGuid); 
    } 
    return VapiValueConverter.fromDataValue(dataValue);
  }
  
  public DataValue toVapiComparableValue(String property, Object comparableValue) {
    assert property != null;
    assert comparableValue != null;
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
    throw new IllegalArgumentException("Unsupported type " + comparableValue.getClass());
  }
  
  public List<DataValue> toVapiComparableList(String property, Collection<?> comparableCollection) {
    assert property != null;
    assert comparableCollection != null;
    List<DataValue> vapiComparableList = new ArrayList<>(comparableCollection.size());
    for (Object comparableElement : comparableCollection)
      vapiComparableList.add(toVapiComparableValue(property, comparableElement)); 
    return vapiComparableList;
  }
  
  public boolean isTypeRequired() {
    return false;
  }
}
