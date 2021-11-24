package com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectTypeRuleSpec {
  private String type;
  
  @XmlElementWrapper(name = "attributes")
  @XmlElement(name = "attribute")
  private List<String> attributes;
  
  private Map<String, String> filters;
  
  private boolean obfuscateSubstring;
  
  @XmlElementWrapper(name = "obfuscationIndices")
  @XmlElement(name = "index")
  private List<Integer> obfuscationIndices;
  
  public String getType() {
    return this.type;
  }
  
  public List<String> getAttributes() {
    return this.attributes;
  }
  
  public Map<String, String> getFilters() {
    return this.filters;
  }
  
  public boolean getObfuscateSubstring() {
    return this.obfuscateSubstring;
  }
  
  public List<Integer> getObfuscationIndices() {
    return this.obfuscationIndices;
  }
}
