package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class RouterUtils {
  public static Query replaceCriteria(Query originalQuery, List<PropertyPredicate> newCriteria) {
    Filter newFilter;
    if (newCriteria == null || newCriteria.isEmpty()) {
      newFilter = null;
    } else {
      assert originalQuery.getFilter() != null;
      newFilter = new Filter(newCriteria, originalQuery.getFilter().getOperator());
    } 
    return 
      QueryCopy.copy(originalQuery)
      .where(newFilter)
      .build();
  }
  
  public static Collection<Object> toCollection(Object predicateComparableValue) {
    Collection<Object> valueAsCollection;
    if (predicateComparableValue instanceof Collection) {
      Collection<Object> asCollection = (Collection<Object>)predicateComparableValue;
      valueAsCollection = asCollection;
    } else {
      valueAsCollection = Collections.singleton(predicateComparableValue);
    } 
    return valueAsCollection;
  }
  
  public static RuntimeException unexpectedOperator(LogicalOperator operator) {
    return new IllegalStateException("Internal error (incomplete implementation): unexpected LogicalOperator " + operator);
  }
}
