package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.adapters.vmomi.QueryPipelineDataProvider;
import com.vmware.cis.data.internal.provider.ext.search.SearchModelDescriptor;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCacheFactory;
import com.vmware.cis.data.internal.provider.util.SchemaUtil;
import com.vmware.cis.data.internal.util.QueryMarker;
import com.vmware.vim.binding.vim.dp.BatchQuerySpec;
import com.vmware.vim.binding.vim.dp.BatchResultSet;
import com.vmware.vim.binding.vim.dp.QuerySpec;
import com.vmware.vim.binding.vim.dp.ResourceModel;
import com.vmware.vim.binding.vim.dp.ResourceModelInfo;
import com.vmware.vim.binding.vim.dp.ResultSet;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VimDataProvider implements QueryPipelineDataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(VimDataProvider.class);
  
  private static final SearchModelDescriptor _vimSearch = VimSearchModel.createVimSearchModel();
  
  private final Client _vlsiClient;
  
  private final URI _serviceUri;
  
  private final QuerySchemaCache _schemaCache;
  
  private final String _schemaCacheKey;
  
  private final Callable<QuerySchema> _schemaGet;
  
  public VimDataProvider(Client vlsiClient) {
    this(vlsiClient, QuerySchemaCacheFactory.createNoOpCache(), null);
  }
  
  public VimDataProvider(Client vlsiClient, QuerySchemaCache schemaCache, String serviceTypeAndVersion) {
    assert vlsiClient != null;
    assert schemaCache != null;
    this._vlsiClient = vlsiClient;
    this._serviceUri = vlsiClient.getBinding().getEndpointUri();
    this._schemaCache = schemaCache;
    this._schemaCacheKey = "VimDataProvider:" + serviceTypeAndVersion;
    this._schemaGet = new Callable<QuerySchema>() {
        public QuerySchema call() {
          QuerySchema schema = VimDataProvider.this.getSchemaImpl();
          QuerySchema extSchema = VimDataProvider._vimSearch.addModel(schema);
          return SchemaUtil.merge(extSchema, VimDataProviderFilterableProperties.SCHEMA);
        }
        
        public String toString() {
          return "Get Schema from " + VimDataProvider.this._serviceUri;
        }
      };
  }
  
  public ResultSet executeQuery(Query query) {
    assert query != null;
    if (_vimSearch.isSearchQuery(query))
      return executeSearchQuery(query); 
    return executeSingleQuery(query);
  }
  
  public QuerySchema getSchema() {
    return this._schemaCache.get(this._schemaCacheKey, this._schemaGet);
  }
  
  public String toString() {
    return "VmomiConnection(url=" + this._serviceUri + ")";
  }
  
  private QuerySchema getSchemaImpl() {
    ResourceModel resourceModelStub = createStub(this._vlsiClient);
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(VlsiClientUtil.class
        .getClassLoader());
    try {
      _logger.trace("Requesting schema from VC data provider");
      ResourceModelInfo[] models = resourceModelStub.getSchema();
      QuerySchema schema = VimDataProviderSchemaConverter.convertSchema(models);
      _logger.trace("Received schema from VC data provider: {}", schema);
      return schema;
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
  
  private ResultSet executeSearchQuery(Query query) {
    assert query != null;
    Collection<Query> childQueries = _vimSearch.toChildQueries(query);
    Collection<ResultSet> childResults = executeQueryBatch(childQueries);
    return _vimSearch.toAggregatedResult(childResults, query);
  }
  
  private ResultSet executeSingleQuery(Query query) {
    assert query != null;
    QuerySpec querySpec = VimDataProviderQueryConverter.convertQuery(query);
    if (querySpec == null)
      return ResultSet.Builder.properties(query.getProperties())
        .totalCount(query.getWithTotalCount() ? Integer.valueOf(0) : null)
        .build(); 
    ResultSet vmodlResult = query(querySpec);
    return VimDataProviderResultSetConverter.convertResultSet(vmodlResult, query);
  }
  
  public Collection<ResultSet> executeQueryBatch(Collection<Query> queryBatch) {
    assert queryBatch != null;
    QuerySpec[] querySpecs = VimDataProviderQueryConverter.convertQueries(queryBatch);
    ResultSet[] vmodlResults = queryBatch(querySpecs);
    Collection<ResultSet> results = VimDataProviderResultSetConverter.convertResultSets(vmodlResults, queryBatch);
    return results;
  }
  
  private ResultSet query(QuerySpec query) {
    try {
      return queryImpl(query);
    } catch (InvalidArgument ex) {
      throw new IllegalArgumentException("Invalid query", ex);
    } 
  }
  
  private ResultSet queryImpl(QuerySpec query) {
    ResultSet result;
    ResourceModel resourceModelStub = createStub(this._vlsiClient);
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(VlsiClientUtil.class
        .getClassLoader());
    try {
      _logger.trace("Sending query {} to VC data provider at {}\n{}", new Object[] { QueryMarker.getQueryId(), this._serviceUri, query });
      result = resourceModelStub.query(query);
      _logger.trace("Received response from VC data provider:\n{}", result);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
    return result;
  }
  
  private ResultSet[] queryBatch(QuerySpec[] query) {
    try {
      return queryBatchImpl(query);
    } catch (InvalidArgument ex) {
      throw new IllegalArgumentException("Invalid query in query batch", ex);
    } 
  }
  
  private ResultSet[] queryBatchImpl(QuerySpec[] queryBatch) {
    ResultSet[] results;
    ResourceModel resourceModelStub = createStub(this._vlsiClient);
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(VlsiClientUtil.class
        .getClassLoader());
    try {
      if (_logger.isTraceEnabled())
        _logger.trace("Sending query batch '{}' to VC data provider at {} :\n{}", new Object[] { QueryMarker.getQueryId(), this._serviceUri, 
              
              Arrays.toString((Object[])queryBatch) }); 
      BatchQuerySpec batchSpec = new BatchQuerySpec();
      batchSpec.setQuerySpecs(queryBatch);
      BatchResultSet batchResult = resourceModelStub.queryBatch(batchSpec);
      results = batchResult.getResultSets();
      if (_logger.isTraceEnabled())
        _logger.trace("Received response batch from VC data provider at {} :\n{}", this._serviceUri, 

            
            Arrays.toString((Object[])results)); 
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
    return results;
  }
  
  private static ResourceModel createStub(Client vlsiClient) {
    ManagedObjectReference ref = new ManagedObjectReference("DataProviderResourceModel", "ResourceModel", null);
    ResourceModel stub = VlsiClientUtil.<ResourceModel>createStub(vlsiClient, ResourceModel.class, ref);
    setOpId((ManagedObject)stub);
    return stub;
  }
  
  private static void setOpId(ManagedObject mo) {
    String opId = QueryMarker.getQueryId();
    if (opId == null)
      return; 
    getRequestContext(mo).put("operationID", opId);
  }
  
  private static RequestContextImpl getRequestContext(ManagedObject mo) {
    Stub stub = (Stub)mo;
    RequestContextImpl rc = (RequestContextImpl)stub._getRequestContext();
    if (rc == null) {
      rc = new RequestContextImpl();
      stub._setRequestContext((RequestContext)rc);
    } 
    return rc;
  }
}
