package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingBuilder;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "moRefResultItemMapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceItemToPhResourceMapping extends VelocityResourceMapping implements MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>> {
  private String forType;
  
  public ResourceItemToPhResourceMapping() {}
  
  public ResourceItemToPhResourceMapping(String forType, String resourceType, String idPattern, Map<String, String> attributePatterns, Map<String, String> relationPatterns) {
    super(resourceType, idPattern, attributePatterns, relationPatterns);
    this.forType = forType;
  }
  
  public String getForType() {
    return this.forType;
  }
  
  public Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> build() {
    Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.ResourceItemToPhResourceMapping(this.forType, getResourceType(), getIdPattern(), getAttributePatterns(), getRelationPatterns(), new VelocityPatternEvaluatorFactory());
    return result;
  }
}
