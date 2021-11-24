package com.vmware.cis.data.internal.adapters.vcenter;

import com.google.common.collect.ImmutableList;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.adapters.vmomi.model.ClusterExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.DatacenterExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.DatastoreExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.DsClusterExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.DvPortgroupExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.DvsExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.FolderExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.HostExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.HostProfileExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.NetworkExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.OpaqueNetworkExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.ResourcePoolExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.VAppExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.VmExtendedDataModel;
import com.vmware.cis.data.internal.adapters.vmomi.model.VmwareDvsExtendedDataModel;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.internal.provider.ext.ConnectionSupplier;
import com.vmware.cis.data.internal.provider.ext.CustomPropertyRepositories;
import com.vmware.cis.data.internal.provider.ext.ExtensionConnectionSupplier;
import com.vmware.cis.data.internal.provider.ext.aggregated.AggregatedModelLookup;
import com.vmware.cis.data.internal.provider.ext.clientside.filter.ClientSideFiltering;
import com.vmware.cis.data.internal.provider.ext.relationship.invert.RelationshipInversionRepository;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public final class ExtensionDataProviderConnector implements DataProviderConnector {
  private final DataProviderConnector _providerConnector;
  
  private final CustomPropertyRepositories _customPropertyRepositories;
  
  private final AggregatedModelLookup _aggregatedModelLookup;
  
  private final ExecutorService _executor;
  
  private final RelationshipInversionRepository _relationshipInversions;
  
  private final Map<String, QuerySchema.PropertyInfo> _clientSideProps;
  
  private final QuerySchemaCache _schemaCache;
  
  private final String _schemaCacheKey;
  
  private static final Collection<Class<?>> VMOMI_MODELS = (Collection<Class<?>>)ImmutableList.builder()
    .add(ClusterExtendedDataModel.class)
    .add(DatacenterExtendedDataModel.class)
    .add(DatastoreExtendedDataModel.class)
    .add(DsClusterExtendedDataModel.class)
    .add(DvPortgroupExtendedDataModel.class)
    .add(DvsExtendedDataModel.class)
    .add(FolderExtendedDataModel.class)
    .add(HostExtendedDataModel.class)
    .add(HostProfileExtendedDataModel.class)
    .add(NetworkExtendedDataModel.class)
    .add(OpaqueNetworkExtendedDataModel.class)
    .add(ResourcePoolExtendedDataModel.class)
    .add(VAppExtendedDataModel.class)
    .add(VmExtendedDataModel.class)
    .add(VmwareDvsExtendedDataModel.class)
    .build();
  
  private static final CustomPropertyRepositories VMOMI_CUSTOM_PROP_REPOSITORIES = new CustomPropertyRepositories(VMOMI_MODELS);
  
  public static DataProviderConnector extendVmomi(DataProviderConnector connector, AggregatedModelLookup aggregatedModelLookup, ExecutorService executor, RelationshipInversionRepository relationshipInversions, QuerySchemaCache schemaCache, String schemaCacheKey) {
    return new ExtensionDataProviderConnector(connector, VMOMI_CUSTOM_PROP_REPOSITORIES, aggregatedModelLookup, executor, relationshipInversions, ClientSideFiltering.VMOMI_CLIENT_SIDE_PROPS, schemaCache, schemaCacheKey);
  }
  
  private ExtensionDataProviderConnector(DataProviderConnector providerConnector, CustomPropertyRepositories customPropertyRepositories, AggregatedModelLookup aggregatedModelLookup, ExecutorService executor, RelationshipInversionRepository relationshipInversions, Map<String, QuerySchema.PropertyInfo> clientSideProps, QuerySchemaCache schemaCache, String schemaCacheKey) {
    assert providerConnector != null;
    this._providerConnector = providerConnector;
    this._customPropertyRepositories = customPropertyRepositories;
    this._aggregatedModelLookup = aggregatedModelLookup;
    this._executor = executor;
    this._relationshipInversions = relationshipInversions;
    this._clientSideProps = clientSideProps;
    this._schemaCache = schemaCache;
    this._schemaCacheKey = schemaCacheKey;
  }
  
  public DataProviderConnection getConnection(AuthenticationTokenSource authn) {
    final DataProviderConnection connection = this._providerConnector.getConnection(authn);
    return new DataProviderConnection() {
        public void close() throws Exception {
          connection.close();
        }
        
        public DataProvider getDataProvider() {
          DataProvider provider = connection.getDataProvider();
          ConnectionSupplier supplier = new ExtensionConnectionSupplier(provider, ExtensionDataProviderConnector.this._customPropertyRepositories, ExtensionDataProviderConnector.this._aggregatedModelLookup, ExtensionDataProviderConnector.this._executor, ExtensionDataProviderConnector.this._relationshipInversions, ExtensionDataProviderConnector.this._clientSideProps, ExtensionDataProviderConnector.this._schemaCache, ExtensionDataProviderConnector.this._schemaCacheKey);
          return supplier.getConnection();
        }
      };
  }
  
  public String toString() {
    return getClass().getSimpleName() + "(" + this._providerConnector.toString() + ")";
  }
}
