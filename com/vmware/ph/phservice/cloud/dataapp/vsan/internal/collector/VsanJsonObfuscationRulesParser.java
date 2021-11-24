package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.ComplexVmodlObjectTypeObfuscationRule;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.RegexBasedRule;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.VmodlObjectTypeObfuscationRule;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationException;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.vsan.binding.vim.VsanObjectTypeRule;
import com.vmware.vim.vsan.binding.vim.VsanRegexBasedRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VsanJsonObfuscationRulesParser {
  private static final String COMPLEX_RULES_PARSING_ERROR_MSG = "Could not parse the obfuscation complex rules definition.";
  
  private static final String COMPLEX_RULES_TRAVERSAL_SPEC_PROPERTY = "traversalSpec";
  
  private static final String COMPLEX_RULES_OBJ_SPEC_PROPERTY = "objSpec";
  
  private static final String COMPLEX_RULES_OBFUSCATE_SUBSTRING_PROPERTY = "obfuscate_substring";
  
  private static final String COMPLEX_RULES_OBFUSCATION_INDICES_PROPERTY = "obfuscationIndices";
  
  private static final int INDEX_NOT_FOUND_VALUE = -1;
  
  private static final Log _log = LogFactory.getLog(VsanJsonObfuscationRulesParser.class);
  
  public List<ObfuscationRule> parse(JSONObject obfuscationJsonObject, VmodlToJsonLdSerializer serializer) {
    JSONArray obfuscationValueJsonArray = obfuscationJsonObject.optJSONArray("obfuscation");
    if (obfuscationValueJsonArray == null || obfuscationValueJsonArray.length() == 0)
      return null; 
    Object obfuscationTypeToRulesObject = serializer.deserialize(obfuscationValueJsonArray);
    if (!(obfuscationTypeToRulesObject instanceof KeyAnyValue[]))
      return null; 
    KeyAnyValue[] obfuscationTypeToRulesArray = (KeyAnyValue[])obfuscationTypeToRulesObject;
    List<ObfuscationRule> regexBasedRules = new ArrayList<>();
    List<ObfuscationRule> typeBasedRules = new ArrayList<>();
    List<ObfuscationRule> complexTypeBasedRules = new ArrayList<>();
    for (KeyAnyValue obfuscationTypeToRules : obfuscationTypeToRulesArray) {
      String obfuscationType = obfuscationTypeToRules.getKey();
      Object obfuscationRules = obfuscationTypeToRules.getValue();
      if (obfuscationRules instanceof VsanRegexBasedRule[]) {
        VsanRegexBasedRule[] vsanRegexBasedRules = (VsanRegexBasedRule[])obfuscationRules;
        List<ObfuscationRule> vsanRegexObfuscationRules = parseVsanRegexBasedRules(vsanRegexBasedRules);
        regexBasedRules.addAll(vsanRegexObfuscationRules);
      } else if (obfuscationRules instanceof VsanObjectTypeRule[]) {
        VsanObjectTypeRule[] vsanObjectTypeRules = (VsanObjectTypeRule[])obfuscationRules;
        ObfuscationRule vsanObjectTypeRule = parseVsanObjectTypeRule(vsanObjectTypeRules, serializer);
        typeBasedRules.add(vsanObjectTypeRule);
      } else if (obfuscationType.equalsIgnoreCase("complexRules")) {
        try {
          JSONObject vsanComplexTypeRules = new JSONObject((String)obfuscationRules);
          ObfuscationRule vsanComplexTypeRule = parseVsanComplexTypeRule(vsanComplexTypeRules);
          complexTypeBasedRules.add(vsanComplexTypeRule);
        } catch (Exception e) {
          _log.debug("Could not parse the obfuscation complex rules definition.", e);
          _log.warn("Could not parse the obfuscation complex rules definition.");
        } 
      } 
    } 
    List<ObfuscationRule> result = new ArrayList<>();
    result.addAll(typeBasedRules);
    result.addAll(regexBasedRules);
    result.addAll(complexTypeBasedRules);
    return result;
  }
  
  private static List<ObfuscationRule> parseVsanRegexBasedRules(VsanRegexBasedRule[] vsanRegexBasedRules) {
    List<ObfuscationRule> result = new ArrayList<>();
    if (vsanRegexBasedRules != null)
      for (VsanRegexBasedRule regexRuleSpec : vsanRegexBasedRules) {
        String[] patterns = regexRuleSpec.getRules();
        for (String pattern : patterns) {
          if (pattern != null)
            try {
              RegexBasedRule regexBasedRule = new RegexBasedRule(pattern, false);
              result.add(regexBasedRule);
            } catch (ObfuscationException e) {
              if (_log.isErrorEnabled())
                _log.error("Could not create a regex rule from obfuscation pattern.", (Throwable)e); 
            }  
        } 
      }  
    return result;
  }
  
  private static ObfuscationRule parseVsanObjectTypeRule(VsanObjectTypeRule[] vsanObjectTypeRules, VmodlToJsonLdSerializer serializer) {
    VmodlObjectTypeObfuscationRule objectTypeRule = null;
    if (vsanObjectTypeRules != null) {
      Map<String, VmodlObjectTypeObfuscationRule.TypeRuleSpec> vmodlObjectTypeToTypeRuleSpec = new HashMap<>(vsanObjectTypeRules.length);
      for (VsanObjectTypeRule vsanObjectTypeRule : vsanObjectTypeRules) {
        String vmodlObjectType = vsanObjectTypeRule.getObjectType();
        VmodlObjectTypeObfuscationRule.TypeRuleSpec vmodlObjectTypeRuleSpec = new VmodlObjectTypeObfuscationRule.TypeRuleSpec(vmodlObjectType, Arrays.asList(vsanObjectTypeRule.getAttributes()), false);
        vmodlObjectTypeToTypeRuleSpec.put(vmodlObjectType, vmodlObjectTypeRuleSpec);
      } 
      objectTypeRule = new VmodlObjectTypeObfuscationRule(vmodlObjectTypeToTypeRuleSpec, serializer);
    } 
    return (ObfuscationRule)objectTypeRule;
  }
  
  private static ObfuscationRule parseVsanComplexTypeRule(JSONObject vsanComplexTypeRulesJsonObject) {
    Set<String> vmodlObjectTypes = vsanComplexTypeRulesJsonObject.keySet();
    Map<String, List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec>> typeToComplexRuleSpecs = new HashMap<>();
    for (String vmodlObjectType : vmodlObjectTypes) {
      List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec> complexObfuscationSpecs = parseVsanComplexRuleSpecForType(vsanComplexTypeRulesJsonObject
          .getJSONArray(vmodlObjectType), vmodlObjectType);
      typeToComplexRuleSpecs.put(vmodlObjectType, complexObfuscationSpecs);
    } 
    return (ObfuscationRule)new ComplexVmodlObjectTypeObfuscationRule(typeToComplexRuleSpecs);
  }
  
  private static List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec> parseVsanComplexRuleSpecForType(JSONArray complexRuleSpecsArray, String vmodlObjectType) {
    List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec> complexRuleSpecs = new ArrayList<>(complexRuleSpecsArray.length());
    for (int i = 0; i < complexRuleSpecsArray.length(); i++) {
      JSONObject complexRuleSpecObject = complexRuleSpecsArray.getJSONObject(i);
      try {
        ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec complexObfuscationSpec = new ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec(vmodlObjectType, parseVsanAttributePaths(complexRuleSpecObject), parseVsanAttributeNameToValueFilter(complexRuleSpecObject), parseObfuscateBySubstring(complexRuleSpecObject), parseVsanObfuscationIndices(complexRuleSpecObject));
        complexRuleSpecs.add(complexObfuscationSpec);
      } catch (JSONException e) {
        _log.debug("Could not parse the obfuscation complex rules definition.", (Throwable)e);
      } 
    } 
    return complexRuleSpecs;
  }
  
  private static List<String> parseVsanAttributePaths(JSONObject complexRuleSpecObject) {
    List<String> attributePaths = new ArrayList<>();
    JSONArray attributePathsJsonArray = complexRuleSpecObject.getJSONArray("traversalSpec");
    if (attributePathsJsonArray != null) {
      StringBuilder attributePath = new StringBuilder();
      for (int i = 0; i < attributePathsJsonArray.length(); i++) {
        String traversalSpecAttribute = attributePathsJsonArray.getString(i);
        if (traversalSpecAttribute != null)
          attributePath.append(traversalSpecAttribute); 
        if (attributePath.length() > 0 && i < attributePathsJsonArray.length() - 1)
          attributePath.append("-"); 
      } 
      attributePaths.add(attributePath.toString());
    } 
    return attributePaths;
  }
  
  private static Map<String, String> parseVsanAttributeNameToValueFilter(JSONObject complexRuleSpecObject) {
    Map<String, String> attributeNameToValue = new HashMap<>();
    JSONObject objSpec = complexRuleSpecObject.optJSONObject("objSpec");
    if (objSpec != null)
      for (String objAttributeName : objSpec.keySet()) {
        Object objAttributeValue = objSpec.get(objAttributeName);
        attributeNameToValue.put(objAttributeName, objAttributeValue.toString());
      }  
    return attributeNameToValue;
  }
  
  private static boolean parseObfuscateBySubstring(JSONObject complexRuleSpecObject) {
    boolean obfuscateBySubstring = complexRuleSpecObject.optBoolean("obfuscate_substring", false);
    return obfuscateBySubstring;
  }
  
  private static List<Integer> parseVsanObfuscationIndices(JSONObject complexRuleSpecObject) {
    List<Integer> obfuscationIndices = new ArrayList<>();
    JSONArray obfuscationIndicesArray = complexRuleSpecObject.optJSONArray("obfuscationIndices");
    if (obfuscationIndicesArray != null)
      for (int i = 0; i < obfuscationIndicesArray.length(); i++) {
        int index = obfuscationIndicesArray.optInt(i, -1);
        if (index != -1)
          obfuscationIndices.add(Integer.valueOf(index)); 
      }  
    return obfuscationIndices;
  }
}
