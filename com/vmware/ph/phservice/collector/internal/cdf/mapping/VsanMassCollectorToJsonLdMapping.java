package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VsanMassCollectorToJsonLdMapping implements Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> {
  static final String VSAN_MASS_COLLECTOR_JSON_OUTPUT_PROPERTY = "vsanRetrievePropertiesJson";
  
  static final String PERF_DIAGNOSE_DATA_JSON_OUTPUT_PROPERTY = "perfDiagnoseDataJson";
  
  static final String OBJECT_ID_PROPERTY = "objectId";
  
  static final String TRANSACTION_ID_PROPERTY = "transactionId";
  
  static final String RESULT_JSON_KEY = "result";
  
  static final String DATA_JSON_KEY = "data";
  
  private static final String ID_JSON_KEY = "@id";
  
  private static final String JSON_START_OBJECT = "{";
  
  private static final String JSON_END_OBJECT = "}";
  
  private static final String JSON_NEXT_ELEMENT = ",";
  
  private static final String JSON_FIELD_PATTERN = "\"%s\":";
  
  private static final String JSON_FIELD_VALUE_PATTERN = "\"%s\":\"%s\"";
  
  public Collection<JsonLd> map(NamedPropertiesResourceItem input, Context context) {
    if (input == null || input.getResourceItem() == null)
      return Collections.emptyList(); 
    JsonLd jsonLdOutput = null;
    List<String> actualPropertyNames = input.getActualPropertyNames();
    if (actualPropertyNames.contains("vsanRetrievePropertiesJson")) {
      jsonLdOutput = getVsanMassCollectorResult(input, context);
    } else if (actualPropertyNames.contains("perfDiagnoseDataJson")) {
      jsonLdOutput = getPerfDiagnoseDataJsonResult(input, context);
    } 
    return Optional.<JsonLd>ofNullable(jsonLdOutput)
      .map(Collections::singletonList)
      .orElse(Collections.emptyList());
  }
  
  private JsonLd getVsanMassCollectorResult(NamedPropertiesResourceItem input, Context context) {
    String result = (String)input.getResourceItem().get("vsanRetrievePropertiesJson");
    if (result == null)
      return null; 
    Map<String, Object> jsonProperties = generateJsonProperties(input, context, "objectId");
    int indexOfResultValue = getIndexOfJsonFieldValue(result, "result");
    String resultJson = generateResultJson(jsonProperties, result, indexOfResultValue);
    return new JsonLd(resultJson);
  }
  
  private JsonLd getPerfDiagnoseDataJsonResult(NamedPropertiesResourceItem input, Context context) {
    Object result = input.getResourceItem().get("perfDiagnoseDataJson");
    if (result == null)
      return null; 
    Map<String, Object> jsonProperties = generateJsonProperties(input, context, "transactionId");
    String resultJson = generateResultJson(jsonProperties, result
        
        .toString(), 0);
    return new JsonLd(resultJson);
  }
  
  protected Map<String, Object> generateJsonProperties(NamedPropertiesResourceItem input, Context context, String objectIdProperty) {
    Map<String, Object> jsonProperties = new LinkedHashMap<>();
    Object objectId = input.getResourceItem().get(objectIdProperty);
    if (objectId != null)
      jsonProperties.put("@id", objectId); 
    return jsonProperties;
  }
  
  private static String generateResultJson(Map<String, Object> jsonProperties, String result, int indexOfResultValue) {
    int capacity = estimateExpectedResultJsonLength(jsonProperties, "data", result);
    StringBuilder resultJson = new StringBuilder(capacity);
    resultJson.append("{");
    for (Map.Entry<String, Object> jsonProperty : jsonProperties.entrySet()) {
      resultJson.append(
          String.format("\"%s\":\"%s\"", new Object[] { jsonProperty.getKey(), jsonProperty
              .getValue() }));
      resultJson.append(",");
    } 
    resultJson.append(String.format("\"%s\":", new Object[] { "data" }));
    resultJson.append(result, indexOfResultValue, result.length());
    if (!result.endsWith("}"))
      resultJson.append("}"); 
    return resultJson.toString();
  }
  
  private static int estimateExpectedResultJsonLength(Map<String, Object> jsonProperties, String resultFieldName, String result) {
    int resultJsonLength = result.length();
    int jsonFieldValueLength = "\"%s\":\"%s\"".length();
    for (Map.Entry<String, Object> jsonProperty : jsonProperties.entrySet()) {
      resultJsonLength += ((String)jsonProperty.getKey()).length() + jsonFieldValueLength;
      resultJsonLength += jsonProperty.getValue().toString().length();
    } 
    resultJsonLength += resultFieldName.length() + jsonFieldValueLength;
    return resultJsonLength;
  }
  
  private static int getIndexOfJsonFieldValue(String json, String fieldName) {
    return json.indexOf(':', json.indexOf(fieldName)) + 1;
  }
}
