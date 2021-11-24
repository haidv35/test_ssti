package com.vmware.ph.phservice.provider.common.vmomi.pc;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import java.util.ArrayList;
import java.util.List;

public class PcDataProviderUtil {
  public static List<String> convertQueryPropertiesToPcProperties(List<String> queryPropertyNames) {
    List<String> pcPropertyNames = new ArrayList<>();
    for (String queryPropertyName : queryPropertyNames) {
      if (queryPropertyName.startsWith("@"))
        continue; 
      String pcPropertyName = queryPropertyName.replaceAll("\\/", "\\.");
      int index = pcPropertyName.indexOf(".");
      if (index != -1)
        pcPropertyName = pcPropertyName.substring(index + 1); 
      pcPropertyNames.add(pcPropertyName);
    } 
    return pcPropertyNames;
  }
  
  public static ResultSet convertPcResourceItemsToQueryResultSet(List<String> supportedPropertyNames, List<PropertyCollectorReader.PcResourceItem> pcResourceItems) {
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(supportedPropertyNames);
    for (PropertyCollectorReader.PcResourceItem pcResultItem : pcResourceItems) {
      assert supportedPropertyNames.size() == pcResultItem
        .getPropertyValues().size();
      assert ((String)supportedPropertyNames.get(0)).equals("@modelKey");
      Object modelKey = pcResultItem.getPropertyValues().get(0);
      resultSetBuilder.item(modelKey, pcResultItem.getPropertyValues());
    } 
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
}
