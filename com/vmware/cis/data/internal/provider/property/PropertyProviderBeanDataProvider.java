package com.vmware.cis.data.internal.provider.property;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PropertyProviderBeanDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(PropertyProviderBeanDataProvider.class);
  
  private final Object _propertyProvider;
  
  private final Map<String, PropertyProviderMethod> _providerMethodByProperty;
  
  public static PropertyProviderBeanDataProvider toDataProvider(Object propertyProvider) {
    Validate.notNull(propertyProvider);
    Collection<PropertyProviderMethod> propertyMethods = PropertyProviderMethod.forPropertyProvider(propertyProvider);
    if (propertyMethods.isEmpty()) {
      _logger.warn("Ignore property provider without property provider methods: {}", propertyProvider
          
          .getClass().getCanonicalName());
      return null;
    } 
    Map<String, PropertyProviderMethod> providerMethodByProperty = new HashMap<>(propertyMethods.size());
    for (PropertyProviderMethod propertyMethod : propertyMethods)
      providerMethodByProperty.put(propertyMethod.getPropertyName(), propertyMethod); 
    _logger.info("Loaded property provider '{}' for properties: {}", propertyProvider
        .getClass().getCanonicalName(), providerMethodByProperty
        .keySet());
    return new PropertyProviderBeanDataProvider(propertyProvider, providerMethodByProperty);
  }
  
  private PropertyProviderBeanDataProvider(Object propertyProvider, Map<String, PropertyProviderMethod> providerMethodByProperty) {
    assert propertyProvider != null;
    assert providerMethodByProperty != null;
    this._propertyProvider = propertyProvider;
    this._providerMethodByProperty = providerMethodByProperty;
  }
  
  public Collection<String> getPropertyNames() {
    return Collections.unmodifiableSet(this._providerMethodByProperty.keySet());
  }
  
  public ResultSet executeQuery(Query query) {
    _logger.trace("Query for property provider '{}': {}", this._propertyProvider
        .getClass().getSimpleName(), query);
    Validate.notNull(query, "Query must not be null");
    Validate.isTrue(!query.getWithTotalCount(), "Total count is not supported");
    Validate.isTrue(!query.getProperties().isEmpty(), "Query must request properties");
    Collection<?> keys = getKeys(query.getFilter());
    List<String> select = query.getProperties();
    List<Collection<?>> columns = new ArrayList<>(select.size());
    for (String property : select) {
      Collection<?> column = computeColumn(property, keys);
      columns.add(column);
    } 
    ResultSet result = ResultSetUtil.toResult(keys, select, columns);
    _logger.trace("Result of query for property provider '{}': {}", this._propertyProvider
        .getClass().getSimpleName(), result);
    return result;
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.PropertyInfo> infoByProperty = new HashMap<>(this._providerMethodByProperty.size());
    for (String propertyName : this._providerMethodByProperty.keySet())
      infoByProperty.put(propertyName, QuerySchema.PropertyInfo.forNonFilterableProperty()); 
    return QuerySchema.forProperties(infoByProperty);
  }
  
  public String toString() {
    return "PropertyProvider[" + this._propertyProvider
      .getClass().getCanonicalName() + "]";
  }
  
  static Collection<?> getKeys(Filter filter) {
    Validate.notNull(filter, "Filter must not be null");
    assert !filter.getCriteria().isEmpty();
    Validate.isTrue((filter.getCriteria().size() == 1), "Filter must contain exactly one predicate");
    PropertyPredicate predicate = filter.getCriteria().get(0);
    Validate.isTrue("@modelKey".equals(predicate.getProperty()), "Cannot filter by property: " + predicate
        .getProperty());
    Validate.isTrue((PropertyPredicate.ComparisonOperator.EQUAL
        .equals(predicate.getOperator()) || PropertyPredicate.ComparisonOperator.IN
        .equals(predicate.getOperator())), "Invalid comparison operator: " + predicate
        .getOperator());
    Object comparableValue = predicate.getComparableValue();
    if (comparableValue instanceof Collection)
      return Collections.unmodifiableCollection((Collection)comparableValue); 
    return Collections.singletonList(comparableValue);
  }
  
  private Collection<?> computeColumn(String property, Collection<?> keys) {
    assert property != null;
    assert keys != null;
    assert !keys.isEmpty();
    if ("@modelKey".equals(property))
      return keys; 
    Validate.isTrue(!PropertyUtil.isSpecialProperty(property), "Unsupported special property: " + property);
    PropertyProviderMethod propertyMethod = this._providerMethodByProperty.get(property);
    Validate.notNull(propertyMethod, "Could not find implementation for property: " + property);
    return propertyMethod.getPropertyValuesForKeys(keys);
  }
}
