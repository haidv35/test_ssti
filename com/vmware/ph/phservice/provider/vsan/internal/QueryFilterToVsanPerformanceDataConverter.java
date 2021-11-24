package com.vmware.ph.phservice.provider.vsan.internal;

import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.internal.TimeIntervalUtil;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextParser;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlJsonLdQueryContextParser;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryFilterToVsanPerformanceDataConverter {
  private final QueryContextParser _queryContextParser;
  
  private final PerfStatsQueryDataParser _perfStatsQueryDataParser;
  
  public QueryFilterToVsanPerformanceDataConverter(VmodlToJsonLdSerializer serializer) {
    this(serializer, new PerfStatsQueryDataParser());
  }
  
  public QueryFilterToVsanPerformanceDataConverter(VmodlToJsonLdSerializer serializer, PerfStatsQueryDataParser perfStatsQueryDataParser) {
    this._queryContextParser = (QueryContextParser)new VmodlJsonLdQueryContextParser(serializer);
    this._perfStatsQueryDataParser = perfStatsQueryDataParser;
  }
  
  public <T> T getContextProperty(Query query, Class<T> clazz, String propName) {
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query, this._queryContextParser);
    List<T> values = queryContext.getObjects(propName, clazz);
    if (values == null || values.isEmpty())
      throw new IllegalArgumentException("Missing " + propName + "Obj."); 
    T value = values.get(0);
    return value;
  }
  
  public Map<String, String[]> gePerfStatsQueryDataFilterProperty(Query query, String propName) {
    Object perfStatsQueryDataObj = QueryUtil.getFilterPropertyComparableValue(query, propName);
    if (perfStatsQueryDataObj == null)
      throw new IllegalArgumentException("Missing " + propName + "Obj."); 
    try {
      Map<String, String[]> entityTypeToFields = this._perfStatsQueryDataParser.parse(perfStatsQueryDataObj.toString());
      return entityTypeToFields;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid perfStatsQueryDataObj provided: ", e);
    } 
  }
  
  public Calendar getCalendarFilterProperty(Query query, String propName) {
    try {
      String calendarDeltaFilterValue = QueryUtil.getFilterPropertyComparableValues(query, propName).get(0);
      Calendar calendar = TimeIntervalUtil.convertOffsetTimeInMsToCalendar(calendarDeltaFilterValue);
      return calendar;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid " + propName + "Obj provided: ", e);
    } 
  }
  
  public static class PerfStatsQueryDataParser {
    public Map<String, String[]> parse(String perfStatsQueryDataStr) {
      Map<String, String[]> entityTypeToFields = (Map)new LinkedHashMap<>();
      JSONObject jsonObject = new JSONObject(perfStatsQueryDataStr);
      Iterator<String> types = jsonObject.keys();
      while (types.hasNext()) {
        String entityType = types.next();
        try {
          List<Object> fields = jsonObject.getJSONArray(entityType).toList();
          entityTypeToFields.put(entityType, fields
              
              .toArray(new String[fields.size()]));
        } catch (JSONException e) {
          String field = jsonObject.getString(entityType);
          entityTypeToFields.put(entityType, new String[] { field });
        } 
      } 
      return entityTypeToFields;
    }
  }
}
