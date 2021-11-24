package com.vmware.cis.data.internal.provider.ext.derived;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DerivedPropertyProviderConnection implements DataProvider {
  private static Logger _logger = LoggerFactory.getLogger(DerivedPropertyProviderConnection.class);
  
  private final DataProvider _connection;
  
  private final DerivedPropertyLookup _derivedPropertyLookup;
  
  public DerivedPropertyProviderConnection(DataProvider connection, DerivedPropertyLookup derivedPropertyLookup) {
    assert connection != null;
    assert derivedPropertyLookup != null;
    this._connection = connection;
    this._derivedPropertyLookup = derivedPropertyLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    validateNoDerivedProperties(query.getFilter());
    validateNoDerivedProperties(query.getSortCriteria());
    Map<String, DerivedPropertyDescriptor> derivedPropertyByName = getDerivedProperties(query, this._derivedPropertyLookup);
    if (derivedPropertyByName.isEmpty())
      return this._connection.executeQuery(query); 
    Query executableQuery = toExecutableQuery(query, derivedPropertyByName);
    if (_logger.isTraceEnabled())
      _logger.trace("Sending query with replaced derived properties {}", query); 
    ResultSet rawResult = this._connection.executeQuery(executableQuery);
    if (_logger.isTraceEnabled())
      _logger.trace("Received response for replaced derived properties {}", rawResult); 
    return processResult(rawResult, query.getProperties(), derivedPropertyByName);
  }
  
  public QuerySchema getSchema() {
    QuerySchema schema = this._connection.getSchema();
    if (schema.getModels().isEmpty())
      return schema; 
    return this._derivedPropertyLookup.addDerivedProps(schema);
  }
  
  public String toString() {
    return this._connection.toString();
  }
  
  private void validateNoDerivedProperties(Filter filter) {
    if (filter == null)
      return; 
    for (PropertyPredicate predicate : filter.getCriteria())
      validateNoDerivedProperties(predicate); 
  }
  
  private void validateNoDerivedProperties(PropertyPredicate predicate) {
    assert predicate != null;
    String property = predicate.getProperty();
    DerivedPropertyDescriptor descriptor = getDescriptor(property);
    if (descriptor != null)
      throw new IllegalArgumentException(
          String.format("Derived properties cannot be used for filtering: %s", new Object[] { predicate })); 
  }
  
  private void validateNoDerivedProperties(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    for (SortCriterion sortCriterion : sortCriteria)
      validateNoDerivedProperties(sortCriterion); 
  }
  
  private void validateNoDerivedProperties(SortCriterion sortCriterion) {
    assert sortCriterion != null;
    String property = sortCriterion.getProperty();
    DerivedPropertyDescriptor descriptor = getDescriptor(property);
    if (descriptor != null)
      throw new IllegalArgumentException(String.format("Derived properties cannot be used for sorting: %s", new Object[] { sortCriterion })); 
  }
  
  private DerivedPropertyDescriptor getDescriptor(String property) {
    return this._derivedPropertyLookup.getDerivedPropertyDescriptor(property);
  }
  
  private static Map<String, DerivedPropertyDescriptor> getDerivedProperties(Query originalQuery, DerivedPropertyLookup derivedPropertyLookup) {
    assert originalQuery != null;
    assert derivedPropertyLookup != null;
    Map<String, DerivedPropertyDescriptor> derivedPropertyByName = new HashMap<>();
    for (String property : originalQuery.getProperties()) {
      DerivedPropertyDescriptor descriptor = derivedPropertyLookup.getDerivedPropertyDescriptor(property);
      if (descriptor != null)
        derivedPropertyByName.put(property, descriptor); 
    } 
    return derivedPropertyByName;
  }
  
  private static Query toExecutableQuery(Query originalQuery, Map<String, DerivedPropertyDescriptor> derivedPropertyByName) {
    assert originalQuery != null;
    assert derivedPropertyByName != null;
    assert !derivedPropertyByName.isEmpty();
    List<String> properties = toExecutableProperties(originalQuery
        .getProperties(), derivedPropertyByName);
    Collection<String> resourceModels = QueryQualifier.getFromClause(properties, originalQuery
        .getFilter(), originalQuery.getSortCriteria());
    return QueryCopy.copyAndSelect(originalQuery, properties)
      .from(resourceModels)
      .build();
  }
  
  private static List<String> toExecutableProperties(List<String> originalSelect, Map<String, DerivedPropertyDescriptor> derivedPropertyByName) {
    assert originalSelect != null;
    assert derivedPropertyByName != null;
    List<String> executableProperties = new ArrayList<>();
    for (String property : originalSelect) {
      DerivedPropertyDescriptor descriptor = derivedPropertyByName.get(property);
      if (descriptor != null) {
        executableProperties.addAll(descriptor.getSourceProperties());
        continue;
      } 
      executableProperties.add(property);
    } 
    return new ArrayList<>(new LinkedHashSet<>(executableProperties));
  }
  
  private static ResultSet processResult(ResultSet rawResult, List<String> originalSelect, Map<String, DerivedPropertyDescriptor> derivedPropertyByName) {
    assert rawResult != null;
    assert originalSelect != null;
    assert derivedPropertyByName != null;
    assert !derivedPropertyByName.isEmpty();
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(originalSelect);
    for (ResourceItem rawItem : rawResult.getItems()) {
      List<Object> propertyValues = processPropertyValues(rawItem, rawResult
          .getProperties(), originalSelect, derivedPropertyByName);
      resultBuilder.item(rawItem.getKey(), propertyValues);
    } 
    return resultBuilder.totalCount(rawResult.getTotalCount()).build();
  }
  
  private static List<Object> processPropertyValues(ResourceItem rawItem, List<String> rawItemProperties, List<String> originalSelect, Map<String, DerivedPropertyDescriptor> derivedPropertyByName) {
    assert rawItem != null;
    assert rawItemProperties != null;
    assert originalSelect != null;
    assert derivedPropertyByName != null;
    List<Object> values = new ArrayList(originalSelect.size());
    for (String property : originalSelect) {
      Object value;
      DerivedPropertyDescriptor descriptor = derivedPropertyByName.get(property);
      if (descriptor != null) {
        value = descriptor.invokeDerivedPropertyMethod(rawItem);
      } else {
        value = rawItem.get(property);
      } 
      values.add(value);
    } 
    return values;
  }
}
