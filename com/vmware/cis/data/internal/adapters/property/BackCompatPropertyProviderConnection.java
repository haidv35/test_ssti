package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ResultSetAnalyzer;
import com.vmware.cis.data.internal.provider.join.RelationalAlgebra;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

final class BackCompatPropertyProviderConnection implements DataProvider {
  private final DataProvider _provider;
  
  private final QuerySchema _schema;
  
  private final Client _vlsiClient;
  
  private final TaskExecutor _taskExecutor;
  
  private final BackCompatPropertyProviderRepository _propertyProviderRepository;
  
  BackCompatPropertyProviderConnection(DataProvider provider, BackCompatPropertyProviderRepository propertyProviderRepository, Client vlsiClient, ExecutorService executor) {
    assert provider != null;
    assert executor != null;
    assert vlsiClient != null;
    assert propertyProviderRepository != null;
    this._provider = provider;
    this._schema = provider.getSchema();
    this._vlsiClient = vlsiClient;
    this._taskExecutor = new TaskExecutor(executor, TaskExecutor.ErrorHandlingPolicy.STRICT);
    this._propertyProviderRepository = propertyProviderRepository;
  }
  
  public ResultSet executeQuery(Query query) {
    if (skip(query))
      return this._provider.executeQuery(query); 
    List<String> remainingProperties = new ArrayList<>();
    Query rawQuery = QueryCopy.copyAndSelect(query, adaptSelect(query.getProperties(), remainingProperties)).build();
    ResultSet rawResult = this._provider.executeQuery(rawQuery);
    if (rawResult.getItems().isEmpty())
      return rawResult; 
    List<Object> keys = ResultSetAnalyzer.gatherModelKeysOrdered(rawResult);
    List<ResultSet> propertyProviderResults = execPropertyProviders(remainingProperties, keys);
    List<ResultSet> resultsToJoin = new ArrayList<>(propertyProviderResults);
    resultsToJoin.add(rawResult);
    ResultSet mergedResults = RelationalAlgebra.joinSelectAndProject(resultsToJoin, keys, query
        .getProperties());
    return ResultSet.Builder.copy(mergedResults)
      .totalCount(rawResult.getTotalCount())
      .build();
  }
  
  public QuerySchema getSchema() {
    QuerySchema propertyProviderSchema = getPropertyProviderSchema();
    QuerySchema baseSchema = this._provider.getSchema();
    return SchemaUtil.merge(propertyProviderSchema, baseSchema);
  }
  
  public String toString() {
    return this._provider.toString();
  }
  
  private QuerySchema getPropertyProviderSchema() {
    Map<String, QuerySchema.PropertyInfo> infosByProperty = new LinkedHashMap<>();
    for (String property : this._propertyProviderRepository.getProperties())
      infosByProperty.put(property, QuerySchema.PropertyInfo.forNonFilterableProperty()); 
    return QuerySchema.forProperties(infosByProperty);
  }
  
  private boolean skip(Query query) {
    assert query != null;
    for (String property : query.getProperties()) {
      if (isFromPropertyProvider(property))
        return false; 
    } 
    return true;
  }
  
  private List<String> adaptSelect(List<String> properties, List<String> remainingProperties) {
    assert properties != null;
    assert !properties.isEmpty();
    List<String> executableSelect = new ArrayList<>(properties.size());
    for (String property : properties) {
      if (isFromPropertyProvider(property)) {
        remainingProperties.add(property);
        continue;
      } 
      executableSelect.add(property);
    } 
    return executableSelect;
  }
  
  private boolean isFromPropertyProvider(String property) {
    assert property != null;
    return (this._propertyProviderRepository.getProperties().contains(property) && 
      SchemaUtil.getPropertyInfoForQualifiedName(this._schema, property) == null);
  }
  
  private List<ResultSet> execPropertyProviders(List<String> properties, final List<Object> keys) {
    Map<BackCompatPropertyProvider, List<String>> propertiesByProvider = this._propertyProviderRepository.getPropertiesByProvider(properties);
    List<Callable<ResultSet>> tasks = new ArrayList<>(properties.size());
    for (BackCompatPropertyProvider propertyProvider : propertiesByProvider.keySet()) {
      final List<String> providerProperties = propertiesByProvider.get(propertyProvider);
      tasks.add(new Callable<ResultSet>() {
            public ResultSet call() throws Exception {
              List<Collection<?>> columns = propertyProvider.fetchPropertyValues(providerProperties, keys, BackCompatPropertyProviderConnection.this._provider, BackCompatPropertyProviderConnection.this._vlsiClient);
              return ResultSetUtil.toResult(keys, providerProperties, columns);
            }
            
            public String toString() {
              return "execute property provider for " + providerProperties;
            }
          });
    } 
    List<ResultSet> results = this._taskExecutor.invokeTasks(tasks);
    assert !results.isEmpty();
    return results;
  }
}
