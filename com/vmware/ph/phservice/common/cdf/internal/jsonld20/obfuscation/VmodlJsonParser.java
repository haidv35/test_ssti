package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VmodlJsonParser implements AutoCloseable {
  private static final Log _log = LogFactory.getLog(VmodlJsonParser.class);
  
  private static final JsonFactory JSON_FACTORY = new JsonFactory((ObjectCodec)new ObjectMapper());
  
  private final String _json;
  
  private final JsonParser _jsonParser;
  
  private final JsonParserContext _jsonParserContext;
  
  private VmodlJsonParser(String json, JsonParser jsonParser) {
    this._json = json;
    this._jsonParser = jsonParser;
    this._jsonParserContext = new JsonParserContext();
  }
  
  public static VmodlJsonParser createParser(String json) throws JsonParseException, IOException {
    JsonParser jsonParser = JSON_FACTORY.createParser(json);
    return new VmodlJsonParser(json, jsonParser);
  }
  
  public JsonToken nextToken() throws IOException {
    this._jsonParserContext.incrementTokenOffset();
    return this._jsonParser.nextToken();
  }
  
  public JsonToken currentToken() {
    return this._jsonParser.currentToken();
  }
  
  public String getCurrentName() throws IOException {
    return this._jsonParser.currentName();
  }
  
  public String getValueAsString() throws IOException {
    return this._jsonParser.getValueAsString();
  }
  
  public boolean getBooleanValue() throws IOException {
    return this._jsonParser.getBooleanValue();
  }
  
  public Object getEmbeddedObject() throws IOException {
    return this._jsonParser.getEmbeddedObject();
  }
  
  public void close() throws Exception {
    this._jsonParser.close();
  }
  
  public boolean isClosed() {
    return this._jsonParser.isClosed();
  }
  
  public String getVmodlObjectType() throws IOException {
    String vmodlObjectType;
    JsonToken nextToken = nextToken();
    if (nextToken == JsonToken.FIELD_NAME && "@type"
      .equals(this._jsonParser
        .getCurrentName())) {
      nextToken();
      vmodlObjectType = this._jsonParser.getValueAsString();
    } else {
      vmodlObjectType = getFieldValue("@type");
    } 
    return vmodlObjectType;
  }
  
  public ObjectNode getObjectNode(String vmodlObjectType) throws IOException {
    if (currentToken() == JsonToken.VALUE_STRING)
      nextToken(); 
    ObjectNode objectNode = (ObjectNode)this._jsonParser.readValueAsTree();
    if (!objectNode.has("@type"))
      objectNode.put("@type", vmodlObjectType); 
    return objectNode;
  }
  
  public String getFieldValue(String fieldName) {
    Map<String, String> fieldNameToFieldValue = getFieldValues(Collections.singleton(fieldName));
    String fieldValue = fieldNameToFieldValue.get(fieldName);
    return fieldValue;
  }
  
  public Map<String, String> getFieldValues(Set<String> fieldNames) {
    Map<String, String> fieldNameToValue = new HashMap<>(fieldNames.size());
    Set<String> fieldNamesToGet = new HashSet<>(fieldNames);
    try (JsonParser parser = JSON_FACTORY.createParser(this._json)) {
      skipToOffset(parser, this._jsonParserContext.currentTokenOffset());
      JsonToken currentToken = parser.nextToken();
      while (currentToken != null && currentToken != JsonToken.END_OBJECT && 
        !fieldNamesToGet.isEmpty()) {
        String fieldName = parser.getCurrentName();
        if (this._jsonParserContext.currentTokenOffset() > 0 && (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.START_ARRAY)) {
          parser.skipChildren();
        } else if (fieldNamesToGet.contains(fieldName)) {
          if (currentToken == JsonToken.FIELD_NAME)
            parser.nextToken(); 
          String properyValue = parser.getValueAsString();
          fieldNameToValue.put(fieldName, properyValue);
          fieldNamesToGet.remove(fieldName);
        } 
        currentToken = parser.nextToken();
      } 
    } catch (IOException e) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("Could not lookup fields %s in the given JSON stream.", new Object[] { fieldNames }), e); 
    } 
    for (String fieldNameToGet : fieldNamesToGet)
      fieldNameToValue.put(fieldNameToGet, null); 
    return fieldNameToValue;
  }
  
  private class JsonParserContext {
    private int _tokenOffset;
    
    private JsonParserContext() {}
    
    private void incrementTokenOffset() {
      this._tokenOffset++;
    }
    
    private int currentTokenOffset() {
      return this._tokenOffset;
    }
  }
  
  private static void skipToOffset(JsonParser parser, int tokenOffset) throws IOException {
    while (tokenOffset > 0) {
      parser.nextToken();
      tokenOffset--;
    } 
  }
}
