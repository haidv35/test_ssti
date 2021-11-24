package com.vmware.ph.phservice.provider.appliance.healthstatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.vmware.com/cis/cm/common/jaxb/healthstatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class HealthStatus {
  static final String HEALTH_STATUS_NAMESPACE = "http://www.vmware.com/cis/cm/common/jaxb/healthstatus";
  
  @XmlElement(name = "status", namespace = "http://www.vmware.com/cis/cm/common/jaxb/healthstatus")
  private String status;
  
  public String getStatus() {
    return this.status;
  }
}
