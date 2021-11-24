package com.vmware.ph.phservice.provider.appliance.healthstatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "vimhealth", namespace = "http://www.vmware.com/vi/healthservice")
@XmlAccessorType(XmlAccessType.FIELD)
public class VimHealth {
  static final String HEALTH_STATUS_NAMESPACE = "http://www.vmware.com/vi/healthservice";
  
  @XmlElement(name = "health", namespace = "http://www.vmware.com/vi/healthservice")
  private Health health;
  
  public Health getHealth() {
    return this.health;
  }
  
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Health {
    @XmlAttribute(name = "id")
    private String id;
    
    @XmlElement(name = "name", namespace = "http://www.vmware.com/vi/healthservice")
    private String name;
    
    @XmlElement(name = "status", namespace = "http://www.vmware.com/vi/healthservice")
    private String status;
    
    public String getId() {
      return this.id;
    }
    
    public String getName() {
      return this.name;
    }
    
    public String getStatus() {
      return this.status;
    }
  }
}
