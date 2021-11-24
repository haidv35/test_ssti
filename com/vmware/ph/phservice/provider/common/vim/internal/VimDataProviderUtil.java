package com.vmware.ph.phservice.provider.common.vim.internal;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VimDataProviderUtil {
  public static List<VimResourceItem> getVimResourceItems(List<String> propertiesNames, DataRetriever<Object> dataRetriever) {
    List<Object> vimDataList = dataRetriever.retrieveData();
    List<String> nonQualifiedProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(propertiesNames);
    return getVimResourceItems(vimDataList, nonQualifiedProperties, dataRetriever);
  }
  
  public static List<VimResourceItem> getVimResourceItems(List<Object> vimDataList, List<String> supportedNonQualifiedQueryProperties, DataRetriever<Object> dataRetriever) {
    List<VimResourceItem> retrievedResourceItemList = new ArrayList<>();
    for (Object data : vimDataList) {
      List<Object> propertyValues = new ArrayList();
      URI objectUri = DataProviderUtil.createModelKey(data
          .getClass(), dataRetriever.getKey(data));
      propertyValues.add(objectUri);
      for (String property : supportedNonQualifiedQueryProperties) {
        if (!property.startsWith("@")) {
          Object propertyValue = DataProviderUtil.getPropertyValue(data, property);
          propertyValues.add(propertyValue);
        } 
      } 
      VimResourceItem item = new VimResourceItem(propertyValues);
      retrievedResourceItemList.add(item);
    } 
    return retrievedResourceItemList;
  }
  
  public static ResultSet convertVimResourceItemsToResultSet(List<VimResourceItem> vimResourceItems, List<String> queryPropertyNames) {
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryPropertyNames);
    for (VimResourceItem vimResourceItem : vimResourceItems) {
      List<Object> vimResItemPropValues = vimResourceItem.getPropertyValues();
      if (queryPropertyNames.size() != vimResItemPropValues.size())
        throw new IllegalArgumentException("Invalid query properties."); 
      Object key = vimResItemPropValues.get(0);
      resultSetBuilder.item(key, vimResItemPropValues);
    } 
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
  
  public static ResultSet convertVimResourceItemsToResultSet(List<VimResourceItem> vimResourceItems, List<String> queryPropertyNames, List<String> supportedNonQualifiedQueryProperties) {
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryPropertyNames);
    for (VimResourceItem vimResourceItem : vimResourceItems) {
      List<Object> vimResItemPropValues = vimResourceItem.getPropertyValues();
      if (supportedNonQualifiedQueryProperties.size() != vimResItemPropValues
        .size())
        throw new IllegalArgumentException("Invalid query properties."); 
      List<Object> queryResultItem = new ArrayList();
      int supportedPropertyIndex = 0;
      for (String queryPropertyName : queryPropertyNames) {
        if (!supportedNonQualifiedQueryProperties.contains(QuerySchemaUtil.getActualPropertyName(queryPropertyName))) {
          queryResultItem.add(null);
          continue;
        } 
        queryResultItem.add(vimResItemPropValues.get(supportedPropertyIndex));
        supportedPropertyIndex++;
      } 
      Object key = queryResultItem.get(0);
      resultSetBuilder.item(key, queryResultItem);
    } 
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
}
