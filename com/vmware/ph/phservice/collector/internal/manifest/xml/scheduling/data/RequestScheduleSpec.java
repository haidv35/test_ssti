package com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "requestSchedules")
public class RequestScheduleSpec {
  @XmlElement(name = "schedule")
  private List<ScheduleSpec> schedules = new LinkedList<>();
  
  public List<ScheduleSpec> getSchedules() {
    return this.schedules;
  }
}
