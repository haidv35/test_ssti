package com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScheduleSpec {
  @XmlAttribute(required = true)
  private String interval;
  
  @XmlAttribute(name = "retry-interval")
  private String retryInterval;
  
  @XmlAttribute(name = "max-retries")
  private int maxRetriesCount;
  
  @XmlElementWrapper(name = "queries")
  @XmlElement(name = "query")
  private List<String> queryNames;
  
  public String getInterval() {
    return this.interval;
  }
  
  public void setInterval(String interval) {
    this.interval = interval;
  }
  
  public String getRetryInterval() {
    return this.retryInterval;
  }
  
  public void setRetryInterval(String retryInterval) {
    this.retryInterval = retryInterval;
  }
  
  public int getMaxRetriesCount() {
    return this.maxRetriesCount;
  }
  
  public void setMaxRetriesCount(int maxRetriesCount) {
    this.maxRetriesCount = maxRetriesCount;
  }
  
  public void setQueryNames(List<String> queryNames) {
    this.queryNames = queryNames;
  }
  
  public List<String> getQueryNames() {
    return this.queryNames;
  }
}
