package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "filter")
public class FilterSpec {
  @XmlElementWrapper(name = "criteria")
  @XmlElement(name = "propertyPredicate", type = PropertyPredicate.class)
  private List<PropertyPredicate> criteria;
  
  @XmlElement(name = "operator")
  private LogicalOperator operator;
  
  public List<PropertyPredicate> getCriteria() {
    return this.criteria;
  }
  
  public LogicalOperator getOperator() {
    return this.operator;
  }
}
