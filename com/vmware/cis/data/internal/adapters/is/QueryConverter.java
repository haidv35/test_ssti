package com.vmware.cis.data.internal.adapters.is;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

final class QueryConverter {
  public static String convertQuery(Query query) {
    validateQuery(query);
    Set<String> functionSet = XQueryDefinitions.getRequiredFunctions();
    Collection<String> resourceModels = query.getResourceModels();
    String resourceModel = resourceModels.iterator().next();
    String filterXQuery = getFilter(functionSet, resourceModel, query.getFilter());
    String sortXQuery = getSort(functionSet, resourceModel, query.getSortCriteria());
    String selectXQuery = getProperties(functionSet, resourceModel, query.getProperties(), query
        .getOffset(), query.getLimit());
    StringBuilder builder = new StringBuilder();
    builder.append("declare namespace vim25=\"urn:vim25\";\ndeclare namespace qs=\"urn:vmware:queryservice\";\ndeclare namespace query=\"query\";\ndeclare default element namespace \"urn:vim25\";\ndeclare namespace vapi=\"urn:vim25\";\ndeclare namespace xlink=\"http://www.w3.org/1999/xlink\";\ndeclare option xhive:fts-analyzer-class \"com.vmware.vim.query.server.store.impl.CaseInsensitiveWhitespaceAnalyzer\";\n");
    for (String function : functionSet) {
      builder.append(XQueryDefinitions.getFunctionExpression(function));
      builder.append('\n');
    } 
    builder.append('\n');
    builder.append(filterXQuery);
    builder.append(sortXQuery);
    builder.append(selectXQuery);
    builder.append(XQueryUtil.RETURN_CLAUSE);
    return builder.toString();
  }
  
  private static void validateQuery(Query query) {
    Collection<String> resourceModels = query.getResourceModels();
    if (resourceModels.size() != 1)
      throw new IllegalArgumentException("Only one resource model expected"); 
  }
  
  private static String getFilter(Set<String> functionSet, String model, Filter filter) {
    StringBuilder builder = new StringBuilder();
    if (filter == null || filter.getCriteria().isEmpty()) {
      builder.append("let ");
      builder.append("$targetSet");
      builder.append(" := ");
      builder.append("/");
      builder.append(model);
      builder.append('\n');
      return builder.toString();
    } 
    String setOperator = extractSetOperator(filter.getOperator());
    int targetSetCount = addFilters(functionSet, model, filter, builder);
    builder.append('\n');
    builder.append("let ");
    builder.append("$targetSet");
    builder.append(" := ");
    if (targetSetCount == 1) {
      builder.append("$targetSet");
      builder.append(0);
      builder.append('\n');
    } else {
      builder.append('(');
      builder.append("$targetSet");
      builder.append(0);
      for (int i = 1; i < targetSetCount; i++) {
        builder.append(setOperator);
        builder.append("$targetSet");
        builder.append(i);
      } 
      builder.append(')');
      builder.append('\n');
    } 
    return builder.toString();
  }
  
  private static String extractSetOperator(LogicalOperator operator) {
    return operator.equals(LogicalOperator.AND) ? " intersect " : " union ";
  }
  
  private static int addFilters(Set<String> functionSet, String model, Filter filter, StringBuilder builder) {
    int targetSetCount = 0;
    Map<String, List<PropertyPredicate>> predicatesByProperty = groupPredicatesByProperty(filter);
    List<String> simpleXQueryCriteria = new ArrayList<>();
    String xQueryMerged = null;
    for (Map.Entry<String, List<PropertyPredicate>> entry : predicatesByProperty
      .entrySet()) {
      XQueryDefinitions.PropertyDefinition propertyDefinition = XQueryDefinitions.getPropertyDefinition(model, entry.getKey());
      String template = propertyDefinition.getFilterTemplate();
      if (template == null) {
        String convertedProperty = propertyDefinition.getFilterExpression();
        if (convertedProperty == null) {
          convertedProperty = convertPropertyFilter(entry.getKey());
        } else {
          addAllFunctions(functionSet, propertyDefinition.getFilterFunctions());
        } 
        for (PropertyPredicate predicate : entry.getValue()) {
          String xQueryCriterion;
          if (predicate.getOperator().equals(PropertyPredicate.ComparisonOperator.IN)) {
            xQueryCriterion = getInCriterion(convertedProperty, predicate
                .getComparableValue());
          } else {
            xQueryCriterion = getCriterion(convertedProperty, predicate
                .getOperator(), predicate.getComparableValue());
          } 
          simpleXQueryCriteria.add(xQueryCriterion);
        } 
        continue;
      } 
      addAllFunctions(functionSet, propertyDefinition.getFilterFunctions());
      Map<String, List<String>> xQueriesByModel = new HashMap<>();
      for (PropertyPredicate predicate : entry.getValue())
        foreignKeyXQueriesByModel(predicate, xQueriesByModel); 
      String xQueryFromTemplate = combineCriteria(xQueriesByModel, filter
          .getOperator());
      String xQueryFilterFilter = String.format(template, new Object[] { xQueryFromTemplate });
      addTargetSet(builder, targetSetCount++, xQueryFilterFilter);
    } 
    if (!simpleXQueryCriteria.isEmpty()) {
      xQueryMerged = mergeCriteria(model, filter.getOperator(), simpleXQueryCriteria);
      addTargetSet(builder, targetSetCount++, xQueryMerged);
    } 
    return targetSetCount;
  }
  
  private static void foreignKeyXQueriesByModel(PropertyPredicate predicate, Map<String, List<String>> xQueriesByModel) {
    Map<String, List<String>> onePredicateXQueriesByModel = new HashMap<>();
    if (predicate.getOperator().equals(PropertyPredicate.ComparisonOperator.IN)) {
      List<ManagedObjectReference> foreignKeys = (List<ManagedObjectReference>)predicate.getComparableValue();
      Map<String, List<ManagedObjectReference>> keysByModel = groupKeysByModel(foreignKeys);
      for (Map.Entry<String, List<ManagedObjectReference>> entry : keysByModel.entrySet()) {
        String criterion = getInCriterion("@qs:resource", entry
            .getValue());
        List<String> xQueries = onePredicateXQueriesByModel.get(entry.getKey());
        if (xQueries == null) {
          xQueries = new ArrayList<>();
          onePredicateXQueriesByModel.put(entry.getKey(), xQueries);
        } 
        xQueries.add(criterion);
      } 
    } else if (predicate.getOperator().equals(PropertyPredicate.ComparisonOperator.EQUAL)) {
      String criterion = getCriterion("@qs:resource", PropertyPredicate.ComparisonOperator.EQUAL, predicate
          .getComparableValue());
      ManagedObjectReference mor = (ManagedObjectReference)predicate.getComparableValue();
      String model = mor.getType();
      List<String> xQueries = onePredicateXQueriesByModel.get(model);
      if (xQueries == null) {
        xQueries = new ArrayList<>();
        onePredicateXQueriesByModel.put(model, xQueries);
      } 
      xQueries.add(criterion);
    } else {
      throw new IllegalArgumentException("Only EQUAL operator supported");
    } 
    for (Map.Entry<String, List<String>> partialMapEntry : onePredicateXQueriesByModel.entrySet()) {
      String partialMapModel = partialMapEntry.getKey();
      List<String> xQueries = xQueriesByModel.get(partialMapModel);
      if (xQueries == null) {
        xQueries = new ArrayList<>();
        xQueriesByModel.put(partialMapModel, xQueries);
      } 
      xQueries.addAll(partialMapEntry.getValue());
    } 
  }
  
  private static Map<String, List<ManagedObjectReference>> groupKeysByModel(List<ManagedObjectReference> keys) {
    Map<String, List<ManagedObjectReference>> keysByModel = new HashMap<>();
    for (ManagedObjectReference foreignKey : keys) {
      String model = foreignKey.getType();
      List<ManagedObjectReference> mors = keysByModel.get(model);
      if (mors == null) {
        mors = new ArrayList<>();
        keysByModel.put(model, mors);
      } 
      mors.add(foreignKey);
    } 
    return keysByModel;
  }
  
  private static String combineCriteria(Map<String, List<String>> xQueryByModel, LogicalOperator operator) {
    StringBuilder builder = new StringBuilder();
    String mergeOperator = operator.equals(LogicalOperator.AND) ? " and " : " or ";
    boolean first = true;
    builder.append("local:product('vpx')");
    if (xQueryByModel.size() > 1)
      builder.append('('); 
    for (Map.Entry<String, List<String>> entry : xQueryByModel.entrySet()) {
      if (!first)
        builder.append(','); 
      first = false;
      builder.append("/");
      builder.append(entry.getKey());
      builder.append('[');
      builder.append(StringUtils.join(entry.getValue(), mergeOperator));
      builder.append(']');
    } 
    if (xQueryByModel.size() > 1)
      builder.append(')'); 
    return builder.toString();
  }
  
  private static void addTargetSet(StringBuilder builder, int targetSetCount, String assignedValue) {
    builder.append("let ");
    builder.append("$targetSet");
    builder.append(targetSetCount);
    builder.append(" := ");
    builder.append(assignedValue);
  }
  
  private static Map<String, List<PropertyPredicate>> groupPredicatesByProperty(Filter filter) {
    Map<String, List<PropertyPredicate>> predicatesByProperty = new HashMap<>();
    for (PropertyPredicate predicate : filter.getCriteria()) {
      String property = predicate.getProperty();
      List<PropertyPredicate> predicates = predicatesByProperty.get(property);
      if (predicates == null) {
        predicates = new LinkedList<>();
        predicatesByProperty.put(property, predicates);
      } 
      predicates.add(predicate);
    } 
    return predicatesByProperty;
  }
  
  private static String getInCriterion(String property, Object comparableValues) {
    Collection<?> values = (Collection)comparableValues;
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    boolean first = true;
    for (Object value : values) {
      if (!first)
        builder.append(" or "); 
      first = false;
      String singleInCriterion = getCriterion(property, PropertyPredicate.ComparisonOperator.EQUAL, value);
      builder.append(singleInCriterion);
    } 
    builder.append(')');
    return builder.toString();
  }
  
  private static String getCriterion(String property, PropertyPredicate.ComparisonOperator operator, Object comparableValue) {
    return property + convertOperator(operator) + 
      convertComparableValue(comparableValue);
  }
  
  private static String mergeCriteria(String model, LogicalOperator operator, List<String> xQueryCriteria) {
    StringBuilder builder = new StringBuilder();
    builder.append("/");
    builder.append(model);
    builder.append('[');
    String criteria = combineCriteria(operator, xQueryCriteria);
    builder.append(criteria);
    builder.append(']');
    builder.append('\n');
    return builder.toString();
  }
  
  private static String combineCriteria(LogicalOperator operator, List<String> xQueryCriteria) {
    StringBuilder builder = new StringBuilder();
    String mergeOperator = operator.equals(LogicalOperator.AND) ? " and " : " or ";
    Iterator<String> iterator = xQueryCriteria.iterator();
    builder.append(iterator.next());
    while (iterator.hasNext()) {
      builder.append(mergeOperator);
      builder.append(iterator.next());
    } 
    return builder.toString();
  }
  
  private static String convertOperator(PropertyPredicate.ComparisonOperator operator) {
    if (operator.equals(PropertyPredicate.ComparisonOperator.EQUAL))
      return "="; 
    throw new IllegalArgumentException("Operator not supported");
  }
  
  private static String getSort(Set<String> functionSet, String model, List<SortCriterion> criteria) {
    if (criteria.isEmpty())
      return "let $resultSortedFlag := false()\nlet $resultSorted := <query:resultSorted>{$resultSortedFlag}</query:resultSorted>\n"; 
    if (criteria.size() > 1)
      throw new IllegalArgumentException("Only one sort criterion supported"); 
    SortCriterion criterion = criteria.get(0);
    if (!PropertyUtil.isModelKey(criterion.getProperty()))
      throw new IllegalArgumentException("Only supported sort property is @modelKey"); 
    return String.format("let $orderedTargetSet := for $target in $targetSet\n\n  order by fn:string($target[1]/@qs:resource) %s\n  return $target\n\nlet $resultSortedFlag := true()\nlet $resultSorted := <query:resultSorted>{$resultSortedFlag}</query:resultSorted>\n\nlet $targetSet := $orderedTargetSet\n", new Object[] { criterion
          .getSortDirection().toString().toLowerCase() });
  }
  
  private static String getProperties(Set<String> functionSet, String model, Collection<String> properties, int offset, int limit) {
    int last;
    StringBuilder builder = new StringBuilder();
    if (limit < 0) {
      last = Integer.MAX_VALUE;
    } else {
      if (limit == 0)
        return "let $items := ()\nlet $itemCount := <query:itemCount>{count(($targetSet))}</query:itemCount>\n"; 
      last = limit + offset;
    } 
    builder.append(String.format("let $items := for $target in if ($resultSortedFlag) then $targetSet[fn:position()>=%d and fn:position()<=%d] else $targetSet\nlet $resourceId := $target/@qs:resource\nlet $targetDocId := $target/@qs:id\nreturn <query:item query:provider=\"{$targetDocId}\" query:resource=\"{$resourceId}\">\n<query:properties>\n", new Object[] { Integer.valueOf(offset + 1), Integer.valueOf(last) }));
    for (String property : properties) {
      if (PropertyUtil.isModelKey(property))
        continue; 
      String node = getReturnNode(functionSet, model, property);
      String path = getPath(model, property);
      builder.append("  {local:returnnode($target,");
      builder.append(node);
      builder.append(',');
      builder.append(path);
      builder.append(")}\n");
    } 
    builder.append("</query:properties>\n</query:item>\nlet $itemCount := <query:itemCount>{count(($targetSet))}</query:itemCount>\n");
    return builder.toString();
  }
  
  private static String getReturnNode(Set<String> functionSet, String model, String property) {
    XQueryDefinitions.PropertyDefinition propertyDefinition = XQueryDefinitions.getPropertyDefinition(model, property);
    String expression = propertyDefinition.getSelectExpression();
    if (expression != null) {
      addAllFunctions(functionSet, propertyDefinition.getSelectFunctions());
      return expression;
    } 
    if (property.indexOf('/') > 0)
      return "$target/vim25:" + property
        .replaceAll("/", "/vim25:"); 
    return "$target/" + property;
  }
  
  private static String getPath(String model, String property) {
    return "'" + XQueryUtil.getNodeName(property) + "'";
  }
  
  private static String convertPropertyFilter(String property) {
    if (PropertyUtil.isModelKey(property))
      return "@qs:id"; 
    return property.replaceAll("/", "/vim25:");
  }
  
  private static void addAllFunctions(Set<String> functionSet, Collection<String> dependentFunctionsSet) {
    for (String dependentFunction : dependentFunctionsSet)
      functionSet.addAll(
          XQueryDefinitions.getAllDependentFunctions(dependentFunction)); 
  }
  
  private static String convertComparableValue(Object value) {
    if (value instanceof ManagedObjectReference)
      return "'" + XQueryUtil.fromMoR(value) + "'"; 
    if (value instanceof Boolean)
      return ((Boolean)value).booleanValue() ? "true()" : "false()"; 
    if (value instanceof String)
      return "'" + value + "'"; 
    if (value instanceof Number)
      return value.toString(); 
    return value.toString();
  }
}
