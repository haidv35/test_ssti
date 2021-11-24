package com.vmware.ph.phservice.cloud.dataapp.server.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vmware.vapi.bindings.ApiEnumeration;
import java.io.IOException;

class VapiSpecificJsonSerializer extends JsonSerializer<ApiEnumeration> {
  public void serialize(ApiEnumeration apiEnumeration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeString(apiEnumeration.name());
    jsonGenerator.flush();
  }
}
