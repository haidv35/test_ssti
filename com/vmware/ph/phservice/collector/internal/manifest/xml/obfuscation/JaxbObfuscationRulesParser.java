package com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation;

import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data.ObfuscationSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data.ObjectTypeRuleSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.data.RegexRuleSpec;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.ComplexVmodlObjectTypeObfuscationRule;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.RegexBasedRule;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.obfuscation.VmodlObjectTypeObfuscationRule;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationException;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

public class JaxbObfuscationRulesParser implements ObfuscationRulesParser {
  private static final Log _log = LogFactory.getLog(JaxbObfuscationRulesParser.class);
  
  private static JAXBContext _instance;
  
  private static JAXBContext getInstance() throws JAXBException {
    if (_instance == null)
      _instance = JAXBContext.newInstance(new Class[] { ObfuscationSpec.class }); 
    return _instance;
  }
  
  public JaxbObfuscationRulesParser() {
    try {
      getInstance();
    } catch (JAXBException e) {
      throw new Bug("Bad hardcoded JAXB configuration.", e);
    } 
  }
  
  public List<ObfuscationRule> parse(Node xmlNode) {
    ObfuscationSpec obfuscationSpec = getObfuscationSpec(xmlNode);
    List<ObfuscationRule> obfuscationRules = new ArrayList<>();
    obfuscationRules.addAll(buildObfuscationRules(obfuscationSpec));
    return obfuscationRules;
  }
  
  ObfuscationSpec getObfuscationSpec(Node xmlNode) {
    JAXBElement<ObfuscationSpec> obfuscationSpecElement;
    try {
      Unmarshaller unmarshaller = getInstance().createUnmarshaller();
      obfuscationSpecElement = unmarshaller.unmarshal(xmlNode, ObfuscationSpec.class);
    } catch (JAXBException e) {
      throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Failed to parse the request manifest: " + e
            
            .getMessage(), e));
    } 
    return obfuscationSpecElement.getValue();
  }
  
  private static List<ObfuscationRule> buildObfuscationRules(ObfuscationSpec obfuscationSpec) {
    if (obfuscationSpec == null)
      return Collections.emptyList(); 
    List<ObfuscationRule> obfuscationRules = new ArrayList<>();
    ObfuscationRule objectTypeRule = buildTypeRule(obfuscationSpec.getObjectTypeRuleSpecs());
    if (objectTypeRule != null)
      obfuscationRules.add(objectTypeRule); 
    ObfuscationRule complexTypeRule = buildComplexRule(obfuscationSpec.getObjectTypeRuleSpecs());
    if (complexTypeRule != null)
      obfuscationRules.add(complexTypeRule); 
    obfuscationRules.addAll(
        buildRegexRules(obfuscationSpec.getRegexRuleSpecs()));
    return obfuscationRules;
  }
  
  private static ObfuscationRule buildTypeRule(List<ObjectTypeRuleSpec> objectTypeRuleSpecs) {
    Map<String, VmodlObjectTypeObfuscationRule.TypeRuleSpec> typeToRuleSpec = new HashMap<>();
    if (objectTypeRuleSpecs != null)
      for (ObjectTypeRuleSpec objectTypeRuleSpec : objectTypeRuleSpecs) {
        String vmodlType = objectTypeRuleSpec.getType();
        Map<String, String> filters = objectTypeRuleSpec.getFilters();
        if (filters == null) {
          VmodlObjectTypeObfuscationRule.TypeRuleSpec typeRuleSpec = new VmodlObjectTypeObfuscationRule.TypeRuleSpec(vmodlType, objectTypeRuleSpec.getAttributes(), objectTypeRuleSpec.getObfuscateSubstring());
          typeToRuleSpec.put(vmodlType, typeRuleSpec);
        } 
      }  
    VmodlObjectTypeObfuscationRule objectTypeRule = null;
    if (!typeToRuleSpec.isEmpty())
      objectTypeRule = new VmodlObjectTypeObfuscationRule(typeToRuleSpec); 
    return (ObfuscationRule)objectTypeRule;
  }
  
  private static ObfuscationRule buildComplexRule(List<ObjectTypeRuleSpec> objectTypeRuleSpecs) {
    Map<String, List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec>> typeToComplexRuleSpecs = getTypeToComplexRuleSpecs(objectTypeRuleSpecs);
    ComplexVmodlObjectTypeObfuscationRule complexTypeRule = null;
    if (!typeToComplexRuleSpecs.isEmpty())
      complexTypeRule = new ComplexVmodlObjectTypeObfuscationRule(typeToComplexRuleSpecs); 
    return (ObfuscationRule)complexTypeRule;
  }
  
  private static Map<String, List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec>> getTypeToComplexRuleSpecs(List<ObjectTypeRuleSpec> objectTypeRuleSpecs) {
    Map<String, List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec>> typeToComplexRuleSpecs = new HashMap<>();
    if (objectTypeRuleSpecs == null)
      return typeToComplexRuleSpecs; 
    for (ObjectTypeRuleSpec objectTypeRuleSpec : objectTypeRuleSpecs) {
      String vmodlType = objectTypeRuleSpec.getType();
      if (objectTypeRuleSpec.getFilters() != null) {
        ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec complexRuleSpec = new ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec(vmodlType, objectTypeRuleSpec.getAttributes(), objectTypeRuleSpec.getFilters(), objectTypeRuleSpec.getObfuscateSubstring(), objectTypeRuleSpec.getObfuscationIndices());
        List<ComplexVmodlObjectTypeObfuscationRule.ComplexRuleSpec> complexRuleSpecs = typeToComplexRuleSpecs.get(vmodlType);
        if (complexRuleSpecs == null)
          complexRuleSpecs = new ArrayList<>(); 
        complexRuleSpecs.add(complexRuleSpec);
        typeToComplexRuleSpecs.put(vmodlType, complexRuleSpecs);
      } 
    } 
    return typeToComplexRuleSpecs;
  }
  
  private static List<ObfuscationRule> buildRegexRules(List<RegexRuleSpec> regexRuleSpecs) {
    List<ObfuscationRule> regexBasedRules = new ArrayList<>();
    if (regexRuleSpecs != null)
      for (RegexRuleSpec regexRuleSpec : regexRuleSpecs) {
        String pattern = regexRuleSpec.getPattern();
        if (pattern != null)
          try {
            RegexBasedRule regexBasedRule = new RegexBasedRule(pattern, regexRuleSpec.getObfuscateSubstring());
            regexBasedRules.add(regexBasedRule);
          } catch (ObfuscationException e) {
            ExceptionsContextManager.store((Throwable)e);
            if (_log.isErrorEnabled())
              _log.error("Could not create a regex rule from obfuscation pattern.", (Throwable)e); 
          }  
      }  
    return regexBasedRules;
  }
}
