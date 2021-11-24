package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "propertyPredicate")
public class PropertyPredicate {
  @XmlElement(name = "property")
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  private String property;
  
  @XmlElement(name = "operator")
  private ComparisonOperator operator;
  
  @XmlElement(name = "comparableValue")
  @XmlJavaTypeAdapter(PropertyPredicateComparableValueAdapter.class)
  private Object comparableValue;
  
  @XmlElement(name = "ignoreCase")
  private boolean ignoreCase;
  
  public String getProperty() {
    return this.property;
  }
  
  public ComparisonOperator getOperator() {
    return this.operator;
  }
  
  public Object getComparableValue() {
    return this.comparableValue;
  }
  
  public boolean isIgnoreCase() {
    return this.ignoreCase;
  }
}
