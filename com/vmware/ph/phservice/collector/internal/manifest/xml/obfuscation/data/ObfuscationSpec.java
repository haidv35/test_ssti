package com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class ObfuscationSpec {
  @XmlElementWrapper(name = "regexBased")
  @XmlElement(name = "rule")
  private List<RegexRuleSpec> regexRuleSpecs;
  
  @XmlElementWrapper(name = "typeBased")
  @XmlElement(name = "rule")
  private List<ObjectTypeRuleSpec> objectTypeRuleSpecs;
  
  public List<RegexRuleSpec> getRegexRuleSpecs() {
    return this.regexRuleSpecs;
  }
  
  public List<ObjectTypeRuleSpec> getObjectTypeRuleSpecs() {
    return this.objectTypeRuleSpecs;
  }
}
