package com.vmware.cis.data.api;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.cis.data.query.util.QueryServiceFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.Validate;

public final class MockQueryServiceFactory implements QueryServiceFactory {
  private static final String QUERY_FILTERS_ARE_NOT_THE_SAME_MESSAGE = "The query filters are not the same";
  
  private int _invocationIndex = 0;
  
  private final List<Query> _expectedQueries = new ArrayList<>();
  
  private final List<ResultSet> _expectedResults = new ArrayList<>();
  
  private final QueryService _queryService;
  
  public MockQueryServiceFactory() {
    this._queryService = QueryService.Builder.forProvider(new MockDataProvider()).build();
  }
  
  public QueryService getQueryService() {
    return this._queryService;
  }
  
  public void expectAndReturn(QueryCommand command, ResultSet resultSet) {
    Validate.notNull(command);
    Validate.notNull(resultSet);
    this._expectedQueries.add(command.getQuery());
    this._expectedResults.add(resultSet);
  }
  
  private class MockDataProvider implements DataProvider {
    private MockDataProvider() {}
    
    public ResultSet executeQuery(Query query) {
      int currentInvocation = MockQueryServiceFactory.this._invocationIndex++;
      if (currentInvocation >= MockQueryServiceFactory.this._expectedQueries.size())
        throw new AssertionError("The query service has been called more times than the recorded expectations:" + query); 
      Query expectedQuery = MockQueryServiceFactory.this._expectedQueries.get(currentInvocation);
      MockQueryServiceFactory.assertQueries(expectedQuery, query);
      return MockQueryServiceFactory.this._expectedResults.get(currentInvocation);
    }
    
    public QuerySchema getSchema() {
      throw new UnsupportedOperationException("Not implemented");
    }
  }
  
  private static void assertQueries(Query expectedQuery, Query query) {
    assertSelectedProperties(expectedQuery.getProperties(), query.getProperties());
    assertEquals(expectedQuery.getResourceModels(), query.getResourceModels(), "The query resource models are not the same");
    assertFilter(expectedQuery.getFilter(), query.getFilter());
    assertEquals(expectedQuery.getSortCriteria(), query.getSortCriteria(), "The query sort criteria are not the same");
    assertEquals(Integer.valueOf(expectedQuery.getOffset()), Integer.valueOf(query.getOffset()), "The query offsets are not the same");
    assertEquals(Integer.valueOf(expectedQuery.getLimit()), Integer.valueOf(query.getLimit()), "The query limits are not the same");
    assertEquals(Boolean.valueOf(expectedQuery.getWithTotalCount()), Boolean.valueOf(query.getWithTotalCount()), "The query withTotalCount flags are not the same");
  }
  
  private static void assertFilter(Filter expectedFilter, Filter filter) {
    String errorMessage = "The query filters are not the same - expected:<" + expectedFilter + "> but was:<" + filter + ">";
    if (expectedFilter == null && filter == null)
      return; 
    if (expectedFilter == null || filter == null)
      throw new AssertionError(errorMessage); 
    if (!expectedFilter.getOperator().equals(filter.getOperator()))
      throw new AssertionError(errorMessage); 
    if (expectedFilter.getCriteria().size() != filter.getCriteria().size())
      throw new AssertionError(errorMessage); 
    Iterator<PropertyPredicate> expectedPredicatesIterator = expectedFilter.getCriteria().iterator();
    Iterator<PropertyPredicate> actualPredicatesIterator = filter.getCriteria().iterator();
    while (expectedPredicatesIterator.hasNext()) {
      if (!areEqualPredicates(expectedPredicatesIterator.next(), actualPredicatesIterator
          .next()))
        throw new AssertionError(errorMessage); 
    } 
  }
  
  private static boolean areEqualPredicates(PropertyPredicate predicate1, PropertyPredicate predicate2) {
    if (!predicate1.getOperator().equals(PropertyPredicate.ComparisonOperator.IN))
      return Objects.equals(predicate1, predicate2); 
    if (!predicate1.getOperator().equals(predicate2.getOperator()))
      return false; 
    if (predicate1.isIgnoreCase() != predicate2.isIgnoreCase())
      return false; 
    if (!predicate1.getProperty().equals(predicate2.getProperty()))
      return false; 
    Set<Object> set1 = new HashSet((Collection)predicate1.getComparableValue());
    Set<Object> set2 = new HashSet((Collection)predicate2.getComparableValue());
    if (!set1.equals(set2))
      return false; 
    return true;
  }
  
  private static void assertSelectedProperties(List<String> expectedProperties, List<String> properties) {
    assertEquals(Integer.valueOf(expectedProperties.size()), Integer.valueOf(properties.size()), "The sizes of query properties are not the same");
    for (int i = 0; i < expectedProperties.size(); i++) {
      String expectedProperty = expectedProperties.get(i);
      String property = properties.get(i);
      assertEquals(expectedProperty, property, "The query properties on position " + i + " are not the same");
    } 
  }
  
  private static void assertEquals(Object expected, Object actual, String msg) {
    if (Objects.equals(expected, actual))
      return; 
    throw new AssertionError(msg + " - expected:<" + expected + "> but was:<" + actual + ">");
  }
}
