package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.Monitoring;
import com.vmware.appliance.MonitoringTypes;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonitoringDataProvider extends BaseVapiDataProvider {
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Monitoring.class);
  }
  
  public MonitoringDataProvider(String applianceId, VapiClient vapiClient) {
    super(applianceId, vapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<String, MonitoringTypes.MonitoredItem> executeService(Service service, Object filter) {
    List<MonitoringTypes.MonitoredItem> monitoredItems = ((Monitoring)service).list();
    HashMap<String, MonitoringTypes.MonitoredItem> result = new LinkedHashMap<>();
    for (MonitoringTypes.MonitoredItem monitoredItem : monitoredItems)
      result.put(monitoredItem.getId(), monitoredItem); 
    return result;
  }
}
