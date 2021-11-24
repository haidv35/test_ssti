package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.PropertyPredicate;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.api.SortCriterion;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import com.vmware.cis.data.internal.provider.ext.ConnectionSupplier;
import com.vmware.cis.data.internal.provider.join.RelationalAlgebra;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.provider.util.QueryQualifier;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelatedPropertyProviderConnection implements DataProvider {
  private static final String RELATED_PROPERTY_PREFIX = "rel-";
  
  private static final Logger _logger = LoggerFactory.getLogger(RelatedPropertyProviderConnection.class);
  
  private final DataProvider _connection;
  
  private final ConnectionSupplier _extConnectionSupplier;
  
  private final RelatedPropertyLookup _relatedPropertyLookup;
  
  public RelatedPropertyProviderConnection(DataProvider connection, ConnectionSupplier extConnectionSupplier, RelatedPropertyLookup relatedPropertyLookup) {
    assert connection != null;
    assert extConnectionSupplier != null;
    assert relatedPropertyLookup != null;
    this._connection = connection;
    this._extConnectionSupplier = extConnectionSupplier;
    this._relatedPropertyLookup = relatedPropertyLookup;
  }
  
  public ResultSet executeQuery(Query query) {
    ResultSet result;
    Validate.notNull(query);
    Map<String, RelatedPropertyDescriptor> relatedSelectProperties = new LinkedHashMap<>();
    Map<String, RelatedPropertyDescriptor> relatedSortProperties = SortByRelatedProperty.getRelatedPropertiesInSort(query, this._relatedPropertyLookup);
    Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder = new LinkedHashMap<>();
    DataProvider extConnection = this._extConnectionSupplier.getConnection();
    DataProvider countingProvider = QueryIdLogConfigurator.withQueryCounter(extConnection, "rel-");
    Query extendedQuery = extendQuery(query, this._relatedPropertyLookup, relatedSelectProperties, relatedSortProperties, relatedFilterPropertiesOrder, countingProvider);
    if (extendedQuery == null)
      return ResultSet.EMPTY_RESULT; 
    if (query.equals(extendedQuery)) {
      result = this._connection.executeQuery(extendedQuery);
    } else {
      result = countingProvider.executeQuery(extendedQuery);
    } 
    assert result != null;
    Map<String, SortCriterion> relatedSelectPropertiesSortCriteria = SortByRelatedProperty.getSortCriteriaForRelatedPropertiesInSelect(query
        .getSortCriteria(), relatedSelectProperties.keySet(), relatedSortProperties
        .keySet(), relatedFilterPropertiesOrder.keySet());
    boolean doClientPaging = doClientPaging(
        !query.getSortCriteria().equals(extendedQuery.getSortCriteria()), query
        .getOffset(), query.getLimit());
    return extendResult(result, query, relatedSelectProperties, relatedFilterPropertiesOrder, relatedSelectPropertiesSortCriteria, doClientPaging, countingProvider);
  }
  
  public QuerySchema getSchema() {
    QuerySchema schema = this._connection.getSchema();
    if (schema.getModels().isEmpty())
      return schema; 
    return this._relatedPropertyLookup.addRelatedProps(schema);
  }
  
  public String toString() {
    return this._connection.toString();
  }
  
  private static Query extendQuery(Query originalQuery, RelatedPropertyLookup relatedPropertyLookup, Map<String, RelatedPropertyDescriptor> relatedSelectProperties, Map<String, RelatedPropertyDescriptor> relatedSortProperties, Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder, DataProvider connection) {
    assert relatedSelectProperties != null;
    assert relatedSortProperties != null;
    assert relatedFilterPropertiesOrder != null;
    List<SortCriterion> extendedSort = SortByRelatedProperty.getSortWithoutRelatedProperties(originalQuery
        .getSortCriteria(), relatedSortProperties);
    boolean sortExtended = !originalQuery.getSortCriteria().equals(extendedSort);
    boolean doClientPaging = doClientPaging(sortExtended, originalQuery
        .getOffset(), originalQuery.getLimit());
    Filter filter = originalQuery.getFilter();
    Filter extendedFilter = null;
    boolean filterExtended = false;
    if (filter != null) {
      extendedFilter = FilterByRelatedProperty.resolveRelatedPropertiesInFilter(originalQuery, relatedSortProperties, relatedFilterPropertiesOrder, relatedPropertyLookup, connection);
      if (extendedFilter == null)
        return null; 
      filterExtended = !filter.equals(extendedFilter);
    } else if (doClientPaging) {
      _logger.warn("Sorting and paging is applied using a custom related property in an unbounded (without filter) query. This may have a serious performance impact! Executed query: %s", originalQuery);
    } 
    List<String> extendedSelect = SelectRelatedProperty.resolveRelatedPropertiesInSelect(originalQuery.getProperties(), doClientPaging, relatedSelectProperties, relatedFilterPropertiesOrder
        
        .keySet(), relatedPropertyLookup);
    boolean selectExtended = !originalQuery.getProperties().equals(extendedSelect);
    if (!selectExtended && !filterExtended && !sortExtended)
      return originalQuery; 
    if (originalQuery.getProperties().contains("@modelKey") && 
      !extendedSelect.contains("@modelKey"))
      extendedSelect.add("@modelKey"); 
    Collection<String> resourceModels = QueryQualifier.getFromClause(extendedSelect, extendedFilter, extendedSort);
    if (resourceModels.isEmpty())
      resourceModels = originalQuery.getResourceModels(); 
    Query extendedQuery = QueryCopy.copyAndSelect(originalQuery, extendedSelect).from(resourceModels).where(extendedFilter).orderBy(extendedSort).offset(doClientPaging ? 0 : originalQuery.getOffset()).limit(doClientPaging ? -1 : originalQuery.getLimit()).build();
    if (_logger.isTraceEnabled()) {
      String str = (selectExtended ? "select, " : "") + (filterExtended ? "filter, " : "") + (sortExtended ? "sort, " : "");
      _logger.trace("[QueryModel] Processing a query containing related properties in [ {} ]..", str
          .substring(0, str.length() - 2));
      _logger.trace("[QueryModel] The query to be executed is: {}", extendedQuery);
    } 
    return extendedQuery;
  }
  
  private static ResultSet extendResult(ResultSet result, Query originalQuery, Map<String, RelatedPropertyDescriptor> relatedSelectProperties, Map<RelatedPropertyDescriptor, List<Object>> relatedFilterPropertiesOrder, Map<String, SortCriterion> relatedSelectPropertiesSortCriteria, boolean doClientPaging, DataProvider connection) {
    assert result != null;
    assert originalQuery != null;
    assert relatedSelectProperties != null;
    assert relatedFilterPropertiesOrder != null;
    assert relatedSelectPropertiesSortCriteria != null;
    assert connection != null;
    if (relatedSelectProperties.isEmpty() && relatedFilterPropertiesOrder.isEmpty())
      return result; 
    if (_logger.isTraceEnabled()) {
      if (!relatedSelectProperties.isEmpty())
        _logger.trace("[QueryModel] Processing the result for a query containing related properties in the select clause: {}", relatedSelectProperties
            
            .keySet()); 
      if (!relatedFilterPropertiesOrder.isEmpty())
        _logger.trace("[QueryModel] Processing the result for a query containing related properties in the filter clause used for sort: {}", relatedFilterPropertiesOrder
            
            .keySet()); 
    } 
    ResultSet extendedResult = result;
    boolean sortByFilter = !relatedFilterPropertiesOrder.isEmpty();
    boolean sortBySelect = !relatedSelectPropertiesSortCriteria.isEmpty();
    assert !((sortByFilter && sortBySelect) ? 1 : 0);
    if (sortByFilter) {
      List<String> srcModelPropsForRelatedSelectProperties = SelectRelatedProperty.getSourceModelPropertiesForRelatedSelectProperties(relatedSelectProperties
          .values());
      Set<String> queryProperties = new HashSet<>(originalQuery.getProperties());
      queryProperties.addAll(srcModelPropsForRelatedSelectProperties);
      extendedResult = SortByRelatedProperty.reorderResultByFilter(extendedResult, relatedFilterPropertiesOrder, queryProperties, originalQuery
          
          .getSortCriteria());
      if (doClientPaging)
        extendedResult = ResultSetUtil.applyLimitAndOffset(extendedResult, originalQuery
            .getLimit(), originalQuery.getOffset()); 
    } 
    extendedResult = SelectRelatedProperty.addRelatedPropertiesToResult(extendedResult, originalQuery, relatedSelectProperties, relatedSelectPropertiesSortCriteria, doClientPaging, connection);
    if (sortBySelect && doClientPaging)
      extendedResult = ResultSetUtil.applyLimitAndOffset(extendedResult, originalQuery
          .getLimit(), originalQuery.getOffset()); 
    if (doClientPaging)
      extendedResult = retrieveRemainingProperties(originalQuery, extendedResult, connection); 
    return extendedResult;
  }
  
  private static ResultSet retrieveRemainingProperties(Query query, ResultSet result, DataProvider connection) {
    assert query != null;
    List<String> queryProperties = query.getProperties();
    Set<String> remainingPropertiesSet = new LinkedHashSet<>(queryProperties);
    remainingPropertiesSet.removeAll(result.getProperties());
    if (remainingPropertiesSet.isEmpty()) {
      if (!queryProperties.contains("@modelKey"))
        return ResultSetUtil.removePropertyFromResultSet(result, "@modelKey"); 
      return result;
    } 
    List<Object> modelKeys = ResultSetAnalyzer.gatherModelKeysOrdered(result);
    if (modelKeys.isEmpty())
      return result; 
    List<String> remainingProperties = new ArrayList<>(remainingPropertiesSet);
    Collection<String> remainingResourceModels = QueryQualifier.getFromClause(queryProperties, null, null);
    if (remainingResourceModels.isEmpty())
      remainingResourceModels = query.getResourceModels(); 
    Query remainingPropertiesQuery = Query.Builder.select(PropertyUtil.plusModelKey(remainingProperties)).from(remainingResourceModels).where("@modelKey", PropertyPredicate.ComparisonOperator.IN, modelKeys).build();
    if (_logger.isTraceEnabled())
      _logger.trace("Sending query of remaining properties after replacing related properties {}", remainingPropertiesQuery); 
    ResultSet remainingResult = connection.executeQuery(remainingPropertiesQuery);
    if (_logger.isTraceEnabled())
      _logger.trace("Received response for query of remaining properties after replacing related properties {}", remainingResult); 
    ResultSet mergedResults = RelationalAlgebra.joinSelectAndProject(
        Arrays.asList(new ResultSet[] { result, remainingResult }, ), modelKeys, queryProperties);
    return ResultSet.Builder.copy(mergedResults)
      .totalCount(result.getTotalCount())
      .build();
  }
  
  private static boolean doClientPaging(boolean sortExtended, int offset, int limit) {
    return (sortExtended && (offset > 0 || limit > 0));
  }
}
