package com.vmware.ph.phservice.provider.vcenter.performance;

import com.vmware.vim.binding.vim.PerformanceManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PerfEntityMetricsUtil {
  private static final Log _log = LogFactory.getLog(PerfEntityMetricsUtil.class);
  
  static final List<Object[]> EMPTY_ENTITY_METRICS = Collections.emptyList();
  
  public static List<PerfEntityMetrics> convertCsvEntityMetricToPerfMetrics(List<ManagedObjectReference> entityMoRefs, PerformanceManager.EntityMetricBase[] entityMetrics, PerfMetadata perfMetadata) {
    Map<ManagedObjectReference, PerfEntityMetrics> entityMoRefToPerfMetrics = createEmptyEntityMoRefToPerfMetrics(entityMoRefs);
    if (!ArrayUtils.isEmpty((Object[])entityMetrics)) {
      Map<ManagedObjectReference, PerfEntityMetrics> convertedEntityMoRefToPerfMetrics = convertEntityMetricCsvToPerfMetrics(entityMetrics, perfMetadata);
      entityMoRefToPerfMetrics.putAll(convertedEntityMoRefToPerfMetrics);
    } 
    return new ArrayList<>(entityMoRefToPerfMetrics.values());
  }
  
  private static Map<ManagedObjectReference, PerfEntityMetrics> createEmptyEntityMoRefToPerfMetrics(List<ManagedObjectReference> entityMoRefs) {
    Map<ManagedObjectReference, PerfEntityMetrics> entityMoRefToPerfMetrics = new LinkedHashMap<>(entityMoRefs.size());
    for (ManagedObjectReference entityMoRef : entityMoRefs) {
      PerfEntityMetrics emptyEntityMetric = createEmptyEntityMetrics(entityMoRef);
      entityMoRefToPerfMetrics.put(entityMoRef, emptyEntityMetric);
    } 
    return entityMoRefToPerfMetrics;
  }
  
  private static Map<ManagedObjectReference, PerfEntityMetrics> convertEntityMetricCsvToPerfMetrics(PerformanceManager.EntityMetricBase[] entityMetrics, PerfMetadata perfMetadata) {
    Map<ManagedObjectReference, PerfEntityMetrics> entityMetricsToPerfMetrics = new LinkedHashMap<>(entityMetrics.length);
    for (PerformanceManager.EntityMetricBase entityMetric : entityMetrics) {
      PerfEntityMetrics perfEntityMetric = convertEntityMetricCsvToPerfMetrics(perfMetadata, (PerformanceManager.EntityMetricCSV)entityMetric);
      entityMetricsToPerfMetrics.put(entityMetric.entity, perfEntityMetric);
    } 
    return entityMetricsToPerfMetrics;
  }
  
  private static PerfEntityMetrics convertEntityMetricCsvToPerfMetrics(PerfMetadata performanceMetadata, PerformanceManager.EntityMetricCSV entityMetric) {
    String entityMoRefValue = entityMetric.entity.getValue();
    _log.trace(
        String.format("Converting collected metrics for entity of type [%s] and value [%s]", new Object[] { entityMetric.entity.getType(), entityMoRefValue }));
    if (StringUtils.isBlank(entityMetric.sampleInfoCSV)) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("sampleInfo is null or empty for entity %s.", new Object[] { entityMetric.entity })); 
      return createEmptyEntityMetrics(entityMetric.entity);
    } 
    String[] sampleInfo = entityMetric.sampleInfoCSV.split(",");
    List<Object[]> perfMetrics = new LinkedList();
    for (PerformanceManager.MetricSeriesCSV csvMetric : entityMetric.value) {
      String counterName = performanceMetadata.getCounterNameForCounterKey(csvMetric.id.counterId);
      if (StringUtils.isBlank(csvMetric.value)) {
        if (_log.isTraceEnabled())
          _log.trace(
              String.format("Found null csvMetric.value for entity [%s] and counter [%s].", new Object[] { entityMoRefValue, counterName })); 
      } else {
        String[] samples = csvMetric.value.split(",");
        String instance = (csvMetric.id.instance == null) ? "" : csvMetric.id.instance.trim();
        for (int i = 0; i < sampleInfo.length && i / 2 < samples.length; i += 2) {
          int samplingPeriod;
          long sampleValue;
          try {
            samplingPeriod = Integer.valueOf(sampleInfo[i]).intValue();
          } catch (NumberFormatException e) {
            if (_log.isTraceEnabled())
              _log.trace(
                  String.format("Found non-int sampling period [%s] while converting perf stats for entity [%s]", new Object[] { sampleInfo[i], entityMoRefValue })); 
          } 
          try {
            sampleValue = Long.valueOf(samples[i / 2]).longValue();
          } catch (NumberFormatException e) {
            if (_log.isTraceEnabled())
              _log.trace(
                  String.format("Found non-long sample value [%s] while converting perf stats for counter [%s] of entity %s.", new Object[] { samples[i / 2], counterName, entityMoRefValue })); 
          } 
          String timestamp = sampleInfo[i + 1];
          Object[] perfMetric = { counterName, Integer.valueOf(samplingPeriod), timestamp, Long.valueOf(sampleValue), instance };
          perfMetrics.add(perfMetric);
        } 
      } 
    } 
    return new PerfEntityMetrics(entityMetric.entity, perfMetrics);
  }
  
  private static PerfEntityMetrics createEmptyEntityMetrics(ManagedObjectReference entityMoRef) {
    return new PerfEntityMetrics(entityMoRef, EMPTY_ENTITY_METRICS);
  }
}
