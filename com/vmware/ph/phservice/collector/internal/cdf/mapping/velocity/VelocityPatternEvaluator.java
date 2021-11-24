package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityPatternEvaluator {
  private final VelocityContext _velocityContext;
  
  private final VelocityEngine _velocityEngine;
  
  VelocityPatternEvaluator(VelocityContext velocityContext, VelocityEngine velocityEngine) {
    this._velocityContext = velocityContext;
    this._velocityEngine = velocityEngine;
  }
  
  public String evaluateMappingPattern(String mappingPattern, String logTag) {
    return VelocityHelper.executeVelocityExpression(mappingPattern, this._velocityEngine, this._velocityContext, logTag);
  }
  
  public Map<String, String> evaluateMultipleMappingPatterns(Map<String, String> relationPatterns) {
    Map<String, String> relations = new HashMap<>();
    if (relationPatterns != null)
      for (Map.Entry<String, String> ent : relationPatterns.entrySet()) {
        String evaluatedRelationValue = evaluateMappingPattern(ent.getValue(), ent.getKey());
        if (!StringUtils.isBlank(evaluatedRelationValue))
          relations.put(ent.getKey(), evaluatedRelationValue); 
      }  
    return relations;
  }
  
  public Map<String, Object> evaluateAttributePatterns(Map<String, String> attributePatterns) {
    if (attributePatterns == null)
      return Collections.emptyMap(); 
    return VelocityHelper.evaluateAttributes(attributePatterns, this._velocityEngine, this._velocityContext);
  }
}
