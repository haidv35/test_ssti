package com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling;

import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.manifest.scheduling.ScheduleIntervalParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.RequestScheduleSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.ScheduleSpec;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestScheduleSpecConverter {
  private static final int DEFAULT_MAX_RETRY_COUNT = 1;
  
  public static Map<CollectionSchedule, NamedQuery[]> convertToSchedulesToQueriesMap(RequestScheduleSpec requestScheduleSpec, NamedQuery[] queries) {
    Map<String, NamedQuery> queryNameToQuery = getQueryNameToQuery(queries);
    Map<CollectionSchedule, NamedQuery[]> schedulesToQueries = (Map)new HashMap<>();
    for (ScheduleSpec scheduleSpec : requestScheduleSpec.getSchedules()) {
      CollectionSchedule schedule = convertScheduleSpecToSchedule(scheduleSpec);
      NamedQuery[] queriesForSchedule = getScheduledQueries(queryNameToQuery, scheduleSpec
          
          .getQueryNames());
      addQueriesForSchedule(schedulesToQueries, schedule, queriesForSchedule);
    } 
    return schedulesToQueries;
  }
  
  public static CollectionSchedule convertScheduleSpecToSchedule(ScheduleSpec scheduleSpec) {
    long intervalMillis = ScheduleIntervalParser.parseIntervalMillis(scheduleSpec.getInterval());
    long retryIntervalMillis = ScheduleIntervalParser.parseIntervalMillis(scheduleSpec.getRetryInterval());
    int maxRetriesCount = scheduleSpec.getMaxRetriesCount();
    if (maxRetriesCount == 0 && retryIntervalMillis > 0L)
      maxRetriesCount = 1; 
    return new CollectionSchedule(intervalMillis, retryIntervalMillis, maxRetriesCount);
  }
  
  private static NamedQuery[] getScheduledQueries(Map<String, NamedQuery> queryNameToQuery, List<String> queryNames) {
    NamedQuery[] queries = new NamedQuery[queryNames.size()];
    for (int i = 0; i < queryNames.size(); i++) {
      String queryName = queryNames.get(i);
      NamedQuery namedQuery = queryNameToQuery.get(queryName);
      queries[i] = namedQuery;
    } 
    return queries;
  }
  
  private static Map<String, NamedQuery> getQueryNameToQuery(NamedQuery[] queries) {
    Map<String, NamedQuery> queryNameToNamedQuery = new HashMap<>();
    for (NamedQuery query : queries)
      queryNameToNamedQuery.put(query.getName(), query); 
    return queryNameToNamedQuery;
  }
  
  private static void addQueriesForSchedule(Map<CollectionSchedule, NamedQuery[]> scheduleToQueries, CollectionSchedule schedule, NamedQuery[] queriesToAdd) {
    NamedQuery[] existingQueriesForSchedule = scheduleToQueries.get(schedule);
    if (existingQueriesForSchedule != null) {
      int originalQueryCount = existingQueriesForSchedule.length;
      int newQueryCount = originalQueryCount + queriesToAdd.length;
      existingQueriesForSchedule = Arrays.<NamedQuery>copyOf(existingQueriesForSchedule, newQueryCount);
      for (int i = originalQueryCount, j = 0; i < newQueryCount; i++, j++)
        existingQueriesForSchedule[i] = queriesToAdd[j]; 
    } else {
      existingQueriesForSchedule = queriesToAdd;
    } 
    scheduleToQueries.put(schedule, existingQueriesForSchedule);
  }
}
