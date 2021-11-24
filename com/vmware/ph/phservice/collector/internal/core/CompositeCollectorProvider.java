package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorProvider;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CompositeCollectorProvider implements CollectorProvider {
  private final List<CollectorProvider> _collectorProviders;
  
  public CompositeCollectorProvider(List<CollectorProvider> collectorProviders) {
    this._collectorProviders = collectorProviders;
  }
  
  public boolean isActive() {
    return true;
  }
  
  public Collector getCollector(CollectionSchedule collectionSchedule) {
    CollectorProvider activeCollectorProvider = getActiveCollectorProvider();
    if (activeCollectorProvider != null)
      return activeCollectorProvider.getCollector(collectionSchedule); 
    return null;
  }
  
  public Set<CollectionSchedule> getCollectorSchedules() {
    CollectorProvider activeCollectorProvider = getActiveCollectorProvider();
    if (activeCollectorProvider != null)
      return activeCollectorProvider.getCollectorSchedules(); 
    return Collections.emptySet();
  }
  
  private CollectorProvider getActiveCollectorProvider() {
    for (CollectorProvider collectorProvider : this._collectorProviders) {
      if (collectorProvider.isActive())
        return collectorProvider; 
    } 
    return null;
  }
}
