package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class QueryAnalyzer {
  public static QueryAnalysis analyzeQuery(Query query) {
    validateQuery(query);
    String model = query.getResourceModels().iterator().next();
    Set<String> nativeProperties = new LinkedHashSet<>();
    Set<String> foreignKeyProperties = new LinkedHashSet<>();
    Map<String, FormulaRegistry.Formula> formulasByProperty = new LinkedHashMap<>();
    Map<String, String> unqualifiedPropertiesMap = new LinkedHashMap<>();
    List<PropertyPredicate> morPredicates = new ArrayList<>();
    List<PropertyPredicate> clientSidePredicates = new ArrayList<>();
    for (String property : query.getProperties()) {
      if (PropertyUtil.isSpecialProperty(property)) {
        unqualifiedPropertiesMap.put(property, property);
        continue;
      } 
      QualifiedProperty qp = QualifiedProperty.forQualifiedName(property);
      unqualifiedPropertiesMap.put(property, qp.getSimpleProperty());
      PropertyRegistry.PropertyDefinition definition = PropertyRegistry.getPropertyDefinition(model, qp.getSimpleProperty());
      if (definition.getFilterSpecForSelect() != null) {
        foreignKeyProperties.add(qp.getSimpleProperty());
        continue;
      } 
      FormulaRegistry.Formula formula = FormulaRegistry.getComputedProperty(model, qp.getSimpleProperty());
      if (formula == null) {
        nativeProperties.add(qp.getSimpleProperty());
        continue;
      } 
      nativeProperties.addAll(formula.getRequiredProperties());
      formulasByProperty.put(qp.getSimpleProperty(), formula);
    } 
    Filter filter = query.getFilter();
    boolean joinByAnd = true;
    if (filter != null) {
      joinByAnd = filter.getOperator().equals(LogicalOperator.AND);
      List<PropertyPredicate> predicates = filter.getCriteria();
      for (PropertyPredicate predicate : predicates) {
        String property = predicate.getProperty();
        if (PropertyUtil.isModelKey(property)) {
          morPredicates.add(predicate);
          continue;
        } 
        QualifiedProperty qp = QualifiedProperty.forQualifiedName(property);
        PropertyRegistry.PropertyDefinition definition = PropertyRegistry.getPropertyDefinition(model, qp.getSimpleProperty());
        PropertyPredicate convertedPredicate = new PropertyPredicate(qp.getSimpleProperty(), predicate.getOperator(), predicate.getComparableValue());
        if (definition.getFilterSpecForPredicate() != null) {
          morPredicates.add(convertedPredicate);
          continue;
        } 
        FormulaRegistry.Formula formula = FormulaRegistry.getComputedProperty(model, qp.getSimpleProperty());
        if (formula != null) {
          nativeProperties.addAll(formula.getRequiredProperties());
          formulasByProperty.put(qp.getSimpleProperty(), formula);
        } else {
          nativeProperties.add(qp.getSimpleProperty());
        } 
        clientSidePredicates.add(convertedPredicate);
      } 
    } 
    SortCriterion.SortDirection sortDirection = null;
    if (!query.getSortCriteria().isEmpty())
      sortDirection = ((SortCriterion)query.getSortCriteria().get(0)).getSortDirection(); 
    return new QueryAnalysis(model, nativeProperties, foreignKeyProperties, morPredicates, clientSidePredicates, sortDirection, formulasByProperty, unqualifiedPropertiesMap, joinByAnd);
  }
  
  static void validateQuery(Query query) {
    if (query.getResourceModels().size() != 1)
      throw new IllegalArgumentException("Only one model supported"); 
    if (query.getSortCriteria().size() > 1)
      throw new IllegalArgumentException("At most one sort criterion supported"); 
    if (query.getSortCriteria().size() == 1) {
      String property = ((SortCriterion)query.getSortCriteria().get(0)).getProperty();
      if (!PropertyUtil.isModelKey(property))
        throw new IllegalArgumentException("Only sort by @modelKey allowed"); 
    } 
    if (query.getFilter() == null)
      return; 
    for (PropertyPredicate predicate : query.getFilter().getCriteria()) {
      PropertyPredicate.ComparisonOperator operator = predicate.getOperator();
      if (!operator.equals(PropertyPredicate.ComparisonOperator.EQUAL) && 
        !operator.equals(PropertyPredicate.ComparisonOperator.IN))
        throw new IllegalArgumentException("Only EQUAL and IN operators supported"); 
    } 
  }
  
  public static final class QueryAnalysis {
    private final String _model;
    
    private final Collection<String> _nativeProperties;
    
    private final Collection<String> _foreignKeyProperties;
    
    private final List<PropertyPredicate> _morPredicates;
    
    private final List<PropertyPredicate> _clientSidePredicates;
    
    private final Map<String, FormulaRegistry.Formula> _formulasByProperty;
    
    private final Map<String, String> _originalByUnqualifiedProperties;
    
    private final SortCriterion.SortDirection _sortDirection;
    
    private final boolean _isIntersection;
    
    QueryAnalysis(String model, Collection<String> nativeProperties, Collection<String> foreignKeyProperties, List<PropertyPredicate> morPredicates, List<PropertyPredicate> clientSidePredicates, SortCriterion.SortDirection sortDirection, Map<String, FormulaRegistry.Formula> formulasByProperty, Map<String, String> originalByUnqualifiedProperties, boolean joinByAnd) {
      this._model = model;
      this._nativeProperties = nativeProperties;
      this._foreignKeyProperties = foreignKeyProperties;
      this._formulasByProperty = formulasByProperty;
      this._morPredicates = morPredicates;
      this._clientSidePredicates = clientSidePredicates;
      this._originalByUnqualifiedProperties = originalByUnqualifiedProperties;
      this._sortDirection = sortDirection;
      this._isIntersection = joinByAnd;
    }
    
    public String getModel() {
      return this._model;
    }
    
    public Collection<String> getNativeProperties() {
      return this._nativeProperties;
    }
    
    public Collection<String> getForeignKeyProperties() {
      return this._foreignKeyProperties;
    }
    
    public Collection<PropertyPredicate> getMorPredicates() {
      return this._morPredicates;
    }
    
    public List<PropertyPredicate> getClientSidePredicates() {
      return this._clientSidePredicates;
    }
    
    public SortCriterion.SortDirection getSortDirection() {
      return this._sortDirection;
    }
    
    public Map<String, String> getOriginalByUnqualifiedProperties() {
      return this._originalByUnqualifiedProperties;
    }
    
    public Map<String, FormulaRegistry.Formula> getFormulas() {
      return this._formulasByProperty;
    }
    
    public boolean isIntersection() {
      return this._isIntersection;
    }
  }
}
