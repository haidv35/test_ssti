package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.ext.search.SearchModelDescriptor;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCacheFactory;
import com.vmware.cis.data.internal.util.QueryMarker;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.binding.cis.data.provider.BatchQuerySpec;
import com.vmware.vim.binding.cis.data.provider.BatchResultSet;
import com.vmware.vim.binding.cis.data.provider.QuerySpec;
import com.vmware.vim.binding.cis.data.provider.ResourceModel;
import com.vmware.vim.binding.cis.data.provider.ResultSet;
import com.vmware.vim.binding.cis.data.provider.schema.ResourceModelInfo;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VmomiDataProviderConnection implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(VmomiDataProviderConnection.class);
  
  private static final SearchModelDescriptor _vimSearch = VimSearchModel.createVimSearchModel();
  
  private final Client _vlsiClient;
  
  private final URI _serviceUri;
  
  private final QuerySchemaCache _schemaCache;
  
  private final String _schemaCacheKey;
  
  private final Callable<QuerySchema> _schemaGet;
  
  private final AtomicInteger _isBatchSupported = new AtomicInteger(-1);
  
  public VmomiDataProviderConnection(Client vlsiClient) {
    this(vlsiClient, QuerySchemaCacheFactory.createNoOpCache(), "");
  }
  
  public VmomiDataProviderConnection(Client vlsiClient, QuerySchemaCache schemaCache, String serviceTypeAndVersion) {
    assert vlsiClient != null;
    assert schemaCache != null;
    assert serviceTypeAndVersion != null;
    this._vlsiClient = vlsiClient;
    this._serviceUri = vlsiClient.getBinding().getEndpointUri();
    this._schemaCache = schemaCache;
    this._schemaCacheKey = "VmomiDataProvider:" + serviceTypeAndVersion;
    this._schemaGet = new Callable<QuerySchema>() {
        public QuerySchema call() {
          QuerySchema schema = VmomiDataProviderConnection.this.getSchemaImpl();
          QuerySchema extSchema = VmomiDataProviderConnection._vimSearch.addModel(schema);
          return extSchema;
        }
        
        public String toString() {
          return "Get Schema from " + VmomiDataProviderConnection.this._serviceUri;
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
      QuerySchema schema = VmomiDataProviderSchemaConverter.convertSchema(models);
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
    QuerySpec querySpec = VmomiDataProviderQueryConverter.convertQuery(query);
    if (querySpec == null)
      return ResultSet.Builder.properties(query.getProperties())
        .totalCount(query.getWithTotalCount() ? Integer.valueOf(0) : null)
        .build(); 
    ResultSet vmodlResult = query(querySpec);
    return VmomiDataProviderResultSetConverter.convertResultSet(vmodlResult, query);
  }
  
  private Collection<ResultSet> executeQueryBatch(Collection<Query> queryBatch) {
    assert queryBatch != null;
    if (isBatchSupported())
      return executeQueryBatchNative(queryBatch); 
    return executeQueryBatchBackCompat(queryBatch);
  }
  
  private Collection<ResultSet> executeQueryBatchBackCompat(Collection<Query> queryBatch) {
    assert queryBatch != null;
    QueryIdLogConfigurator.QueryCounter queryCounter = QueryIdLogConfigurator.newQueryCounter("");
    long beginTime = System.currentTimeMillis();
    Collection<ResultSet> resultBatch = new ArrayList<>(queryBatch.size());
    for (Query query : queryBatch) {
      ResultSet result;
      if (Thread.currentThread().isInterrupted()) {
        long totalTime = System.currentTimeMillis() - beginTime;
        throw new RuntimeException(String.format("Interrupted vmomi query batch after %d ms execution. Executed %d out of %d queries.", new Object[] { Long.valueOf(totalTime), 
                Integer.valueOf(resultBatch.size()), 
                Integer.valueOf(queryBatch.size()) }));
      } 
      QueryIdLogConfigurator logCfg = queryCounter.onQueryStart();
      try {
        result = executeQuery(query);
      } finally {
        logCfg.close();
      } 
      resultBatch.add(result);
    } 
    return resultBatch;
  }
  
  private Collection<ResultSet> executeQueryBatchNative(Collection<Query> queryBatch) {
    assert queryBatch != null;
    QuerySpec[] querySpecs = VmomiDataProviderQueryConverter.convertQueries(queryBatch);
    ResultSet[] vmodlResults = queryBatch(querySpecs);
    Collection<ResultSet> results = VmomiDataProviderResultSetConverter.convertResultSets(vmodlResults, queryBatch);
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
  
  private boolean isBatchSupported() {
    int batchSupported = this._isBatchSupported.get();
    if (batchSupported >= 0)
      return (batchSupported > 0); 
    QuerySchema schema = getSchema();
    QuerySchema.ModelInfo hsInfo = schema.getModels().get("HostSystem");
    QuerySchema.PropertyInfo hostMemoryUsage = null;
    if (hsInfo != null)
      hostMemoryUsage = hsInfo.getProperties().get("memoryUsage"); 
    batchSupported = (hostMemoryUsage != null) ? 1 : 0;
    this._isBatchSupported.compareAndSet(-1, batchSupported);
    return (batchSupported > 0);
  }
  
  private static ResourceModel createStub(Client vlsiClient) {
    ManagedObjectReference ref = new ManagedObjectReference("DPResourceModel", "ResourceModel", null);
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
