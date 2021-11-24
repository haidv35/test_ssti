package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PredicatePropertyProviderConnection implements DataProvider {
  private static Logger _logger = LoggerFactory.getLogger(PredicatePropertyProviderConnection.class);
  
  private final DataProvider _connection;
  
  private final PredicatePropertyLookup _predicatePropertyLookup;
  
  public PredicatePropertyProviderConnection(DataProvider connection, PredicatePropertyLookup predicatePropertyLookup) {
    assert connection != null;
    assert predicatePropertyLookup != null;
    this._connection = connection;
    this._predicatePropertyLookup = predicatePropertyLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    validateNoOrderByPredicateProperty(query.getSortCriteria());
    Map<String, PredicatePropertyDescriptor> predicatePropsInFilter = getDescriptors(query.getFilter());
    Map<String, PredicatePropertyDescriptor> predicatePropsInSelect = getDescriptors(query.getProperties());
    if (predicatePropsInFilter.isEmpty() && predicatePropsInSelect.isEmpty())
      return this._connection.executeQuery(query); 
    logPredicateProps(query, predicatePropsInFilter, predicatePropsInSelect);
    List<String> executableSelect = PredicatePropertySelect.toExecutableSelect(query.getProperties(), predicatePropsInSelect);
    Filter executableFilter = PredicatePropertyFilter.toExecutableFilter(query.getFilter(), predicatePropsInFilter);
    Collection<String> resourceModels = QueryQualifier.getFromClause(executableSelect, executableFilter, query
        .getSortCriteria());
    Query executableQuery = QueryCopy.copyAndSelect(query, executableSelect).from(resourceModels).where(executableFilter).build();
    if (_logger.isTraceEnabled())
      _logger.trace("Sending query with replaced predicate properties {}", executableQuery); 
    ResultSet result = this._connection.executeQuery(executableQuery);
    if (_logger.isTraceEnabled())
      _logger.trace("Received response for query with replaced predicate property {}", executableQuery); 
    if (predicatePropsInSelect.isEmpty())
      return result; 
    return PredicatePropertySelect.convertResult(result, query.getProperties(), predicatePropsInSelect);
  }
  
  public QuerySchema getSchema() {
    QuerySchema schema = this._connection.getSchema();
    if (schema.getModels().isEmpty())
      return schema; 
    return this._predicatePropertyLookup.addPredicateProps(schema);
  }
  
  public String toString() {
    return this._connection.toString();
  }
  
  private void validateNoOrderByPredicateProperty(List<SortCriterion> sortCriteria) {
    assert sortCriteria != null;
    for (SortCriterion sortCriterion : sortCriteria) {
      if (getDescriptor(sortCriterion.getProperty()) != null)
        throw new IllegalArgumentException(String.format("Predicate properties cannot be used for sorting: %s", new Object[] { sortCriterion })); 
    } 
  }
  
  private Map<String, PredicatePropertyDescriptor> getDescriptors(Filter filter) {
    if (filter == null)
      return Collections.emptyMap(); 
    Map<String, PredicatePropertyDescriptor> descriptors = new HashMap<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria())
      addDescriptor(predicate.getProperty(), descriptors); 
    return Collections.unmodifiableMap(descriptors);
  }
  
  private Map<String, PredicatePropertyDescriptor> getDescriptors(List<String> select) {
    assert select != null;
    Map<String, PredicatePropertyDescriptor> descriptors = new HashMap<>(select.size());
    for (String property : select)
      addDescriptor(property, descriptors); 
    return Collections.unmodifiableMap(descriptors);
  }
  
  private void addDescriptor(String property, Map<String, PredicatePropertyDescriptor> descriptors) {
    assert property != null;
    assert descriptors != null;
    if (PropertyUtil.isSpecialProperty(property))
      return; 
    if (descriptors.containsKey(property))
      return; 
    PredicatePropertyDescriptor descriptor = getDescriptor(property);
    if (descriptor == null)
      return; 
    descriptors.put(property, descriptor);
  }
  
  private PredicatePropertyDescriptor getDescriptor(String property) {
    return this._predicatePropertyLookup.getPredicatePropertyDescriptor(property);
  }
  
  private void logPredicateProps(Query query, Map<String, PredicatePropertyDescriptor> predicatePropsInFilter, Map<String, PredicatePropertyDescriptor> predicatePropsInSelect) {
    if (!predicatePropsInFilter.isEmpty())
      if (_logger.isTraceEnabled()) {
        _logger.trace("Query filters on predicate properties {}: {}", predicatePropsInFilter
            .keySet(), query);
      } else {
        _logger.debug("Query filters on predicate properties {}", predicatePropsInFilter
            .keySet());
      }  
    if (!predicatePropsInSelect.isEmpty())
      if (_logger.isTraceEnabled()) {
        _logger.trace("Query selects predicate properties {}: {}", predicatePropsInSelect
            .keySet(), query);
      } else {
        _logger.debug("Query selects predicate properties {}", predicatePropsInSelect
            .keySet());
      }  
  }
}
