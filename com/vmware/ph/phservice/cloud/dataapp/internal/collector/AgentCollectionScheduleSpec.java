package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AgentCollectionScheduleSpec {
  public static final AgentCollectionScheduleSpec EMPTY_SPEC = new AgentCollectionScheduleSpec(
      
      Collections.emptyMap());
  
  private final Map<CollectionSchedule, Set<String>> _collectionScheduleToObjectIds;
  
  private AgentCollectionScheduleSpec(Map<CollectionSchedule, Set<String>> collectionScheduleToObjectIds) {
    this._collectionScheduleToObjectIds = collectionScheduleToObjectIds;
  }
  
  public Set<CollectionSchedule> getCollectionSchedules() {
    return Collections.unmodifiableSet(this._collectionScheduleToObjectIds.keySet());
  }
  
  public Set<String> getObjectIds(CollectionSchedule collectionSchedule) {
    Set<String> objectIds = this._collectionScheduleToObjectIds.get(collectionSchedule);
    return (objectIds != null) ? 
      Collections.<String>unmodifiableSet(objectIds) : 
      Collections.<String>emptySet();
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    AgentCollectionScheduleSpec that = (AgentCollectionScheduleSpec)o;
    return Objects.equals(this._collectionScheduleToObjectIds, that._collectionScheduleToObjectIds);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this._collectionScheduleToObjectIds });
  }
  
  public static class Builder implements com.vmware.ph.phservice.common.Builder<AgentCollectionScheduleSpec> {
    private final Map<CollectionSchedule, Set<String>> _collectionScheduleToObjectIds = new HashMap<>();
    
    public Builder add(String objectId, Set<CollectionSchedule> collectionSchedules) {
      for (CollectionSchedule collectionSchedule : collectionSchedules) {
        Set<String> objectIdsForSchedule = this._collectionScheduleToObjectIds.get(collectionSchedule);
        if (objectIdsForSchedule == null) {
          objectIdsForSchedule = new HashSet<>();
          this._collectionScheduleToObjectIds.put(collectionSchedule, objectIdsForSchedule);
        } 
        objectIdsForSchedule.add(objectId);
      } 
      return this;
    }
    
    public AgentCollectionScheduleSpec build() {
      return new AgentCollectionScheduleSpec(this._collectionScheduleToObjectIds);
    }
  }
}
