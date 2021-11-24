package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingBuilder;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "freeFormDataMapping")
public class ResourceItemToFreeFormDataMapping implements MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>> {
  private String idPattern;
  
  private String resourceType;
  
  private String dataProperty;
  
  public String getIdPattern() {
    return this.idPattern;
  }
  
  public String getResourceType() {
    return this.resourceType;
  }
  
  public String getDataProperty() {
    return this.dataProperty;
  }
  
  public Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> build() {
    Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.ResourceItemToFreeFormDataMapping(this.idPattern, this.resourceType, this.dataProperty);
    return result;
  }
}
