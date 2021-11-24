package com.vmware.cis.data.internal.provider.join;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.LogicalOperator;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ProviderBySchemaLookup;
import com.vmware.cis.data.internal.provider.QueryExecutor;
import com.vmware.cis.data.internal.provider.profiler.ProfiledDataProvider;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class DistributedJoinProviderConnection implements DataProvider {
  private final QueryDecomposer _queryDecomposer;
  
  private final QueryExecutor _queryExecutor;
  
  public static DataProvider createDistributedJoin(ProviderBySchemaLookup providerLookup, QueryExecutor queryExecutor) {
    Validate.notNull(providerLookup);
    Validate.notNull(queryExecutor);
    QueryDecomposer queryDecomposer = new ModelKeyQueryDecomposer(providerLookup);
    return ProfiledDataProvider.create(new DistributedJoinProviderConnection(queryDecomposer, queryExecutor));
  }
  
  private DistributedJoinProviderConnection(QueryDecomposer queryDecomposer, QueryExecutor queryExecutor) {
    assert queryDecomposer != null;
    assert queryExecutor != null;
    this._queryDecomposer = queryDecomposer;
    this._queryExecutor = queryExecutor;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    Map<DataProvider, Query> queryByProvider = this._queryDecomposer.decomposeByProvider(query);
    Map<Query, ResultSet> resultByQuery = this._queryExecutor.executeQueries(queryByProvider);
    JoinOperator joinOperator = getJoinOperator(query.getFilter());
    ResultSet joinResult = joinResults(joinOperator, resultByQuery);
    joinResult = ResultSetUtil.applyLimitAndOffset(joinResult, query.getLimit(), query.getOffset());
    return joinResult;
  }
  
  public QuerySchema getSchema() {
    throw new UnsupportedOperationException("Distributed JOIN Provider schema is not yet supported");
  }
  
  private ResultSet joinResults(JoinOperator joinOperator, Map<Query, ResultSet> resultByQuery) {
    List<ResultSet> unorderedResults = new ArrayList<>(resultByQuery.size());
    ResultSet sortedResult = null;
    boolean sortedResultsAreFiltered = false;
    for (Map.Entry<Query, ResultSet> queryAndResult : resultByQuery.entrySet()) {
      Query query = queryAndResult.getKey();
      ResultSet result = queryAndResult.getValue();
      if (!query.getSortCriteria().isEmpty()) {
        sortedResult = result;
        if (query.getFilter() != null)
          sortedResultsAreFiltered = true; 
        continue;
      } 
      unorderedResults.add(result);
    } 
    ResultSet joinResult = joinOperator.join(unorderedResults);
    if (sortedResult != null) {
      if (!sortedResultsAreFiltered)
        joinOperator = new LeftJoinOperator(); 
      joinResult = joinOperator.joinOrderedResult(joinResult, sortedResult);
    } 
    return joinResult;
  }
  
  private JoinOperator getJoinOperator(Filter filter) {
    if (filter == null)
      return new FullOuterJoinOperator(); 
    switch (filter.getOperator()) {
      case AND:
        return new InnerJoinOperator();
      case OR:
        return new FullOuterJoinOperator();
    } 
    throw new UnsupportedOperationException("Unsupported logical operator: " + filter
        .getOperator());
  }
}
