package com.vmware.cis.data.internal.provider.ext;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelLookup;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelProviderConnection;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedSchemaProviderConnection;
import com.vmware.cis.data.internal.provider.ext.alias.AliasPropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.clientside.filter.ClientSideFiltering;
import com.vmware.cis.data.internal.provider.ext.derived.DerivedPropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.derived.DerivedPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.derived.NativeSchemaAwareDerivedPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.predicate.NativeSchemaAwarePredicatePropertyRepository;
import com.vmware.cis.data.internal.provider.ext.predicate.PredicatePropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.predicate.PredicatePropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.NativeSchemaAwareRelatedPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedLengthProviderConnection;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyProviderConnection;
import com.vmware.cis.data.internal.provider.ext.relationship.RelatedPropertyRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.invert.NativeSchemaAwareRelationshipInversionRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.invert.RelationshipInversionRepository;
import com.vmware.cis.data.internal.provider.ext.relationship.invert.RelationshipInvertor;
import com.vmware.cis.data.internal.provider.ext.relationship.invert.RelationshipInvertorSchemaDataProvider;
import com.vmware.cis.data.internal.provider.profiler.ProfiledDataProvider;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCacheDecorator;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public final class ExtensionConnectionSupplier implements ConnectionSupplier {
  private final DataProvider _extConnection;
  
  public ExtensionConnectionSupplier(DataProvider connection, CustomPropertyRepositories customPropertyRepositories, AggregatedModelLookup aggregatedModelLookup, ExecutorService executor, RelationshipInversionRepository relationshipInversions, Map<String, QuerySchema.PropertyInfo> clientSideProps, QuerySchemaCache schemaCache, String schemaCacheKey) {
    assert connection != null;
    this._extConnection = buildExtConnection(connection, this, customPropertyRepositories, aggregatedModelLookup, executor, relationshipInversions, clientSideProps, schemaCache, schemaCacheKey);
  }
  
  public DataProvider getConnection() {
    return this._extConnection;
  }
  
  private static DataProvider buildExtConnection(DataProvider connection, ConnectionSupplier extConnectionSupplier, CustomPropertyRepositories customPropertyRepositories, AggregatedModelLookup aggregatedModelLookup, ExecutorService executor, RelationshipInversionRepository relationshipInversions, Map<String, QuerySchema.PropertyInfo> clientSideProps, QuerySchemaCache schemaCache, String schemaCacheKey) {
    String nativeCacheKey = "NativeProperties:" + schemaCacheKey;
    DataProvider nativeConnection = cacheConnection(connection, nativeCacheKey, schemaCache);
    QuerySchema nativeSchema = nativeConnection.getSchema();
    return relationshipInvertorConnection(
        aggregatedConnection(
          clientSideFilteringConnection(
            predicateConnection(
              derivedConnection(
                aliasConnection(
                  relatedConnection(
                    relationshipInversionSchemaConnection(






                      
                      aggregatedSchemaConnection(nativeConnection, aggregatedModelLookup), relationshipInversions), extConnectionSupplier, customPropertyRepositories, nativeSchema), customPropertyRepositories), customPropertyRepositories, nativeSchema), customPropertyRepositories, nativeSchema), clientSideProps, schemaCache, schemaCacheKey), aggregatedModelLookup, executor, schemaCache, schemaCacheKey), relationshipInversions, nativeSchema);
  }
  
  private static DataProvider aliasConnection(DataProvider conn, CustomPropertyRepositories repositories) {
    if (repositories == null)
      return conn; 
    return profileProvider(new AliasPropertyProviderConnection(conn, repositories
          .getAliasPropertyRepository()));
  }
  
  private static DataProvider relatedConnection(DataProvider conn, ConnectionSupplier connSupplier, CustomPropertyRepositories repositories, QuerySchema nativeSchema) {
    if (repositories == null)
      return conn; 
    RelatedPropertyRepository relatedRepository = repositories.getRelatedPropertyRepository();
    NativeSchemaAwareRelatedPropertyRepository schemaAwareRelatedRepository = new NativeSchemaAwareRelatedPropertyRepository(relatedRepository, nativeSchema);
    RelatedPropertyProviderConnection relatedConnection = new RelatedPropertyProviderConnection(conn, connSupplier, schemaAwareRelatedRepository);
    RelatedLengthProviderConnection relatedLengthConnection = new RelatedLengthProviderConnection(relatedConnection, schemaAwareRelatedRepository);
    return profileProvider(relatedLengthConnection);
  }
  
  private static DataProvider derivedConnection(DataProvider conn, CustomPropertyRepositories repositories, QuerySchema nativeSchema) {
    if (repositories == null)
      return conn; 
    DerivedPropertyRepository derivedRepository = repositories.getDerivedPropertyRepository();
    NativeSchemaAwareDerivedPropertyRepository schemaAwareDerivedRepository = new NativeSchemaAwareDerivedPropertyRepository(derivedRepository, nativeSchema);
    return profileProvider(new DerivedPropertyProviderConnection(conn, schemaAwareDerivedRepository));
  }
  
  private static DataProvider predicateConnection(DataProvider conn, CustomPropertyRepositories repositories, QuerySchema nativeSchema) {
    if (repositories == null)
      return conn; 
    PredicatePropertyRepository predicateRepository = repositories.getPredicatePropertyRepository();
    NativeSchemaAwarePredicatePropertyRepository schemaAwarePredicateRepository = new NativeSchemaAwarePredicatePropertyRepository(predicateRepository, nativeSchema);
    return profileProvider(new PredicatePropertyProviderConnection(conn, schemaAwarePredicateRepository));
  }
  
  private static DataProvider aggregatedConnection(DataProvider conn, AggregatedModelLookup aggregatedModelLookup, ExecutorService aggregatedModelExecutor, QuerySchemaCache schemaCache, String schemaCacheKey) {
    if (aggregatedModelLookup == null || aggregatedModelExecutor == null)
      return conn; 
    String aggregatedCacheKey = "NonAggregatedProperties:" + schemaCacheKey;
    DataProvider cached = cacheConnection(conn, aggregatedCacheKey, schemaCache);
    return profileProvider(new AggregatedModelProviderConnection(cached, aggregatedModelLookup, aggregatedModelExecutor));
  }
  
  private static DataProvider relationshipInvertorConnection(DataProvider conn, RelationshipInversionRepository relationshipInversions, QuerySchema nativeSchema) {
    if (relationshipInversions == null)
      return conn; 
    NativeSchemaAwareRelationshipInversionRepository schemaAwareRelationshipInversions = new NativeSchemaAwareRelationshipInversionRepository(relationshipInversions, nativeSchema);
    return profileProvider(new RelationshipInvertor(conn, schemaAwareRelationshipInversions));
  }
  
  private static DataProvider aggregatedSchemaConnection(DataProvider conn, AggregatedModelLookup aggregatedModelLookup) {
    if (aggregatedModelLookup == null)
      return conn; 
    return new AggregatedSchemaProviderConnection(conn, aggregatedModelLookup);
  }
  
  private static DataProvider relationshipInversionSchemaConnection(DataProvider conn, RelationshipInversionRepository relationshipInversions) {
    if (relationshipInversions == null)
      return conn; 
    return new RelationshipInvertorSchemaDataProvider(conn, relationshipInversions);
  }
  
  private static DataProvider clientSideFilteringConnection(DataProvider conn, Map<String, QuerySchema.PropertyInfo> clientSideProps, QuerySchemaCache schemaCache, String schemaCacheKey) {
    if (clientSideProps == null)
      return conn; 
    String clientSideCacheKey = "CustomProperties:" + schemaCacheKey;
    DataProvider cached = cacheConnection(conn, clientSideCacheKey, schemaCache);
    return new ClientSideFiltering(cached, clientSideProps);
  }
  
  private static DataProvider profileProvider(DataProvider dataProvider) {
    return ProfiledDataProvider.create(dataProvider);
  }
  
  private static DataProvider cacheConnection(DataProvider conn, String schemaCacheKey, QuerySchemaCache schemaCache) {
    if (schemaCache == null || schemaCacheKey == null)
      return conn; 
    return QuerySchemaCacheDecorator.cacheProvider(conn, schemaCacheKey, schemaCache);
  }
}
