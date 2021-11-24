package com.vmware.ph.phservice.collector.scheduler;

import com.vmware.ph.phservice.common.PersistenceService;
import com.vmware.ph.phservice.common.PersistenceServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PersistenceServiceCollectorLoopExecutionTracker implements CollectorLoopExecutionTracker {
  private static final Log _log = LogFactory.getLog(PersistenceServiceCollectorLoopExecutionTracker.class);
  
  private static final String PHSERVICES_FIRSTBOOT_TIME_KEY = "phservices.firstboot_time";
  
  private static final String PHSERVICES_KEY_PATTERN = "phservices.%s_%s_%d.";
  
  private static final String LAST_COLLECTION_START_TIME_KEY_SUFFIX = "last_collection_start_time";
  
  private static final String LAST_COLLECTION_COMPLETE_TIME_KEY_SUFFIX = "last_collection_complete_time";
  
  private static final String PHSERVICES_INTERVAL_LAST_COLLECTION_START_TIME_KEY = "phservices.%s_%s_%d.last_collection_start_time";
  
  private static final String PHSERVICES_INTERVAL_LAST_COLLECTION_COMPLETE_TIME_KEY = "phservices.%s_%s_%d.last_collection_complete_time";
  
  private static final String COLLECTION_FAILURE_COUNT_KEY_SUFFIX = "collection_failure_count";
  
  private static final String PHSERVICES_COLLECTION_FAILURE_COUNT_KEY = "phservices.%s_%s_%d.collection_failure_count";
  
  private static final String COLLECTION_SCHEDULE_PATTERN_KEY_SUFFIX = "collection_schedule_pattern";
  
  private static final String PHSERVICES_COLLECTION_SCHEDULE_PATTERN_KEY = "phservices.%s_%s_%d.collection_schedule_pattern";
  
  private final PersistenceService _persistenceService;
  
  public PersistenceServiceCollectorLoopExecutionTracker(PersistenceService persistenceService) {
    this._persistenceService = persistenceService;
  }
  
  public long getFirstBootTime() {
    return readLong("phservices.firstboot_time");
  }
  
  public void setFirstBootTime(long firstBootTime) {
    writeLong("phservices.firstboot_time", firstBootTime);
  }
  
  public CollectionScheduleExecutionState getCollectionScheduleExecutionState(CollectionSchedule schedule) {
    String schedulePatternKey = constructCollectionSchedulePatternKey(schedule);
    String lastCollectionStartTimeKey = constructLastCollectionStartTimeKey(schedule);
    String lastCollectionCompleteTimeKey = constructLastCollectionCompleteTimeKey(schedule);
    String collectionFailureCountKey = constructCollectionFailureCountKey(schedule);
    List<String> scheduleStateKeys = new ArrayList<>();
    scheduleStateKeys.add(schedulePatternKey);
    scheduleStateKeys.add(lastCollectionStartTimeKey);
    scheduleStateKeys.add(lastCollectionCompleteTimeKey);
    scheduleStateKeys.add(collectionFailureCountKey);
    Long lastCollectionStartTime = null;
    Long lastCollectionCompleteTime = null;
    Integer collectionFailureCount = null;
    String schedulePattern = null;
    try {
      Map<String, String> schedulePropertyKeyToSchedulePropertyValueMap = this._persistenceService.readValues(scheduleStateKeys);
      schedulePattern = schedulePropertyKeyToSchedulePropertyValueMap.get(schedulePatternKey);
      lastCollectionStartTime = parseLong(schedulePropertyKeyToSchedulePropertyValueMap
          .get(lastCollectionStartTimeKey));
      lastCollectionCompleteTime = parseLong(schedulePropertyKeyToSchedulePropertyValueMap
          .get(lastCollectionCompleteTimeKey));
      collectionFailureCount = parseInt(schedulePropertyKeyToSchedulePropertyValueMap.get(collectionFailureCountKey));
    } catch (PersistenceServiceException e) {
      _log.error("Failed to acquire execution state for schedule " + schedule, (Throwable)e);
    } 
    return new CollectionScheduleExecutionState(schedule, schedulePattern, lastCollectionStartTime, lastCollectionCompleteTime, collectionFailureCount);
  }
  
  public void setCollectionScheduleExecutionState(CollectionScheduleExecutionState collectionScheduleExecutionState) {
    CollectionSchedule schedule = collectionScheduleExecutionState.getSchedule();
    Map<String, String> scheduleStatePropertyKeyToPropertyValueMap = new HashMap<>();
    String schedulePattern = collectionScheduleExecutionState.getSchedulePattern();
    if (StringUtils.isNotBlank(schedulePattern))
      scheduleStatePropertyKeyToPropertyValueMap.put(
          constructCollectionSchedulePatternKey(schedule), schedulePattern); 
    scheduleStatePropertyKeyToPropertyValueMap.put(
        constructLastCollectionStartTimeKey(schedule), 
        String.valueOf(collectionScheduleExecutionState.getLastCollectionStartTime()));
    scheduleStatePropertyKeyToPropertyValueMap.put(
        constructLastCollectionCompleteTimeKey(schedule), 
        String.valueOf(collectionScheduleExecutionState.getLastCollectionCompleteTime()));
    scheduleStatePropertyKeyToPropertyValueMap.put(
        constructCollectionFailureCountKey(schedule), 
        String.valueOf(collectionScheduleExecutionState.getFailureCount()));
    try {
      this._persistenceService.writeValues(scheduleStatePropertyKeyToPropertyValueMap);
    } catch (PersistenceServiceException e) {
      _log.error("An exception occurred while trying to write collection schedule execution state", (Throwable)e);
    } 
  }
  
  public void removeCollectionScheduleExecutionStates(Set<CollectionSchedule> schedulesToExclude) {
    try {
      Set<String> outdatedCollectionScheduleExecutionKeys = getScheduleExecutionStateKeysToRemove(schedulesToExclude);
      if (!outdatedCollectionScheduleExecutionKeys.isEmpty()) {
        if (_log.isDebugEnabled())
          _log.debug("Removing the following collection schedule records: " + outdatedCollectionScheduleExecutionKeys); 
        this._persistenceService.deleteValues(outdatedCollectionScheduleExecutionKeys);
      } 
    } catch (PersistenceServiceException e) {
      _log.error("An exception occurred while removing schedules", (Throwable)e);
    } 
  }
  
  private long readLong(String key) {
    long result = 0L;
    try {
      Long value = this._persistenceService.readLong(key);
      if (value != null)
        result = value.longValue(); 
    } catch (PersistenceServiceException e) {
      _log.error("An exception occurred while trying to retrieve parameter for key " + key, (Throwable)e);
    } 
    return result;
  }
  
  private void writeLong(String key, long value) {
    try {
      this._persistenceService.writeLong(key, value);
    } catch (PersistenceServiceException e) {
      _log.error("An exception occurred while trying to write parameter for key " + key, (Throwable)e);
    } 
  }
  
  private static Integer parseInt(String value) {
    if (value != null)
      return Integer.valueOf(Integer.parseInt(value)); 
    return null;
  }
  
  private static Long parseLong(String value) {
    if (value != null)
      return Long.valueOf(Long.parseLong(value)); 
    return null;
  }
  
  private Set<String> getScheduleExecutionStateKeysToRemove(Set<CollectionSchedule> schedules) throws PersistenceServiceException {
    Set<String> scheduleExecutionKeysToRemove = new HashSet<>(this._persistenceService.getAllKeys());
    for (CollectionSchedule schedule : schedules) {
      String lastCollectionStartTimeKey = constructLastCollectionStartTimeKey(schedule);
      scheduleExecutionKeysToRemove.remove(lastCollectionStartTimeKey);
      String lastCollectionCompleteTimeKey = constructLastCollectionCompleteTimeKey(schedule);
      scheduleExecutionKeysToRemove.remove(lastCollectionCompleteTimeKey);
      String collectionSchedulePatternKey = constructCollectionSchedulePatternKey(schedule);
      scheduleExecutionKeysToRemove.remove(collectionSchedulePatternKey);
      String collectionFailureCountKey = constructCollectionFailureCountKey(schedule);
      scheduleExecutionKeysToRemove.remove(collectionFailureCountKey);
    } 
    return filterKeys(scheduleExecutionKeysToRemove, new String[] { "last_collection_start_time", "last_collection_complete_time", "collection_schedule_pattern", "collection_failure_count" });
  }
  
  private static String constructLastCollectionStartTimeKey(CollectionSchedule schedule) {
    return constructPersistenceKeyFromCollectionSchedule("phservices.%s_%s_%d.last_collection_start_time", schedule);
  }
  
  private static String constructLastCollectionCompleteTimeKey(CollectionSchedule schedule) {
    return constructPersistenceKeyFromCollectionSchedule("phservices.%s_%s_%d.last_collection_complete_time", schedule);
  }
  
  private static String constructCollectionFailureCountKey(CollectionSchedule schedule) {
    return constructPersistenceKeyFromCollectionSchedule("phservices.%s_%s_%d.collection_failure_count", schedule);
  }
  
  private static String constructCollectionSchedulePatternKey(CollectionSchedule schedule) {
    return constructPersistenceKeyFromCollectionSchedule("phservices.%s_%s_%d.collection_schedule_pattern", schedule);
  }
  
  private static String constructPersistenceKeyFromCollectionSchedule(String persistenceKeyPattern, CollectionSchedule schedule) {
    return String.format(persistenceKeyPattern, new Object[] { schedule
          
          .getIntervalAsString(), schedule
          .getRetryIntervalAsString(), 
          Integer.valueOf(schedule.getMaxRetriesCount()) }).toLowerCase();
  }
  
  private static Set<String> filterKeys(Set<String> keys, String... filterPatterns) {
    Set<String> resultKeys = new HashSet<>();
    for (String key : keys) {
      for (String filterPattern : filterPatterns) {
        if (key.contains(filterPattern))
          resultKeys.add(key); 
      } 
    } 
    return resultKeys;
  }
}
