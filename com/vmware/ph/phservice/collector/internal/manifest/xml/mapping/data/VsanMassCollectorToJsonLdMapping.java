package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingBuilder;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class VsanMassCollectorToJsonLdMapping implements MappingBuilder<NamedPropertiesResourceItem, Collection<JsonLd>> {
  public Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> build() {
    Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.VsanMassCollectorToJsonLdMapping();
    return result;
  }
}
