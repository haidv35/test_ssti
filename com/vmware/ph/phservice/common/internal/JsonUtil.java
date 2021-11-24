package com.vmware.ph.phservice.common.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.ph.phservice.common.internal.serializer.NullKeySerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JsonUtil {
  private static final Log _log = LogFactory.getLog(JsonUtil.class);
  
  private static final ObjectMapper _mapper = new ObjectMapper();
  
  static {
    _mapper.getSerializerProvider().setNullKeySerializer((JsonSerializer)new NullKeySerializer());
  }
  
  public static String toJson(Object object) {
    String result;
    try {
      result = _mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      if (_log.isWarnEnabled())
        _log.warn("Unable to convert value to json: " + object, (Throwable)e); 
      result = null;
    } 
    return result;
  }
  
  public static String toPrettyJson(String result) {
    if (result == null)
      return null; 
    try {
      Object jsonObject = _mapper.readValue(result, Object.class);
      String prettyJsonString = _mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
      _log.trace("Pretty printed JSON: " + prettyJsonString);
      return prettyJsonString;
    } catch (IOException exception) {
      _log.warn(
          String.format("Trying to pretty print an invalid JSON: %s. Will return unchanged input.", new Object[] { result }), exception);
      return result;
    } 
  }
  
  public static Map<String, Object> jsonToMap(String json) {
    Map<String, Object> jsonMap = new HashMap<>();
    try {
      jsonMap = (Map<String, Object>)_mapper.readValue(json, new TypeReference<Map<String, Object>>() {
          
          });
    } catch (JsonProcessingException e) {
      _log.warn(String.format("Failed parsing JSON string to Map: %s", new Object[] { json }), (Throwable)e);
    } 
    return jsonMap;
  }
  
  public static List<Object> jsonToList(String json) {
    List<Object> jsonList = new ArrayList();
    try {
      jsonList = (List<Object>)_mapper.readValue(json, new TypeReference<List<Object>>() {
          
          });
    } catch (JsonProcessingException e) {
      _log.warn(String.format("Failed parsing JSON string to List: %s", new Object[] { json }), (Throwable)e);
    } 
    return jsonList;
  }
}
