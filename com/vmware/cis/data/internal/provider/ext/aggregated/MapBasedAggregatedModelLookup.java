package com.vmware.cis.data.internal.provider.ext.aggregated;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class MapBasedAggregatedModelLookup implements AggregatedModelLookup {
  private final Map<String, Set<String>> _childModelsByAggregatedModel;
  
  public MapBasedAggregatedModelLookup(Map<String, Set<String>> childModelsByAggregatedModel) {
    validateMap(childModelsByAggregatedModel);
    this
      ._childModelsByAggregatedModel = Collections.unmodifiableMap(childModelsByAggregatedModel);
  }
  
  public Set<String> getChildrenOfAggregatedModel(String resourceModel) {
    Validate.notEmpty(resourceModel);
    Set<String> childModels = this._childModelsByAggregatedModel.get(resourceModel);
    if (childModels != null)
      return Collections.unmodifiableSet(childModels); 
    return Collections.emptySet();
  }
  
  public Set<String> getAllAggregatedModels() {
    return this._childModelsByAggregatedModel.keySet();
  }
  
  private static void validateMap(Map<String, Set<String>> childModelsByAggregatedModel) {
    Validate.notNull(childModelsByAggregatedModel);
    for (Map.Entry<String, Set<String>> e : childModelsByAggregatedModel.entrySet()) {
      Validate.notNull(e.getKey(), "Aggregated model name must not be null");
      Validate.notEmpty(e.getValue(), "Set of child models must not be null or empty");
    } 
  }
}
