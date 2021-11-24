package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.MonitoringTypes;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemLoadDataReader {
  private static final Log _log = LogFactory.getLog(SystemLoadDataReader.class);
  
  private static final int NO_OFFSET_FROM_CURRENT_TIME_MILLIS = 0;
  
  private final String _vcInstanceUuid;
  
  private final VapiClient _vapiClient;
  
  public SystemLoadDataReader(String vcInstanceUuid, VapiClient vapiClient) {
    this._vcInstanceUuid = vcInstanceUuid;
    this._vapiClient = vapiClient;
  }
  
  public Map<String, Double> getAverageValuesForMonitoredItems(List<String> monitoredItemNames, int lastXMinutes) {
    Query monitoringQuery = createMonitoringQuery(monitoredItemNames, lastXMinutes);
    MonitoringQueryDataProvider monitoringQueryDataProvider = new MonitoringQueryDataProvider(this._vcInstanceUuid, this._vapiClient);
    ResultSet resultSet = monitoringQueryDataProvider.executeQuery(monitoringQuery);
    String valuePropertyName = qualifyMonitoringQueryProperty("value");
    Map<String, Double> monitoredItemsAverageValues = new HashMap<>(monitoredItemNames.size());
    for (ResourceItem resourceItem : resultSet.getItems()) {
      MonitoringTypes.MonitoredItemData monitoredItemData = (MonitoringTypes.MonitoredItemData)resourceItem.get(valuePropertyName);
      String monitoredItemName = monitoredItemData.getName();
      List<String> monitoredItemDataValues = monitoredItemData.getData();
      String monitoredItemDataValue = getFirstNonEmptyString(monitoredItemDataValues);
      if (monitoredItemDataValue == null) {
        _log.warn(
            String.format("Cannot extract non-empty value for monitored item %s. Data array: %s", new Object[] { monitoredItemName, monitoredItemDataValues }));
        return Collections.emptyMap();
      } 
      Double dataValue = Double.valueOf(Double.parseDouble(monitoredItemDataValue));
      monitoredItemsAverageValues.put(monitoredItemName, dataValue);
    } 
    return monitoredItemsAverageValues;
  }
  
  private static Query createMonitoringQuery(List<String> monitoredItemNames, int lastXMinutes) {
    Map<String, Object> filterPropertyNameToPropertyValue = new HashMap<>();
    filterPropertyNameToPropertyValue.put(
        qualifyMonitoringQueryProperty("filter/names"), monitoredItemNames);
    filterPropertyNameToPropertyValue.put(
        qualifyMonitoringQueryProperty("filter/function"), MonitoringTypes.FunctionType.AVG
        
        .name());
    filterPropertyNameToPropertyValue.put(
        qualifyMonitoringQueryProperty("filter/interval"), MonitoringTypes.IntervalType.MINUTES5
        
        .name());
    filterPropertyNameToPropertyValue.put(
        qualifyMonitoringQueryProperty("filter/startTime"), 
        
        String.valueOf(-TimeUnit.MINUTES.toMillis(lastXMinutes)));
    filterPropertyNameToPropertyValue.put(
        qualifyMonitoringQueryProperty("filter/endTime"), 
        
        String.valueOf(0));
    Filter monitoringQueryFilter = QueryUtil.createFilterForPropertiesAndValues(filterPropertyNameToPropertyValue);
    Query monitoringQuery = Query.Builder.select(new String[] { "@modelKey", qualifyMonitoringQueryProperty("value") }).from(new String[] { MonitoringQueryDataProvider.RESOURCENAME }).where(monitoringQueryFilter).build();
    return monitoringQuery;
  }
  
  private static String getFirstNonEmptyString(List<String> dataArray) {
    for (String item : dataArray) {
      if (!StringUtils.isBlank(item))
        return item; 
    } 
    return null;
  }
  
  private static String qualifyMonitoringQueryProperty(String monitoringQueryPropertyName) {
    return QuerySchemaUtil.qualifyProperty(MonitoringQueryDataProvider.RESOURCENAME, monitoringQueryPropertyName);
  }
}
