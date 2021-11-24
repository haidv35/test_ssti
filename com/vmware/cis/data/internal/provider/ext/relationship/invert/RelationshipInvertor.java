package com.vmware.cis.data.internal.provider.ext.relationship.invert;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.provider.util.filter.KeyPredicateMerger;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelationshipInvertor implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(RelationshipInvertor.class);
  
  private final DataProvider _dataProvider;
  
  private final RelationshipInversionLookup _relationshipInversions;
  
  public RelationshipInvertor(DataProvider dataProvider, RelationshipInversionLookup relationshipInversions) {
    assert dataProvider != null;
    assert relationshipInversions != null;
    this._dataProvider = dataProvider;
    this._relationshipInversions = relationshipInversions;
  }
  
  public ResultSet executeQuery(Query query) {
    assert query != null;
    if (skipQuery(query)) {
      _logger.trace("Skip query with no invertible relationships");
      return this._dataProvider.executeQuery(query);
    } 
    Filter filter = query.getFilter();
    List<PropertyPredicate> transformed = evaluateInvertibleRelationshipPredicates(filter);
    if (transformed.isEmpty())
      return ResultSetUtil.emptyResult(query); 
    Filter transformedFilter = KeyPredicateMerger.mergeKeyPredicates(filter(transformed, filter.getOperator()));
    if (KeyPredicateMerger.FILTER_MATCH_NONE == transformedFilter)
      return ResultSetUtil.emptyResult(query); 
    Query transformedQuery = QueryCopy.copy(query).where(transformedFilter).build();
    return this._dataProvider.executeQuery(transformedQuery);
  }
  
  public QuerySchema getSchema() {
    return this._relationshipInversions.addInvertibleRelationships(this._dataProvider.getSchema());
  }
  
  public String toString() {
    return this._dataProvider.toString();
  }
  
  private static Filter filter(List<PropertyPredicate> predicates, LogicalOperator logicalOperator) {
    assert predicates != null;
    assert logicalOperator != null;
    if (predicates.size() == 1)
      return new Filter(predicates); 
    return new Filter(predicates, logicalOperator);
  }
  
  private boolean skipQuery(Query query) {
    assert query != null;
    Filter filter = query.getFilter();
    if (filter == null)
      return true; 
    for (PropertyPredicate predicate : filter.getCriteria()) {
      if (!this._relationshipInversions.invert(predicate.getProperty()).isEmpty())
        return false; 
    } 
    return true;
  }
  
  private List<PropertyPredicate> evaluateInvertibleRelationshipPredicates(Filter filter) {
    assert filter != null;
    List<PropertyPredicate> transformed = new ArrayList<>();
    for (PropertyPredicate predicate : filter.getCriteria()) {
      PropertyPredicate t = evaluateInvertibleRelationshipPredicate(predicate);
      if (t != null) {
        transformed.add(t);
        continue;
      } 
      if (LogicalOperator.AND.equals(filter.getOperator())) {
        _logger.trace("Return empty result because predicate matched no objects: {}", predicate);
        return Collections.emptyList();
      } 
    } 
    return transformed;
  }
  
  private PropertyPredicate evaluateInvertibleRelationshipPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    String property = predicate.getProperty();
    Collection<String> inverses = this._relationshipInversions.invert(property);
    if (inverses.isEmpty())
      return predicate; 
    _logger.trace("Relationship '{}' will be inverted to: {}", property, inverses);
    validateRelationshipPredicate(predicate);
    Collection<?> foreignKeyValues = getComparableValues(predicate);
    Collection<?> keys = fetchFlat(inverses, foreignKeyValues);
    if (keys.isEmpty()) {
      _logger.trace("Predicate on relationship '{}' matched no objects", property);
      return null;
    } 
    _logger.trace("Replace predicate on relationship '{}' by predicate on keys: {}", property, keys);
    return createEqualityPredicate(keys);
  }
  
  private Collection<?> fetchFlat(Collection<String> properties, Collection<?> keys) {
    assert properties != null;
    assert keys != null;
    List<Object> allValues = new ArrayList();
    for (String property : properties) {
      String model = QualifiedProperty.forQualifiedName(property).getResourceModel();
      Collection<?> values = fetchPropertyValues(model, property, keys);
      allValues.addAll(values);
    } 
    return distinct(allValues);
  }
  
  private Collection<?> fetchPropertyValues(String model, String property, Collection<?> keys) {
    assert model != null;
    assert property != null;
    assert keys != null;
    if (keys.isEmpty())
      return keys; 
    Query query = Query.Builder.select(new String[] { property }).from(new String[] { model }).where(new PropertyPredicate[] { createEqualityPredicate(keys) }).build();
    ResultSet result = this._dataProvider.executeQuery(query);
    Collection<?> flat = getPropertyValues(result, property);
    return distinct(flat);
  }
  
  private static Collection<?> distinct(Collection<?> collection) {
    assert collection != null;
    return new ArrayList(new LinkedHashSet(collection));
  }
  
  private static void validateRelationshipPredicate(PropertyPredicate predicate) {
    assert predicate != null;
    PropertyPredicate.ComparisonOperator op = predicate.getOperator();
    if (!PropertyPredicate.ComparisonOperator.EQUAL.equals(op) && !PropertyPredicate.ComparisonOperator.IN.equals(op))
      throw new IllegalArgumentException("Unexpected comparison operator in predicate on relationship: " + predicate); 
  }
  
  private static Collection<?> getPropertyValues(ResultSet result, String property) {
    assert result != null;
    assert property != null;
    if (result.getItems().isEmpty())
      return Collections.emptyList(); 
    List<Object> values = new ArrayList(result.getItems().size());
    for (ResourceItem item : result.getItems()) {
      Object value = item.get(property);
      if (value == null)
        continue; 
      if (value.getClass().isArray()) {
        for (int i = 0; i < Array.getLength(value); i++) {
          Object element = Array.get(value, i);
          values.add(element);
        } 
        continue;
      } 
      if (value instanceof Collection) {
        Collection<?> elements = (Collection)value;
        values.addAll(elements);
        continue;
      } 
      values.add(value);
    } 
    return values;
  }
  
  private static Collection<?> getComparableValues(PropertyPredicate predicate) {
    assert predicate != null;
    PropertyPredicate.ComparisonOperator op = predicate.getOperator();
    if (PropertyPredicate.ComparisonOperator.EQUAL.equals(op))
      return Collections.singletonList(predicate.getComparableValue()); 
    if (PropertyPredicate.ComparisonOperator.IN.equals(op))
      return (Collection)predicate.getComparableValue(); 
    return Collections.emptyList();
  }
  
  private static PropertyPredicate createEqualityPredicate(@Nonnull Collection<?> values) {
    PropertyPredicate.ComparisonOperator op;
    Object<?> comparableValue;
    assert !values.isEmpty();
    if (values.size() == 1) {
      op = PropertyPredicate.ComparisonOperator.EQUAL;
      comparableValue = (Object<?>)values.iterator().next();
    } else {
      op = PropertyPredicate.ComparisonOperator.IN;
      comparableValue = (Object<?>)values;
    } 
    return new PropertyPredicate("@modelKey", op, comparableValue);
  }
}
