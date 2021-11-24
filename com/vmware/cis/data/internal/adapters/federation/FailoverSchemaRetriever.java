package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.util.TaskExecutor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FailoverSchemaRetriever {
  private static final Logger _logger = LoggerFactory.getLogger(FailoverSchemaRetriever.class);
  
  private final TaskExecutor _executor;
  
  private final QuerySchemaCache _schemaCache;
  
  public FailoverSchemaRetriever(TaskExecutor executor, QuerySchemaCache schemaCache) {
    assert executor != null;
    assert schemaCache != null;
    this._executor = executor;
    this._schemaCache = schemaCache;
  }
  
  public Map<String, QuerySchema> getSchemasByInstanceUuid(Collection<FederationConnection.FederatedProviderInfo> providerInfos) {
    assert providerInfos != null;
    List<Callable<PartialQuerySchema>> fetchSchemaTasks = new ArrayList<>();
    Map<String, List<FederationConnection.FederatedProviderInfo>> infosByServiceTypeAndVersion = groupByServiceTypeAndVersion(providerInfos);
    for (String serviceTypeAndVersion : infosByServiceTypeAndVersion.keySet()) {
      List<FederationConnection.FederatedProviderInfo> providerInfoGroup = infosByServiceTypeAndVersion.get(serviceTypeAndVersion);
      fetchSchemaTasks.add(createFetchSchemaTask(serviceTypeAndVersion, providerInfoGroup));
    } 
    List<PartialQuerySchema> partialSchemas = this._executor.invokeTasks(fetchSchemaTasks);
    Map<String, QuerySchema> schemaByInstanceUuid = new HashMap<>(partialSchemas.size());
    for (PartialQuerySchema partialSchema : partialSchemas) {
      for (FederationConnection.FederatedProviderInfo providerInfo : partialSchema.providerInfos)
        schemaByInstanceUuid.put(providerInfo.getServiceInstanceUuid(), partialSchema.schema); 
    } 
    return schemaByInstanceUuid;
  }
  
  private Callable<PartialQuerySchema> createFetchSchemaTask(final String serviceTypeAndVersion, final List<FederationConnection.FederatedProviderInfo> providerInfos) {
    assert serviceTypeAndVersion != null;
    assert !providerInfos.isEmpty();
    final Callable<QuerySchema> schemaGet = createGetSchemaFromCacheTask(providerInfos);
    return new Callable<PartialQuerySchema>() {
        public FailoverSchemaRetriever.PartialQuerySchema call() throws Exception {
          QuerySchema schema = FailoverSchemaRetriever.this._schemaCache.get(serviceTypeAndVersion, schemaGet);
          return new FailoverSchemaRetriever.PartialQuerySchema(schema, providerInfos);
        }
        
        public String toString() {
          return "Get schema executed on " + providerInfos;
        }
      };
  }
  
  private static Callable<QuerySchema> createGetSchemaFromCacheTask(final List<FederationConnection.FederatedProviderInfo> providerInfos) {
    assert !providerInfos.isEmpty();
    return new Callable<QuerySchema>() {
        public QuerySchema call() {
          int lastIndex = providerInfos.size() - 1;
          for (int i = 0; i < lastIndex; i++) {
            FederationConnection.FederatedProviderInfo current = providerInfos.get(i);
            try {
              return FailoverSchemaRetriever.getSchema(current);
            } catch (RuntimeException e) {
              FailoverSchemaRetriever._logger.warn("Failed to get schema from {}", current, e);
            } 
          } 
          FederationConnection.FederatedProviderInfo last = providerInfos.get(lastIndex);
          return FailoverSchemaRetriever.getSchema(last);
        }
      };
  }
  
  private static QuerySchema getSchema(FederationConnection.FederatedProviderInfo providerInfo) {
    cancelIfInterrupted();
    _logger.debug("Get schema from {}", providerInfo);
    QuerySchema schema = providerInfo.getDataProvider().getSchema();
    _logger.trace("Received schema from {}: {}", providerInfo, schema);
    return schema;
  }
  
  private static Map<String, List<FederationConnection.FederatedProviderInfo>> groupByServiceTypeAndVersion(Collection<FederationConnection.FederatedProviderInfo> providerInfos) {
    assert providerInfos != null;
    Map<String, List<FederationConnection.FederatedProviderInfo>> infosByServiceTypeAndVersion = new HashMap<>();
    for (FederationConnection.FederatedProviderInfo providerInfo : providerInfos) {
      String serviceTypeAndVersion = providerInfo.getServiceTypeAndVersion();
      List<FederationConnection.FederatedProviderInfo> providerInfoGroup = infosByServiceTypeAndVersion.get(serviceTypeAndVersion);
      if (providerInfoGroup == null) {
        providerInfoGroup = new ArrayList<>();
        infosByServiceTypeAndVersion.put(serviceTypeAndVersion, providerInfoGroup);
      } 
      providerInfoGroup.add(providerInfo);
    } 
    return infosByServiceTypeAndVersion;
  }
  
  private static void cancelIfInterrupted() {
    if (Thread.currentThread().isInterrupted())
      throw new CancellationException(String.format("Call to %s was cancelled.", new Object[] { FailoverSchemaRetriever.class
              .getSimpleName() })); 
  }
  
  private static final class PartialQuerySchema {
    public final QuerySchema schema;
    
    public final List<FederationConnection.FederatedProviderInfo> providerInfos;
    
    public PartialQuerySchema(QuerySchema schema, List<FederationConnection.FederatedProviderInfo> providerInfos) {
      this.schema = schema;
      this.providerInfos = providerInfos;
    }
  }
}
