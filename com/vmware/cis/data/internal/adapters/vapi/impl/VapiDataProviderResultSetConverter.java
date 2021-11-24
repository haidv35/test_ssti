package com.vmware.cis.data.internal.adapters.vapi.impl;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.provider.vapi.ResourceModelTypes;
import com.vmware.vapi.data.DataValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

final class VapiDataProviderResultSetConverter {
  private final VapiPropertyValueConverter _propertyValueConverter;
  
  private final String _serverGuid;
  
  private final boolean _unqualifyProperties;
  
  VapiDataProviderResultSetConverter(VapiPropertyValueConverter propertyValueConverter, String serverGuid, boolean unqualifyProperties) {
    assert propertyValueConverter != null;
    assert serverGuid != null;
    this._propertyValueConverter = propertyValueConverter;
    this._serverGuid = serverGuid;
    this._unqualifyProperties = unqualifyProperties;
  }
  
  public ResultSet convertResultSet(ResourceModelTypes.ResultSet vapiResultSet, List<String> properties, int offset, int limit, boolean withTotalCount) {
    Validate.notNull(vapiResultSet);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    if (limit != 0)
      convertItems(resultBuilder, vapiResultSet.getItems(), vapiResultSet
          .getProperties(), properties); 
    Integer totalCount = getTotalCount(vapiResultSet, withTotalCount, 
        hasOffsetOrLimit(offset, limit));
    return resultBuilder
      .totalCount(totalCount)
      .build();
  }
  
  private static boolean hasOffsetOrLimit(int offset, int limit) {
    boolean hasOffset = (offset > 0);
    boolean hasLimit = (limit >= 0);
    return (hasOffset || hasLimit);
  }
  
  private static Integer getTotalCount(ResourceModelTypes.ResultSet vapiResultSet, boolean withTotalCount, boolean hasOffsetOrLimit) {
    if (withTotalCount) {
      if (vapiResultSet.getTotalCount() == null) {
        if (hasOffsetOrLimit)
          throw new IllegalArgumentException("Response does not contain total item count"); 
        return Integer.valueOf(vapiResultSet.getItems().size());
      } 
      return Integer.valueOf(vapiResultSet.getTotalCount().intValue());
    } 
    return null;
  }
  
  private void convertItems(ResultSet.Builder resultBuilder, List<ResourceModelTypes.ResourceItem> vapiItems, List<String> vapiProperties, List<String> queryProperties) {
    if (vapiItems == null)
      return; 
    int keyPosition = getKeyPosition(vapiProperties);
    int typePosition = vapiProperties.indexOf("@type");
    for (int i = 0; i < vapiItems.size(); i++) {
      ResourceModelTypes.ResourceItem vapiItem = vapiItems.get(i);
      DataValue vapiKey = vapiItem.getPropertyValues().get(keyPosition);
      DataValue vapiType = (typePosition == -1) ? null : vapiItem.getPropertyValues().get(typePosition);
      List<Object> item = convertItem(vapiItem, vapiType, vapiProperties, queryProperties);
      Object key = this._propertyValueConverter.fromVapiResultDataValue("@modelKey", vapiType, vapiKey, this._serverGuid);
      resultBuilder.item(key, item);
    } 
  }
  
  private static int getKeyPosition(List<String> properties) {
    int index = 0;
    for (String property : properties) {
      if (PropertyUtil.isModelKey(property))
        return index; 
      index++;
    } 
    throw new IllegalArgumentException("No key in the vapi properties.");
  }
  
  private List<Object> convertItem(ResourceModelTypes.ResourceItem vapiItem, DataValue vapiType, List<String> vapiProperties, List<String> queryProperties) {
    Validate.notNull(vapiProperties);
    if (vapiProperties.size() != vapiItem.getPropertyValues().size())
      throw new IllegalArgumentException(String.format("The number of property values in a vAPI ResourceItem does not match the number of properties in the vAPI ResultSet: %d != %d", new Object[] { Integer.valueOf(vapiItem.getPropertyValues().size()), Integer.valueOf(vapiProperties.size()) })); 
    Map<String, DataValue> vapiValueByProperty = toVapiValueByProperty(vapiItem
        .getPropertyValues(), vapiProperties);
    List<Object> values = new ArrayList(queryProperties.size());
    for (String property : queryProperties) {
      DataValue vapiValue = vapiValueByProperty.get(convertProperty(property));
      Object value = null;
      if (vapiValue != null)
        value = this._propertyValueConverter.fromVapiResultDataValue(property, vapiType, vapiValue, this._serverGuid); 
      values.add(value);
    } 
    return values;
  }
  
  private String convertProperty(String property) {
    if (this._unqualifyProperties)
      return unqualify(property); 
    return property;
  }
  
  private static String unqualify(String qualified) {
    assert qualified != null;
    if (PropertyUtil.isSpecialProperty(qualified))
      return qualified; 
    return QualifiedProperty.forQualifiedName(qualified).getSimpleProperty();
  }
  
  private Map<String, DataValue> toVapiValueByProperty(List<DataValue> vapiValues, List<String> vapiProperties) {
    assert vapiValues != null;
    assert vapiProperties != null;
    assert vapiValues.size() == vapiProperties.size();
    Map<String, DataValue> valueByProperty = new HashMap<>(vapiValues.size());
    Iterator<DataValue> valueIt = vapiValues.iterator();
    for (String vapiProperty : vapiProperties) {
      DataValue dataValue = valueIt.next();
      if (dataValue != null)
        valueByProperty.put(vapiProperty, dataValue); 
    } 
    return valueByProperty;
  }
}
