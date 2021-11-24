package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@XmlRootElement(name = "indepedentResultsMapping")
@XmlAccessorType(XmlAccessType.FIELD)
public class IndependentResultsMapping implements MappingBuilder<NamedQueryResultSet, Payload> {
  public static final com.vmware.ph.phservice.collector.internal.cdf.mapping.IndependentResultsMapping EMPTY_INDEPENDENT_RESULTS_MAPPING = new com.vmware.ph.phservice.collector.internal.cdf.mapping.IndependentResultsMapping();
  
  private Map<String, Mappings.Wrapper<ResultSetToPayloadMapping>> resultSetMappings = new HashMap<>();
  
  public IndependentResultsMapping() {}
  
  public IndependentResultsMapping(Map<String, Mappings.Wrapper<ResultSetToPayloadMapping>> resultSetMappings) {
    this.resultSetMappings = resultSetMappings;
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public Mapping<NamedQueryResultSet, Payload> build() {
    Map<String, Mapping<ResultSet, Payload>> cdfResultSetMappings = new HashMap<>();
    for (Map.Entry<String, Mappings.Wrapper<ResultSetToPayloadMapping>> xmlMapping : this.resultSetMappings.entrySet()) {
      Mappings.Wrapper<ResultSetToPayloadMapping> wrappedMapping = xmlMapping.getValue();
      cdfResultSetMappings.put(xmlMapping.getKey(), ((ResultSetToPayloadMapping)wrappedMapping.getValue()).build());
    } 
    Mapping<NamedQueryResultSet, Payload> result = new com.vmware.ph.phservice.collector.internal.cdf.mapping.IndependentResultsMapping(cdfResultSetMappings);
    return result;
  }
}
