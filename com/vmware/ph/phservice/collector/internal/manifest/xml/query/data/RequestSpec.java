package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestSpec {
  @XmlElement(name = "query")
  private List<QuerySpec> queries = new ArrayList<>();
  
  public RequestSpec() {}
  
  public RequestSpec(List<QuerySpec> queries) {
    this.queries = queries;
  }
  
  public List<QuerySpec> getQueries() {
    return this.queries;
  }
  
  public String toString() {
    return "RequestSpec [\n\t querySpec=" + this.queries + "\n]";
  }
}
