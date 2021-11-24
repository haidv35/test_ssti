package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.cis.data.ResourceModelTypes;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StringValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class DsVapiResultSetConverter {
  private final VapiPropertyValueConverter _propertyValueConverter;
  
  private String _applianceId = null;
  
  DsVapiResultSetConverter(VapiPropertyValueConverter propertyValueConverter) {
    assert propertyValueConverter != null;
    this._propertyValueConverter = propertyValueConverter;
  }
  
  ResultSet convertResultSet(ResourceModelTypes.ResultSet vapiResultSet, Query query) {
    assert vapiResultSet != null;
    assert query != null;
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(query.getProperties());
    if (vapiResultSet.getTotalCount() != null)
      resultBuilder.totalCount(Integer.valueOf(vapiResultSet.getTotalCount().intValue())); 
    if (!query.getWithTotalCount())
      convertItems(resultBuilder, vapiResultSet
          
          .getItems(), vapiResultSet
          .getProperties(), query); 
    return resultBuilder.build();
  }
  
  private void convertItems(ResultSet.Builder resultBuilder, List<ResourceModelTypes.ResourceItem> vapiItems, List<String> vapiProperties, Query query) {
    if (vapiItems == null || vapiItems.isEmpty())
      return; 
    List<String> queryProperties = query.getProperties();
    int keyPosition = getKeyPosition(vapiProperties);
    int typePosition = vapiProperties.indexOf("@type");
    for (ResourceModelTypes.ResourceItem vapiItem : vapiItems) {
      StringValue stringValue;
      DataValue vapiKey = vapiItem.getPropertyValues().get(keyPosition);
      DataValue vapiType = (typePosition == -1) ? null : vapiItem.getPropertyValues().get(typePosition);
      if (vapiType == null) {
        String resourceName = query.getResourceModels().iterator().next();
        stringValue = new StringValue(resourceName);
      } 
      List<Object> item = convertItem(vapiItem, (DataValue)stringValue, vapiProperties, queryProperties);
      Object key = this._propertyValueConverter.fromVapiResultDataValue("@modelKey", (DataValue)stringValue, vapiKey, this._applianceId);
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
    assert vapiProperties != null;
    if (vapiProperties.size() != vapiItem.getPropertyValues().size())
      throw new IllegalArgumentException(String.format("The number of property values in a vAPI ResourceItem does not match the number of properties in the vAPI ResultSet: %d != %d", new Object[] { Integer.valueOf(vapiItem.getPropertyValues().size()), Integer.valueOf(vapiProperties.size()) })); 
    Map<String, DataValue> vapiValueByProperty = toVapiValueByProperty(vapiItem
        .getPropertyValues(), vapiProperties);
    List<Object> values = new ArrayList(queryProperties.size());
    for (String property : queryProperties) {
      DataValue vapiValue = vapiValueByProperty.get(property);
      Object value = null;
      if (vapiValue != null)
        value = this._propertyValueConverter.fromVapiResultDataValue(property, vapiType, vapiValue, this._applianceId); 
      values.add(value);
    } 
    return values;
  }
  
  private static Map<String, DataValue> toVapiValueByProperty(List<DataValue> vapiValues, List<String> vapiProperties) {
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
  
  public void setApplianceId(String applianceId) {
    this._applianceId = applianceId;
  }
}
