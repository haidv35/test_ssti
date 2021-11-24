package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryExecutor {
  private static final Logger _logger = LoggerFactory.getLogger(QueryExecutor.class);
  
  private final TaskExecutor _taskExecutor;
  
  public QueryExecutor(ExecutorService executor) {
    assert executor != null;
    this._taskExecutor = new TaskExecutor(executor, TaskExecutor.ErrorHandlingPolicy.STRICT);
  }
  
  public Map<Query, ResultSet> executeQueries(Map<DataProvider, Query> queryByProvider) {
    assert queryByProvider != null;
    assert !queryByProvider.isEmpty();
    List<Callable<QueryResult>> queryTasks = createQueryTasks(queryByProvider);
    List<QueryResult> queryResults = this._taskExecutor.invokeTasks(queryTasks);
    Map<Query, ResultSet> resultByQuery = new HashMap<>(queryByProvider.size());
    for (QueryResult queryResult : queryResults)
      resultByQuery.put(queryResult.getQuery(), queryResult.getResult()); 
    return resultByQuery;
  }
  
  public ResultSet executeQuery(DataProvider provider, Query query) {
    ResultSet result;
    assert provider != null;
    assert query != null;
    try {
      markExecutionStart(provider, query);
      result = provider.executeQuery(query);
      markExecutionEnd(provider, result);
    } catch (Exception e) {
      _logger.error("Data Provider '" + provider.toString() + "' threw error while executing query:" + query, e);
      throw e;
    } 
    return result;
  }
  
  private List<Callable<QueryResult>> createQueryTasks(Map<DataProvider, Query> queryByProvider) {
    List<Callable<QueryResult>> queryTasks = new ArrayList<>(queryByProvider.size());
    for (Map.Entry<DataProvider, Query> providerAndQuery : queryByProvider.entrySet()) {
      final Query query = providerAndQuery.getValue();
      final DataProvider provider = providerAndQuery.getKey();
      Callable<QueryResult> queryTask = new Callable<QueryResult>() {
          public QueryExecutor.QueryResult call() throws Exception {
            ResultSet result = QueryExecutor.this.executeQuery(provider, query);
            QueryExecutor.QueryResult queryResult = new QueryExecutor.QueryResult(query, result);
            return queryResult;
          }
          
          public String toString() {
            return String.format("query for %s executed on %s", new Object[] { this.val$query.getResourceModels(), this.val$provider.toString() });
          }
        };
      queryTasks.add(queryTask);
    } 
    return queryTasks;
  }
  
  private static void markExecutionStart(DataProvider provider, Query query) {
    if (_logger.isDebugEnabled())
      _logger.debug("Dispatching query to Data Provider: " + provider
          .getClass().getCanonicalName()); 
    if (_logger.isTraceEnabled())
      _logger.trace("The query to be executed is: " + query); 
  }
  
  private static void markExecutionEnd(DataProvider provider, ResultSet result) {
    if (_logger.isTraceEnabled())
      _logger.trace("The result set for the query is: " + result); 
  }
  
  private static class QueryResult {
    private final Query _query;
    
    private final ResultSet _result;
    
    public QueryResult(Query query, ResultSet result) {
      this._query = query;
      this._result = result;
    }
    
    public Query getQuery() {
      return this._query;
    }
    
    public ResultSet getResult() {
      return this._result;
    }
  }
}
