package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorProvider;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import java.util.Set;

public class OutcomeRetainingCollectorProvider implements CollectorProvider {
  private final CollectorProvider _collectorProvider;
  
  private OutcomeRetainingCollector _outcomeRetainingCollector;
  
  public OutcomeRetainingCollectorProvider(CollectorProvider collectorProvider) {
    this._collectorProvider = collectorProvider;
  }
  
  public boolean isActive() {
    return this._collectorProvider.isActive();
  }
  
  public Collector getCollector(CollectionSchedule collectionSchedule) {
    Collector collector = this._collectorProvider.getCollector(collectionSchedule);
    this._outcomeRetainingCollector = new OutcomeRetainingCollector(collector);
    return this._outcomeRetainingCollector;
  }
  
  public Set<CollectionSchedule> getCollectorSchedules() {
    return this._collectorProvider.getCollectorSchedules();
  }
  
  public OutcomeRetainingCollector getOutcomeRetainingCollector() {
    return this._outcomeRetainingCollector;
  }
}
