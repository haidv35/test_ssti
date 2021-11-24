package com.vmware.ph.phservice.provider.appliance.vmon;

import com.vmware.appliance.vmon.Service;
import com.vmware.appliance.vmon.ServiceTypes;
import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VmonDataProvider extends BaseVapiDataProvider {
  private static final Log _log = LogFactory.getLog(VmonDataProvider.class);
  
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  static final String NAME_FILTER_PROPERTY = "filter/service";
  
  static {
    VapiResourceUtil.addToMap(RESOURCENAME_TO_SERVICECLAZZ, Service.class);
  }
  
  public VmonDataProvider(String applianceId, VapiClient vmonVapiClient) {
    super(applianceId, vmonVapiClient, RESOURCENAME_TO_SERVICECLAZZ);
  }
  
  protected Map<String, ServiceTypes.Info> executeService(Service service, Object filter) {
    Map<String, ServiceTypes.Info> serviceIdToInfo;
    Service vmonService = (Service)service;
    if (filter != null) {
      serviceIdToInfo = getFilteredServiceIdToInfo(vmonService, filter);
    } else {
      serviceIdToInfo = vmonService.listDetails();
    } 
    return serviceIdToInfo;
  }
  
  protected Object convertQueryFilterToServiceFilter(Query query) {
    List<String> filteredServiceNames = QueryUtil.getFilterPropertyComparableValues(query, "filter/service");
    if (filteredServiceNames.isEmpty())
      return null; 
    return filteredServiceNames;
  }
  
  private static Map<String, ServiceTypes.Info> getFilteredServiceIdToInfo(Service vmonService, Object filter) {
    Map<String, ServiceTypes.Info> filteredServiceIdToInfo = new LinkedHashMap<>();
    List<String> nameFilterValues = (List<String>)filter;
    for (String serviceName : nameFilterValues) {
      ServiceTypes.Info serviceInfo = vmonService.get(serviceName);
      if (serviceInfo != null) {
        filteredServiceIdToInfo.put(serviceName, serviceInfo);
        continue;
      } 
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("No service with name '%s' was discovered.", new Object[] { serviceName })); 
    } 
    return filteredServiceIdToInfo;
  }
}
