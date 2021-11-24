package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Vmodl1ModelKeyRouter implements QueryRouter {
  public static final String GLOBAL_SERVER_GUID = "GLOBAL";
  
  public Query route(Query query, String targetInstanceId) {
    if (query.getFilter() == null)
      return query; 
    Filter filter = query.getFilter();
    List<PropertyPredicate> processedPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (PropertyUtil.isModelKey(predicate.getProperty())) {
        List<Object> keysForTargetInstance;
        String predicateTargetInstance;
        switch (predicate.getOperator()) {
          case EQUAL:
          case IN:
            keysForTargetInstance = extractModelKeysForServiceId(predicate
                .getComparableValue(), targetInstanceId);
            if (!keysForTargetInstance.isEmpty()) {
              processedPredicates.add(toModelKeyPredicate(keysForTargetInstance));
              continue;
            } 
            if (filter.getOperator() == LogicalOperator.AND)
              return null; 
            continue;
          case NOT_EQUAL:
            predicateTargetInstance = getServiceInstanceIdFromModelKey(predicate
                .getComparableValue());
            if (targetInstanceId.equals(predicateTargetInstance)) {
              processedPredicates.add(predicate);
              continue;
            } 
            if (filter.getOperator() == LogicalOperator.OR)
              return RouterUtils.replaceCriteria(query, null); 
            continue;
        } 
        processedPredicates.add(predicate);
        continue;
      } 
      processedPredicates.add(predicate);
    } 
    if (processedPredicates.isEmpty() && filter.getOperator() == LogicalOperator.OR)
      return null; 
    return RouterUtils.replaceCriteria(query, processedPredicates);
  }
  
  private List<Object> extractModelKeysForServiceId(Object predicateModelKeyValue, String targetInstanceId) {
    Collection<Object> modelKeyValues = RouterUtils.toCollection(predicateModelKeyValue);
    List<Object> keysForService = new ArrayList(modelKeyValues.size());
    for (Object modelKeyObj : modelKeyValues) {
      if (targetInstanceId.equals(getServiceInstanceIdFromModelKey(modelKeyObj)))
        keysForService.add(modelKeyObj); 
    } 
    return keysForService;
  }
  
  private String getServiceInstanceIdFromModelKey(Object modelKeyObj) {
    if (!(modelKeyObj instanceof ManagedObjectReference))
      throw new IllegalArgumentException(String.format("The comparableValue for %s must be ManagedObjectReference or a collection of ManagedObjectReference.", new Object[] { "@modelKey" })); 
    ManagedObjectReference key = (ManagedObjectReference)modelKeyObj;
    if (isGlobalResource(key))
      throw new IllegalArgumentException(String.format("The comparableValue for %s must not be global resource: %s", new Object[] { "@modelKey", key })); 
    return key.getServerGuid();
  }
  
  private static PropertyPredicate toModelKeyPredicate(List<Object> modelKeyValues) {
    PropertyPredicate.ComparisonOperator operator;
    Object<Object> comparableValue;
    assert !modelKeyValues.isEmpty();
    if (modelKeyValues.size() == 1) {
      operator = PropertyPredicate.ComparisonOperator.EQUAL;
      comparableValue = (Object<Object>)modelKeyValues.get(0);
    } else {
      operator = PropertyPredicate.ComparisonOperator.IN;
      comparableValue = (Object<Object>)modelKeyValues;
    } 
    return new PropertyPredicate("@modelKey", operator, comparableValue);
  }
  
  static boolean isGlobalResource(ManagedObjectReference key) {
    assert key != null;
    return "GLOBAL".equalsIgnoreCase(key.getServerGuid());
  }
}
