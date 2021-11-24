package com.vmware.cis.data.internal.adapters.vapi;

import com.vmware.vapi.data.DataValue;
import java.util.Collection;
import java.util.List;

public interface VapiPropertyValueConverter {
  Object fromVapiResultDataValue(String paramString1, DataValue paramDataValue1, DataValue paramDataValue2, String paramString2);
  
  DataValue toVapiComparableValue(String paramString, Object paramObject);
  
  List<DataValue> toVapiComparableList(String paramString, Collection<?> paramCollection);
  
  boolean isTypeRequired();
}
