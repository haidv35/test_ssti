package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class VelocityResourceMapping {
  private String resourceType = "";
  
  private String idPattern;
  
  private Map<String, String> attributePatterns;
  
  private Map<String, String> relationPatterns = new HashMap<>();
  
  public VelocityResourceMapping() {}
  
  public VelocityResourceMapping(String resourceType, String idPattern, Map<String, String> attributePatterns, Map<String, String> relationPatterns) {
    this.resourceType = resourceType;
    this.idPattern = idPattern;
    this.attributePatterns = attributePatterns;
    this.relationPatterns = relationPatterns;
  }
  
  public String getResourceType() {
    return this.resourceType;
  }
  
  public String getIdPattern() {
    return this.idPattern;
  }
  
  public Map<String, String> getAttributePatterns() {
    return this.attributePatterns;
  }
  
  public Map<String, String> getRelationPatterns() {
    return this.relationPatterns;
  }
}
