package com.vmware.ph.phservice.provider.vcenter.event;

import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.vim.event.Event;
import com.vmware.vim.binding.vim.event.EventFilterSpec;
import com.vmware.vim.binding.vim.event.EventHistoryCollector;
import com.vmware.vim.binding.vim.event.EventManager;
import com.vmware.vim.binding.vim.fault.InvalidState;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EventsReader implements ItemsStream<Event> {
  private static final Log _log = LogFactory.getLog(EventsReader.class);
  
  private final VcClient _vcClient;
  
  private final EventFilterSpec _eventFilterSpec;
  
  private EventHistoryCollector _eventHistoryCollector;
  
  public EventsReader(VcClient vcClient, EventFilterSpec eventFilterSpec) {
    this._vcClient = Objects.<VcClient>requireNonNull(vcClient);
    this._eventFilterSpec = Objects.<EventFilterSpec>requireNonNull(eventFilterSpec);
  }
  
  EventsReader(VcClient vcClient, EventFilterSpec eventFilterSpec, EventHistoryCollector eventHistoryCollector) {
    this._vcClient = vcClient;
    this._eventFilterSpec = eventFilterSpec;
    this._eventHistoryCollector = eventHistoryCollector;
  }
  
  public List<Event> read(int numItems) throws IllegalArgumentException {
    if (this._eventHistoryCollector == null)
      try {
        this
          ._eventHistoryCollector = createEventHistoryCollector(this._vcClient, this._eventFilterSpec);
      } catch (InvalidState|IllegalStateException e) {
        _log.debug("Failed to create an EventHistoryCollector", e);
        return Collections.emptyList();
      }  
    List<Event> events = new ArrayList<>();
    try {
      Event[] nextEvents = this._eventHistoryCollector.readNext(numItems);
      if (nextEvents != null)
        events.addAll(Arrays.asList(nextEvents)); 
    } catch (InvalidArgument e) {
      throw new IllegalArgumentException(e);
    } 
    return events;
  }
  
  public int getLimit() {
    return this._eventFilterSpec.getMaxCount().intValue();
  }
  
  public void close() {
    if (this._eventHistoryCollector != null) {
      this._eventHistoryCollector.remove();
      this._eventHistoryCollector = null;
    } 
  }
  
  private static EventHistoryCollector createEventHistoryCollector(VcClient vcClient, EventFilterSpec eventFilterSpec) throws IllegalStateException, InvalidState {
    ManagedObjectReference eventManagerMoRef = vcClient.getServiceInstanceContent().getEventManager();
    EventManager eventManager = vcClient.<EventManager>createMo(eventManagerMoRef);
    if (eventManager == null)
      throw new IllegalStateException("Could not acquire Event Manager."); 
    ManagedObjectReference eventHistoryCollectorMoRef = null;
    eventHistoryCollectorMoRef = eventManager.createCollector(eventFilterSpec);
    EventHistoryCollector eventHistoryCollector = vcClient.<EventHistoryCollector>createMo(eventHistoryCollectorMoRef);
    return eventHistoryCollector;
  }
}
