package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractForeignKeyRouter implements QueryRouter {
  public final Query route(Query query, String targetInstanceId) {
    if (query.getFilter() == null)
      return query; 
    Filter filter = query.getFilter();
    List<PropertyPredicate> processedPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      List<Object> keysForTargetInstance;
      switch (predicate.getOperator()) {
        case EQUAL:
        case IN:
          keysForTargetInstance = tryExtractForeignKeysForInstanceId(predicate, targetInstanceId);
          if (keysForTargetInstance == null) {
            processedPredicates.add(predicate);
            continue;
          } 
          if (!keysForTargetInstance.isEmpty()) {
            processedPredicates.add(toEqualityPredicate(predicate.getProperty(), keysForTargetInstance));
            continue;
          } 
          if (filter.getOperator() == LogicalOperator.AND)
            return null; 
          continue;
      } 
      processedPredicates.add(predicate);
    } 
    if (processedPredicates.isEmpty() && filter.getOperator() == LogicalOperator.OR)
      return null; 
    return RouterUtils.replaceCriteria(query, processedPredicates);
  }
  
  protected abstract List<Object> tryExtractForeignKeysForInstanceId(PropertyPredicate paramPropertyPredicate, String paramString);
  
  private static PropertyPredicate toEqualityPredicate(String property, List<Object> values) {
    PropertyPredicate.ComparisonOperator operator;
    Object<Object> comparableValue;
    assert !values.isEmpty();
    if (values.size() == 1) {
      operator = PropertyPredicate.ComparisonOperator.EQUAL;
      comparableValue = (Object<Object>)values.iterator().next();
    } else {
      operator = PropertyPredicate.ComparisonOperator.IN;
      comparableValue = (Object<Object>)values;
    } 
    return new PropertyPredicate(property, operator, comparableValue);
  }
}
