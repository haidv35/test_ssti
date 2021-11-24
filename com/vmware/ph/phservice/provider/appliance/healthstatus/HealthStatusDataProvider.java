package com.vmware.ph.phservice.provider.appliance.healthstatus;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.internal.security.CertUtil;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QueryFilterConverter;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;

public class HealthStatusDataProvider implements DataProvider {
  private static final Log _log = LogFactory.getLog(HealthStatusDataProvider.class);
  
  private static final Map<String, Class<?>> RESOURCE_MODEL_NAME_TO_CLASS = new HashMap<>();
  
  static {
    RESOURCE_MODEL_NAME_TO_CLASS.put(HealthStatus.class
        .getSimpleName(), HealthStatus.class);
    RESOURCE_MODEL_NAME_TO_CLASS.put(VimHealth.class
        .getSimpleName(), VimHealth.class);
    RESOURCE_MODEL_NAME_TO_CLASS.put(HmsHealthStatus.class
        .getSimpleName(), HmsHealthStatus.class);
  }
  
  private static final ServiceRegistration.ServiceType HMS_SERVICE_TYPE = new ServiceRegistration.ServiceType("com.vmware.cis", "com.vmware.vcHms");
  
  private static final ServiceRegistration.ServiceType VRMS_SERVICE_TYPE = new ServiceRegistration.ServiceType("com.vmware.cis", "com.vmware.vr.vrms");
  
  private static final ServiceRegistration.EndpointType VRMS_HMS_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("vmomi", "com.vmware.vim.hms");
  
  private static final String HMS_HEALTH_STATUS_URL_PARAMETER = "ui=1";
  
  static final ServiceRegistration.EndpointType HEALTH_STATUS_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("rest", "com.vmware.cis.common.healthstatus");
  
  private final LookupClient _lookupClient;
  
  private final KeyStore _trustStore;
  
  private final String _nodeId;
  
  private final boolean _shouldUseEnvoySidecar;
  
  public HealthStatusDataProvider(LookupClient lookupClient, KeyStore trustStore, String nodeId, boolean shouldUseEnvoySidecar) {
    this._lookupClient = lookupClient;
    this._trustStore = trustStore;
    this._nodeId = nodeId;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._lookupClient
        .getVmodlContext(), this._lookupClient
        .getVmodlVersion());
  }
  
  public ResultSet executeQuery(Query query) {
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    ServiceRegistration.ServiceType serviceType = (ServiceRegistration.ServiceType)QueryFilterConverter.convertQueryFilter(query
        .getFilter(), ServiceRegistration.ServiceType.class);
    Pair<String, URI> serviceIdAndHealthStatusUrl = getServiceIdAndHealthStatusUrl(this._lookupClient, serviceType, this._nodeId, this._shouldUseEnvoySidecar);
    String serviceId = (String)serviceIdAndHealthStatusUrl.getFirst();
    URI healthStatusUrl = (URI)serviceIdAndHealthStatusUrl.getSecond();
    if (serviceId == null || healthStatusUrl == null)
      return resultSetBuilder.build(); 
    String resourceModelName = query.getResourceModels().iterator().next();
    Class<?> healthStatusClass = RESOURCE_MODEL_NAME_TO_CLASS.get(resourceModelName);
    if (healthStatusClass.equals(HmsHealthStatus.class))
      healthStatusUrl = addParametersToHealthStatusUrl(healthStatusUrl, 
          
          Arrays.asList(new String[] { "ui=1" })); 
    Object healthStatus = readHealthStatus(healthStatusUrl, healthStatusClass, 

        
        getTrustStore(serviceType));
    if (healthStatus == null)
      return resultSetBuilder.build(); 
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    URI modelKey = DataProviderUtil.createModelKey(resourceModelName, serviceId);
    List<Object> propertyValues = DataProviderUtil.getPropertyValuesFromObjectAndValueMap(healthStatus, modelKey, nonQualifiedQueryProperties, 


        
        QueryUtil.getNonQualifiedFilterPropertyToComparableValue(query
          .getFilter()));
    resultSetBuilder.item(modelKey, propertyValues);
    ResultSet result = resultSetBuilder.build();
    return result;
  }
  
  Object readHealthStatus(URI healthStatusUrl, Class<?> healthStatusClass, KeyStore trusStore) {
    if (healthStatusUrl == null || healthStatusClass == null || trusStore == null)
      return null; 
    String healthStatusString = HealthStatusReader.readHealthStatus(healthStatusUrl, trusStore);
    Object healthStatus = HealthStatusParser.parseHealthStatus(healthStatusString, healthStatusClass);
    return healthStatus;
  }
  
  private KeyStore getTrustStore(ServiceRegistration.ServiceType serviceType) {
    KeyStore trustStore = this._trustStore;
    if (HMS_SERVICE_TYPE.getProduct().equals(serviceType.getProduct()) && HMS_SERVICE_TYPE
      .getType().equals(serviceType.getType())) {
      String[] hmsSslTrust = ServiceLocatorUtil.getSslTrustForServiceAndEndpointType(this._lookupClient, VRMS_SERVICE_TYPE, VRMS_HMS_ENDPOINT_TYPE, this._nodeId);
      trustStore = CertUtil.createKeyStoreFromCerts(hmsSslTrust);
    } 
    return trustStore;
  }
  
  private static Pair<String, URI> getServiceIdAndHealthStatusUrl(LookupClient lookupClient, ServiceRegistration.ServiceType serviceType, String nodeId, boolean shouldUseEnvoySidecar) {
    ServiceRegistration.Info[] servicesList = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, serviceType, HEALTH_STATUS_ENDPOINT_TYPE, nodeId);
    URI healthStatusUrl = null;
    String serviceId = null;
    if (servicesList == null || servicesList.length == 0) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("Health status URL for service '%s' was not discovered. No health status collected.", new Object[] { String.valueOf(serviceType) })); 
    } else if (servicesList.length > 1) {
      if (_log.isWarnEnabled())
        _log.warn(
            String.format("Several services were discovered for service filter '%s'. The filter should specify a single service.", new Object[] { String.valueOf(serviceType) })); 
    } else {
      serviceId = servicesList[0].getServiceId();
      ServiceRegistration.Endpoint[] serviceEndpoints = servicesList[0].getServiceEndpoints();
      if (serviceEndpoints != null && serviceEndpoints.length != 0) {
        ServiceRegistration.Endpoint healthStatusEndpoint = serviceEndpoints[0];
        healthStatusUrl = ServiceLocatorUtil.convertUriToEnvoySidecarIfNeeded(
            ServiceLocatorUtil.getEndpointUri(healthStatusEndpoint), servicesList[0]
            .getNodeId(), nodeId, shouldUseEnvoySidecar);
      } 
    } 
    return new Pair(serviceId, healthStatusUrl);
  }
  
  private static URI addParametersToHealthStatusUrl(URI healthStatusUrl, List<String> helathStatusUrlParameters) {
    if (helathStatusUrlParameters.isEmpty())
      return healthStatusUrl; 
    URIBuilder uriBuilder = new URIBuilder(healthStatusUrl);
    for (String helathStatusUrlParameter : helathStatusUrlParameters) {
      String[] parameterNameValuePair = helathStatusUrlParameter.split("=");
      String parameterName = parameterNameValuePair[0];
      String parameterValue = parameterNameValuePair[1];
      uriBuilder.setParameter(parameterName, parameterValue);
    } 
    URI healthStatusUrlWithParameters = null;
    try {
      healthStatusUrlWithParameters = uriBuilder.build();
    } catch (URISyntaxException e) {
      if (_log.isDebugEnabled())
        _log.debug("Failed to add parameters to health status URL. Returning original URL.", e); 
      healthStatusUrlWithParameters = healthStatusUrl;
    } 
    return healthStatusUrlWithParameters;
  }
  
  private static QuerySchema createQuerySchema(VmodlContext vmodlContext, VmodlVersion vmodlVersion) {
    Map<String, QuerySchema.ModelInfo> healthStatusModelInfos = new HashMap<>();
    for (Map.Entry<String, Class<?>> healthStatusResourceModelEntry : RESOURCE_MODEL_NAME_TO_CLASS.entrySet()) {
      String healthStatusClassResourceModelName = healthStatusResourceModelEntry.getKey();
      Class<?> healthStatusClass = healthStatusResourceModelEntry.getValue();
      healthStatusModelInfos.put(healthStatusClassResourceModelName, 
          
          createModelInfoForHealthStatusClass(vmodlContext, vmodlVersion, healthStatusClass));
    } 
    QuerySchema healthStatusQuerySchema = QuerySchema.forModels(healthStatusModelInfos);
    return healthStatusQuerySchema;
  }
  
  private static QuerySchema.ModelInfo createModelInfoForHealthStatusClass(VmodlContext vmodlContext, VmodlVersion vmodlVersion, Class<?> healthStatusClass) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    List<String> serviceTypeProperties = VmodlUtil.getProperties(vmodlTypeMap
        .getLoadedVmodlType(ServiceRegistration.ServiceType.class), vmodlTypeMap, vmodlVersion);
    List<String> healthStatusProperties = DataProviderUtil.getPropertyNames(healthStatusClass, "get", true);
    List<String> properties = new ArrayList<>();
    properties.addAll(serviceTypeProperties);
    properties.addAll(healthStatusProperties);
    QuerySchema.ModelInfo healthStatusModelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(properties);
    return healthStatusModelInfo;
  }
}
