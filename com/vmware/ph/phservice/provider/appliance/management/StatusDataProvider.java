package com.vmware.ph.phservice.provider.appliance.management;

import com.vmware.appliance.techpreview.services.Status;
import com.vmware.appliance.techpreview.services.StatusTypes;
import com.vmware.cis.data.api.Query;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vapi.BaseVapiDataProvider;
import com.vmware.ph.phservice.provider.common.vapi.VapiResourceUtil;
import com.vmware.vapi.bindings.Service;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatusDataProvider extends BaseVapiDataProvider {
  private static final Log _log = LogFactory.getLog(StatusDataProvider.class);
  
  private static final Map<String, Class<? extends Service>> RESOURCENAME_TO_SERVICECLAZZ = new LinkedHashMap<>();
  
  private static final Map<String, List<String>> RESOURCENAME_TO_FILTER_PROPERTIES = new LinkedHashMap<>();
  
  private static final String RESOURCE_NAME = VapiResourceUtil.getResourceName(Status.class);
  
  static final String NAME_FILTER_PROPERTY = "filter/name";
  
  static final String TIMEOUT_FILTER_PROPERTY = "filter/timeout";
  
  static final long DEFAULT_TIMEOUT_VALUE_SECONDS = 10L;
  
  static {
    RESOURCENAME_TO_SERVICECLAZZ.put(RESOURCE_NAME, Status.class);
    RESOURCENAME_TO_FILTER_PROPERTIES.put(RESOURCE_NAME, 
        
        Arrays.asList(new String[] { "filter", "filter/name", "filter/timeout" }));
  }
  
  public StatusDataProvider(String serverGuid, VapiClient vapiClient) {
    super(serverGuid, vapiClient, RESOURCENAME_TO_SERVICECLAZZ, RESOURCENAME_TO_FILTER_PROPERTIES);
  }
  
  protected Map<? extends Object, ? extends Object> executeService(Service service, Object filter) {
    HashMap<Object, Object> result = new HashMap<>();
    if (filter != null) {
      Status statusService = (Status)service;
      StatusServiceFilter statusServiceFilter = (StatusServiceFilter)filter;
      for (String serviceName : statusServiceFilter.getServiceNames()) {
        StatusTypes.ServiceStatus serviceStatus = statusService.get(serviceName, statusServiceFilter.getTimeout());
        result.put(serviceName, serviceStatus);
      } 
    } 
    return result;
  }
  
  protected Object convertQueryFilterToServiceFilter(Query query) {
    List<String> serviceNames = getServiceNames(query);
    if (serviceNames == null)
      return null; 
    long serviceStatusRequestTimeout = getServiceRequestTimeout(query);
    StatusServiceFilter statusServiceFilter = new StatusServiceFilter(serviceNames, serviceStatusRequestTimeout);
    return statusServiceFilter;
  }
  
  private static List<String> getServiceNames(Query query) {
    List<String> serviceNames = QueryUtil.getFilterPropertyComparableValues(query, "filter/name");
    if (serviceNames.isEmpty() || StringUtils.isBlank(serviceNames.get(0))) {
      _log.warn("The value for filter/name is missing. Service status cannot be acquired!");
      return null;
    } 
    return serviceNames;
  }
  
  private static long getServiceRequestTimeout(Query query) {
    List<String> timeoutFilterValues = QueryUtil.getFilterPropertyComparableValues(query, "filter/timeout");
    long serviceStatusRequestTimeout = 10L;
    if (!timeoutFilterValues.isEmpty() && 
      !StringUtils.isBlank(timeoutFilterValues.get(0))) {
      try {
        serviceStatusRequestTimeout = Long.parseLong(timeoutFilterValues.get(0));
      } catch (NumberFormatException e) {
        _log.warn(
            String.format("The filter value '%s' for '%s' is not a valid long. Defaulting to timeout %d seconds.", new Object[] { timeoutFilterValues.get(0), "filter/timeout", 
                
                Long.valueOf(10L) }));
      } 
    } else {
      _log.warn(
          String.format("The value for filter/timeout is missing.  Defaulting to timeout %d seconds.", new Object[] { Long.valueOf(10L) }));
    } 
    return serviceStatusRequestTimeout;
  }
  
  private static class StatusServiceFilter {
    private final List<String> _serviceNames;
    
    private final long _timeout;
    
    public StatusServiceFilter(List<String> serviceNames, long timeout) {
      this._serviceNames = serviceNames;
      this._timeout = timeout;
    }
    
    public List<String> getServiceNames() {
      return this._serviceNames;
    }
    
    public long getTimeout() {
      return this._timeout;
    }
  }
}
