package com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class RegexRuleSpec {
  private String pattern;
  
  private boolean obfuscateSubstring;
  
  public String getPattern() {
    return this.pattern;
  }
  
  public boolean getObfuscateSubstring() {
    return this.obfuscateSubstring;
  }
}
