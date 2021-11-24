package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import org.json.JSONObject;

public class VelocityJsonLdToJsonLdConverter {
  public JsonLd convert(VelocityJsonLd.Builder velocityJsonLd, VmodlToJsonLdSerializer serializer) {
    JSONObject jsonObject;
    if (velocityJsonLd instanceof VelocityJsonLd.BuilderFromResultItem) {
      VelocityJsonLd.BuilderFromMoRef b = (VelocityJsonLd.BuilderFromMoRef)velocityJsonLd;
      jsonObject = serializer.serialize(b.moRef, velocityJsonLd.attributes, '/');
    } else if (velocityJsonLd instanceof VelocityJsonLd.BuilderFromMoRef) {
      VelocityJsonLd.BuilderFromMoRef b = (VelocityJsonLd.BuilderFromMoRef)velocityJsonLd;
      jsonObject = serializer.serialize(b.moRef, velocityJsonLd.attributes, '.');
    } else if (velocityJsonLd instanceof VelocityJsonLd.BuilderFromTypeAndId) {
      VelocityJsonLd.BuilderFromTypeAndId b = (VelocityJsonLd.BuilderFromTypeAndId)velocityJsonLd;
      jsonObject = serializer.serialize(b.type, b.id, velocityJsonLd.attributes, '.');
    } else {
      throw new Bug("Processing Velocity results of type " + velocityJsonLd
          
          .getClass().getName() + " is not implemented.");
    } 
    JsonLd jsonLd = new JsonLd(jsonObject);
    return jsonLd;
  }
}
