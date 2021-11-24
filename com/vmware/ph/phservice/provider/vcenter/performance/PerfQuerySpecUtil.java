package com.vmware.ph.phservice.provider.vcenter.performance;

import com.vmware.ph.phservice.common.internal.DateUtil;
import com.vmware.vim.binding.vim.PerformanceManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public final class PerfQuerySpecUtil {
  private static final String INSTANCE_PROPERTY_DEFAULT_VALUE = "*";
  
  public static PerformanceManager.QuerySpec[] createPerfQuerySpec(List<ManagedObjectReference> entityMoRefs, List<String> countersNames, PerfInterval perfInterval, Integer perfMaxSample, List<String> perfInstances, PerfMetadata perfMetadata) {
    PerformanceManager.QuerySpec[] perfQueries = new PerformanceManager.QuerySpec[entityMoRefs.size()];
    Iterator<ManagedObjectReference> entityMoRefsIterator = entityMoRefs.iterator();
    int i = 0;
    while (entityMoRefsIterator.hasNext()) {
      ManagedObjectReference entityMoRef = entityMoRefsIterator.next();
      PerformanceManager.QuerySpec perfQuery = createPerfQuerySpec(entityMoRef, countersNames, perfInterval, perfMaxSample, perfInstances, perfMetadata);
      perfQueries[i] = perfQuery;
      i++;
    } 
    return perfQueries;
  }
  
  private static PerformanceManager.QuerySpec createPerfQuerySpec(ManagedObjectReference entityMoRef, List<String> countersNames, PerfInterval perfInterval, Integer perfMaxSample, List<String> perfInstances, PerfMetadata perfMetadata) {
    Integer samplingPeriod = perfMetadata.getSamplingPeriodForInterval(perfInterval);
    if (samplingPeriod == null)
      throw new IllegalArgumentException("Cannot create spec for invalid performance interval: " + perfInterval); 
    Calendar startTime = getIntervalStartTime(perfInterval);
    List<PerformanceManager.MetricId> metricId = new ArrayList<>(countersNames.size());
    for (String counterName : countersNames) {
      Integer counterKey = perfMetadata.getCounterKeyForCounterName(counterName);
      if (counterKey == null)
        throw new IllegalArgumentException("Cannot create spec for invalid performance counter: " + counterName); 
      if (perfInstances != null && perfInstances.size() > 0) {
        for (String instance : perfInstances)
          metricId.add(new PerformanceManager.MetricId(counterKey.intValue(), instance)); 
        continue;
      } 
      metricId.add(new PerformanceManager.MetricId(counterKey.intValue(), "*"));
    } 
    PerformanceManager.QuerySpec spec = new PerformanceManager.QuerySpec();
    spec.intervalId = samplingPeriod;
    spec.startTime = startTime;
    spec.maxSample = perfMaxSample;
    spec.format = PerformanceManager.Format.csv.name();
    spec.entity = entityMoRef;
    spec.metricId = metricId.<PerformanceManager.MetricId>toArray(new PerformanceManager.MetricId[metricId.size()]);
    return spec;
  }
  
  private static Calendar getIntervalStartTime(PerfInterval perfInterval) {
    Calendar startTime;
    if (perfInterval == PerfInterval.REALTIME)
      return null; 
    int amountInDays = 0;
    switch (perfInterval) {
      case DAILY:
        amountInDays = -1;
        startTime = DateUtil.createUtcCalendar();
        startTime.add(5, amountInDays);
        return startTime;
      case WEEKLY:
        amountInDays = -7;
        startTime = DateUtil.createUtcCalendar();
        startTime.add(5, amountInDays);
        return startTime;
      case MONTHLY:
        amountInDays = -30;
        startTime = DateUtil.createUtcCalendar();
        startTime.add(5, amountInDays);
        return startTime;
      case YEARLY:
        amountInDays = -365;
        startTime = DateUtil.createUtcCalendar();
        startTime.add(5, amountInDays);
        return startTime;
    } 
    throw new IllegalArgumentException(String.format("Supported values for historical interval are %s", new Object[] { Arrays.toString(PerfInterval.values()) }));
  }
}
