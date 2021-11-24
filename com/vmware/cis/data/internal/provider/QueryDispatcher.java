package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.join.DistributedJoinProviderConnection;
import com.vmware.cis.data.internal.provider.profiler.ProfiledDataProvider;
import com.vmware.cis.data.internal.util.QueryCopy;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.Validate;

public final class QueryDispatcher implements DataProvider {
  private final ProviderBySchemaLookup _providerLookup;
  
  private final QueryClauseAnalyzer _clauseAnalyzer;
  
  private final QueryExecutor _queryExecutor;
  
  private final PropertyRetriever _propertyRetriever;
  
  public static DataProvider createDispatcher(Collection<DataProvider> providers, ExecutorService executor) {
    Validate.notNull(providers);
    Validate.notNull(executor);
    ProviderBySchemaLookup lookup = ProviderRepository.forProviders(providers);
    return new QueryQualifyingConnection(ProfiledDataProvider.create(new QueryDispatcher(lookup, executor)));
  }
  
  public QueryDispatcher(ProviderBySchemaLookup providerLookup, ExecutorService executor) {
    assert providerLookup != null;
    assert executor != null;
    this._providerLookup = providerLookup;
    this._clauseAnalyzer = new QueryClauseAnalyzer(this._providerLookup);
    this._queryExecutor = new QueryExecutor(executor);
    this._propertyRetriever = new PropertyRetriever(this._clauseAnalyzer, this._queryExecutor);
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    Query initialQuery = query;
    DataProvider provider = this._clauseAnalyzer.getQueryProvider(query);
    if (provider == null) {
      provider = DistributedJoinProviderConnection.createDistributedJoin(this._providerLookup, this._queryExecutor);
    } else {
      List<String> properties = getPropertiesForProvider(query.getProperties(), provider);
      if (!query.getProperties().equals(properties)) {
        if (!properties.contains("@modelKey"))
          properties.add("@modelKey"); 
        initialQuery = QueryCopy.copyAndSelect(query, properties).build();
      } 
    } 
    ResultSet initialResult = this._queryExecutor.executeQuery(provider, initialQuery);
    assert initialResult != null;
    ResultSet finalResult = this._propertyRetriever.gatherRemainingProperties(query, initialResult);
    Integer totalCount = null;
    if (query.getWithTotalCount())
      totalCount = initialResult.getTotalCount(); 
    return ResultSet.Builder.copy(finalResult)
      .totalCount(totalCount)
      .build();
  }
  
  public QuerySchema getSchema() {
    return this._providerLookup.getSchema();
  }
  
  private List<String> getPropertiesForProvider(Collection<String> properties, DataProvider provider) {
    assert properties != null;
    assert provider != null;
    List<String> providerProperties = new ArrayList<>();
    for (String property : properties) {
      DataProvider propertyProvider;
      if (QueryClauseAnalyzer.isSpecialProperty(property)) {
        propertyProvider = provider;
      } else {
        propertyProvider = this._providerLookup.getProviderForProperty(property);
      } 
      if (provider == propertyProvider)
        providerProperties.add(property); 
    } 
    return providerProperties;
  }
}
