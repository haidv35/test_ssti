package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlUtil;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.ph.phservice.provider.common.vmomi.VmomiDataProviderUtil;
import com.vmware.vim.binding.vim.InternalServiceInstanceContent;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.vmomi.core.types.ComplexType;
import com.vmware.vim.vmomi.core.types.DataObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class InternalSingletonMoDataProvider implements DataProvider {
  private final VcClient _vcClient;
  
  private final SingletonMoReader _internalMoReader;
  
  private final DataProvider[] _conflictingDataProviders;
  
  public InternalSingletonMoDataProvider(VcClient vcClient, DataProvider... conflictingDataProviders) {
    this(vcClient, 
        SingletonMoReader.createInternalSingletonMoReader(vcClient), conflictingDataProviders);
  }
  
  InternalSingletonMoDataProvider(VcClient vcClient, SingletonMoReader internalMoReader, DataProvider... conflictingDataProviders) {
    this._internalMoReader = internalMoReader;
    this._conflictingDataProviders = conflictingDataProviders;
    this._vcClient = vcClient;
  }
  
  public QuerySchema getSchema() {
    return createSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    VmodlType vmodlType = getVmodlType(query);
    ManagedObject managedObject = this._internalMoReader.getManagedObject(vmodlType);
    URI managedObjectModelKey = VmomiDataProviderUtil.createManagedObjectModelKey(managedObject);
    List<Object> propertyValues = DataProviderUtil.getPropertyValues(managedObject, managedObjectModelKey, nonQualifiedQueryProperties);
    propertyValues = PageUtil.pageItems(propertyValues, query.getOffset(), query.getLimit());
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    resultSetBuilder.item(managedObjectModelKey, propertyValues);
    return resultSetBuilder.build();
  }
  
  private QuerySchema createSchema() {
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    VmodlVersion vmodlVersion = this._vcClient.getVmodlVersion();
    DataObjectType vmodlType = (DataObjectType)vmodlTypeMap.getVmodlType(InternalServiceInstanceContent.class);
    List<VmodlType> vmodlTypes = VmodlUtil.getManagedObjectReferenceVmodlTypesInParentTypeProperties((ComplexType)vmodlType);
    Map<String, QuerySchema.ModelInfo> internalMoSchemaModels = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(vmodlTypes, vmodlTypeMap, vmodlVersion);
    QuerySchema internalMoQuerySchema = QuerySchema.forModels(internalMoSchemaModels);
    return QuerySchemaUtil.resolveConflict(internalMoQuerySchema, this._conflictingDataProviders);
  }
  
  private VmodlType getVmodlType(Query query) {
    String moWsdlType = query.getResourceModels().iterator().next();
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    return vmodlTypeMap.getVmodlType(moWsdlType);
  }
}
