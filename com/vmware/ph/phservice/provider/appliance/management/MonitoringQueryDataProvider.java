package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.Monitoring;
import com.vmware.appliance.MonitoringTypes;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.internal.DateUtil;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonitoringQueryDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  private static final Map<String, List<String>> RESOURCENAME_TO_FILTER_PROPERTIES = new LinkedHashMap<>();
  
  static final String RESOURCENAME = VapiResourceUtil.getResourceName(Monitoring.class) + ".query";
  
  static final String INTERVAL_FILTER_PROPERTY = "filter/interval";
  
  static final String FUNCTION_FILTER_PROPERTY = "filter/function";
  
  static final String NAMES_FILTER_PROPERTY = "filter/names";
  
  static final String STARTTIME_FILTER_PROPERTY = "filter/startTime";
  
  static final String ENDTIME_FILTER_PROPERTY = "filter/endTime";
  
  static {
    RESOURCENAME_TO_SERVICECLAZZ.put(RESOURCENAME, Monitoring.class);
    RESOURCENAME_TO_FILTER_PROPERTIES.put(RESOURCENAME, 
        
        Arrays.asList(new String[] { "filter", "filter/interval", "filter/function", "filter/names", "filter/startTime", "filter/endTime" }));
  }
  
  public MonitoringQueryDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ, RESOURCENAME_TO_FILTER_PROPERTIES);
  }
  
  protected Map<MonitoringTypes.MonitoredItemDataRequest, MonitoringTypes.MonitoredItemData> executeService(Service service, Object filter) {
    MonitoringTypes.MonitoredItemDataRequest request = null;
    if (filter != null)
      request = (MonitoringTypes.MonitoredItemDataRequest)filter; 
    List<MonitoringTypes.MonitoredItemData> items = ((Monitoring)service).query(request);
    Map<MonitoringTypes.MonitoredItemDataRequest, MonitoringTypes.MonitoredItemData> requestToData = new LinkedHashMap<>();
    if (items != null)
      for (MonitoringTypes.MonitoredItemData item : items) {
        MonitoringTypes.MonitoredItemDataRequest monitoredItemDataId = new MonitoringTypes.MonitoredItemDataRequest();
        monitoredItemDataId.setNames(Arrays.asList(new String[] { item.getName() }));
        monitoredItemDataId.setInterval(item.getInterval());
        monitoredItemDataId.setFunction(item.getFunction());
        monitoredItemDataId.setStartTime(item.getStartTime());
        monitoredItemDataId.setEndTime(item.getEndTime());
        requestToData.put(monitoredItemDataId, item);
      }  
    return requestToData;
  }
  
  protected Object convertQueryFilterToServiceFilter(Query query) {
    Filter filter = query.getFilter();
    if (filter == null)
      return null; 
    MonitoringTypes.MonitoredItemDataRequest request = new MonitoringTypes.MonitoredItemDataRequest();
    try {
      String intervalFilterValue = ((String)QueryUtil.getFilterPropertyComparableValues(query, "filter/interval").get(0)).toUpperCase();
      request.setInterval(MonitoringTypes.IntervalType.valueOf(intervalFilterValue));
      String functionFilterValue = ((String)QueryUtil.getFilterPropertyComparableValues(query, "filter/function").get(0)).toUpperCase();
      request.setFunction(MonitoringTypes.FunctionType.valueOf(functionFilterValue));
      List<String> namesFilterValue = QueryUtil.getFilterPropertyComparableValues(query, "filter/names");
      request.setNames(namesFilterValue);
      String startTimeFilterValue = QueryUtil.getFilterPropertyComparableValues(query, "filter/startTime").get(0);
      Calendar startCalendar = convertTimeInMsToCalendar(startTimeFilterValue);
      request.setStartTime(startCalendar);
      String endTimeFilterValue = QueryUtil.getFilterPropertyComparableValues(query, "filter/endTime").get(0);
      Calendar endCalendar = convertTimeInMsToCalendar(endTimeFilterValue);
      request.setEndTime(endCalendar);
    } catch (Exception e) {
      throw new IllegalArgumentException("Incorrect filter values provided", e);
    } 
    if (request.getEndTime().before(request.getStartTime()))
      throw new IllegalArgumentException("Invalid start/end time period provided"); 
    return request;
  }
  
  protected String convertResourceEntityIdToString(String resourceName, Object resourceEntityId) {
    MonitoringTypes.MonitoredItemDataRequest id = (MonitoringTypes.MonitoredItemDataRequest)resourceEntityId;
    StringBuilder idStringBuilder = new StringBuilder();
    idStringBuilder
      .append(id.getNames().get(0))
      .append(id.getInterval().name())
      .append(id.getFunction().name())
      .append(id.getStartTime().getTimeInMillis())
      .append(id.getEndTime().getTimeInMillis());
    return idStringBuilder.toString();
  }
  
  private static Calendar convertTimeInMsToCalendar(String timeInMillisStr) {
    long timeInMillis = Long.parseLong(timeInMillisStr);
    Calendar calendar = DateUtil.createUtcCalendar();
    if (timeInMillis > 0L) {
      calendar.setTimeInMillis(timeInMillis);
    } else {
      calendar.setTimeInMillis(calendar.getTimeInMillis() + timeInMillis);
    } 
    return calendar;
  }
}
