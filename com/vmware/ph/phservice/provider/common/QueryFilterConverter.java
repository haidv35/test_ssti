package com.vmware.ph.phservice.provider.common;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.ph.phservice.common.internal.TimeIntervalUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryFilterConverter {
  private static final int SINGLE_ELEMENT_ARRAY_SIZE = 1;
  
  private static final Log _log = LogFactory.getLog(QueryFilterConverter.class);
  
  public static <T> T convertQueryFilter(Filter queryFilter, @Nonnull Class<T> objectTypeClass) {
    if (queryFilter == null)
      return null; 
    T objectOfType = null;
    try {
      objectOfType = objectTypeClass.newInstance();
    } catch (InstantiationException|IllegalAccessException e) {
      _log.error(
          String.format("Failed to create vmodl filter of class '%s'.", new Object[] { objectTypeClass }));
    } 
    if (objectOfType == null)
      return null; 
    for (PropertyPredicate queryFilterPropertyPredicate : queryFilter.getCriteria()) {
      String propertyPath = QuerySchemaUtil.getActualPropertyName(queryFilterPropertyPredicate
          .getProperty());
      Object queryFilterComprableValue = getEqualsPropertyPredicateComparableValue(queryFilterPropertyPredicate);
      Class<?> lastPropertyInPathClass = DataProviderUtil.getTypeOfLastPropertyInPropertyPath(objectTypeClass, propertyPath);
      if (lastPropertyInPathClass == null) {
        if (_log.isDebugEnabled())
          _log.debug("Filter property does not exist in class: " + queryFilterComprableValue); 
        continue;
      } 
      Object lastPropertyInPathValue = parsePropertyPredicateComparableValue(queryFilterComprableValue, lastPropertyInPathClass);
      DataProviderUtil.setPropertyValue(objectOfType, propertyPath, lastPropertyInPathValue);
    } 
    return objectOfType;
  }
  
  private static Object getEqualsPropertyPredicateComparableValue(PropertyPredicate propertyPredicate) {
    PropertyPredicate.ComparisonOperator comparisonOperator = propertyPredicate.getOperator();
    boolean isComparisonOperatorSupported = (comparisonOperator == PropertyPredicate.ComparisonOperator.EQUAL);
    if (isComparisonOperatorSupported)
      return propertyPredicate.getComparableValue(); 
    return null;
  }
  
  private static <T> Object parsePropertyPredicateComparableValue(Object queryFilterComprableValue, Class<T> lastPropertyInPathClass) {
    Object parsedComparableValue = null;
    if (lastPropertyInPathClass.isArray()) {
      parsedComparableValue = parseArrayValue(queryFilterComprableValue, lastPropertyInPathClass
          
          .getComponentType());
    } else {
      parsedComparableValue = parseStringValue((String)queryFilterComprableValue, lastPropertyInPathClass);
    } 
    return parsedComparableValue;
  }
  
  private static Object[] parseArrayValue(Object targetOject, Class<?> valueType) {
    Object[] parsedValues = null;
    if (targetOject instanceof List) {
      List<Object> values = (List<Object>)targetOject;
      parsedValues = (Object[])Array.newInstance(valueType, values.size());
      for (int i = 0; i < values.size(); i++)
        parsedValues[i] = parseStringValue((String)values.get(i), valueType); 
    } else {
      parsedValues = (Object[])Array.newInstance(valueType, 1);
      parsedValues[0] = parseStringValue((String)targetOject, valueType);
    } 
    return parsedValues;
  }
  
  private static Object parseStringValue(String value, Class<?> type) {
    Object parsedValue = null;
    if (String.class.equals(type)) {
      parsedValue = value;
    } else if (Calendar.class.equals(type)) {
      parsedValue = parseStringValueToCalendar(value);
    } else {
      try {
        Method valueOfMethod = type.getDeclaredMethod("valueOf", new Class[] { String.class });
        parsedValue = valueOfMethod.invoke(null, new Object[] { value });
      } catch (Exception e) {
        if (_log.isWarnEnabled())
          _log.warn(
              String.format("Failed to parse value '%s' for class '%s'.", new Object[] { value, type.getCanonicalName() })); 
        if (_log.isDebugEnabled())
          _log.debug("Enum constant parsing failure.", e); 
      } 
    } 
    return parsedValue;
  }
  
  private static Calendar parseStringValueToCalendar(String value) {
    Calendar calendar = TimeIntervalUtil.convertOffsetTimeInMsToCalendar(value);
    return calendar;
  }
}
