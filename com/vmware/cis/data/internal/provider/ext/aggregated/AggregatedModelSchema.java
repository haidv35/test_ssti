package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.api.QuerySchema;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AggregatedModelSchema {
  private static final Logger _logger = LoggerFactory.getLogger(AggregatedModelSchema.class);
  
  public static QuerySchema addAggregatedModels(QuerySchema schema, AggregatedModelLookup lookup) {
    assert schema != null;
    assert lookup != null;
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>(schema.getModels());
    for (String aggregatedModel : lookup.getAllAggregatedModels()) {
      Set<String> childModels = lookup.getChildrenOfAggregatedModel(aggregatedModel);
      if (childModels.contains(aggregatedModel))
        throw new UnsupportedOperationException("Aggregated model has the same name as one of its child models: " + aggregatedModel); 
      QuerySchema.ModelInfo aggregatedModelInfo = getAggregatedModelInfo(schema, aggregatedModel, childModels);
      if (aggregatedModelInfo == null)
        continue; 
      models.put(aggregatedModel, aggregatedModelInfo);
    } 
    return QuerySchema.forModels(models);
  }
  
  private static QuerySchema.ModelInfo getAggregatedModelInfo(QuerySchema schema, String aggregatedModel, Set<String> childModels) {
    assert schema != null;
    assert aggregatedModel != null;
    assert childModels != null;
    assert !childModels.isEmpty();
    Map<String, QuerySchema.PropertyInfo> props = new HashMap<>();
    for (String childModel : childModels) {
      QuerySchema.ModelInfo childModelInfo = schema.getModels().get(childModel);
      if (childModelInfo != null)
        props.putAll(childModelInfo.getProperties()); 
    } 
    if (props.isEmpty()) {
      _logger.debug("Hide aggregated model '{}' because it contains no properties", aggregatedModel);
      return null;
    } 
    return new QuerySchema.ModelInfo(props);
  }
}
