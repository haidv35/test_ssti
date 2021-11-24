package com.vmware.ph.phservice.provider.esx.cfg;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.internal.ProductVersion;
import com.vmware.ph.phservice.common.vim.internal.VimSessionManagerBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.HostReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.vim.binding.vim.HostSystem;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

public final class EsxCfgInfoDataProvider implements DataProvider {
  static final String HOST_SYSTEM_RESOURCE_MODEL_TYPE = HostSystem.class
    .getSimpleName();
  
  static final String ESX_CFG_INFO_PROPERTY = "esxCfgInfo";
  
  static final String ESX_CFG_INFO_FILTER_PROPERTY = "esxCfgInfo/filter";
  
  static final String ESX_CFG_INFO_FORMAT_PROPERTY = "esxCfgInfo/format";
  
  static final String ESX_CFG_INFO_SUBTREE_PROPERTY = "esxCfgInfo/subtree";
  
  private static final String DEFAULT_ESX_CFG_INFO_FORMAT_VALUE = "xml";
  
  private static final String DEFAULT_ESX_CFG_INFO_SUBTREE_VALUE = "all";
  
  private static final ProductVersion MIN_SUPPORTED_HOST_VERSION = new ProductVersion("6.6.2");
  
  private final VcClient _vcClient;
  
  private final HostReader _hostReader;
  
  private final EsxCfgInfoReader _esxCfgInfoReader;
  
  public EsxCfgInfoDataProvider(VcClient vcClient, HttpClient httpClient) {
    this._vcClient = Objects.<VcClient>requireNonNull(vcClient);
    this._hostReader = new HostReader(this._vcClient);
    this._esxCfgInfoReader = new EsxCfgInfoReader(httpClient, new Builder<SessionManager>() {
          public SessionManager build() {
            return EsxCfgInfoDataProvider.this._vcClient.getSessionManager();
          }
        });
  }
  
  public EsxCfgInfoDataProvider(VmomiClient vimClient, HttpClient httpClient) {
    this._vcClient = null;
    this._hostReader = new HostReader(vimClient);
    this
      
      ._esxCfgInfoReader = new EsxCfgInfoReader(httpClient, new VimSessionManagerBuilder(vimClient.getVlsiClient()));
  }
  
  EsxCfgInfoDataProvider(VcClient vcClient, HostReader hostReader, EsxCfgInfoReader esxCfgInfoReader) {
    this._vcClient = vcClient;
    this._hostReader = hostReader;
    this._esxCfgInfoReader = esxCfgInfoReader;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema();
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    List<String> esxCfgInfoFilters = QueryUtil.getFilterPropertyComparableValues(query, "esxCfgInfo/filter");
    if (esxCfgInfoFilters.isEmpty())
      throw new IllegalArgumentException("No white-list filters provided for ESX cfg info. Cannot proceed with data collection."); 
    String esxCfgInfoFormat = (String)QueryUtil.getFilterPropertyComparableValue(query, "esxCfgInfo/format");
    if (StringUtils.isBlank(esxCfgInfoFormat))
      esxCfgInfoFormat = "xml"; 
    String esxCfgInfoSubtree = (String)QueryUtil.getFilterPropertyComparableValue(query, "esxCfgInfo/subtree");
    if (StringUtils.isBlank(esxCfgInfoSubtree))
      esxCfgInfoSubtree = "all"; 
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    Map<ManagedObjectReference, String> hostMoRefToEsxCfgInfo = collectEsxCfgInfo(this._hostReader, this._esxCfgInfoReader, query, esxCfgInfoFilters, esxCfgInfoFormat, esxCfgInfoSubtree);
    for (Map.Entry<ManagedObjectReference, String> hostMoRefToEsxCfgInfoEntry : hostMoRefToEsxCfgInfo.entrySet()) {
      ManagedObjectReference hostMoRef = hostMoRefToEsxCfgInfoEntry.getKey();
      String esxCfgInfo = hostMoRefToEsxCfgInfoEntry.getValue();
      if (!StringUtils.isBlank(esxCfgInfo))
        resultSetBuilder.item(hostMoRef, 
            
            buildResultItemPropertyValues(queryProperties, hostMoRef, esxCfgInfo, esxCfgInfoFilters, esxCfgInfoFormat, esxCfgInfoSubtree)); 
    } 
    return resultSetBuilder.build();
  }
  
  private static Map<ManagedObjectReference, String> collectEsxCfgInfo(HostReader hostReader, EsxCfgInfoReader esxCfgInfoReader, Query query, List<String> esxCfgInfoFilters, String esxCfgInfoFormat, String esxCfgInfoSubtree) {
    Map<ManagedObjectReference, String> hostMoRefToEsxCfgInfo = new HashMap<>();
    List<ManagedObjectReference> hostMoRefs = hostReader.getHostMoRefs();
    hostMoRefs = PageUtil.pageItems(hostMoRefs, query.getOffset(), query.getLimit());
    for (ManagedObjectReference hostMoRef : hostMoRefs) {
      ProductVersion hostVersion = hostReader.getHostVersion(hostMoRef);
      if (hostVersion != null && hostVersion
        .compareTo(MIN_SUPPORTED_HOST_VERSION) >= 0) {
        List<String> hostManagementIps = hostReader.getManagementIps(hostMoRef);
        String esxCfgInfo = esxCfgInfoReader.getEsxCfgInfo(hostManagementIps, esxCfgInfoFilters, esxCfgInfoFormat, esxCfgInfoSubtree);
        hostMoRefToEsxCfgInfo.put(hostMoRef, esxCfgInfo);
      } 
    } 
    return hostMoRefToEsxCfgInfo;
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<String, QuerySchema.ModelInfo>() {
      
      };
    return QuerySchema.forModels(models);
  }
  
  private static List<Object> buildResultItemPropertyValues(List<String> queryProperties, Object modelKey, String esxCfgInfo, List<String> esxCfgInfoFilters, String esxCfgInfoFormat, String esxCfgInfoSubtree) {
    List<Object> propertyValues = new ArrayList();
    propertyValues.add(modelKey);
    for (String property : queryProperties) {
      switch (QuerySchemaUtil.getActualPropertyName(property)) {
        case "esxCfgInfo":
          propertyValues.add(esxCfgInfo);
        case "esxCfgInfo/filter":
          propertyValues.add(esxCfgInfoFilters);
        case "esxCfgInfo/format":
          propertyValues.add(esxCfgInfoFormat);
        case "esxCfgInfo/subtree":
          propertyValues.add(esxCfgInfoSubtree);
      } 
    } 
    return propertyValues;
  }
}
