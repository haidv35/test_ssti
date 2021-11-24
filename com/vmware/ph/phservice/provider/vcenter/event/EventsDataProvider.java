package com.vmware.ph.phservice.provider.vcenter.event;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.internal.DateUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vim.event.Event;
import com.vmware.vim.binding.vim.event.EventFilterSpec;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EventsDataProvider implements DataProvider {
  private static final String EVENTS_VMODL_PACKAGE_NAME = "com.vmware.vim.binding.vim.event";
  
  private static final Log _log = LogFactory.getLog(EventsDataProvider.class);
  
  private final VcClient _vcClient;
  
  private final QueryFilterToEventFilterSpecConverter _queryFilterConverter;
  
  private static final String PROPERTY_NAME_FILTER = "filter";
  
  static final String PROPERTY_NAME_FILTER_BEGIN_TIME = "filter/beginTime";
  
  static final String PROPERTY_NAME_FILTER_END_TIME = "filter/endTime";
  
  public EventsDataProvider(VcClient vcClient) {
    this(vcClient, new QueryFilterToEventFilterSpecConverter());
  }
  
  EventsDataProvider(VcClient vcClient, QueryFilterToEventFilterSpecConverter filterConverter) {
    this._vcClient = vcClient;
    this._queryFilterConverter = filterConverter;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._vcClient);
  }
  
  public ResultSet executeQuery(Query query) {
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    String queryModelName = query.getResourceModels().iterator().next();
    EventFilterSpec eventFilterSpec = this._queryFilterConverter.createEventFilterSpec(queryModelName, 
        
        DateUtil.createUtcCalendar(), query
        .getFilter());
    ItemsStream<Event> eventsReader = new EventsReader(this._vcClient, eventFilterSpec);
    return getEventsData(query, eventsReader);
  }
  
  static ResultSet getEventsData(Query query, ItemsStream<Event> eventsReader) {
    ResultSet.Builder eventsResultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    if (eventsReader != null)
      try {
        List<Event> vcEvents = PageUtil.pageItems(eventsReader, query
            
            .getOffset(), query
            .getLimit());
        for (Event vcEvent : vcEvents)
          addEventDataToResultSet(vcEvent, query
              
              .getProperties(), eventsResultSetBuilder); 
      } finally {
        try {
          eventsReader.close();
        } catch (IOException e) {
          _log.debug("Failed to close the Items Stream.", e);
        } 
      }  
    return eventsResultSetBuilder.build();
  }
  
  static void addEventDataToResultSet(Event vcEvent, List<String> eventQueryProperties, ResultSet.Builder eventsResultSetBuilder) {
    List<String> nonQualifiedEventQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(eventQueryProperties);
    URI eventModelKey = DataProviderUtil.createModelKey(vcEvent
        .getClass(), 
        String.valueOf(vcEvent.getKey()));
    List<Object> eventQueryPropertyValues = DataProviderUtil.getPropertyValues(vcEvent, eventModelKey, nonQualifiedEventQueryProperties);
    eventsResultSetBuilder.item(eventModelKey, eventQueryPropertyValues);
  }
  
  private static QuerySchema createQuerySchema(VcClient vcClient) {
    VmodlContext vmodlContext = vcClient.getVmodlContext();
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    VmodlVersion vmodlVersion = vcClient.getVmodlVersion();
    List<VmodlType> eventDataObjectVmodlTypes = VmodlTypeToQuerySchemaModelInfoConverter.getAllDataObjectVmodlTypesInPackage(vmodlTypeMap, "com.vmware.vim.binding.vim.event");
    Map<String, QuerySchema.ModelInfo> eventsTypeNameToModelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(eventDataObjectVmodlTypes, vmodlTypeMap, vmodlVersion);
    Map<String, QuerySchema.PropertyInfo> filterPropertyNameToPropertyInfo = buildFilterProperties();
    Map<String, QuerySchema.ModelInfo> eventsTypeNameToModelInfoWithFilters = QuerySchemaUtil.addPropertiesToModelInfos(eventsTypeNameToModelInfo, filterPropertyNameToPropertyInfo);
    return QuerySchema.forModels(eventsTypeNameToModelInfoWithFilters);
  }
  
  private static Map<String, QuerySchema.PropertyInfo> buildFilterProperties() {
    QuerySchema.PropertyInfo nonFilterablePropertyInfo = QuerySchema.PropertyInfo.forNonFilterableProperty();
    QuerySchema.PropertyInfo intFilterablePropertyInfo = QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT);
    Map<String, QuerySchema.PropertyInfo> filterPropertyNameToPropertyInfo = new HashMap<>();
    filterPropertyNameToPropertyInfo.put("filter", nonFilterablePropertyInfo);
    filterPropertyNameToPropertyInfo.put("filter/beginTime", intFilterablePropertyInfo);
    filterPropertyNameToPropertyInfo.put("filter/endTime", intFilterablePropertyInfo);
    return filterPropertyNameToPropertyInfo;
  }
}
