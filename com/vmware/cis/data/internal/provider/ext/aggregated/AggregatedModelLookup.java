package com.vmware.cis.data.internal.provider.ext.aggregated;

import java.util.Set;

public interface AggregatedModelLookup {
  Set<String> getChildrenOfAggregatedModel(String paramString);
  
  Set<String> getAllAggregatedModels();
}
