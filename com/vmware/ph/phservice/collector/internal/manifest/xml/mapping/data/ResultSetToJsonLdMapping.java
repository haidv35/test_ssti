package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResultSetToJsonLdMapping {
  private String mappingCode;
  
  public String getMappingCode() {
    return this.mappingCode;
  }
}
