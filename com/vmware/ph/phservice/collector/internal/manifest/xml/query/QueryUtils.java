package com.vmware.ph.phservice.collector.internal.manifest.xml.query;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.ComparisonOperator;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.FilterSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.LogicalOperator;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.PropertyPredicate;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.PropertySpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.QuerySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryUtils {
  public static NamedQuery viseQuerySpecToRiseQuery(QuerySpec viseQuerySpec) {
    String[] coreResourceModels = { (viseQuerySpec.getConstraint()).targetType };
    List<String> coreProperties = determineCoreProperties(viseQuerySpec);
    Filter coreFilter = toCoreFilter(viseQuerySpec.getFilter());
    int coreOffset = 0;
    int coreLimit = determineCoreLimit(viseQuerySpec);
    List<SortCriterion> coreSortCriteria = determineCoreSortCriteria(viseQuerySpec);
    Query.Builder riseQueryBuilder = Query.Builder.select(coreProperties).from(coreResourceModels).where(coreFilter).offset(coreOffset).limit(coreLimit).orderBy(coreSortCriteria);
    Boolean withTotalCount = Boolean.valueOf(viseQuerySpec.getWithTotalCount());
    if (Boolean.TRUE.equals(withTotalCount))
      riseQueryBuilder.withTotalCount(); 
    Query riseQuery = riseQueryBuilder.build();
    return new NamedQuery(riseQuery, viseQuerySpec
        
        .getName(), viseQuerySpec
        .getCpuThreshold(), viseQuerySpec
        .getMemoryThreshold(), viseQuerySpec
        .getPageSize());
  }
  
  private static List<String> determineCoreProperties(QuerySpec viseQuerySpec) {
    Boolean withTotalCount = Boolean.valueOf(viseQuerySpec.getWithTotalCount());
    if (Boolean.TRUE.equals(withTotalCount))
      return Collections.emptyList(); 
    List<PropertySpec> viseProperties = viseQuerySpec.getPropertySpecs();
    List<String> coreProperties = new ArrayList<>();
    coreProperties.add("@modelKey");
    if (viseProperties != null && viseProperties.size() > 0) {
      String[] visePropertyNames = ((PropertySpec)viseProperties.get(0)).propertyNames;
      for (String visePropertyName : visePropertyNames) {
        String corePropertyName = toCorePropertyName(visePropertyName);
        coreProperties.add(corePropertyName);
      } 
    } 
    return coreProperties;
  }
  
  private static String toCorePropertyName(String visePropertyName) {
    String corePropertyName = visePropertyName.replaceAll("\\.", "/");
    if (corePropertyName.endsWith("_length"))
      corePropertyName = corePropertyName.replaceAll("_length", "length"); 
    return corePropertyName;
  }
  
  private static Filter toCoreFilter(FilterSpec filter) {
    Filter coreFilter = null;
    if (filter != null && filter.getCriteria() != null) {
      List<PropertyPredicate> coreCriteria = toCoreCriteria(filter.getCriteria());
      if (filter.getOperator() != null) {
        LogicalOperator logicalOperator = toCoreLogicalOperator(filter.getOperator());
        coreFilter = new Filter(coreCriteria, logicalOperator);
      } else {
        coreFilter = new Filter(coreCriteria);
      } 
    } 
    return coreFilter;
  }
  
  private static List<PropertyPredicate> toCoreCriteria(List<PropertyPredicate> predicates) {
    List<PropertyPredicate> coreCriteria = new ArrayList<>(predicates.size());
    for (PropertyPredicate predicate : predicates) {
      PropertyPredicate.ComparisonOperator comparisonOperator = toCoreComparisonOperator(predicate.getOperator());
      String corePredicateProperty = toCorePropertyName(predicate.getProperty());
      coreCriteria.add(new PropertyPredicate(corePredicateProperty, comparisonOperator, predicate


            
            .getComparableValue(), predicate
            .isIgnoreCase()));
    } 
    return coreCriteria;
  }
  
  private static LogicalOperator toCoreLogicalOperator(LogicalOperator logicalOperator) {
    LogicalOperator coreLogicalOperator = null;
    switch (logicalOperator) {
      case EQUAL:
        coreLogicalOperator = LogicalOperator.AND;
        break;
      case NOT_EQUAL:
        coreLogicalOperator = LogicalOperator.OR;
        break;
    } 
    return coreLogicalOperator;
  }
  
  private static PropertyPredicate.ComparisonOperator toCoreComparisonOperator(ComparisonOperator comparisonOperator) {
    PropertyPredicate.ComparisonOperator coreComparisonOperator = null;
    if (comparisonOperator == null)
      throw new IllegalArgumentException("Invalid comparison operator."); 
    switch (comparisonOperator) {
      case EQUAL:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.EQUAL;
        break;
      case NOT_EQUAL:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.NOT_EQUAL;
        break;
      case GREATER:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.GREATER;
        break;
      case GREATER_OR_EQUAL:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.GREATER_OR_EQUAL;
        break;
      case LESS:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.LESS;
        break;
      case LESS_OR_EQUAL:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.LESS_OR_EQUAL;
        break;
      case IN:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.IN;
        break;
      case NOT_IN:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.NOT_IN;
        break;
      case LIKE:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.LIKE;
        break;
      case UNSET:
        coreComparisonOperator = PropertyPredicate.ComparisonOperator.UNSET;
        break;
    } 
    return coreComparisonOperator;
  }
  
  private static int determineCoreLimit(QuerySpec viseQuerySpec) {
    Boolean withTotalCount = Boolean.valueOf(viseQuerySpec.getWithTotalCount());
    if (Boolean.TRUE.equals(withTotalCount))
      return 0; 
    int coreLimit = -1;
    if (null != viseQuerySpec.getMaxResultCount())
      coreLimit = viseQuerySpec.getMaxResultCount().intValue(); 
    return coreLimit;
  }
  
  private static List<SortCriterion> determineCoreSortCriteria(QuerySpec viseQuerySpec) {
    Boolean withTotalCount = Boolean.valueOf(viseQuerySpec.getWithTotalCount());
    if (Boolean.TRUE.equals(withTotalCount))
      return Collections.emptyList(); 
    return Collections.singletonList(new SortCriterion("@modelKey"));
  }
}
