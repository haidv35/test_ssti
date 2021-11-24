package com.vmware.ph.phservice.provider.vsan.internal;

import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfEntityMetricCSV;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfMetricSeriesCSV;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class VsanPerfEntityMetricsUtil {
  private static final Log _log = LogFactory.getLog(VsanPerfEntityMetricsUtil.class);
  
  static final List<Object[]> EMPTY_ENTITY_METRICS = Collections.emptyList();
  
  public static List<Object[]> convertEntityMetricCsvToFreeFormList(VsanPerfEntityMetricCSV[] entityMetrics) {
    List<Object[]> result = new ArrayList();
    for (VsanPerfEntityMetricCSV entityMetric : entityMetrics) {
      VsanPerfEntityMetrics perfEntityMetric = convertEntityMetricCsvToPerfMetrics(entityMetric);
      result.addAll(perfEntityMetric.getEntityMetrics());
    } 
    return result;
  }
  
  private static VsanPerfEntityMetrics convertEntityMetricCsvToPerfMetrics(VsanPerfEntityMetricCSV entityMetric) {
    String entityRefId = entityMetric.entityRefId;
    _log.trace(
        String.format("Converting collected metrics for entity [%s]", new Object[] { entityRefId }));
    if (StringUtils.isBlank(entityMetric.sampleInfo)) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("sampleInfo is null or empty for entity %s.", new Object[] { entityRefId })); 
      return createEmptyEntityMetrics(entityRefId);
    } 
    String[] sampleInfo = entityMetric.sampleInfo.split(",");
    List<Object[]> perfMetrics = new LinkedList();
    for (VsanPerfMetricSeriesCSV csvMetric : entityMetric.value) {
      String counterLabel = csvMetric.metricId.label;
      Integer samplingPeriod = csvMetric.metricId.metricsCollectInterval;
      if (StringUtils.isBlank(csvMetric.values)) {
        if (_log.isTraceEnabled())
          _log.trace(
              String.format("Found null csvMetric.values for entity [%s] and counter [%s].", new Object[] { entityRefId, counterLabel })); 
      } else {
        String[] samples = csvMetric.values.split(",");
        for (int i = 0; i < samples.length; i++) {
          long sampleValue;
          String timestamp = sampleInfo[i];
          try {
            sampleValue = Long.valueOf(samples[i]).longValue();
          } catch (NumberFormatException e) {
            if (_log.isTraceEnabled())
              _log.trace(
                  String.format("Found non-long sample value [%s] while converting perf stats for counter [%s] of entity %s.", new Object[] { samples[i], counterLabel, entityRefId })); 
          } 
          Object[] perfMetric = { counterLabel, samplingPeriod, timestamp, Long.valueOf(sampleValue), entityRefId };
          perfMetrics.add(perfMetric);
        } 
      } 
    } 
    return new VsanPerfEntityMetrics(entityRefId, perfMetrics);
  }
  
  private static VsanPerfEntityMetrics createEmptyEntityMetrics(String entityRefId) {
    return new VsanPerfEntityMetrics(entityRefId, EMPTY_ENTITY_METRICS);
  }
}
