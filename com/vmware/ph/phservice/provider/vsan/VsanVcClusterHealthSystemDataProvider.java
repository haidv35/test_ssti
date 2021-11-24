package com.vmware.ph.phservice.provider.vsan;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.ClusterReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanVcClusterHealthSystemReader;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSummary;
import com.vmware.vim.vsan.binding.vim.vsan.VsanHealthPerspective;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class VsanVcClusterHealthSystemDataProvider implements DataProvider {
  static final String RESOURCE_MODEL_NAME = "ClusterComputeResource";
  
  static final String VSAN_CLUSTER_HEALTH_SUMMARY_PROPERTY = "vsanClusterHealthSummary";
  
  static final String INCLUDE_OBJ_UUIDS_FILTER_PROPERTY = "vsanClusterHealthSummary/includeObjUuids";
  
  static final String FETCH_FROM_CACHE_FILTER_PROPERTY = "vsanClusterHealthSummary/fetchFromCache";
  
  static final String PERSPECTIVE_FILTER_PROPERTY = "vsanClusterHealthSummary/perspective";
  
  static final String INCLUDE_DP_HEALTH_FILTER_PROPERTY = "vsanClusterHealthSummary/includeDataProtectionHealth";
  
  private final ClusterReader _clusterReader;
  
  private final VsanVcClusterHealthSystemReader _healthSystemReader;
  
  private final VmodlContext _vsanVmodlContext;
  
  private final VmodlVersion _vsanVmodlVersion;
  
  public VsanVcClusterHealthSystemDataProvider(VcClient vcClient, VmomiClient vsanHealthVmomiClient) {
    this._clusterReader = new ClusterReader(vcClient);
    this._healthSystemReader = new VsanVcClusterHealthSystemReader(vsanHealthVmomiClient);
    this._vsanVmodlContext = vsanHealthVmomiClient.getVmodlContext();
    this._vsanVmodlVersion = vsanHealthVmomiClient.getVmodlVersion();
  }
  
  VsanVcClusterHealthSystemDataProvider(ClusterReader clusterReader, VsanVcClusterHealthSystemReader healthSystemReader, VmodlContext vsanVmodlContext, VmodlVersion vsanVmodlVersion) {
    this._clusterReader = clusterReader;
    this._healthSystemReader = healthSystemReader;
    this._vsanVmodlContext = vsanVmodlContext;
    this._vsanVmodlVersion = vsanVmodlVersion;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._vsanVmodlContext, this._vsanVmodlVersion, "ClusterComputeResource");
  }
  
  public ResultSet executeQuery(Query query) {
    List<ManagedObjectReference> clusterMoRefs = getClusterMoRefs(query);
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    String[] requestedQueryFelds = getRequestedSummaryFields(nonQualifiedQueryProperties);
    VsanClusterHealthSummaryFilter filter = getFilter(query);
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      VsanClusterHealthSummary healthSummary = this._healthSystemReader.queryVsanClusterHealthSummary(clusterMoRef, filter.includeObjUuids, requestedQueryFelds, filter.fetchFromCache, filter.perspective, filter.includeDataProtectionHealth);
      addVsanClusterHealthSummaryToResultSet(nonQualifiedQueryProperties, resultSetBuilder, "ClusterComputeResource", clusterMoRef, healthSummary);
    } 
    ResultSet result = resultSetBuilder.build();
    return result;
  }
  
  private List<ManagedObjectReference> getClusterMoRefs(Query query) {
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query);
    List<ManagedObjectReference> clusterMoRefs = VmodlQueryContextUtil.getMoRefsFromContext(queryContext, "ClusterComputeResource");
    if (clusterMoRefs == null || clusterMoRefs.isEmpty()) {
      int offset = query.getOffset();
      int limit = query.getLimit();
      clusterMoRefs = this._clusterReader.getClusterMoRefs(offset, limit);
    } 
    return clusterMoRefs;
  }
  
  private static String[] getRequestedSummaryFields(List<String> nonQualifiedQueryProperties) {
    String propertyPrefixToStrip = "vsanClusterHealthSummary/";
    boolean isWholeClusterSummaryRequested = false;
    Set<String> fields = new LinkedHashSet<>();
    for (String nonQualifiedQueryProperty : nonQualifiedQueryProperties) {
      if (nonQualifiedQueryProperty.startsWith("@"))
        continue; 
      if (nonQualifiedQueryProperty.equals("vsanClusterHealthSummary")) {
        isWholeClusterSummaryRequested = true;
        break;
      } 
      nonQualifiedQueryProperty = nonQualifiedQueryProperty.replaceFirst(propertyPrefixToStrip, "");
      int index = nonQualifiedQueryProperty.indexOf("/");
      if (index != -1)
        nonQualifiedQueryProperty = nonQualifiedQueryProperty.substring(0, index); 
      fields.add(nonQualifiedQueryProperty);
    } 
    if (isWholeClusterSummaryRequested)
      return null; 
    return fields.<String>toArray(new String[fields.size()]);
  }
  
  private static VsanClusterHealthSummaryFilter getFilter(Query query) {
    Object includeObjUuidsObj = QueryUtil.getFilterPropertyComparableValue(query, "vsanClusterHealthSummary/includeObjUuids");
    boolean includeObjUuids = (includeObjUuidsObj != null) ? Boolean.valueOf(includeObjUuidsObj.toString()).booleanValue() : false;
    Object fetchFromCacheObj = QueryUtil.getFilterPropertyComparableValue(query, "vsanClusterHealthSummary/includeObjUuids");
    boolean fetchFromCache = (fetchFromCacheObj != null) ? Boolean.valueOf(fetchFromCacheObj.toString()).booleanValue() : false;
    Object perspectiveObj = QueryUtil.getFilterPropertyComparableValue(query, "vsanClusterHealthSummary/perspective");
    VsanHealthPerspective perspective = (perspectiveObj != null) ? VsanHealthPerspective.valueOf(perspectiveObj.toString()) : null;
    Object includeDataProtectionHealthObj = QueryUtil.getFilterPropertyComparableValue(query, "vsanClusterHealthSummary/includeDataProtectionHealth");
    boolean includeDataProtectionHealth = (includeDataProtectionHealthObj != null) ? Boolean.valueOf(includeDataProtectionHealthObj.toString()).booleanValue() : false;
    VsanClusterHealthSummaryFilter filter = new VsanClusterHealthSummaryFilter();
    filter.includeObjUuids = includeObjUuids;
    filter.fetchFromCache = fetchFromCache;
    filter.perspective = perspective;
    filter.includeDataProtectionHealth = includeDataProtectionHealth;
    return filter;
  }
  
  private static ResultSet addVsanClusterHealthSummaryToResultSet(List<String> nonQualifiedQueryProperties, ResultSet.Builder resultSetBuilder, String resourceModelName, ManagedObjectReference clusterMoRef, VsanClusterHealthSummary vsanClusterHealthSummary) {
    String propertyPrefixToStrip = "vsanClusterHealthSummary/";
    ListIterator<String> queryPropertiesIterator = nonQualifiedQueryProperties.listIterator();
    while (queryPropertiesIterator.hasNext()) {
      String queryProperty = queryPropertiesIterator.next();
      queryPropertiesIterator.set(queryProperty
          .replaceFirst(propertyPrefixToStrip, ""));
    } 
    Map<String, Object> fallbackPropertyToValue = new HashMap<>();
    fallbackPropertyToValue.put("vsanClusterHealthSummary", vsanClusterHealthSummary);
    List<Object> propertyValues = DataProviderUtil.getPropertyValuesFromObjectAndValueMap(vsanClusterHealthSummary, clusterMoRef, nonQualifiedQueryProperties, fallbackPropertyToValue);
    resultSetBuilder.item(clusterMoRef, propertyValues);
    return resultSetBuilder.build();
  }
  
  private static QuerySchema createQuerySchema(VmodlContext vmodlContext, VmodlVersion vmodlVersion, String resourceModelName) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    QuerySchema.ModelInfo modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlClassesPropertiesToModelInfo(
        Arrays.asList((Class<?>[][])new Class[] { VsanClusterHealthSummary.class }, ), "vsanClusterHealthSummary", vmodlTypeMap, vmodlVersion);
    Map<String, QuerySchema.PropertyInfo> propertiesInfo = new TreeMap<>();
    propertiesInfo.put("vsanClusterHealthSummary", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanClusterHealthSummary/includeObjUuids", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanClusterHealthSummary/fetchFromCache", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanClusterHealthSummary/perspective", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanClusterHealthSummary/includeDataProtectionHealth", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    modelInfo = QuerySchema.ModelInfo.merge(
        Arrays.asList(new QuerySchema.ModelInfo[] { new QuerySchema.ModelInfo(propertiesInfo), modelInfo }));
    QuerySchema querySchema = QuerySchemaUtil.buildQuerySchemaFromModelInfo(resourceModelName, modelInfo);
    return querySchema;
  }
  
  private static class VsanClusterHealthSummaryFilter {
    boolean includeObjUuids;
    
    boolean fetchFromCache;
    
    VsanHealthPerspective perspective;
    
    boolean includeDataProtectionHealth;
    
    private VsanClusterHealthSummaryFilter() {}
  }
}
