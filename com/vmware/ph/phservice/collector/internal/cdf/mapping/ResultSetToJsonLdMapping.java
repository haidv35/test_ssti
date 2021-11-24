package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluator;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ResultSetToJsonLdMapping implements Mapping<ResultSet, Collection<JsonLd>> {
  private String _mappingCode;
  
  private VelocityPatternEvaluatorFactory _velocityPatternEvaluatorFactory;
  
  public ResultSetToJsonLdMapping(String mappingCode, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    this._mappingCode = mappingCode;
    this._velocityPatternEvaluatorFactory = velocityPatternEvaluatorFactory;
  }
  
  public Collection<JsonLd> map(ResultSet input, Context parentContext) {
    List<JsonLd> results = Collections.emptyList();
    if (this._mappingCode != null) {
      VelocityPatternEvaluator velocityPatternEvaluator = this._velocityPatternEvaluatorFactory.create(
          Collections.singletonMap("result", input));
      String logTag = "[" + getClass().getSimpleName() + "]";
      String expressionResult = velocityPatternEvaluator.evaluateMappingPattern(this._mappingCode, logTag);
      JsonLd jsonLdResult = null;
      if (expressionResult != null) {
        jsonLdResult = new JsonLd(expressionResult);
        results = Collections.singletonList(jsonLdResult);
      } 
    } 
    return Collections.unmodifiableList(results);
  }
}
