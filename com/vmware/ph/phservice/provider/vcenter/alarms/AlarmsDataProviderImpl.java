package com.vmware.ph.phservice.provider.vcenter.alarms;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcDataProviderUtil;
import com.vmware.vim.binding.vim.alarm.AlarmManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmsDataProviderImpl implements DataProvider {
  private static final Log log = LogFactory.getLog(AlarmsDataProviderImpl.class);
  
  private static final String ALARM_MO_TYPE_NAME = "Alarm";
  
  private final VcClient _vcClient;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final VcPropertyCollectorReader _pcReader;
  
  public AlarmsDataProviderImpl(VcClient vcClient) {
    this._vcClient = vcClient;
    this._vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    this._pcReader = new VcPropertyCollectorReader(this._vcClient);
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> pcPropertyNames = PcDataProviderUtil.convertQueryPropertiesToPcProperties(query.getProperties());
    List<PropertyCollectorReader.PcResourceItem> pcResourceItems = collectAlarms(pcPropertyNames, query
        
        .getOffset(), query
        .getLimit());
    ResultSet queryResultSet = PcDataProviderUtil.convertPcResourceItemsToQueryResultSet(query
        .getProperties(), pcResourceItems);
    return queryResultSet;
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.ModelInfo> models = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(
        Collections.singletonList(this._vmodlTypeMap.getVmodlType("Alarm")), this._vmodlTypeMap, this._vcClient
        
        .getVmodlVersion());
    return QuerySchema.forModels(models);
  }
  
  private List<PropertyCollectorReader.PcResourceItem> collectAlarms(List<String> pcPropertyNames, int queryOffset, int queryLimit) {
    AlarmManager manager = fetchAlarmManager();
    if (manager == null)
      return null; 
    if (queryLimit == 0)
      return null; 
    ManagedObjectReference[] alarms = manager.getAlarm(null);
    if (alarms == null || alarms.length == 0)
      return null; 
    List<ManagedObjectReference> alarmMoRefs = Arrays.asList(alarms);
    if (queryLimit != -1)
      alarmMoRefs = PageUtil.pageItems(alarmMoRefs, queryOffset, queryLimit); 
    PropertyCollector.FilterSpec pcFilterSpec = PropertyCollectorUtil.createMoRefsFilterSpec(alarmMoRefs
        .<ManagedObjectReference>toArray(new ManagedObjectReference[alarmMoRefs.size()]), pcPropertyNames, this._vmodlTypeMap);
    try {
      List<PropertyCollectorReader.PcResourceItem> pcResourceItems = this._pcReader.retrieveContent(pcFilterSpec, pcPropertyNames, 0, -1);
      return pcResourceItems;
    } catch (InvalidProperty ip) {
      log.warn(ip.getMessage());
      return null;
    } 
  }
  
  private AlarmManager fetchAlarmManager() {
    return this._vcClient.<AlarmManager>createMo((this._vcClient.getServiceInstanceContent()).alarmManager);
  }
}
