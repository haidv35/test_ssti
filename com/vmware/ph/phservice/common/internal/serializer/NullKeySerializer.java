package com.vmware.ph.phservice.common.internal.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class NullKeySerializer extends StdSerializer<Object> {
  public NullKeySerializer() {
    this(null);
  }
  
  public NullKeySerializer(Class<Object> t) {
    super(t);
  }
  
  public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) throws IOException {
    jsonGenerator.writeFieldName("null");
  }
}
