package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.ResultSetToJsonLdMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.ResultSetToResourceMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.SafeMappingWrapper;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "resultSetMapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultSetToPayloadMapping implements MappingBuilder<ResultSet, Payload> {
  @XmlAnyElement(lax = true)
  private List<MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>>> itemMappings = new ArrayList<>();
  
  @XmlElement(required = false)
  private VelocityResourceMapping setMapping;
  
  @XmlElement(required = false)
  private ResultSetToJsonLdMapping setToJsonLdMapping;
  
  public ResultSetToPayloadMapping() {}
  
  public ResultSetToPayloadMapping(List<MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>>> itemMappings, VelocityResourceMapping setMapping) {
    this.itemMappings = itemMappings;
    this.setMapping = setMapping;
  }
  
  public List<MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>>> getItemMappings() {
    return this.itemMappings;
  }
  
  public VelocityResourceMapping getSetMapping() {
    return this.setMapping;
  }
  
  public Mapping<ResultSet, Payload> build() {
    List<Mapping<NamedPropertiesResourceItem, Collection<JsonLd>>> cdfItemMappings = new ArrayList<>();
    for (MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>> itemMapping : this.itemMappings) {
      Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> resourceItemMapping = itemMapping.build();
      cdfItemMappings.add(new SafeMappingWrapper<>(resourceItemMapping));
    } 
    VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory = new VelocityPatternEvaluatorFactory();
    List<Mapping<ResultSet, Collection<JsonLd>>> setMappings = new ArrayList<>();
    if (this.setMapping != null) {
      ResultSetToResourceMapping cdfSetMapping = new ResultSetToResourceMapping(this.setMapping.getResourceType(), this.setMapping.getIdPattern(), this.setMapping.getAttributePatterns(), this.setMapping.getRelationPatterns(), velocityPatternEvaluatorFactory);
      setMappings.add(cdfSetMapping);
    } 
    if (this.setToJsonLdMapping != null) {
      ResultSetToJsonLdMapping jsonLdSetMapping = new ResultSetToJsonLdMapping(this.setToJsonLdMapping.getMappingCode(), velocityPatternEvaluatorFactory);
      setMappings.add(jsonLdSetMapping);
    } 
    Mapping<ResultSet, Payload> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.ResultSetToPayloadMapping(cdfItemMappings, setMappings);
    return result;
  }
}
