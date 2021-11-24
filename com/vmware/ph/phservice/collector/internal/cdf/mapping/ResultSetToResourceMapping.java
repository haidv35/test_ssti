package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityResourceMapping;
import java.util.HashMap;
import java.util.Map;

public class ResultSetToResourceMapping extends VelocityResourceMapping<ResultSet> {
  public ResultSetToResourceMapping(String resourceType, String idPattern, Map<String, String> attributePatterns, Map<String, String> relationPatterns, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    super(resourceType, idPattern, attributePatterns, relationPatterns, velocityPatternEvaluatorFactory);
  }
  
  public Map<String, Object> createDictionary(ResultSet input) {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("totalMatchedObjectCount", getItemsTotalCount(input));
    ctx.put("totalCount", getTotalCount(input));
    return ctx;
  }
  
  Integer getTotalCount(ResultSet resultSet) {
    if (resultSet == null)
      return Integer.valueOf(0); 
    if (resultSet.getTotalCount() != null)
      return resultSet.getTotalCount(); 
    return Integer.valueOf(0);
  }
  
  Integer getItemsTotalCount(ResultSet resultSet) {
    if (resultSet == null)
      return null; 
    if (resultSet.getTotalCount() != null)
      return resultSet.getTotalCount(); 
    return Integer.valueOf(resultSet.getItems().size());
  }
}
