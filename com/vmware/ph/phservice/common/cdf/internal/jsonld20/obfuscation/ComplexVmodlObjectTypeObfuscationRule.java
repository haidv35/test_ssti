package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vmware.ph.phservice.common.cdf.internal.VmodlDynamicTypeUtil;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationCache;
import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public final class ComplexVmodlObjectTypeObfuscationRule extends StreamingJsonParserObfuscationRule {
  public static final String DEFAULT_ATTRIBUTE_PATH_DELIMITER_PATTERN = "-";
  
  private static final int ATTRIBUTE_PATH_SPLIT_COUNT_LIMIT = 2;
  
  private final Map<String, List<ComplexRuleSpec>> _typeToComplexRuleSpecs;
  
  public ComplexVmodlObjectTypeObfuscationRule(Map<String, List<ComplexRuleSpec>> typeToComplexRuleSpecs) {
    Objects.requireNonNull(typeToComplexRuleSpecs);
    this._typeToComplexRuleSpecs = typeToComplexRuleSpecs;
  }
  
  protected boolean isObfuscationRuleEmpty() {
    return this._typeToComplexRuleSpecs.isEmpty();
  }
  
  protected void handleStartObject(String vmodlObjectType, VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    if (vmodlObjectType == null || 
      !this._typeToComplexRuleSpecs.containsKey(vmodlObjectType)) {
      writeVmodlStartObject(vmodlObjectType, jsonParser, objectTracker, jsonGenerator);
    } else if (vmodlObjectType != null) {
      ObjectNode rootObject = jsonParser.getObjectNode(vmodlObjectType);
      List<JsonNode> vmodlObjects = rootObject.findParents("@type");
      Deque<JsonNode> unobfuscatedObjects = new LinkedList<>();
      if (vmodlObjects != null)
        unobfuscatedObjects.addAll(vmodlObjects); 
      while (!unobfuscatedObjects.isEmpty()) {
        ObjectNode objectNode = (ObjectNode)unobfuscatedObjects.pop();
        vmodlObjectType = objectNode.get("@type").asText();
        List<ComplexRuleSpec> complexRuleSpecs = this._typeToComplexRuleSpecs.get(vmodlObjectType);
        if (complexRuleSpecs != null)
          applyComplexRulesToObject(objectNode, complexRuleSpecs, obfuscationCache); 
      } 
      jsonGenerator.writeTree((TreeNode)rootObject);
    } 
  }
  
  protected void handleValue(VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    writeRawValue(jsonParser, jsonGenerator);
  }
  
  protected void handleEndObject(VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    jsonGenerator.writeEndObject();
  }
  
  private static void applyComplexRulesToObject(ObjectNode objectNode, List<ComplexRuleSpec> complexRuleSpecs, ObfuscationCache obfuscationCache) {
    for (ComplexRuleSpec complexRuleSpec : complexRuleSpecs) {
      boolean isComplexRuleFilterMatched = isComplexRuleFilterMatched(objectNode, complexRuleSpec
          
          .getAttributeNameToValueFilters());
      if (isComplexRuleFilterMatched) {
        List<String> attributePaths = complexRuleSpec.getAttributePaths();
        for (String attributePath : attributePaths)
          obfuscateRecursively((JsonNode)objectNode, attributePath, complexRuleSpec

              
              .getObfuscationIndices(), complexRuleSpec
              .getSubstringObfuscationSpec(), obfuscationCache); 
      } 
    } 
  }
  
  private static boolean isComplexRuleFilterMatched(ObjectNode objectNode, Map<String, String> attributeNameToValueFilters) {
    Set<String> unmatchedFilterAttributeNames = new HashSet<>(attributeNameToValueFilters.keySet());
    for (String filterAttribute : attributeNameToValueFilters.keySet()) {
      String filterValue = attributeNameToValueFilters.get(filterAttribute);
      if (objectNode.has(filterAttribute)) {
        String attributeValue = objectNode.get(filterAttribute).asText();
        if (StringUtils.equals(filterValue, attributeValue))
          unmatchedFilterAttributeNames.remove(filterAttribute); 
      } 
    } 
    return unmatchedFilterAttributeNames.isEmpty();
  }
  
  private static void obfuscateRecursively(JsonNode object, String attributePath, List<Integer> obfuscationIndices, SubstringObfuscationSpec substringObfuscationSpec, ObfuscationCache obfuscationCache) {
    String[] currentAttributePath = attributePath.split("-", 2);
    String currentAttribute = resolveJsonAttributeNameFromAttributePath(object, currentAttributePath[0]);
    if (!object.has(currentAttribute))
      return; 
    JsonNode currentAttributeValue = object.get(currentAttribute);
    if (currentAttributePath.length == 1) {
      if (currentAttributeValue.isArray()) {
        obfuscateArray((ArrayNode)currentAttributeValue, obfuscationIndices, substringObfuscationSpec, obfuscationCache);
      } else if (object.isObject()) {
        obfuscateObjectAttribute((ObjectNode)object, currentAttribute, substringObfuscationSpec, obfuscationCache);
      } 
    } else {
      String remainingAttributePath = currentAttributePath[1];
      if (currentAttributeValue.isArray()) {
        for (JsonNode arrayElementNode : currentAttributeValue)
          obfuscateRecursively(arrayElementNode, remainingAttributePath, obfuscationIndices, substringObfuscationSpec, obfuscationCache); 
      } else if (currentAttributeValue.isObject()) {
        obfuscateRecursively(currentAttributeValue, remainingAttributePath, obfuscationIndices, substringObfuscationSpec, obfuscationCache);
      } 
    } 
  }
  
  private static void obfuscateObjectAttribute(ObjectNode object, String attribute, SubstringObfuscationSpec substringObfuscationSpec, ObfuscationCache obfuscationCache) {
    JsonNode attributeValue = object.get(attribute);
    String attributeValueAsString = convertJsonNodeToString(attributeValue);
    if (attributeValueAsString != null) {
      String obfuscatedAttributeValue = obfuscateString(attributeValueAsString, substringObfuscationSpec, obfuscationCache);
      object.put(attribute, obfuscatedAttributeValue);
    } 
  }
  
  private static void obfuscateArray(ArrayNode array, List<Integer> obfuscationIndices, SubstringObfuscationSpec substringObfuscationSpec, ObfuscationCache obfuscationCache) {
    boolean areObfuscationIndicesBlank = (obfuscationIndices == null || obfuscationIndices.isEmpty());
    for (int i = 0; i < array.size(); i++) {
      JsonNode arrayElementNode = array.get(i);
      if (arrayElementNode.isArray()) {
        obfuscateArray((ArrayNode)arrayElementNode, obfuscationIndices, substringObfuscationSpec, obfuscationCache);
      } else if (areObfuscationIndicesBlank || obfuscationIndices.contains(Integer.valueOf(i))) {
        String obfuscatedValue = obfuscateString(arrayElementNode
            
            .asText(), substringObfuscationSpec, obfuscationCache);
        array.remove(i);
        array.insert(i, obfuscatedValue);
      } 
    } 
  }
  
  private static String resolveJsonAttributeNameFromAttributePath(JsonNode objectNode, String attributePath) {
    JsonNode typeNode = objectNode.get("@type");
    String jsonAttributeName = attributePath;
    if (typeNode != null) {
      String objectType = typeNode.textValue();
      if (VmodlDynamicTypeUtil.isDynamicType(objectType)) {
        String dynamicPropertyKey = VmodlDynamicTypeUtil.getDynamicPropertyKeyForVmodlType(objectType);
        String dynamicPropertyValue = VmodlDynamicTypeUtil.getDynamicPropertyValueForVmodlType(objectType);
        String valueOfDynamicPropertyKeyInObject = objectNode.get(dynamicPropertyKey).textValue();
        if (StringUtils.equals(valueOfDynamicPropertyKeyInObject, attributePath))
          jsonAttributeName = dynamicPropertyValue; 
      } 
    } 
    return jsonAttributeName;
  }
  
  private static String convertJsonNodeToString(JsonNode attributeValue) {
    String attributeValueAsString;
    if (attributeValue.isNull() || attributeValue.isMissingNode()) {
      attributeValueAsString = null;
    } else if (attributeValue.isValueNode()) {
      attributeValueAsString = attributeValue.asText();
    } else {
      attributeValueAsString = attributeValue.toString();
    } 
    return attributeValueAsString;
  }
  
  public static class ComplexRuleSpec {
    private final String _vmodlType;
    
    private final List<String> _attributePaths;
    
    private final Map<String, String> _attributeNameToValueFilters;
    
    private final List<Integer> _obfuscationIndices;
    
    private final SubstringObfuscationSpec _substringObfuscationSpec;
    
    public ComplexRuleSpec(String vmodlType, List<String> attributePaths, Map<String, String> attributeNameToValue, boolean obfuscateBySubstring, List<Integer> obfuscationIndices) {
      this(vmodlType, attributePaths, attributeNameToValue, new SubstringObfuscationSpec(obfuscateBySubstring), obfuscationIndices);
    }
    
    public ComplexRuleSpec(String vmodlType, List<String> attributePaths, Map<String, String> attributeNameToValueFilters, SubstringObfuscationSpec substringObfuscationSpec, List<Integer> obfuscationIndices) {
      this._vmodlType = vmodlType;
      this._attributePaths = attributePaths;
      this._attributeNameToValueFilters = attributeNameToValueFilters;
      this._substringObfuscationSpec = substringObfuscationSpec;
      this._obfuscationIndices = obfuscationIndices;
    }
    
    public String getVmodlType() {
      return this._vmodlType;
    }
    
    public List<String> getAttributePaths() {
      return this._attributePaths;
    }
    
    public Map<String, String> getAttributeNameToValueFilters() {
      return (this._attributeNameToValueFilters != null) ? this._attributeNameToValueFilters : 
        
        Collections.<String, String>emptyMap();
    }
    
    public List<Integer> getObfuscationIndices() {
      return this._obfuscationIndices;
    }
    
    public SubstringObfuscationSpec getSubstringObfuscationSpec() {
      return this._substringObfuscationSpec;
    }
  }
}
