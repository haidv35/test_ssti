package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationCache;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationException;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class StreamingJsonParserObfuscationRule implements ObfuscationRule {
  private static final JsonFactory JSON_FACTORY = new JsonFactory((ObjectCodec)new ObjectMapper());
  
  public Object apply(Object input) throws ObfuscationException {
    return apply(input, null);
  }
  
  public Object apply(Object input, ObfuscationCache obfuscationCache) throws ObfuscationException {
    if (isObfuscationRuleEmpty())
      return input; 
    if (input == null || !(input instanceof JsonLd))
      return input; 
    String unobfuscatedJson = input.toString();
    String obfuscatedJson = obfuscateJsonString(unobfuscatedJson, obfuscationCache);
    ((JsonLd)input).setJsonString(obfuscatedJson);
    return input;
  }
  
  protected abstract boolean isObfuscationRuleEmpty();
  
  protected String obfuscateJsonString(String json, ObfuscationCache obfuscationCache) throws ObfuscationException {
    String result = null;
    try(Writer writer = new StringWriter(); 
        JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(writer); 
        VmodlJsonParser jsonParser = VmodlJsonParser.createParser(json)) {
      VmodlObjectTracker objectTracker = new VmodlObjectTracker();
      while (!jsonParser.isClosed()) {
        JsonToken jsonToken = jsonParser.nextToken();
        if (jsonToken != null)
          handleCurrentJsonToken(jsonParser, objectTracker, jsonGenerator, obfuscationCache); 
      } 
      jsonGenerator.flush();
      result = writer.toString();
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      throw new ObfuscationException("Could not obfuscate JSON input!", e);
    } 
    return result;
  }
  
  protected void handleCurrentJsonToken(VmodlJsonParser jsonParser, VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    String vmodlObjectType, fieldName;
    JsonToken currentToken = jsonParser.currentToken();
    switch (currentToken) {
      case START_OBJECT:
        vmodlObjectType = jsonParser.getVmodlObjectType();
        handleStartObject(vmodlObjectType, jsonParser, objectTracker, jsonGenerator, obfuscationCache);
        return;
      case END_OBJECT:
        handleEndObject(jsonParser, objectTracker, jsonGenerator, obfuscationCache);
        return;
      case START_ARRAY:
        jsonGenerator.writeStartArray();
        return;
      case END_ARRAY:
        jsonGenerator.writeEndArray();
        return;
      case FIELD_NAME:
        fieldName = jsonParser.getCurrentName();
        objectTracker.addCurrentObjectAttribute(fieldName);
        jsonGenerator.writeFieldName(fieldName);
        return;
      case VALUE_NULL:
      case VALUE_TRUE:
      case VALUE_FALSE:
      case VALUE_EMBEDDED_OBJECT:
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
      case VALUE_STRING:
        handleValue(jsonParser, objectTracker, jsonGenerator, obfuscationCache);
        return;
    } 
    throw new IllegalStateException("Cannot handle unknown token " + currentToken);
  }
  
  protected abstract void handleStartObject(String paramString, VmodlJsonParser paramVmodlJsonParser, VmodlObjectTracker paramVmodlObjectTracker, JsonGenerator paramJsonGenerator, ObfuscationCache paramObfuscationCache) throws IOException;
  
  protected abstract void handleEndObject(VmodlJsonParser paramVmodlJsonParser, VmodlObjectTracker paramVmodlObjectTracker, JsonGenerator paramJsonGenerator, ObfuscationCache paramObfuscationCache) throws IOException;
  
  protected abstract void handleValue(VmodlJsonParser paramVmodlJsonParser, VmodlObjectTracker paramVmodlObjectTracker, JsonGenerator paramJsonGenerator, ObfuscationCache paramObfuscationCache) throws IOException;
  
  protected void writeVmodlStartObject(String vmodlObjectType, VmodlJsonParser jsonParser, VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartObject();
    JsonToken currentToken = jsonParser.currentToken();
    if (currentToken == JsonToken.FIELD_NAME) {
      String fieldName = jsonParser.getCurrentName();
      objectTracker.addCurrentObjectAttribute(fieldName);
      jsonGenerator.writeFieldName(fieldName);
    } else if (currentToken == JsonToken.VALUE_STRING) {
      jsonGenerator.writeStringField("@type", vmodlObjectType);
    } 
  }
  
  protected void writeRawValue(VmodlJsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    JsonToken valueToken = jsonParser.currentToken();
    switch (valueToken) {
      case VALUE_STRING:
        jsonGenerator.writeString(jsonParser.getValueAsString());
        break;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        jsonGenerator.writeNumber(jsonParser.getValueAsString());
        break;
      case VALUE_TRUE:
      case VALUE_FALSE:
        jsonGenerator.writeBoolean(jsonParser.getBooleanValue());
        break;
      case VALUE_NULL:
        jsonGenerator.writeNull();
        break;
      case VALUE_EMBEDDED_OBJECT:
        jsonGenerator.writeEmbeddedObject(jsonParser.getEmbeddedObject());
        break;
    } 
  }
  
  protected static String obfuscateString(String valueAsString, SubstringObfuscationSpec substringObfuscationSpec, ObfuscationCache obfuscationCache) {
    String obfuscatedValue;
    if (substringObfuscationSpec.shouldObfuscateBySubstrings()) {
      obfuscatedValue = ObfuscationUtil.obfuscateSubstring(valueAsString, substringObfuscationSpec
          
          .getSubstringSplitPattern(), substringObfuscationSpec
          .getObfuscatedSubstringDelimiter(), obfuscationCache);
    } else {
      obfuscatedValue = ObfuscationUtil.obfuscateString(valueAsString, obfuscationCache);
    } 
    return obfuscatedValue;
  }
  
  protected static class VmodlObjectTracker {
    private final Deque<String> _currentObjectStack = new LinkedList<>();
    
    private final Map<String, String> _objectTypeToDynamicPropertyKeyInObjectValue = new HashMap<>();
    
    private final Map<String, String> _objectTypeToCurrentAttribute = new HashMap<>();
    
    private final Map<String, String> _objectTypeToObjectId = new HashMap<>();
    
    private final Map<String, String> _objectTypeToObjectName = new HashMap<>();
    
    protected String getCurrentObjectType() {
      return this._currentObjectStack.peek();
    }
    
    protected String getValueOfDynamicPropertyKeyInCurrentObject() {
      String objectType = getCurrentObjectType();
      return this._objectTypeToDynamicPropertyKeyInObjectValue.get(objectType);
    }
    
    protected void addVmodlObject(String vmodlObjectType) {
      this._currentObjectStack.push(vmodlObjectType);
    }
    
    protected void addVmodlDynamicTypeObject(String vmodlObjectType, String valueOfDynamicPropertyKeyInObject) {
      this._currentObjectStack.push(vmodlObjectType);
      this._objectTypeToDynamicPropertyKeyInObjectValue.put(vmodlObjectType, valueOfDynamicPropertyKeyInObject);
    }
    
    protected void addVmodlObjectId(String vmodlObjectId) {
      String objectType = getCurrentObjectType();
      this._objectTypeToObjectId.put(objectType, vmodlObjectId);
    }
    
    protected void addVmodlObjectName(String vmodlObjectName) {
      String objectType = getCurrentObjectType();
      this._objectTypeToObjectName.put(objectType, vmodlObjectName);
    }
    
    protected void addCurrentObjectAttribute(String currentAttributeName) {
      String objectType = getCurrentObjectType();
      this._objectTypeToCurrentAttribute.put(objectType, currentAttributeName);
    }
    
    protected String getCurrentObjectId() {
      String objectType = getCurrentObjectType();
      return this._objectTypeToObjectId.get(objectType);
    }
    
    protected String getCurrentObjectName() {
      String objectType = getCurrentObjectType();
      return this._objectTypeToObjectName.get(objectType);
    }
    
    protected String getCurrentObjectAttribute() {
      String objectType = getCurrentObjectType();
      return this._objectTypeToCurrentAttribute.get(objectType);
    }
    
    protected void removeCurrentVmodlObject() {
      String removedObjectType = this._currentObjectStack.pop();
      this._objectTypeToCurrentAttribute.remove(removedObjectType);
      this._objectTypeToDynamicPropertyKeyInObjectValue.remove(removedObjectType);
      this._objectTypeToObjectId.remove(removedObjectType);
      this._objectTypeToObjectName.remove(removedObjectType);
    }
  }
}
