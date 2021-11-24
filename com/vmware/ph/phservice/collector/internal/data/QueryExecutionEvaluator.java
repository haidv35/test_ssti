package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.QueryCommand;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryExecutionEvaluator {
  private static final Log _log = LogFactory.getLog(QueryExecutionEvaluator.class);
  
  static final String SYSTEM_LOAD_QUERY_RESOURCE_MODEL = "SystemLoad";
  
  static final String MEM_TOTAL_QUERY_PROPERTY = "mem_total";
  
  static final String MEM_UTIL_QUERY_PROPERTY = "mem_util";
  
  static final String CPU_UTIL_QUERY_PROPERTY = "cpu_util";
  
  static final String INTERVAL_MINUTES_FILTER_PROPERTY = "intervalMinutes";
  
  static final String DEFAULT_LOAD_MONITORING_INTERVAL_MINUTES = Integer.toString(5);
  
  static final String[] SYSTEM_LOAD_QUERY_PROPERTIES = new String[] { "@modelKey", "cpu_util", "mem_util", "mem_total" };
  
  private final Object _lock = new Object();
  
  private final QueryService _queryService;
  
  private volatile boolean _isInitialized = false;
  
  private Double _cpuUsedPercent;
  
  private Double _memoryUsedPercent;
  
  public QueryExecutionEvaluator(QueryService queryService) {
    this._queryService = queryService;
  }
  
  public boolean shouldExecuteQuery(NamedQuery query) {
    Double cpuThreshold = query.getCpuThreshold();
    Double memoryThreshold = query.getMemoryThreshold();
    if (isNullOrZero(cpuThreshold) && isNullOrZero(memoryThreshold))
      return true; 
    initializeSystemLoadStateOnce();
    return (isThresholdConditionMet(cpuThreshold, this._cpuUsedPercent) && 
      isThresholdConditionMet(memoryThreshold, this._memoryUsedPercent));
  }
  
  private void initializeSystemLoadStateOnce() {
    if (this._isInitialized)
      return; 
    synchronized (this._lock) {
      if (this._isInitialized)
        return; 
      loadSystemLoadState();
    } 
  }
  
  private void loadSystemLoadState() {
    try {
      QueryCommand queryCommand = buildSystemLoadQueryCommand();
      ResultSet resultSet = queryCommand.fetch();
      ResourceItem systemLoadResourceItem = resultSet.getItems().iterator().next();
      this._cpuUsedPercent = (Double)systemLoadResourceItem.get("cpu_util");
      Double memUtil = (Double)systemLoadResourceItem.get("mem_util");
      Double memTotal = (Double)systemLoadResourceItem.get("mem_total");
      this._memoryUsedPercent = Double.valueOf(memUtil.doubleValue() / memTotal.doubleValue() * 100.0D);
    } catch (Exception e) {
      _log.warn("Could not check system load due to an exception. Will act defensively and report that the system is loaded.", e);
      this._cpuUsedPercent = Double.valueOf(Double.MAX_VALUE);
      this._memoryUsedPercent = Double.valueOf(Double.MAX_VALUE);
    } finally {
      this._isInitialized = true;
    } 
  }
  
  private QueryCommand buildSystemLoadQueryCommand() {
    PropertyPredicate intervalMinutesPredicate = new PropertyPredicate("intervalMinutes", PropertyPredicate.ComparisonOperator.EQUAL, DEFAULT_LOAD_MONITORING_INTERVAL_MINUTES);
    Filter systemLoadFilter = new Filter(Collections.singletonList(intervalMinutesPredicate));
    QueryCommand queryCommand = this._queryService.select(SYSTEM_LOAD_QUERY_PROPERTIES).from(new String[] { "SystemLoad" }).where(systemLoadFilter).orderBy("@modelKey").build();
    return queryCommand;
  }
  
  private static boolean isThresholdConditionMet(Double threshold, Double loadValue) {
    return (threshold == null || Double.compare(threshold.doubleValue(), loadValue.doubleValue()) > 0);
  }
  
  private static boolean isNullOrZero(Double value) {
    boolean isNullOrZero = (value == null || value.doubleValue() == 0.0D);
    return isNullOrZero;
  }
}
