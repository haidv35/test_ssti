package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityResourceMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResultSetToPayloadMapping implements Mapping<ResultSet, Payload> {
  private static final Log logger = LogFactory.getLog(ResultSetToPayloadMapping.class);
  
  private final Collection<Mapping<NamedPropertiesResourceItem, Collection<JsonLd>>> _itemMappings;
  
  private final Collection<Mapping<ResultSet, Collection<JsonLd>>> _setMappings;
  
  public ResultSetToPayloadMapping(Collection<Mapping<NamedPropertiesResourceItem, Collection<JsonLd>>> itemMappings, Collection<Mapping<ResultSet, Collection<JsonLd>>> setMappings) {
    this._itemMappings = itemMappings;
    if (setMappings == null) {
      this._setMappings = Collections.emptyList();
    } else {
      this._setMappings = setMappings;
    } 
  }
  
  public Payload map(ResultSet input, Context context) {
    Payload.Builder builder = new Payload.Builder();
    applyItemMappings(input, builder, context);
    applySetMappings(input, builder, context);
    return builder.build();
  }
  
  private void applySetMappings(ResultSet input, Payload.Builder builder, Context context) {
    for (Mapping<ResultSet, Collection<JsonLd>> setMapping : this._setMappings) {
      Collection<JsonLd> result = setMapping.map(input, context);
      result.forEach(builder::add);
    } 
  }
  
  private void applyItemMappings(ResultSet input, Payload.Builder builder, Context context) {
    if (input.getItems() == null)
      return; 
    for (ResourceItem item : input.getItems()) {
      for (Mapping<NamedPropertiesResourceItem, ?> itemMapping : this._itemMappings) {
        NamedPropertiesResourceItem namedPropertiesResourceItem = new NamedPropertiesResourceItem(item, input.getProperties());
        Object result = itemMapping.map(namedPropertiesResourceItem, context);
        if (result instanceof Payload) {
          builder.add((Payload)result);
        } else if (result instanceof Collection) {
          builder.add((Collection)result);
        } else if (result != null) {
          logger.warn("Ignoring unexpected result of type " + result.getClass().getName());
        } 
        if (itemMapping instanceof VelocityResourceMapping) {
          Payload payload = ((VelocityResourceMapping)itemMapping).getPayload();
          if (payload == null) {
            if (logger.isDebugEnabled())
              logger.debug("getPayload() returned null for item " + item); 
            continue;
          } 
          if (logger.isTraceEnabled())
            logger.trace(
                String.format("getPayload() returned payload for item %s with %d Json-Ld objects.", new Object[] { item, Integer.valueOf(payload.getJsons().size()) })); 
          builder.add(payload);
        } 
      } 
    } 
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
  
  public String toString() {
    return "ResultSetToCdfPayloadMapping [itemMappings=" + this._itemMappings + "]";
  }
}
