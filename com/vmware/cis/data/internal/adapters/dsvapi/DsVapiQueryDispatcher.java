package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.cis.data.ResourceModel;
import com.vmware.cis.data.ResourceModelTypes;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.adapters.vapi.impl.DefaultVapiPropertyValueConverter;
import com.vmware.cis.data.internal.provider.ProviderBySchemaLookup;
import com.vmware.cis.data.internal.provider.ProviderRepository;
import com.vmware.cis.data.internal.provider.QueryClauseAnalyzer;
import com.vmware.cis.data.internal.provider.QueryDispatcher;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vapi.std.errors.InvalidArgument;
import com.vmware.vapi.std.errors.Unauthorized;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DsVapiQueryDispatcher implements DataProvider {
  private static Logger _logger = LoggerFactory.getLogger(DsVapiQueryDispatcher.class);
  
  private final ResourceModel _resourceModel;
  
  private final DataProvider _dataProvider;
  
  private final ProviderBySchemaLookup _providerLookup;
  
  private static final DsVapiQueryConverter _queryConverter;
  
  private static final DsVapiResultSetConverter _resultConverter;
  
  static {
    VapiPropertyValueConverter propertyValueConverter = new DefaultVapiPropertyValueConverter();
    _queryConverter = new DsVapiQueryConverter(propertyValueConverter);
    _resultConverter = new DsVapiResultSetConverter(propertyValueConverter);
  }
  
  public DsVapiQueryDispatcher(ResourceModel resourceModel, Collection<DataProvider> dataProviders, ExecutorService executor, String applianceId) {
    assert resourceModel != null;
    assert dataProviders != null;
    assert executor != null;
    this._resourceModel = resourceModel;
    DataProvider dataProvider = createQueryDispatcher(dataProviders, executor);
    this._dataProvider = dataProvider;
    this._providerLookup = ProviderRepository.forProviders(Arrays.asList(new DataProvider[] { dataProvider }));
    _resultConverter.setApplianceId(applianceId);
  }
  
  public ResultSet executeQuery(Query query) {
    assert query != null;
    if (isQuerySupported(query))
      return this._dataProvider.executeQuery(query); 
    _logger.debug(
        String.format("Query is not fully supported by the provided DataProvider(s), rerouting to DataService Public API: %s", new Object[] { query }));
    return executeQueryViaPublicVapi(query);
  }
  
  public QuerySchema getSchema() {
    return this._dataProvider.getSchema();
  }
  
  private static DataProvider createQueryDispatcher(Collection<DataProvider> dataProviders, ExecutorService executor) {
    assert dataProviders != null;
    assert executor != null;
    if (dataProviders.size() > 1)
      return QueryDispatcher.createDispatcher(dataProviders, executor); 
    return dataProviders.iterator().next();
  }
  
  private ResultSet executeQueryViaPublicVapi(Query query) {
    ResourceModelTypes.ResultSet resultSet;
    assert query != null;
    ResourceModelTypes.QuerySpec vapiQuery = _queryConverter.convertQuery(query);
    _logger.trace("Sending query to DS Public vAPI: {}", vapiQuery);
    try {
      resultSet = this._resourceModel.query(vapiQuery);
    } catch (InvalidArgument ex) {
      throw new IllegalArgumentException("Invalid query", ex);
    } catch (Unauthorized ex) {
      _logger.warn("Return empty result because user is unauthorized");
      return ResultSet.Builder.properties(query.getProperties())
        .build();
    } 
    _logger.trace("Received response from DS Public vAPI: {}", resultSet);
    return _resultConverter.convertResultSet(resultSet, query);
  }
  
  private boolean isQuerySupported(Query query) {
    assert query != null;
    try {
      this._providerLookup.getProviderForModels(query.getResourceModels());
      List<String> properties = QueryClauseAnalyzer.gatherPropertiesForSingleResourceModel(query);
      properties.addAll(QueryClauseAnalyzer.gatherPropertiesFromFilter(query.getFilter()));
      properties.addAll(QueryClauseAnalyzer.gatherPropertiesFromSort(query.getSortCriteria()));
      String resourceModel = QueryClauseAnalyzer.getQueryResourceModel(query);
      Set<String> qualifiedProperties = QueryClauseAnalyzer.qualifyPropertiesForResourceModel(properties, resourceModel);
      if (!qualifiedProperties.isEmpty())
        this._providerLookup.getProviderForProperties(qualifiedProperties); 
    } catch (IllegalArgumentException iae) {
      return false;
    } 
    return true;
  }
}
