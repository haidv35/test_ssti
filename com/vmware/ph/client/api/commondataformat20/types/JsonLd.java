package com.vmware.ph.client.api.commondataformat20.types;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class JsonLd {
  public static final String ID_PROPERTY_NAME = "@id";
  
  public static final String TYPE_PROPERTY_NAME = "@type";
  
  private static final JsonFactory _jsonFactory = new JsonFactory((ObjectCodec)new ObjectMapper());
  
  private String _jsonString;
  
  @Deprecated
  public JsonLd(JSONObject json) {
    this._jsonString = json.toString();
  }
  
  public JsonLd(String jsonString) {
    this._jsonString = jsonString;
  }
  
  public JSONObject getJson() {
    JSONObject jsonObject = new JSONObject(this._jsonString);
    return jsonObject;
  }
  
  public String getFieldValueAsString(String fieldName) throws IOException {
    String fieldValue = "";
    JsonParser jsonParser = _jsonFactory.createParser(this._jsonString);
    while (!jsonParser.isClosed()) {
      JsonToken jsonToken = jsonParser.nextToken();
      if (JsonToken.FIELD_NAME.equals(jsonToken) && fieldName.equals(jsonParser.getCurrentName())) {
        jsonParser.nextToken();
        fieldValue = jsonParser.getValueAsString();
        break;
      } 
    } 
    return fieldValue;
  }
  
  @Deprecated
  public String asPrettyPrintedJson() {
    return getJson().toString(2);
  }
  
  public void setJsonString(String jsonString) {
    this._jsonString = jsonString;
  }
  
  public String toString() {
    return this._jsonString;
  }
  
  public int hashCode() {
    return this._jsonString.hashCode();
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    JSONObject otherJson = null;
    if (getClass() == obj.getClass()) {
      otherJson = ((JsonLd)obj).getJson();
    } else if (JSONObject.class.equals(obj.getClass())) {
      otherJson = (JSONObject)obj;
    } else {
      return false;
    } 
    JSONObject thisJson = getJson();
    if (thisJson == null) {
      if (otherJson != null)
        return false; 
    } else if (!thisJson.similar(otherJson)) {
      return false;
    } 
    return true;
  }
  
  public static class Builder {
    private Map<String, Object> _properties = new HashMap<>();
    
    public Builder withId(String id) {
      if (!StringUtils.isBlank(id))
        this._properties.put("@id", id); 
      return this;
    }
    
    public Builder withType(String type) {
      if (!StringUtils.isBlank(type))
        this._properties.put("@type", type); 
      return this;
    }
    
    public Builder withProperty(String key, Object value) {
      this._properties.put(key, value);
      return this;
    }
    
    public Builder withProperties(Map<String, ?> properties) {
      this._properties.putAll(properties);
      return this;
    }
    
    public JsonLd build() throws IOException {
      try(StringWriter jsonStringWriter = new StringWriter(); 
          JsonGenerator jsonGenerator = JsonLd._jsonFactory.createGenerator(jsonStringWriter)) {
        jsonGenerator.writeStartObject();
        for (Map.Entry<String, Object> property : this._properties.entrySet()) {
          String propertyName = property.getKey();
          Object propertyValue = property.getValue();
          jsonGenerator.writeObjectField(propertyName, propertyValue);
        } 
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        return new JsonLd(jsonStringWriter.toString());
      } 
    }
  }
}
