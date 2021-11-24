package com.vmware.ph.phservice.collector.internal.data;

import com.vmware.cis.data.api.Query;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class NamedQuery {
  private final Query _query;
  
  private final String _name;
  
  private final Double _cpuThreshold;
  
  private final Double _memoryThreshold;
  
  private final Integer _pageSize;
  
  public NamedQuery(Query query, String name) {
    this(query, name, null, null);
  }
  
  public NamedQuery(Query query, String name, Double cpuThreshold, Double memoryThreshold) {
    this(query, name, cpuThreshold, memoryThreshold, null);
  }
  
  public NamedQuery(Query query, String name, Double cpuThreshold, Double memoryThreshold, Integer pageSize) {
    this._query = query;
    this._name = name;
    this._cpuThreshold = cpuThreshold;
    this._memoryThreshold = memoryThreshold;
    this._pageSize = pageSize;
  }
  
  public Query getQuery() {
    return this._query;
  }
  
  public String getName() {
    return this._name;
  }
  
  public Double getCpuThreshold() {
    return this._cpuThreshold;
  }
  
  public Double getMemoryThreshold() {
    return this._memoryThreshold;
  }
  
  public Integer getPageSize() {
    return this._pageSize;
  }
  
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Query name: ").append(this._name).append(System.lineSeparator());
    stringBuilder.append("Query: ").append(this._query).append(System.lineSeparator());
    stringBuilder.append("Query page size: ").append(this._pageSize).append(System.lineSeparator());
    stringBuilder.append("Query thresholds: [").append(this._cpuThreshold).append("] CPU, [");
    stringBuilder.append(this._memoryThreshold).append("] memory");
    return stringBuilder.toString();
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  public int hashCode() {
    return (new HashCodeBuilder(17, 31))
      .append(this._query)
      .append(this._name)
      .append(this._cpuThreshold)
      .append(this._memoryThreshold)
      .toHashCode();
  }
}
