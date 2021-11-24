package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "query")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuerySpec {
  @XmlAttribute
  private String name;
  
  @XmlAttribute
  private Integer maxResultCount;
  
  @XmlAttribute
  private Double cpuThreshold;
  
  @XmlAttribute
  private Double memoryThreshold;
  
  @XmlAttribute
  private Integer pageSize;
  
  @XmlAttribute
  private boolean withTotalCount;
  
  @XmlElements({@XmlElement(name = "constraint", type = Constraint.class)})
  private Constraint constraint;
  
  @XmlElement(name = "propertySpec")
  private List<PropertySpec> propertySpecs = new ArrayList<>();
  
  @XmlElement(name = "filter")
  private FilterSpec filter = new FilterSpec();
  
  public QuerySpec() {}
  
  public QuerySpec(String name, Integer maxResultCount, Double cpuThreshold, Double memoryThreshold, Constraint constraint, List<PropertySpec> propertySpecs, FilterSpec filter, Integer pageSize, boolean withTotalCount) {
    this.name = name;
    this.maxResultCount = maxResultCount;
    this.cpuThreshold = cpuThreshold;
    this.memoryThreshold = memoryThreshold;
    this.constraint = constraint;
    this.propertySpecs = propertySpecs;
    this.filter = filter;
    this.pageSize = pageSize;
    this.withTotalCount = withTotalCount;
  }
  
  public String getName() {
    return this.name;
  }
  
  public Integer getMaxResultCount() {
    return this.maxResultCount;
  }
  
  public Double getCpuThreshold() {
    return this.cpuThreshold;
  }
  
  public Double getMemoryThreshold() {
    return this.memoryThreshold;
  }
  
  public Constraint getConstraint() {
    return this.constraint;
  }
  
  public List<PropertySpec> getPropertySpecs() {
    return this.propertySpecs;
  }
  
  public FilterSpec getFilter() {
    return this.filter;
  }
  
  public Integer getPageSize() {
    return this.pageSize;
  }
  
  public boolean getWithTotalCount() {
    return this.withTotalCount;
  }
}
