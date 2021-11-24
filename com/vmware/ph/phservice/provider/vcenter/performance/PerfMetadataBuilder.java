package com.vmware.ph.phservice.provider.vcenter.performance;

import com.vmware.vim.binding.vim.HistoricalInterval;
import com.vmware.vim.binding.vim.PerformanceManager;
import java.util.HashMap;
import java.util.Map;

public class PerfMetadataBuilder {
  private static final int REALTIME_SAMPLING_PERIOD_SECONDS = 20;
  
  private static final String PERF_COUNTER_NAME_PATTERN = "%s.%s.%s.%s.%s";
  
  public static PerfMetadata buildPerfMetadata(PerformanceManager.CounterInfo[] counterInfos, HistoricalInterval[] historicalIntervals) {
    Map<String, Integer> counterNameToCounterKey = new HashMap<>(counterInfos.length);
    Map<Integer, String> counterKeyToCounterName = new HashMap<>(counterInfos.length);
    mapCounterNamesAndKeys(counterInfos, counterNameToCounterKey, counterKeyToCounterName);
    Map<Integer, Integer> intervalToSamplingPeriod = mapIntervalToSamplingPeriod(historicalIntervals);
    return new PerfMetadata(counterNameToCounterKey, counterKeyToCounterName, intervalToSamplingPeriod);
  }
  
  private static void mapCounterNamesAndKeys(PerformanceManager.CounterInfo[] counterInfos, Map<String, Integer> counterNameToCounterKey, Map<Integer, String> counterKeyToCounterName) {
    for (PerformanceManager.CounterInfo counterInfo : counterInfos) {
      String counterName = String.format("%s.%s.%s.%s.%s", new Object[] { counterInfo.groupInfo.key, counterInfo.nameInfo.key, counterInfo.rollupType, counterInfo.statsType, counterInfo.unitInfo.key });
      int counterKey = counterInfo.getKey();
      counterNameToCounterKey.put(counterName, Integer.valueOf(counterKey));
      counterKeyToCounterName.put(Integer.valueOf(counterKey), counterName);
    } 
  }
  
  private static Map<Integer, Integer> mapIntervalToSamplingPeriod(HistoricalInterval[] historicalIntervals) {
    Map<Integer, Integer> intervalKeyToSamplingPeriod = new HashMap<>(historicalIntervals.length);
    for (HistoricalInterval historicalInterval : historicalIntervals)
      intervalKeyToSamplingPeriod.put(historicalInterval.key, 
          Integer.valueOf(historicalInterval.samplingPeriod)); 
    intervalKeyToSamplingPeriod.put(Integer.valueOf(PerfInterval.REALTIME.getKey()), 
        Integer.valueOf(20));
    return intervalKeyToSamplingPeriod;
  }
}
