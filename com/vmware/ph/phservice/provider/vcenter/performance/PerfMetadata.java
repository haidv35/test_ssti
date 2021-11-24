package com.vmware.ph.phservice.provider.vcenter.performance;

import java.util.Collections;
import java.util.Map;

public class PerfMetadata {
  private final Map<String, Integer> _counterNameToCounterKey;
  
  private final Map<Integer, String> _counterKeyToCounterName;
  
  private final Map<Integer, Integer> _intervalKeyToSamplingPeriod;
  
  public PerfMetadata(Map<String, Integer> counterNameToCounterKey, Map<Integer, String> counterKeyToCounterName, Map<Integer, Integer> intervalKeyToSamplingPeriod) {
    this._counterNameToCounterKey = Collections.unmodifiableMap(counterNameToCounterKey);
    this._counterKeyToCounterName = Collections.unmodifiableMap(counterKeyToCounterName);
    this._intervalKeyToSamplingPeriod = Collections.unmodifiableMap(intervalKeyToSamplingPeriod);
  }
  
  public Integer getSamplingPeriodForInterval(PerfInterval perfInterval) {
    return this._intervalKeyToSamplingPeriod.get(Integer.valueOf(perfInterval.getKey()));
  }
  
  public Integer getCounterKeyForCounterName(String perfCounterName) {
    return this._counterNameToCounterKey.get(perfCounterName);
  }
  
  public String getCounterNameForCounterKey(int perfCounterKey) {
    return this._counterKeyToCounterName.get(Integer.valueOf(perfCounterKey));
  }
}
