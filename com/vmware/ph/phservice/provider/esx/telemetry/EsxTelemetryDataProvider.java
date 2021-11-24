package com.vmware.ph.phservice.provider.esx.telemetry;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.internal.ProductVersion;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.HostReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.vim.binding.vim.host.TelemetryManager;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class EsxTelemetryDataProvider implements DataProvider {
  static final String RESOURCE_MODEL_TYPE = "HostTelemetryManager";
  
  static final String TELEMETRY_DATA_PROPERTY = "telemetryData";
  
  static final String TELEMETRY_WHITE_LIST_PROPERTY = "whiteList";
  
  static final String TELEMETRY_BLACK_LIST_PROPERTY = "blackList";
  
  private static final Log _log = LogFactory.getLog(EsxTelemetryDataProvider.class);
  
  private static final ProductVersion MIN_SUPPORTED_HOST_VERSION = new ProductVersion("6.7.0");
  
  private static final String HOST_SYSTEM_MOREF_TYPE = "HostSystem";
  
  private final HostReader _hostReader;
  
  public EsxTelemetryDataProvider(VcClient vcClient) {
    this(new HostReader(vcClient));
  }
  
  public EsxTelemetryDataProvider(VmomiClient vimClient) {
    this(new HostReader(vimClient));
  }
  
  EsxTelemetryDataProvider(HostReader hostReader) {
    this._hostReader = hostReader;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema();
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    if (!QueryUtil.selectsActualProperty(query, "telemetryData")) {
      if (_log.isDebugEnabled())
        _log.debug("No ESX telemetry data has been requested. Returning an empty result."); 
      return resultSetBuilder.build();
    } 
    List<String> whiteList = QueryUtil.getFilterPropertyComparableValues(query, "whiteList");
    List<String> blackList = QueryUtil.getFilterPropertyComparableValues(query, "blackList");
    List<ManagedObjectReference> hostMoRefs = getHostMoRefsFromContext(query);
    if (hostMoRefs.isEmpty())
      hostMoRefs = this._hostReader.getHostMoRefs(); 
    for (ManagedObjectReference hostMoRef : hostMoRefs) {
      String hostSystemInfoUuid = null;
      TelemetryManager.TelemetryInfo telemetryInfo = null;
      try {
        ProductVersion hostVersion = this._hostReader.getHostVersion(hostMoRef);
        if (hostVersion != null && hostVersion
          .compareTo(MIN_SUPPORTED_HOST_VERSION) >= 0) {
          hostSystemInfoUuid = this._hostReader.getHostUuid(hostMoRef);
          TelemetryManager telemetryManager = this._hostReader.getTelemetryManager(hostMoRef);
          if (telemetryManager == null) {
            if (_log.isDebugEnabled())
              _log.debug("Telemetry not available for host " + hostMoRef); 
          } else {
            telemetryInfo = readTelemetryInfo(telemetryManager, whiteList, blackList);
          } 
        } 
      } catch (Exception e) {
        _log.warn("Failed to read ESX telemetry from " + hostMoRef + " with error message '" + e
            
            .getMessage() + "'.");
      } 
      if (hostSystemInfoUuid != null && telemetryInfo != null) {
        URI modelKey = DataProviderUtil.createModelKey(TelemetryManager.class, hostSystemInfoUuid);
        List<Object> propertyValues = buildPropertyValues(queryProperties, modelKey, telemetryInfo, whiteList, blackList);
        resultSetBuilder.item(modelKey, propertyValues);
        continue;
      } 
      if (_log.isDebugEnabled())
        _log.debug("No telemetry info available for host " + hostMoRef); 
    } 
    return resultSetBuilder.build();
  }
  
  private static TelemetryManager.TelemetryInfo readTelemetryInfo(TelemetryManager telemetryManager, List<String> whiteList, List<String> blackList) {
    TelemetryManager.TelemetryFilterSpec filterSpec = createTelemetryFilterSpec(whiteList, blackList);
    ItemsStream<KeyAnyValue> telemetryItemsStream = new EsxTelemetryInfoStream(telemetryManager, filterSpec);
    List<KeyAnyValue> telemetryData = PageUtil.pageItems(telemetryItemsStream, 0, -1);
    telemetryData = filterInvalidEntries(telemetryData);
    TelemetryManager.TelemetryInfo telemetryInfo = new TelemetryManager.TelemetryInfo();
    telemetryInfo.setData(telemetryData
        .<KeyAnyValue>toArray(new KeyAnyValue[telemetryData.size()]));
    return telemetryInfo;
  }
  
  private static List<KeyAnyValue> filterInvalidEntries(List<KeyAnyValue> telemetryData) {
    List<KeyAnyValue> filteredTelemetryData = new ArrayList<>();
    for (KeyAnyValue entry : telemetryData) {
      if (entry.getKey() != null && entry.getValue() != null)
        filteredTelemetryData.add(entry); 
    } 
    return filteredTelemetryData;
  }
  
  private static List<ManagedObjectReference> getHostMoRefsFromContext(Query query) {
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query);
    List<ManagedObjectReference> hostMoRefs = VmodlQueryContextUtil.getMoRefsFromContext(queryContext, "HostSystem");
    return hostMoRefs;
  }
  
  private static TelemetryManager.TelemetryFilterSpec createTelemetryFilterSpec(List<String> whiteList, List<String> blackList) {
    TelemetryManager.TelemetryFilterSpec filterSpec = new TelemetryManager.TelemetryFilterSpec();
    filterSpec.setWhitelist(whiteList.<String>toArray(new String[whiteList.size()]));
    filterSpec.setBlacklist(blackList.<String>toArray(new String[blackList.size()]));
    return filterSpec;
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.PropertyInfo> propertiesInfo = new TreeMap<>();
    propertiesInfo.put("telemetryData", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("whiteList", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("blackList", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    QuerySchema.ModelInfo modelInfo = new QuerySchema.ModelInfo(propertiesInfo);
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    models.put("HostTelemetryManager", modelInfo);
    return QuerySchema.forModels(models);
  }
  
  private static List<Object> buildPropertyValues(List<String> queryProperties, URI modelKey, TelemetryManager.TelemetryInfo telemetryInfo, List<String> esxTelemetryWhiteList, List<String> esxTelemetryBlackList) {
    List<Object> propertyValues = new ArrayList();
    propertyValues.add(modelKey);
    for (String property : queryProperties) {
      switch (QuerySchemaUtil.getActualPropertyName(property)) {
        case "telemetryData":
          propertyValues.add(telemetryInfo);
        case "whiteList":
          propertyValues.add(esxTelemetryWhiteList);
        case "blackList":
          propertyValues.add(esxTelemetryBlackList);
      } 
    } 
    return propertyValues;
  }
}
