package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceItemToJsonLdWithAttributesPatternMapping extends ResourceItemToJsonLdMapping {
  protected String idPattern;
  
  protected Map<String, String> attributePatterns;
  
  public String getIdPattern() {
    return this.idPattern;
  }
  
  public Map<String, String> getAttributePatterns() {
    return this.attributePatterns;
  }
  
  public Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> build() {
    Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.ResourceItemToJsonLdWithAttributesPatternMapping(this.forType, this.mappingCode, this.idPattern, this.attributePatterns, new VelocityPatternEvaluatorFactory());
    return result;
  }
}
