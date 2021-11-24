package com.vmware.ph.phservice.common.vsan;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.TimeIntervalUtil;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfEntityMetricCSV;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfMetricSeriesCSV;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfQuerySpec;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerformanceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanPerformanceManagerReader {
  public static final ManagedObjectReference VSAN_PERF_MANAGER_MO_REF = new ManagedObjectReference("VsanPerformanceManager", "vsan-performance-manager");
  
  private static final int HOUR_TO_MILLIS = 3600000;
  
  private static final int PERF_DIAGNOSIS_MAX_TIME_FOR_DATACOLLECTION_SECONDS = 90;
  
  private static final Log _log = LogFactory.getLog(VsanPerformanceManagerReader.class);
  
  private final VmomiClient _vsanHealthClient;
  
  private final ExecutorService _executorService;
  
  public VsanPerformanceManagerReader(VmomiClient vsanHealthClient, ExecutorService executorService) {
    this._vsanHealthClient = vsanHealthClient;
    this._executorService = executorService;
  }
  
  public ManagedObjectReference getMoRef() throws Exception {
    VsanPerformanceManager vsanPerfManager = this._vsanHealthClient.<VsanPerformanceManager>createStub(VSAN_PERF_MANAGER_MO_REF);
    return vsanPerfManager._getRef();
  }
  
  public VsanPerfEntityMetricCSV[] queryVsanPerf(VsanPerfQuerySpec[] querySpecs, ManagedObjectReference clusterMoRef) throws Exception {
    VsanPerformanceManager vsanPerfManager = this._vsanHealthClient.<VsanPerformanceManager>createStub(VSAN_PERF_MANAGER_MO_REF);
    List<Future<VsanPerfEntityMetricCSV[]>> _queryTasks = new ArrayList<>();
    for (VsanPerfQuerySpec querySpec : querySpecs) {
      Future<VsanPerfEntityMetricCSV[]> task = (Future)this._executorService.submit(new EntityMetricsCsvCallable(querySpec, clusterMoRef, vsanPerfManager));
      _queryTasks.add(task);
    } 
    List<VsanPerfEntityMetricCSV> result = new ArrayList<>();
    for (Future<VsanPerfEntityMetricCSV[]> task : _queryTasks) {
      VsanPerfEntityMetricCSV[] entityCsv = task.get(90L, TimeUnit.SECONDS);
      result.addAll(Arrays.asList(entityCsv));
    } 
    return result.<VsanPerfEntityMetricCSV>toArray(new VsanPerfEntityMetricCSV[result.size()]);
  }
  
  public VsanPerfEntityMetricCSV[] queryVsanPerf(Map<String, String[]> entityTypeToFields, Calendar startTime, Calendar endTime, ManagedObjectReference clusterMoRef) {
    List<VsanPerfEntityMetricCSV[]> timeIntervalEntityMetrics = (List)new ArrayList<>();
    List<Pair<Calendar, Calendar>> intervals = TimeIntervalUtil.splitInterval(new Pair<>(startTime, endTime), 3600000L);
    VsanPerfEntityMetricCSV[] result = null;
    try {
      for (Pair<Calendar, Calendar> interval : intervals) {
        VsanPerfQuerySpec[] querySpecs = createVsanPerfQuerySpecs(entityTypeToFields, interval
            
            .getFirst(), interval
            .getSecond());
        VsanPerfEntityMetricCSV[] entityMetricCsv = queryVsanPerf(querySpecs, clusterMoRef);
        timeIntervalEntityMetrics.add(entityMetricCsv);
      } 
      if (timeIntervalEntityMetrics.size() > 1) {
        result = combinePerfQueryResults(timeIntervalEntityMetrics);
      } else if (timeIntervalEntityMetrics.size() == 1) {
        result = timeIntervalEntityMetrics.get(0);
      } 
    } catch (Exception e) {
      if (_log.isDebugEnabled()) {
        _log.debug("Error occurred while retrieving result from VsanPerformanceManager, so returning emtpy result.", e);
      } else if (_log.isWarnEnabled()) {
        _log.warn("Error occurred while retrieving result from VsanPerformanceManager, so returning emtpy result.:" + e
            
            .getMessage());
      } 
    } 
    return (result != null) ? result : new VsanPerfEntityMetricCSV[0];
  }
  
  static VsanPerfEntityMetricCSV[] combinePerfQueryResults(List<VsanPerfEntityMetricCSV[]> queryResults) {
    List<VsanPerfEntityMetricCSV> result = null;
    Map<String, VsanPerfEntityMetricCSV> entityRefIdToMergedMetricCSV = new LinkedHashMap<>();
    for (VsanPerfEntityMetricCSV[] queryResult : queryResults) {
      if (result == null) {
        result = Arrays.asList(queryResult);
        for (VsanPerfEntityMetricCSV perfEntityMetric : queryResult)
          entityRefIdToMergedMetricCSV.put(perfEntityMetric.entityRefId, perfEntityMetric); 
        continue;
      } 
      for (VsanPerfEntityMetricCSV perfEntityMetric : queryResult) {
        VsanPerfEntityMetricCSV mergedPerfEntityMetric = entityRefIdToMergedMetricCSV.get(perfEntityMetric.entityRefId);
        if (mergedPerfEntityMetric != null) {
          merge(mergedPerfEntityMetric, perfEntityMetric);
        } else {
          result.add(perfEntityMetric);
          entityRefIdToMergedMetricCSV.put(perfEntityMetric.entityRefId, perfEntityMetric);
        } 
      } 
    } 
    return result.<VsanPerfEntityMetricCSV>toArray(new VsanPerfEntityMetricCSV[result.size()]);
  }
  
  static void merge(VsanPerfEntityMetricCSV perfEntityMetric1, VsanPerfEntityMetricCSV perfEntityMetric2) {
    boolean isContiguous = false;
    String[] entity1SampleInfo = perfEntityMetric1.sampleInfo.split(",");
    String entity1LastTs = entity1SampleInfo[entity1SampleInfo.length - 1];
    String[] entity2SampleInfo = perfEntityMetric2.sampleInfo.split(",");
    String entity2FirstTs = entity2SampleInfo[0];
    if (entity1LastTs.equals(entity2FirstTs))
      isContiguous = true; 
    if (isContiguous) {
      if (entity2SampleInfo.length > 1) {
        int index = perfEntityMetric2.sampleInfo.indexOf(',');
        perfEntityMetric1
          .sampleInfo = perfEntityMetric1.sampleInfo + perfEntityMetric2.sampleInfo.substring(index);
      } else {
        return;
      } 
    } else {
      perfEntityMetric1.sampleInfo += "," + perfEntityMetric2.sampleInfo;
    } 
    Arrays.sort(perfEntityMetric1.value, new VsanPerfMetricSeriesCSVComparator());
    Arrays.sort(perfEntityMetric2.value, new VsanPerfMetricSeriesCSVComparator());
    for (int i = 0; i < perfEntityMetric1.value.length; i++) {
      VsanPerfMetricSeriesCSV value1 = perfEntityMetric1.value[i];
      VsanPerfMetricSeriesCSV value2 = perfEntityMetric2.value[i];
      if (!value1.metricId.label.equals(value2.metricId.label) && 
        _log.isDebugEnabled())
        _log.debug(
            String.format("Exception. Mismatch: %s %s", new Object[] { value1.metricId.label, value2.metricId.label })); 
      if (isContiguous) {
        int index = value2.values.indexOf(',');
        value1.values += value2.values.substring(index);
      } else {
        value1.values += "," + value2.values;
      } 
    } 
  }
  
  private static VsanPerfQuerySpec[] createVsanPerfQuerySpecs(Map<String, String[]> entityTypeToFields, Calendar startTime, Calendar endTime) {
    List<VsanPerfQuerySpec> querySpecs = new ArrayList<>();
    for (Map.Entry<String, String[]> entry : entityTypeToFields.entrySet()) {
      String entityType = entry.getKey();
      String[] fields = entry.getValue();
      VsanPerfQuerySpec querySpec = createVsanPerfQuerySpec(entityType, fields, startTime, endTime);
      querySpecs.add(querySpec);
    } 
    return querySpecs.<VsanPerfQuerySpec>toArray(new VsanPerfQuerySpec[querySpecs.size()]);
  }
  
  private static VsanPerfQuerySpec createVsanPerfQuerySpec(String entityType, String[] fields, Calendar startTime, Calendar endTime) {
    VsanPerfQuerySpec querySpec = new VsanPerfQuerySpec();
    String entityRefId = entityType + ":*";
    querySpec.setEntityRefId(entityRefId);
    if (fields.length == 1 && fields[0].equals("All"))
      fields = null; 
    querySpec.setLabels(fields);
    querySpec.setStartTime(startTime);
    querySpec.setEndTime(endTime);
    return querySpec;
  }
  
  private static class EntityMetricsCsvCallable implements Callable<VsanPerfEntityMetricCSV[]> {
    private final VsanPerfQuerySpec _perfQuerySpec;
    
    private final ManagedObjectReference _clusterMoRef;
    
    private final VsanPerformanceManager _vsanPerfManager;
    
    public EntityMetricsCsvCallable(VsanPerfQuerySpec perfQuerySpec, ManagedObjectReference clusterMoRef, VsanPerformanceManager vsanPerfManager) {
      this._perfQuerySpec = perfQuerySpec;
      this._clusterMoRef = clusterMoRef;
      this._vsanPerfManager = vsanPerfManager;
    }
    
    public VsanPerfEntityMetricCSV[] call() throws Exception {
      VsanPerfEntityMetricCSV[] entityMetricsCsv = this._vsanPerfManager.queryVsanPerf(new VsanPerfQuerySpec[] { this._perfQuerySpec }, this._clusterMoRef);
      return entityMetricsCsv;
    }
  }
  
  private static class VsanPerfMetricSeriesCSVComparator implements Comparator<VsanPerfMetricSeriesCSV> {
    private VsanPerfMetricSeriesCSVComparator() {}
    
    public int compare(VsanPerfMetricSeriesCSV o1, VsanPerfMetricSeriesCSV o2) {
      return o1.metricId.label.compareTo(o2.metricId.label);
    }
  }
}
