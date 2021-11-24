package com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.vmware.ph.phservice.common.cdf.internal.VmodlDynamicTypeUtil;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationCache;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class VmodlObjectTypeObfuscationRule extends StreamingJsonParserObfuscationRule {
  private final Map<String, TypeRuleSpec> _typeToRuleSpec;
  
  private final VmodlToJsonLdSerializer _vmodlToJsonLdSerializer;
  
  public VmodlObjectTypeObfuscationRule(Map<String, TypeRuleSpec> typeToRuleSpec) {
    this(typeToRuleSpec, (VmodlToJsonLdSerializer)null);
  }
  
  public VmodlObjectTypeObfuscationRule(Map<String, TypeRuleSpec> typeToRuleSpec, VmodlToJsonLdSerializer vmodlToJsonLdSerializer) {
    Objects.requireNonNull(typeToRuleSpec);
    this._typeToRuleSpec = typeToRuleSpec;
    this._vmodlToJsonLdSerializer = vmodlToJsonLdSerializer;
  }
  
  protected boolean isObfuscationRuleEmpty() {
    return this._typeToRuleSpec.isEmpty();
  }
  
  protected void handleStartObject(String vmodlObjectType, VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    if (VmodlDynamicTypeUtil.isDynamicType(vmodlObjectType)) {
      String dynamicKey = VmodlDynamicTypeUtil.getDynamicPropertyKeyForVmodlType(vmodlObjectType);
      String dynamicKeyValue = jsonParser.getFieldValue(dynamicKey);
      objectTracker.addVmodlDynamicTypeObject(vmodlObjectType, dynamicKeyValue);
    } else {
      objectTracker.addVmodlObject(vmodlObjectType);
    } 
    writeVmodlStartObject(vmodlObjectType, jsonParser, objectTracker, jsonGenerator);
  }
  
  protected void handleValue(VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    String attributeValueAsString = jsonParser.getValueAsString();
    String attributeName = objectTracker.getCurrentObjectAttribute();
    if (attributeName.equals("@id")) {
      objectTracker.addVmodlObjectId(attributeValueAsString);
    } else if (attributeName.equals("moId")) {
      objectTracker.addVmodlObjectId(attributeValueAsString);
    } else if (attributeName.equals("name")) {
      objectTracker.addVmodlObjectName(attributeValueAsString);
    } 
    TypeRuleSpec оbjectTypeRuleSpec = getMatchingObjectTypeRule(this._typeToRuleSpec, objectTracker, attributeValueAsString);
    if (оbjectTypeRuleSpec != null) {
      String obfuscatedValue = obfuscateString(attributeValueAsString, оbjectTypeRuleSpec
          
          .getSubstringObfuscationSpec(), obfuscationCache);
      jsonGenerator.writeString(obfuscatedValue);
    } else {
      writeRawValue(jsonParser, jsonGenerator);
    } 
  }
  
  protected void handleEndObject(VmodlJsonParser jsonParser, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, JsonGenerator jsonGenerator, ObfuscationCache obfuscationCache) throws IOException {
    storeObjectIdAndNameInObfuscationCache(objectTracker, obfuscationCache, this._vmodlToJsonLdSerializer);
    objectTracker.removeCurrentVmodlObject();
    jsonGenerator.writeEndObject();
  }
  
  private static TypeRuleSpec getMatchingObjectTypeRule(Map<String, TypeRuleSpec> vmodlTypeToTypeObfuscationSpec, StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, String attributeValueAsString) {
    String objectType = objectTracker.getCurrentObjectType();
    TypeRuleSpec matchingObjectTypeRuleSpec = null;
    if (vmodlTypeToTypeObfuscationSpec.containsKey(objectType)) {
      TypeRuleSpec оbjectTypeRuleSpec = vmodlTypeToTypeObfuscationSpec.get(objectType);
      String attributeName = objectTracker.getCurrentObjectAttribute();
      String valueOfDynamicPropertyKeyInCurrentObject = objectTracker.getValueOfDynamicPropertyKeyInCurrentObject();
      boolean isAttributeMatchingObfuscationRule = isAttributeMatchingObfuscationRule(оbjectTypeRuleSpec, objectType, attributeName, attributeValueAsString, valueOfDynamicPropertyKeyInCurrentObject);
      if (isAttributeMatchingObfuscationRule)
        matchingObjectTypeRuleSpec = оbjectTypeRuleSpec; 
    } 
    return matchingObjectTypeRuleSpec;
  }
  
  private static boolean isAttributeMatchingObfuscationRule(TypeRuleSpec оbjectTypeRuleSpec, String objectType, String objectAttributeName, String objectAttributeValueAsString, String valueOfDynamicPropertyKeyInObject) {
    boolean isAttributeMatchingObfuscationRule = false;
    if (оbjectTypeRuleSpec != null && objectAttributeValueAsString != null) {
      objectAttributeName = resolveAttributeFromJsonAttributeName(objectType, objectAttributeName, valueOfDynamicPropertyKeyInObject);
      isAttributeMatchingObfuscationRule = оbjectTypeRuleSpec.getAttributes().contains(objectAttributeName);
    } 
    return isAttributeMatchingObfuscationRule;
  }
  
  private static String resolveAttributeFromJsonAttributeName(String objectType, String jsonAttributeName, String valueOfDynamicPropertyKeyInObject) {
    String attributeName = null;
    if (valueOfDynamicPropertyKeyInObject != null) {
      String dynamicPropertyValue = VmodlDynamicTypeUtil.getDynamicPropertyValueForVmodlType(objectType);
      if (StringUtils.equals(dynamicPropertyValue, jsonAttributeName))
        attributeName = valueOfDynamicPropertyKeyInObject; 
    } else {
      attributeName = jsonAttributeName;
    } 
    return attributeName;
  }
  
  private static void storeObjectIdAndNameInObfuscationCache(StreamingJsonParserObfuscationRule.VmodlObjectTracker objectTracker, ObfuscationCache obfuscationCache, VmodlToJsonLdSerializer vmodlToJsonLdSerializer) {
    if (vmodlToJsonLdSerializer == null)
      return; 
    String objectId = objectTracker.getCurrentObjectId();
    if (objectId == null)
      return; 
    String objectName = objectTracker.getCurrentObjectName();
    if (objectName == null)
      return; 
    Object moRefObject = getMoRef(vmodlToJsonLdSerializer, objectId, objectTracker

        
        .getCurrentObjectType());
    if (moRefObject instanceof ManagedObjectReference) {
      ManagedObjectReference moRef = (ManagedObjectReference)moRefObject;
      String id = moRef.getValue();
      obfuscationCache.storeDeobfuscated(id, objectName);
    } 
  }
  
  private static Object getMoRef(VmodlToJsonLdSerializer vmodlToJsonLdSerializer, String moId, String moType) {
    Object moRefObject = null;
    JSONObject moRefObjectJson = new JSONObject();
    moRefObjectJson.put("@id", moId);
    moRefObjectJson.put("@type", moType);
    try {
      moRefObject = vmodlToJsonLdSerializer.deserialize(moRefObjectJson);
    } catch (Exception exception) {}
    if (!(moRefObject instanceof ManagedObjectReference)) {
      moRefObjectJson = new JSONObject();
      moRefObjectJson.put("moId", moId);
      moRefObjectJson.put("@type", moType);
      moRefObject = vmodlToJsonLdSerializer.deserialize(moRefObjectJson);
    } 
    return moRefObject;
  }
  
  public static class TypeRuleSpec {
    private final String _vmodlType;
    
    private final List<String> _attributes;
    
    private final SubstringObfuscationSpec _substringObfuscationSpec;
    
    public TypeRuleSpec(String vmodlType, List<String> attributes, boolean obfuscateBySubstring) {
      this(vmodlType, attributes, new SubstringObfuscationSpec(obfuscateBySubstring));
    }
    
    public TypeRuleSpec(String vmodlType, List<String> attributes, SubstringObfuscationSpec substringObfuscationSpec) {
      this._vmodlType = vmodlType;
      this._attributes = attributes;
      this._substringObfuscationSpec = substringObfuscationSpec;
    }
    
    public String getVmodlType() {
      return this._vmodlType;
    }
    
    public List<String> getAttributes() {
      return this._attributes;
    }
    
    public SubstringObfuscationSpec getSubstringObfuscationSpec() {
      return this._substringObfuscationSpec;
    }
  }
}
