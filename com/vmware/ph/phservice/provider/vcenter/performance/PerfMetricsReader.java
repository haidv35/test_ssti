package com.vmware.ph.phservice.provider.vcenter.performance;

import com.google.common.base.Objects;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.vim.HistoricalInterval;
import com.vmware.vim.binding.vim.PerformanceManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.ManagedObjectNotFound;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerfMetricsReader {
  private static final Log _log = LogFactory.getLog(PerfMetricsReader.class);
  
  private static final int MAX_QUERY_STATS_RETRY_COUNT = 10;
  
  private final Object _lock = new Object();
  
  private final VcClient _vcClient;
  
  private volatile PerformanceManager _perfManager;
  
  private volatile PerfMetadata _perfMetadata;
  
  public PerfMetricsReader(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  public List<PerfEntityMetrics> collectPerformanceData(List<ManagedObjectReference> entityMoRefs, List<String> countersNames, PerfInterval perfInterval, Integer perfMaxSample, List<String> perfInstances) {
    initializePerfManagerOnce();
    initializePerfMetdataOnce();
    PerformanceManager.QuerySpec[] querySpecs = PerfQuerySpecUtil.createPerfQuerySpec(entityMoRefs, countersNames, perfInterval, perfMaxSample, perfInstances, this._perfMetadata);
    PerformanceManager.EntityMetricBase[] entityMetrics = queryStats(querySpecs);
    List<PerfEntityMetrics> perfEntityMetrics = PerfEntityMetricsUtil.convertCsvEntityMetricToPerfMetrics(entityMoRefs, entityMetrics, this._perfMetadata);
    return perfEntityMetrics;
  }
  
  private PerformanceManager.EntityMetricBase[] queryStats(PerformanceManager.QuerySpec[] querySpecs) {
    try {
      PerformanceManager.EntityMetricBase[] entityMetrics = this._perfManager.queryStats(querySpecs);
      return entityMetrics;
    } catch (ManagedObjectNotFound e) {
      _log.warn("Could not fetch performance metrics because an entity was removed during query execution. Will retry again in chunks.", (Throwable)e);
      List<PerformanceManager.QuerySpec> filteredQuerySpecs = filterMoRefQuerySpec(querySpecs, e
          .getObj());
      return queryStatsWithPaging(filteredQuerySpecs);
    } 
  }
  
  private void initializePerfManagerOnce() {
    if (this._perfManager != null)
      return; 
    synchronized (this._lock) {
      if (this._perfManager != null)
        return; 
      ManagedObjectReference perfManagerMoRef = this._vcClient.getServiceInstanceContent().getPerfManager();
      this._perfManager = this._vcClient.<PerformanceManager>createMo(perfManagerMoRef);
    } 
  }
  
  private PerfMetadata initializePerfMetdataOnce() {
    if (this._perfMetadata != null)
      return this._perfMetadata; 
    synchronized (this._lock) {
      if (this._perfMetadata != null)
        return this._perfMetadata; 
      PerformanceManager.CounterInfo[] counterInfos = this._perfManager.getPerfCounter();
      HistoricalInterval[] historicalIntervals = this._perfManager.getHistoricalInterval();
      this
        ._perfMetadata = PerfMetadataBuilder.buildPerfMetadata(counterInfos, historicalIntervals);
      return this._perfMetadata;
    } 
  }
  
  private PerformanceManager.EntityMetricBase[] queryStatsWithPaging(List<PerformanceManager.QuerySpec> querySpecs) {
    int entitiesCount = querySpecs.size();
    int pageSize = getQuerySpecPageSize(entitiesCount);
    List<PerformanceManager.EntityMetricBase> entityMetrics = new ArrayList<>(entitiesCount);
    List<Integer> pagesOffsets = PageUtil.getPagesOffsets(entitiesCount, pageSize);
    for (Iterator<Integer> iterator = pagesOffsets.iterator(); iterator.hasNext(); ) {
      int pageOffset = ((Integer)iterator.next()).intValue();
      List<PerformanceManager.QuerySpec> pageQuerySpecsList = PageUtil.pageItems(querySpecs, pageOffset, pageSize);
      PerformanceManager.QuerySpec[] pageQuerySpecs = pageQuerySpecsList.<PerformanceManager.QuerySpec>toArray(new PerformanceManager.QuerySpec[pageQuerySpecsList.size()]);
      try {
        if (!ArrayUtils.isEmpty((Object[])pageQuerySpecs)) {
          PerformanceManager.EntityMetricBase[] pageEntityMetrics = this._perfManager.queryStats(pageQuerySpecs);
          if (!ArrayUtils.isEmpty((Object[])pageEntityMetrics))
            entityMetrics.addAll(Arrays.asList(pageEntityMetrics)); 
        } 
      } catch (ManagedObjectNotFound e) {
        _log.warn("Could not fetch performance metrics page because an entity was removed during query execution.", (Throwable)e);
      } 
    } 
    return entityMetrics.<PerformanceManager.EntityMetricBase>toArray(new PerformanceManager.EntityMetricBase[entityMetrics.size()]);
  }
  
  private static int getQuerySpecPageSize(int entitiesCount) {
    int pageSize = (entitiesCount + 10 - 1) / 10;
    return pageSize;
  }
  
  private static List<PerformanceManager.QuerySpec> filterMoRefQuerySpec(PerformanceManager.QuerySpec[] querySpecs, ManagedObjectReference filterMoRef) {
    if (filterMoRef == null)
      return Arrays.asList(querySpecs); 
    List<PerformanceManager.QuerySpec> filteredQuerySpecs = new ArrayList<>(querySpecs.length - 1);
    for (PerformanceManager.QuerySpec querySpec : querySpecs) {
      if (!Objects.equal(filterMoRef, querySpec.getEntity()))
        filteredQuerySpecs.add(querySpec); 
    } 
    return filteredQuerySpecs;
  }
}
