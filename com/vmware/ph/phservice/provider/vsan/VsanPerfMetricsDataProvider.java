package com.vmware.ph.phservice.provider.vsan;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.ClusterReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanClusterReader;
import com.vmware.ph.phservice.common.vsan.VsanPerformanceManagerReader;
import com.vmware.ph.phservice.provider.common.QueryContext;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlQueryContextUtil;
import com.vmware.ph.phservice.provider.vsan.internal.QueryFilterToVsanPerformanceDataConverter;
import com.vmware.ph.phservice.provider.vsan.internal.VsanPerfEntityMetricsUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfEntityMetricCSV;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanPerfMetricsDataProvider implements DataProvider {
  public static final String RESOURCE_MODEL_TYPE = "ClusterComputeResource";
  
  public static final String PERF_ENTITY_METRICS_FREE_FORM_PROPERTY = "vsanPerfEntityMetrics";
  
  public static final String PERF_ENTITY_METRICS_CSV_VMODL_PROPERTY = "vsanPerfEntityMetricsCSV";
  
  public static final String PERF_STATS_QUERY_DATA_FILTER_PROPERTY = "vsanPerfStatsQueryData";
  
  public static final String PERF_START_TIME_FILTER_PROPERTY = "vsanPerfStartTime";
  
  public static final String PERF_END_TIME_FILTER_PROPERTY = "vsanPerfEndTime";
  
  private static final Log _log = LogFactory.getLog(VsanPerfMetricsDataProvider.class);
  
  private final ClusterReader _clusterReader;
  
  private final VsanClusterReader _vsanClusterReader;
  
  private final VsanPerformanceManagerReader _vsanPerformanceManagerReader;
  
  private final QueryFilterToVsanPerformanceDataConverter _queryFilterConverter;
  
  public VsanPerfMetricsDataProvider(VmomiClient vsanHealthVmomiClient, VcClient vcClient, VmodlToJsonLdSerializer serializer, ExecutorService executorService) {
    this(new ClusterReader(vcClient), new VsanClusterReader(vsanHealthVmomiClient, vcClient), new VsanPerformanceManagerReader(vsanHealthVmomiClient, executorService), new QueryFilterToVsanPerformanceDataConverter(serializer));
  }
  
  VsanPerfMetricsDataProvider(ClusterReader clusterReader, VsanClusterReader vsanClusterReader, VsanPerformanceManagerReader vsanPerformanceManagerReader, QueryFilterToVsanPerformanceDataConverter queryFilterConverter) {
    this._clusterReader = clusterReader;
    this._vsanClusterReader = vsanClusterReader;
    this._vsanPerformanceManagerReader = vsanPerformanceManagerReader;
    this._queryFilterConverter = queryFilterConverter;
  }
  
  public QuerySchema getSchema() {
    QuerySchema querySchema = createQuerySchema();
    return querySchema;
  }
  
  public ResultSet executeQuery(Query query) {
    List<ManagedObjectReference> clusterMoRefs = getClusterMoRefs(query);
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      List<Object> values = getClusterPropertyValues(query, clusterMoRef);
      resultSetBuilder.item(values.get(0), values);
    } 
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
  
  private List<ManagedObjectReference> getClusterMoRefs(Query query) {
    String resourceModel = query.getResourceModels().iterator().next();
    QueryContext queryContext = QueryContextUtil.getQueryContextFromQueryFilter(query);
    List<ManagedObjectReference> moRefs = VmodlQueryContextUtil.getMoRefsFromContext(queryContext, resourceModel);
    if (!moRefs.isEmpty())
      return moRefs; 
    List<ManagedObjectReference> clusterMoRefs = this._clusterReader.getClusterMoRefs(query.getOffset(), query.getLimit());
    List<ManagedObjectReference> vsanClusterMoRefs = new ArrayList<>();
    for (ManagedObjectReference clusterMoRef : clusterMoRefs) {
      if (this._vsanClusterReader.isVsanEnabled(clusterMoRef))
        vsanClusterMoRefs.add(clusterMoRef); 
    } 
    return vsanClusterMoRefs;
  }
  
  private List<Object> getClusterPropertyValues(Query query, ManagedObjectReference clusterMoRef) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    Map<String, String[]> entityTypeToFields = this._queryFilterConverter.gePerfStatsQueryDataFilterProperty(query, "vsanPerfStatsQueryData");
    Calendar startTime = this._queryFilterConverter.getCalendarFilterProperty(query, "vsanPerfStartTime");
    Calendar endTime = this._queryFilterConverter.getCalendarFilterProperty(query, "vsanPerfEndTime");
    List<Object> propertyValues = new ArrayList();
    for (String nonQualifiedQueryProperty : nonQualifiedQueryProperties) {
      Object propertyValue = getPropertyValue(query, nonQualifiedQueryProperty, entityTypeToFields, startTime, endTime, clusterMoRef);
      propertyValues.add(propertyValue);
    } 
    return propertyValues;
  }
  
  private Object getPropertyValue(Query query, String nonQualifiedQueryProperty, Map<String, String[]> entityTypeToFields, Calendar startTime, Calendar endTime, ManagedObjectReference clusterMoRef) {
    Object<Object[]> result = null;
    try {
      if (QuerySchemaUtil.isQueryPropertyModelKey(nonQualifiedQueryProperty)) {
        result = (Object<Object[]>)clusterMoRef;
      } else if ("vsanPerfEntityMetrics".equals(nonQualifiedQueryProperty)) {
        VsanPerfEntityMetricCSV[] entityMetrics = getPerfData(entityTypeToFields, startTime, endTime, clusterMoRef);
        result = (Object<Object[]>)VsanPerfEntityMetricsUtil.convertEntityMetricCsvToFreeFormList(entityMetrics);
      } else if ("vsanPerfEntityMetricsCSV".equals(nonQualifiedQueryProperty)) {
        VsanPerfEntityMetricCSV[] entityMetrics = getPerfData(entityTypeToFields, startTime, endTime, clusterMoRef);
        VsanPerfEntityMetricCSV[] arrayOfVsanPerfEntityMetricCSV1 = entityMetrics;
      } else if ("vsanPerfStatsQueryData".equals(nonQualifiedQueryProperty)) {
        Map<String, String[]> map = entityTypeToFields;
      } else if ("vsanPerfStartTime".equals(nonQualifiedQueryProperty)) {
        result = (Object<Object[]>)startTime;
      } else if ("vsanPerfEndTime".equals(nonQualifiedQueryProperty)) {
        result = (Object<Object[]>)endTime;
      } 
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      if (_log.isInfoEnabled())
        _log.info("Failed to obtain value for property " + nonQualifiedQueryProperty, e); 
    } 
    return result;
  }
  
  private VsanPerfEntityMetricCSV[] getPerfData(Map<String, String[]> entityTypeToFields, Calendar startTime, Calendar endTime, ManagedObjectReference clusterMoRef) {
    VsanPerfEntityMetricCSV[] perfStatsResult = this._vsanPerformanceManagerReader.queryVsanPerf(entityTypeToFields, startTime, endTime, clusterMoRef);
    return perfStatsResult;
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.PropertyInfo> propertiesInfo = new TreeMap<>();
    propertiesInfo.put("vsanPerfEntityMetrics", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanPerfEntityMetricsCSV", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanPerfStatsQueryData", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanPerfStartTime", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    propertiesInfo.put("vsanPerfEndTime", 
        
        QuerySchema.PropertyInfo.forNonFilterableProperty());
    QuerySchema.ModelInfo modelInfo = new QuerySchema.ModelInfo(propertiesInfo);
    Map<String, QuerySchema.ModelInfo> models = new TreeMap<>();
    models.put("ClusterComputeResource", modelInfo);
    return QuerySchema.forModels(models);
  }
}
