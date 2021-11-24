package com.vmware.ph.phservice.collector.internal.data;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryServiceCollector<OUT> {
  private static final Log _log = LogFactory.getLog(QueryServiceCollector.class);
  
  private final ResultIteratorFactory<OUT> _resultIteratorFactory;
  
  public QueryServiceCollector() {
    this(new ResultIteratorFactory<>());
  }
  
  QueryServiceCollector(ResultIteratorFactory<OUT> iteratorFactory) {
    this._resultIteratorFactory = iteratorFactory;
  }
  
  public Iterable<OUT> collect(NamedQuery[] queries, final NamedQueryResultSetMapping<OUT> responseMapping, final QueryService queryService, Context context, final int pageSize) {
    final Context modifiedContext = new Context(context);
    if (ArrayUtils.isEmpty((Object[])queries))
      return Collections.emptyList(); 
    final NamedQuery[] queriesForExecution = filterQueriesForExecution(queryService, queries, responseMapping);
    if (ArrayUtils.isEmpty((Object[])queriesForExecution))
      return Collections.emptyList(); 
    return new Iterable<OUT>() {
        public Iterator<OUT> iterator() {
          return QueryServiceCollector.this._resultIteratorFactory.getIterator(queryService, responseMapping, modifiedContext, queriesForExecution, pageSize);
        }
      };
  }
  
  private static NamedQuery[] filterQueriesForExecution(QueryService queryService, NamedQuery[] queries, NamedQueryResultSetMapping<?> responseMapping) {
    QueryExecutionEvaluator queryExecutionEvaluator = new QueryExecutionEvaluator(queryService);
    List<NamedQuery> queriesForExecution = new ArrayList<>(queries.length);
    for (int i = 0; i < queries.length; i++) {
      NamedQuery query = queries[i];
      if (responseMapping.isQuerySupported(query.getName()) && queryExecutionEvaluator
        .shouldExecuteQuery(query)) {
        queriesForExecution.add(query);
      } else if (_log.isDebugEnabled()) {
        _log.debug(
            String.format("Query [%s] was excluded from execution.", new Object[] { query.getName() }));
      } 
    } 
    return queriesForExecution.<NamedQuery>toArray(new NamedQuery[queriesForExecution.size()]);
  }
  
  static class ResultIteratorFactory<OUT> {
    public Iterator<OUT> getIterator(final QueryService queryService, final Mapping<NamedQueryResultSet, OUT> responseMapping, final Context context, final NamedQuery[] queries, final int pageSize) {
      Iterable<NamedQueryResultSet> queryPagingIterable = new Iterable<NamedQueryResultSet>() {
          public Iterator<NamedQueryResultSet> iterator() {
            return new BatchQueryPagingIterator(queryService, queries, pageSize);
          }
        };
      Iterable<OUT> responseMappingIterable = Iterables.transform(queryPagingIterable, new Function<NamedQueryResultSet, OUT>() {
            public OUT apply(NamedQueryResultSet inputResultSet) {
              OUT result = responseMapping.map(inputResultSet, context);
              return result;
            }
          });
      return responseMappingIterable.iterator();
    }
  }
}
