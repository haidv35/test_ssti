package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class InstanceUuidRouter implements QueryRouter {
  public final Query route(Query query, String targetInstanceId) {
    if (query.getFilter() == null)
      return query; 
    Filter filter = query.getFilter();
    List<PropertyPredicate> otherPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (PropertyUtil.isInstanceUuid(predicate.getProperty())) {
        boolean matches;
        switch (predicate.getOperator()) {
          case EQUAL:
          case IN:
            matches = containsTargetInstanceId(predicate.getComparableValue(), targetInstanceId);
            if (matches && filter.getOperator() == LogicalOperator.OR)
              return RouterUtils.replaceCriteria(query, null); 
            if (!matches && filter.getOperator() == LogicalOperator.AND)
              return null; 
            continue;
        } 
        throw new UnsupportedOperationException(String.format("%s with property %s doesn't support operators other than %s and %s", new Object[] { PropertyPredicate.class
                
                .getSimpleName(), predicate
                .getProperty(), PropertyPredicate.ComparisonOperator.EQUAL
                .name(), PropertyPredicate.ComparisonOperator.IN
                .name() }));
      } 
      otherPredicates.add(predicate);
    } 
    if (otherPredicates.isEmpty() && filter.getOperator() == LogicalOperator.OR)
      return null; 
    return RouterUtils.replaceCriteria(query, otherPredicates);
  }
  
  private boolean containsTargetInstanceId(Object predicateInstanceUuid, String targetInstanceId) {
    Collection<Object> instanceIdValues = RouterUtils.toCollection(predicateInstanceUuid);
    return instanceIdValues.contains(targetInstanceId);
  }
}
