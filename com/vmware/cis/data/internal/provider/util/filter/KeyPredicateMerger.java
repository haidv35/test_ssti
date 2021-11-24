package com.vmware.cis.data.internal.provider.util.filter;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class KeyPredicateMerger {
  public static final Filter FILTER_MATCH_NONE = new Filter(
      Collections.singletonList(new PropertyPredicate("@modelKey", PropertyPredicate.ComparisonOperator.EQUAL, "no-such-key")));
  
  public static Query mergeKeyPredicates(Query query) {
    assert query != null;
    Filter transformedFilter = mergeKeyPredicates(query
        .getFilter());
    if (FILTER_MATCH_NONE == transformedFilter)
      return null; 
    return QueryCopy.copy(query)
      .where(transformedFilter)
      .build();
  }
  
  public static Filter mergeKeyPredicates(Filter filter) {
    PropertyPredicate keyPredicate;
    assert filter != FILTER_MATCH_NONE;
    if (filter == null)
      return filter; 
    if (filter.getCriteria().size() == 1)
      return filter; 
    if (!hasMultipleKeyPredicates(filter.getCriteria()))
      return filter; 
    LogicalOperator logicalOperator = filter.getOperator();
    Collection<PropertyPredicate> predicates = filter.getCriteria();
    Collection<Collection<?>> inclusionKeySets = getInclusionKeySets(predicates);
    Collection<Collection<?>> exclusionKeySets = getExclusionKeySets(predicates);
    assert !inclusionKeySets.isEmpty() || !exclusionKeySets.isEmpty();
    switch (logicalOperator) {
      case AND:
        keyPredicate = createKeyPredicateForConjunction(inclusionKeySets, exclusionKeySets);
        if (keyPredicate == null)
          return FILTER_MATCH_NONE; 
        break;
      case OR:
        keyPredicate = createKeyPredicateForDisjunction(inclusionKeySets, exclusionKeySets);
        if (keyPredicate == null)
          return null; 
        break;
      default:
        throw new IllegalStateException("Unsupported logical operator : " + logicalOperator);
    } 
    assert keyPredicate != null;
    return replaceKeyPredicates(filter, keyPredicate);
  }
  
  private static boolean hasMultipleKeyPredicates(List<PropertyPredicate> predicates) {
    assert predicates != null;
    boolean hasPredicateOnKey = false;
    for (PropertyPredicate predicate : predicates) {
      if (isKeyPredicate(predicate)) {
        if (hasPredicateOnKey)
          return true; 
        hasPredicateOnKey = true;
      } 
    } 
    return false;
  }
  
  private static Filter replaceKeyPredicates(Filter filter, PropertyPredicate replacementKeyPredicate) {
    assert filter != null;
    assert replacementKeyPredicate != null;
    Collection<PropertyPredicate> predicates = filter.getCriteria();
    List<PropertyPredicate> transformed = new ArrayList<>(predicates.size());
    transformed.add(replacementKeyPredicate);
    for (PropertyPredicate predicate : predicates) {
      if (isKeyPredicate(predicate))
        continue; 
      transformed.add(predicate);
    } 
    if (transformed.size() == 1)
      return new Filter(transformed); 
    return new Filter(transformed, filter.getOperator());
  }
  
  private static PropertyPredicate createKeyPredicateForConjunction(Collection<Collection<?>> inclusionKeySets, Collection<Collection<?>> exclusionKeySets) {
    assert inclusionKeySets != null;
    assert exclusionKeySets != null;
    Collection<?> includedKeys = intersection(inclusionKeySets);
    Collection<?> excludedKeys = union(exclusionKeySets);
    if (inclusionKeySets.isEmpty()) {
      assert !excludedKeys.isEmpty();
      return keyNotIn(excludedKeys);
    } 
    Collection<?> keys = minus(includedKeys, excludedKeys);
    if (keys.isEmpty())
      return null; 
    return keyIn(keys);
  }
  
  private static PropertyPredicate createKeyPredicateForDisjunction(Collection<Collection<?>> inclusionKeySets, Collection<Collection<?>> exclusionKeySets) {
    assert inclusionKeySets != null;
    assert exclusionKeySets != null;
    Collection<?> includedKeys = union(inclusionKeySets);
    Collection<?> excludedKeys = intersection(exclusionKeySets);
    if (exclusionKeySets.isEmpty()) {
      assert !inclusionKeySets.isEmpty();
      Collection<?> collection = union(inclusionKeySets);
      return keyIn(collection);
    } 
    Collection<?> keys = minus(excludedKeys, includedKeys);
    if (keys.isEmpty())
      return null; 
    return keyNotIn(keys);
  }
  
  private static Collection<?> intersection(Collection<Collection<?>> sets) {
    assert sets != null;
    if (sets.isEmpty())
      return Collections.emptyList(); 
    Iterator<Collection<?>> it = sets.iterator();
    Set<Object> intersection = new LinkedHashSet(it.next());
    while (it.hasNext()) {
      intersection.retainAll(it.next());
      if (intersection.isEmpty())
        return Collections.emptyList(); 
    } 
    return new ArrayList(intersection);
  }
  
  private static Collection<?> union(Collection<Collection<?>> sets) {
    assert sets != null;
    if (sets.isEmpty())
      return Collections.emptyList(); 
    Set<Object> union = new LinkedHashSet();
    for (Collection<?> set : sets)
      union.addAll(set); 
    return new ArrayList(union);
  }
  
  private static Collection<?> minus(Collection<?> set1, Collection<?> set2) {
    assert set1 != null;
    assert set2 != null;
    if (set1.isEmpty() || set2.isEmpty())
      return set1; 
    Set<?> diff = new LinkedHashSet(set1);
    diff.removeAll(set2);
    return new ArrayList(diff);
  }
  
  private static Collection<Collection<?>> getInclusionKeySets(Collection<PropertyPredicate> predicates) {
    assert predicates != null;
    List<Collection<?>> keySets = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      if (isKeyInclusionPredicate(predicate)) {
        Collection<?> keys = getComparableValues(predicate);
        keySets.add(keys);
      } 
    } 
    return keySets;
  }
  
  private static Collection<Collection<?>> getExclusionKeySets(Collection<PropertyPredicate> predicates) {
    assert predicates != null;
    List<Collection<?>> keySets = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      if (isKeyExclusionPredicate(predicate)) {
        Collection<?> keys = getComparableValues(predicate);
        keySets.add(keys);
      } 
    } 
    return keySets;
  }
  
  private static boolean isKeyPredicate(PropertyPredicate predicate) {
    return (isKeyInclusionPredicate(predicate) || isKeyExclusionPredicate(predicate));
  }
  
  private static boolean isKeyInclusionPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    String property = predicate.getProperty();
    PropertyPredicate.ComparisonOperator op = predicate.getOperator();
    return ("@modelKey".equals(property) && (PropertyPredicate.ComparisonOperator.EQUAL.equals(op) || PropertyPredicate.ComparisonOperator.IN.equals(op)));
  }
  
  private static boolean isKeyExclusionPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    String property = predicate.getProperty();
    PropertyPredicate.ComparisonOperator op = predicate.getOperator();
    return ("@modelKey".equals(property) && (PropertyPredicate.ComparisonOperator.NOT_EQUAL.equals(op) || PropertyPredicate.ComparisonOperator.NOT_IN.equals(op)));
  }
  
  private static Collection<?> getComparableValues(PropertyPredicate predicate) {
    assert predicate != null;
    PropertyPredicate.ComparisonOperator op = predicate.getOperator();
    if (PropertyPredicate.ComparisonOperator.IN.equals(op) || PropertyPredicate.ComparisonOperator.NOT_IN.equals(op))
      return (Collection)predicate.getComparableValue(); 
    return Collections.singletonList(predicate.getComparableValue());
  }
  
  private static PropertyPredicate keyIn(Collection<?> keys) {
    PropertyPredicate.ComparisonOperator op;
    Object<?> comparableValue;
    assert keys != null;
    assert !keys.isEmpty();
    if (keys.size() == 1) {
      op = PropertyPredicate.ComparisonOperator.EQUAL;
      comparableValue = (Object<?>)keys.iterator().next();
    } else {
      op = PropertyPredicate.ComparisonOperator.IN;
      comparableValue = (Object<?>)keys;
    } 
    return new PropertyPredicate("@modelKey", op, comparableValue);
  }
  
  private static PropertyPredicate keyNotIn(Collection<?> keys) {
    PropertyPredicate.ComparisonOperator op;
    Object<?> comparableValue;
    assert keys != null;
    assert !keys.isEmpty();
    if (keys.size() == 1) {
      op = PropertyPredicate.ComparisonOperator.NOT_EQUAL;
      comparableValue = (Object<?>)keys.iterator().next();
    } else {
      op = PropertyPredicate.ComparisonOperator.NOT_IN;
      comparableValue = (Object<?>)keys;
    } 
    return new PropertyPredicate("@modelKey", op, comparableValue);
  }
}
