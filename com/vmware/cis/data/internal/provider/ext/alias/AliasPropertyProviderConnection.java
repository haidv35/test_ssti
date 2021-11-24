package com.vmware.cis.data.internal.provider.ext.alias;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AliasPropertyProviderConnection implements DataProvider {
  private static Logger _logger = LoggerFactory.getLogger(AliasPropertyProviderConnection.class);
  
  private final DataProvider _connection;
  
  private final AliasPropertyLookup _aliasPropertyLookup;
  
  public AliasPropertyProviderConnection(DataProvider connection, AliasPropertyLookup aliasPropertyLookup) {
    assert connection != null;
    assert aliasPropertyLookup != null;
    this._connection = connection;
    this._aliasPropertyLookup = aliasPropertyLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    Map<String, String> realByAliasMapping = new HashMap<>();
    Query convertedQuery = convertQuery(query, this._aliasPropertyLookup, realByAliasMapping);
    ResultSet result = this._connection.executeQuery(convertedQuery);
    Map<String, List<String>> aliasByRealMapping = Collections.unmodifiableMap(
        reverseMapping(realByAliasMapping));
    return convertResult(result, query.getProperties(), aliasByRealMapping);
  }
  
  public QuerySchema getSchema() {
    QuerySchema baseSchema = this._connection.getSchema();
    if (baseSchema.getModels().isEmpty())
      return baseSchema; 
    QuerySchema aliasSchema = this._aliasPropertyLookup.calculateAliasPropertySchema(baseSchema);
    return SchemaUtil.merge(baseSchema, aliasSchema);
  }
  
  public String toString() {
    return this._connection.toString();
  }
  
  private static Query convertQuery(Query originalQuery, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert originalQuery != null;
    assert aliasPropertyLookup != null;
    assert realByAliasMapping != null;
    List<String> select = convertSelect(originalQuery.getProperties(), aliasPropertyLookup, realByAliasMapping);
    Filter filter = convertFilter(originalQuery.getFilter(), aliasPropertyLookup, realByAliasMapping);
    List<SortCriterion> sort = convertSort(originalQuery.getSortCriteria(), aliasPropertyLookup, realByAliasMapping);
    if (realByAliasMapping.isEmpty())
      return originalQuery; 
    Collection<String> resourceModels = QueryQualifier.getFromClause(select, filter, sort);
    Query convertedQuery = QueryCopy.copyAndSelect(originalQuery, select).from(resourceModels).where(filter).orderBy(sort).build();
    if (_logger.isTraceEnabled()) {
      _logger.trace("[QueryModel] Processing a query containing aliases: {}", realByAliasMapping
          .keySet());
      _logger.trace("[QueryModel] The query to be executed is: {}", convertedQuery);
    } 
    return convertedQuery;
  }
  
  private static List<String> convertSelect(List<String> properties, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert properties != null;
    Set<String> converted = new LinkedHashSet<>(properties.size());
    for (String property : properties) {
      String convertedProperty = convertProperty(property, aliasPropertyLookup, realByAliasMapping);
      converted.add(convertedProperty);
    } 
    return new ArrayList<>(converted);
  }
  
  private static Filter convertFilter(Filter filter, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    if (filter == null)
      return null; 
    assert filter.getCriteria() != null;
    assert !filter.getCriteria().isEmpty();
    List<PropertyPredicate> predicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria())
      predicates.add(convertPredicate(predicate, aliasPropertyLookup, realByAliasMapping)); 
    return new Filter(predicates, filter.getOperator());
  }
  
  private static PropertyPredicate convertPredicate(PropertyPredicate predicate, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert predicate != null;
    return new PropertyPredicate(
        convertProperty(predicate.getProperty(), aliasPropertyLookup, realByAliasMapping), predicate
        
        .getOperator(), predicate
        .getComparableValue(), predicate
        .isIgnoreCase());
  }
  
  private static List<SortCriterion> convertSort(List<SortCriterion> order, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert order != null;
    if (order.isEmpty())
      return order; 
    List<SortCriterion> converted = new ArrayList<>(order.size());
    for (SortCriterion criterion : order)
      converted.add(convertSortCriterion(criterion, aliasPropertyLookup, realByAliasMapping)); 
    return converted;
  }
  
  private static SortCriterion convertSortCriterion(SortCriterion criterion, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert criterion != null;
    return new SortCriterion(
        convertProperty(criterion.getProperty(), aliasPropertyLookup, realByAliasMapping), criterion
        
        .getSortDirection(), criterion
        .isIgnoreCase());
  }
  
  private static String convertProperty(String property, AliasPropertyLookup aliasPropertyLookup, Map<String, String> realByAliasMapping) {
    assert property != null;
    if (PropertyUtil.isSpecialProperty(property))
      return property; 
    String mappedProperty = realByAliasMapping.get(property);
    if (StringUtils.isEmpty(mappedProperty)) {
      String suffix = AliasLengthProperty.resolveSuffix(property);
      String propertyToCheck = AliasLengthProperty.cleanSuffix(property, suffix);
      AliasPropertyDescriptor propertyDescriptor = aliasPropertyLookup.getAliasPropertyDescriptor(propertyToCheck);
      if (propertyDescriptor != null) {
        mappedProperty = AliasLengthProperty.appendSuffix(propertyDescriptor
            .getTargetName(), suffix);
        realByAliasMapping.put(AliasLengthProperty.appendSuffix(propertyDescriptor
              .getName(), suffix), mappedProperty);
      } 
    } 
    if (StringUtils.isEmpty(mappedProperty))
      return property; 
    return mappedProperty;
  }
  
  private static ResultSet convertResult(ResultSet resultSet, List<String> queryProperties, Map<String, List<String>> aliasByRealMapping) {
    assert resultSet != null;
    if (aliasByRealMapping.isEmpty())
      return resultSet; 
    List<Integer> propertyValueIndicesToDuplicate = new ArrayList<>();
    List<String> convertedResultProperties = convertResultProperties(resultSet
        .getProperties(), queryProperties, aliasByRealMapping, propertyValueIndicesToDuplicate);
    if (propertyValueIndicesToDuplicate.isEmpty()) {
      ResultSet.Builder builder = ResultSet.Builder.properties(convertedResultProperties);
      for (ResourceItem item : resultSet.getItems())
        builder.item(item.getKey(), item.getPropertyValues()); 
      return builder.totalCount(resultSet.getTotalCount()).build();
    } 
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(convertedResultProperties);
    convertResourceItems(resultBuilder, resultSet.getItems(), propertyValueIndicesToDuplicate);
    return resultBuilder.totalCount(resultSet.getTotalCount()).build();
  }
  
  private static List<String> convertResultProperties(List<String> resultProperties, List<String> queryProperties, Map<String, List<String>> aliasByRealMapping, List<Integer> propertyValueIndicesToDuplicate) {
    assert resultProperties != null;
    Set<String> queryPropertiesSet = new HashSet<>(queryProperties);
    List<String> convertedProperties = new ArrayList<>(resultProperties.size());
    List<String> duplicateAliasProperties = new ArrayList<>();
    int propertyIndex = 0;
    for (String property : resultProperties) {
      List<String> mappedProperties = aliasByRealMapping.get(property);
      if (CollectionUtils.isEmpty(mappedProperties)) {
        convertedProperties.add(property);
      } else if (queryPropertiesSet.contains(property)) {
        convertedProperties.add(property);
        for (String mappedProperty : mappedProperties) {
          if (!queryPropertiesSet.contains(mappedProperty) || 
            mappedProperty.equals(property))
            continue; 
          duplicateAliasProperties.add(mappedProperty);
          propertyValueIndicesToDuplicate.add(Integer.valueOf(propertyIndex));
        } 
      } else {
        convertedProperties.add(mappedProperties.get(0));
        for (int i = 1; i < mappedProperties.size(); i++) {
          String mappedProperty = mappedProperties.get(i);
          if (queryPropertiesSet.contains(mappedProperty)) {
            duplicateAliasProperties.add(mappedProperty);
            propertyValueIndicesToDuplicate.add(Integer.valueOf(propertyIndex));
          } 
        } 
      } 
      propertyIndex++;
    } 
    convertedProperties.addAll(duplicateAliasProperties);
    return convertedProperties;
  }
  
  private static void convertResourceItems(ResultSet.Builder resultBuilder, List<ResourceItem> resourceItems, List<Integer> propertyValueIndicesToDuplicate) {
    for (ResourceItem resourceItem : resourceItems) {
      List<Object> propertyValues = new ArrayList(resourceItem.getPropertyValues());
      for (Integer duplicateIndex : propertyValueIndicesToDuplicate) {
        if (duplicateIndex.intValue() < propertyValues.size()) {
          Object propValue = propertyValues.get(duplicateIndex.intValue());
          propertyValues.add(propValue);
        } 
      } 
      resultBuilder.item(resourceItem.getKey(), propertyValues);
    } 
  }
  
  private Map<String, List<String>> reverseMapping(Map<String, String> mapping) {
    assert mapping != null;
    Map<String, List<String>> reversedMapping = new HashMap<>();
    for (Map.Entry<String, String> entry : mapping.entrySet()) {
      List<String> reverseValues = reversedMapping.get(entry.getValue());
      if (reverseValues == null) {
        reverseValues = new ArrayList<>();
        reversedMapping.put(entry.getValue(), reverseValues);
      } 
      reverseValues.add(entry.getKey());
    } 
    return reversedMapping;
  }
}
