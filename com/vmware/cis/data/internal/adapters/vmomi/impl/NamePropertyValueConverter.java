package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.google.common.collect.ImmutableSet;
import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.util.filter.OperatorLikeEvaluator;
import com.vmware.cis.data.internal.util.QueryCopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

final class NamePropertyValueConverter {
  private static final int HEX_SEQUENCE_LENGTH = 2;
  
  private static final int HEX_RADIX = 16;
  
  private static final char PERCENTAGE = '%';
  
  private static final char FORWARD_SLASH = '/';
  
  private static final char BACK_SLASH = '\\';
  
  private static final String ESCAPED_BACK_SLASH = "%5c";
  
  private static final String ESCAPED_FORWARD_SLASH = "%2f";
  
  private static final String ESCAPED_PERCENTAGE = "%25";
  
  private static final int ESCAPED_BACK_SLASH_HEX = Integer.parseInt("5c", 16);
  
  private static final int ESCAPED_FORWARD_SLASH_HEX = Integer.parseInt("2f", 16);
  
  private static final int ESCAPED_PERCENTAGE_HEX = Integer.parseInt("25", 16);
  
  private static final Set<String> PROPERTIES_TO_CONVERT = (Set<String>)ImmutableSet.of("Network/name", "OpaqueNetwork/name", "DistributedVirtualPortgroup/name", "ComputeResource/name", "ClusterComputeResource/name", "Datacenter/name", (Object[])new String[] { "Datastore/name", "Folder/name", "StoragePod/name", "HostSystem/name", "ResourcePool/name", "VirtualApp/name", "DistributedVirtualSwitch/name", "VmwareDistributedVirtualSwitch/name", "VirtualMachine/name" });
  
  static Query escapeNamesInFilter(Query query) {
    assert query != null;
    if (requiresConversion(query))
      return convertQuery(query); 
    return query;
  }
  
  static ResultSet unescapeNamesInResult(ResultSet resultSet) {
    assert resultSet != null;
    if (requiresConversion(resultSet))
      return convertResult(resultSet); 
    return resultSet;
  }
  
  private static boolean requiresConversion(Query query) {
    assert query != null;
    Filter filter = query.getFilter();
    if (filter == null)
      return false; 
    for (PropertyPredicate predicate : filter.getCriteria()) {
      String property = predicate.getProperty();
      if (PROPERTIES_TO_CONVERT.contains(property))
        return true; 
    } 
    return false;
  }
  
  private static boolean requiresConversion(ResultSet result) {
    assert result != null;
    List<String> properties = result.getProperties();
    for (String property : properties) {
      if (PROPERTIES_TO_CONVERT.contains(property))
        return true; 
    } 
    return false;
  }
  
  private static Query convertQuery(Query originalQuery) {
    assert originalQuery != null;
    assert requiresConversion(originalQuery);
    Filter filter = originalQuery.getFilter();
    List<PropertyPredicate> convertedPredicates = new ArrayList<>(filter.getCriteria().size());
    for (PropertyPredicate predicate : filter.getCriteria()) {
      String property = predicate.getProperty();
      if (PROPERTIES_TO_CONVERT.contains(property)) {
        convertedPredicates.add(convertPredicate(predicate));
        continue;
      } 
      convertedPredicates.add(predicate);
    } 
    return QueryCopy.copy(originalQuery)
      .where(filter.getOperator(), convertedPredicates).build();
  }
  
  private static PropertyPredicate convertPredicate(PropertyPredicate predicate) {
    Object escapedValue;
    assert predicate != null;
    assert predicate.getComparableValue() != null;
    String property = predicate.getProperty();
    PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
    Object value = predicate.getComparableValue();
    if (operator.equals(PropertyPredicate.ComparisonOperator.IN)) {
      escapedValue = escape(property, (Collection)value);
      return copy(predicate, escapedValue);
    } 
    if (!(value instanceof String)) {
      String msg = String.format("The comparable value for property '%s' must be 'String' not '%s'.", new Object[] { property, value
            
            .getClass().getSimpleName() });
      throw new IllegalArgumentException(msg);
    } 
    if (operator.equals(PropertyPredicate.ComparisonOperator.LIKE)) {
      OperatorLikeEvaluator.StringMatchingInfo matchInfo = OperatorLikeEvaluator.analyzeTemplate((String)value);
      String escapedSearchText = escape(matchInfo.getSearchText());
      escapedValue = OperatorLikeEvaluator.toSearchTemplate(escapedSearchText, matchInfo.getMode());
    } else {
      escapedValue = escape((String)value);
    } 
    return copy(predicate, escapedValue);
  }
  
  private static Collection<?> escape(String property, Collection<?> values) {
    assert property != null;
    assert values != null;
    List<Object> escaped = new ArrayList(values.size());
    for (Object value : values) {
      if (value instanceof String) {
        escaped.add(escape((String)value));
        continue;
      } 
      String msg = String.format("The comparable value for property '%s' must contain only 'String' values not '%s'.", new Object[] { property, value
            
            .getClass().getSimpleName() });
      throw new IllegalArgumentException(msg);
    } 
    return escaped;
  }
  
  private static String escape(String value) {
    assert value != null;
    StringBuilder escaped = new StringBuilder();
    for (int index = 0; index < value.length(); index++) {
      char ch = value.charAt(index);
      switch (ch) {
        case '%':
          escaped.append("%25");
          break;
        case '/':
          escaped.append("%2f");
          break;
        case '\\':
          escaped.append("%5c");
          break;
        default:
          escaped.append(ch);
          break;
      } 
    } 
    return escaped.toString();
  }
  
  private static ResultSet convertResult(ResultSet result) {
    assert result != null;
    assert requiresConversion(result);
    List<String> properties = result.getProperties();
    boolean[] propertyConvertIndex = getPropertyConvertIndex(properties);
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    for (ResourceItem item : result.getItems()) {
      List<Object> values = item.getPropertyValues();
      List<Object> convertedValues = new ArrayList(values.size());
      for (int i = 0; i < values.size(); i++) {
        Object convertedValue;
        boolean propertyToConvert = propertyConvertIndex[i];
        Object value = values.get(i);
        if (!propertyToConvert) {
          convertedValue = value;
        } else if (value == null) {
          convertedValue = value;
        } else if (!(value instanceof String)) {
          convertedValue = value;
        } else {
          convertedValue = unescape((String)value);
        } 
        convertedValues.add(convertedValue);
      } 
      resultBuilder.item(item.getKey(), convertedValues);
    } 
    return resultBuilder.totalCount(result.getTotalCount()).build();
  }
  
  private static boolean[] getPropertyConvertIndex(List<String> properties) {
    assert properties != null;
    boolean[] propertyConvertIndex = new boolean[properties.size()];
    for (int i = 0; i < properties.size(); i++) {
      String property = properties.get(i);
      propertyConvertIndex[i] = PROPERTIES_TO_CONVERT.contains(property);
    } 
    return propertyConvertIndex;
  }
  
  private static String unescape(String value) {
    assert value != null;
    int escapeIndex = value.indexOf('%');
    if (escapeIndex == -1 || escapeIndex + 2 >= value
      .length())
      return value; 
    StringBuilder unescaped = new StringBuilder();
    int index = 0;
    while (index < value.length()) {
      int escapeSequenceEnd = index + 2;
      char ch = value.charAt(index++);
      if (ch != '%' || escapeSequenceEnd >= value.length()) {
        unescaped.append(ch);
        continue;
      } 
      char ch1 = value.charAt(index);
      char ch2 = value.charAt(index + 1);
      Character unescapedCh = getUnescapedChar(ch1, ch2);
      if (unescapedCh == null) {
        unescaped.append(ch);
        continue;
      } 
      unescaped.append(unescapedCh);
      index += 2;
    } 
    return unescaped.toString();
  }
  
  private static Character getUnescapedChar(char ch1, char ch2) {
    int hex1 = Character.digit(ch1, 16);
    if (hex1 == -1)
      return null; 
    int hex2 = Character.digit(ch2, 16);
    if (hex2 == -1)
      return null; 
    int hex = hex1 * 16 + hex2;
    if (hex == ESCAPED_FORWARD_SLASH_HEX)
      return Character.valueOf('/'); 
    if (hex == ESCAPED_BACK_SLASH_HEX)
      return Character.valueOf('\\'); 
    if (hex == ESCAPED_PERCENTAGE_HEX)
      return Character.valueOf('%'); 
    return null;
  }
  
  private static PropertyPredicate copy(PropertyPredicate predicate, Object comparableValue) {
    return new PropertyPredicate(predicate.getProperty(), predicate.getOperator(), comparableValue, predicate
        .isIgnoreCase());
  }
}
