package com.vmware.cis.data.internal.provider.ext.aggregated;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AggregatedModelQueryExecutor {
  private static final Logger _logger = LoggerFactory.getLogger(AggregatedModelQueryExecutor.class);
  
  private final DataProvider _connection;
  
  private final TaskExecutor _taskExecutor;
  
  AggregatedModelQueryExecutor(DataProvider connection, ExecutorService executor) {
    assert connection != null;
    assert executor != null;
    this._connection = connection;
    this._taskExecutor = new TaskExecutor(executor, TaskExecutor.ErrorHandlingPolicy.STRICT);
  }
  
  List<ResultSet> executeQueries(List<Query> queries) {
    assert queries != null;
    List<Callable<ResultSet>> queryTasks = new ArrayList<>(queries.size());
    for (Query query : queries) {
      if (query == null)
        continue; 
      queryTasks.add(new Callable<ResultSet>() {
            public ResultSet call() {
              AggregatedModelQueryExecutor._logger.trace("Sending query with replaced aggregated models {}", query);
              ResultSet resultSet = AggregatedModelQueryExecutor.this._connection.executeQuery(query);
              AggregatedModelQueryExecutor._logger.trace("Received response for query with replaced aggregated models {}", resultSet);
              return resultSet;
            }
            
            public String toString() {
              return "Aggregated model query for " + query.getResourceModels();
            }
          });
    } 
    List<ResultSet> nonNullResults = this._taskExecutor.invokeTasks(queryTasks);
    List<ResultSet> results = new ArrayList<>(queries.size());
    int nonNullIdx = 0;
    for (Query query : queries) {
      if (query == null) {
        results.add(null);
        continue;
      } 
      results.add(nonNullResults.get(nonNullIdx));
      nonNullIdx++;
    } 
    assert queries.size() == results.size();
    return results;
  }
}
