package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemLoadDataProvider implements DataProvider {
  static final String RESOURCE_MODEL_NAME = "SystemLoad";
  
  static final String CPU_UTIL_MONITORING_ITEM = "cpu.util";
  
  static final String MEMORY_USAGE_MONITORING_ITEM = "mem.util";
  
  static final String MEMORY_TOTAL_MONITORING_ITEM = "mem.total";
  
  static final String INTERVAL_MINUTES_PROPERTY = "intervalMinutes";
  
  private static final Log _log = LogFactory.getLog(SystemLoadDataProvider.class);
  
  private static final String QUALIFYING_SYMBOL_PLACEHOLDER = "_";
  
  private static final String QUALIFYING_SYMBOL = ".";
  
  private static final String QUALIFYING_SYMBOL_REGEX = "\\.";
  
  private final String _vcInstanceUuid;
  
  private final SystemLoadDataReader _systemLoadDataReader;
  
  public SystemLoadDataProvider(String vcInstanceId, VapiClient vapiClient) {
    this(vcInstanceId, new SystemLoadDataReader(vcInstanceId, vapiClient));
  }
  
  SystemLoadDataProvider(String vcInstanceId, SystemLoadDataReader systemLoadDataReader) {
    this._vcInstanceUuid = vcInstanceId;
    this._systemLoadDataReader = systemLoadDataReader;
  }
  
  public QuerySchema getSchema() {
    List<String> queryProperties = new ArrayList<>();
    queryProperties.add(getQueryPropertyName("cpu.util"));
    queryProperties.add(getQueryPropertyName("mem.util"));
    queryProperties.add(getQueryPropertyName("mem.total"));
    queryProperties.add("intervalMinutes");
    QuerySchema.ModelInfo modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(queryProperties);
    Map<String, QuerySchema.ModelInfo> modelInfoByModelName = Collections.singletonMap("SystemLoad", modelInfo);
    return QuerySchema.forModels(modelInfoByModelName);
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    URI modelKey = DataProviderUtil.createModelKey("SystemLoad", this._vcInstanceUuid);
    int intervalMinutes = getIntervalMinutesFromQueryFilter(query.getFilter());
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(queryProperties);
    List<Object> monitoredItemsData = getMonitoredItemsData(queryProperties, intervalMinutes);
    if (!monitoredItemsData.isEmpty()) {
      monitoredItemsData.add(0, modelKey);
      resultBuilder.item(modelKey, monitoredItemsData);
    } else if (_log.isWarnEnabled()) {
      _log.warn("Could not collect system load data. Returning empty result.");
    } 
    return resultBuilder.build();
  }
  
  private static int getIntervalMinutesFromQueryFilter(Filter filter) {
    if (filter == null || filter.getCriteria().isEmpty())
      throw new IllegalArgumentException("Missing required filter in query"); 
    if (filter.getCriteria().size() > 1)
      throw new IllegalArgumentException("Filtering by more than 1 property not supported at this time"); 
    PropertyPredicate predicate = filter.getCriteria().iterator().next();
    if (predicate.getOperator() != PropertyPredicate.ComparisonOperator.EQUAL)
      throw new IllegalArgumentException("Comparison operator [" + predicate
          .getOperator() + "] is not supported at this time"); 
    String nonQualifiedPropertyName = QuerySchemaUtil.getActualPropertyName(predicate.getProperty());
    if (!"intervalMinutes".equals(nonQualifiedPropertyName))
      throw new IllegalArgumentException("Filtering by property [" + nonQualifiedPropertyName + "] is not supported"); 
    String propertyValue = (String)predicate.getComparableValue();
    return Integer.parseInt(propertyValue);
  }
  
  private List<Object> getMonitoredItemsData(List<String> queryProperties, int intervalMinutes) {
    List<Object> propertyValues = new LinkedList();
    List<String> monitoredItemNames = getMonitoredItemsNamesFromQueryProperties(queryProperties);
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Retrieving load data for the last [%d] minutes for monitored items %s.", new Object[] { Integer.valueOf(intervalMinutes), monitoredItemNames })); 
    Map<String, Double> monitredItemNameToMonitoredItemData = this._systemLoadDataReader.getAverageValuesForMonitoredItems(monitoredItemNames, intervalMinutes);
    if (!monitredItemNameToMonitoredItemData.isEmpty())
      for (String monitoredItemName : monitoredItemNames)
        propertyValues.add(monitredItemNameToMonitoredItemData
            .get(monitoredItemName));  
    return propertyValues;
  }
  
  private static List<String> getMonitoredItemsNamesFromQueryProperties(List<String> queryProperties) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    List<String> monitoredItemNames = new ArrayList<>(queryProperties.size() - 1);
    for (String property : nonQualifiedQueryProperties) {
      if (!QuerySchemaUtil.isQueryPropertyModelKey(property)) {
        String monitoredItemName = getMonitoredItemName(property);
        monitoredItemNames.add(monitoredItemName);
      } 
    } 
    return monitoredItemNames;
  }
  
  private static String getQueryPropertyName(String monitorItemName) {
    return monitorItemName.replaceAll("\\.", "_");
  }
  
  private static String getMonitoredItemName(String queryProperty) {
    return queryProperty.replaceAll("_", ".");
  }
}
