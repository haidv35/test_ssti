package com.vmware.ph.phservice.provider.vcenter.event;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.vim.binding.vim.event.EventFilterSpec;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class QueryFilterToEventFilterSpecConverter {
  private static final int EVENT_HISTORY_COLLECTOR_MAX_PAGE_SIZE = 1000;
  
  private static final String EVENT_TYPE_ID_PROPERTY = "eventTypeId";
  
  private static final String SEVERITY_PROPERTY = "severity";
  
  private static final String VIM_EVENT_TYPE_ID_PREFIX = "vim.event.";
  
  static final int DEFAULT_DAYS_EVENT_OFFSET = -7;
  
  public EventFilterSpec createEventFilterSpec(String eventsTypeName, Calendar currentTimeUtc, Filter queryFilter) {
    EventFilterSpec eventFilterSpec = new EventFilterSpec();
    eventFilterSpec.setMaxCount(Integer.valueOf(1000));
    eventFilterSpec.setDisableFullMessage(Boolean.valueOf(true));
    updateEventFilterSpecEventTypeId(eventFilterSpec, eventsTypeName);
    updateEventFilterSpecWithDefaultTime(eventFilterSpec, -7, currentTimeUtc);
    if (queryFilter != null)
      updateEventFilterSpecFromQueryFilterProperties(eventFilterSpec, queryFilter, currentTimeUtc); 
    return eventFilterSpec;
  }
  
  private static void updateEventFilterSpecEventTypeId(EventFilterSpec eventFilterSpec, String eventsTypeName) {
    String eventTypeId = "vim.event." + eventsTypeName;
    eventFilterSpec.setEventTypeId(new String[] { eventTypeId });
  }
  
  private static void updateEventFilterSpecWithDefaultTime(EventFilterSpec eventFilterSpec, int daysEventOffset, Calendar currentTimeUtc) {
    Calendar beginTime = (Calendar)currentTimeUtc.clone();
    beginTime.add(5, daysEventOffset);
    Calendar endTime = (Calendar)currentTimeUtc.clone();
    eventFilterSpec.setTime(new EventFilterSpec.ByTime(beginTime, endTime));
  }
  
  private static void updateEventFilterSpecFromQueryFilterProperties(EventFilterSpec eventFilterSpec, Filter queryFilter, Calendar currentTimeUtc) {
    Set<String> evaluatedPredicates = new HashSet<>();
    for (PropertyPredicate eventQueryPredicate : queryFilter.getCriteria()) {
      String nonQualifiedPropertyName = QuerySchemaUtil.getActualPropertyName(eventQueryPredicate.getProperty());
      switch (nonQualifiedPropertyName) {
        case "eventTypeId":
          if (!evaluatedPredicates.contains(nonQualifiedPropertyName)) {
            updateEventFilterSpecEventTypeIdFromPredicate(eventFilterSpec, eventQueryPredicate);
            evaluatedPredicates.add(nonQualifiedPropertyName);
          } 
          break;
        case "severity":
          if (!evaluatedPredicates.contains(nonQualifiedPropertyName)) {
            updateEventFilterSpecCategoryFromPredicate(eventFilterSpec, eventQueryPredicate);
            evaluatedPredicates.add(nonQualifiedPropertyName);
          } 
          break;
        case "filter/beginTime":
          if (!evaluatedPredicates.contains(nonQualifiedPropertyName)) {
            updateEventFilterSpecBeginTimeFromPredicate(eventFilterSpec, eventQueryPredicate, currentTimeUtc);
            evaluatedPredicates.add(nonQualifiedPropertyName);
          } 
          break;
        case "filter/endTime":
          if (!evaluatedPredicates.contains(nonQualifiedPropertyName)) {
            updateEventFilterSpecEndTimeFromPredicate(eventFilterSpec, eventQueryPredicate, currentTimeUtc);
            evaluatedPredicates.add(nonQualifiedPropertyName);
          } 
          break;
        default:
          throw new IllegalArgumentException(
              String.format("Filtering events by %s property is not supported.", new Object[] { nonQualifiedPropertyName }));
      } 
      if (queryFilter.getOperator() == LogicalOperator.OR)
        break; 
    } 
  }
  
  private static void updateEventFilterSpecEventTypeIdFromPredicate(EventFilterSpec eventFilterSpec, PropertyPredicate eventTypeIdPredicate) {
    String eventTypeId = (String)evaluatePropertyPredicateValue(eventTypeIdPredicate);
    if (eventTypeId != null)
      eventFilterSpec.setEventTypeId(new String[] { eventTypeId }); 
  }
  
  private static void updateEventFilterSpecCategoryFromPredicate(EventFilterSpec eventFilterSpec, PropertyPredicate severityPredicate) {
    String severity = (String)evaluatePropertyPredicateValue(severityPredicate);
    if (severity != null)
      eventFilterSpec.setCategory(new String[] { severity }); 
  }
  
  private static void updateEventFilterSpecBeginTimeFromPredicate(EventFilterSpec eventFilterSpec, PropertyPredicate beginTimePredicate, Calendar currentTimeUtc) {
    Calendar beginTime = calculateTimeFromPropertyPredicate(beginTimePredicate, currentTimeUtc);
    if (beginTime != null)
      eventFilterSpec.getTime().setBeginTime(beginTime); 
  }
  
  private static void updateEventFilterSpecEndTimeFromPredicate(EventFilterSpec eventFilterSpec, PropertyPredicate endTimePredicate, Calendar currentTimeUtc) {
    Calendar endTime = calculateTimeFromPropertyPredicate(endTimePredicate, currentTimeUtc);
    if (endTime != null)
      eventFilterSpec.getTime().setEndTime(endTime); 
  }
  
  private static Object evaluatePropertyPredicateValue(PropertyPredicate propertyPredicate) {
    PropertyPredicate.ComparisonOperator comparisonOperator = propertyPredicate.getOperator();
    if (comparisonOperator != PropertyPredicate.ComparisonOperator.EQUAL)
      return null; 
    return propertyPredicate.getComparableValue();
  }
  
  private static Calendar calculateTimeFromPropertyPredicate(PropertyPredicate predicate, Calendar currentTimeUtc) {
    Calendar calculatedTime = null;
    Object predicateValue = evaluatePropertyPredicateValue(predicate);
    if (predicateValue != null) {
      calculatedTime = (Calendar)currentTimeUtc.clone();
      calculatedTime.add(14, 
          
          Integer.parseInt((String)predicateValue));
    } 
    return calculatedTime;
  }
}
