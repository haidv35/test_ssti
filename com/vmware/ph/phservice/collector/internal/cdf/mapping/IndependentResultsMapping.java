package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class IndependentResultsMapping implements NamedQueryResultSetMapping<Payload> {
  private Map<String, Mapping<ResultSet, Payload>> _queryNameToResultSetMapping;
  
  public IndependentResultsMapping() {}
  
  public IndependentResultsMapping(Map<String, Mapping<ResultSet, Payload>> resultSetMappings) {
    this._queryNameToResultSetMapping = resultSetMappings;
  }
  
  public Payload map(NamedQueryResultSet input, Context context) {
    Payload.Builder payloadBuilder = new Payload.Builder();
    Mapping<ResultSet, Payload> mapping = this._queryNameToResultSetMapping.get(input.getQueryName());
    if (mapping != null) {
      Payload resultSetPayload = mapping.map(input.getResultSet(), context);
      payloadBuilder.add(resultSetPayload);
    } 
    return payloadBuilder.build();
  }
  
  public boolean isQuerySupported(String queryName) {
    return this._queryNameToResultSetMapping.containsKey(queryName);
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
  
  public String toString() {
    return "IndependentResultsMapping [resultSetMappings=" + this._queryNameToResultSetMapping + "]";
  }
}
