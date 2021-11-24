package com.vmware.ph.phservice.provider.fcd.collector;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vim.internal.VimDataProviderUtil;
import com.vmware.ph.phservice.provider.common.vim.internal.VimResourceItem;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.ph.phservice.provider.fcd.collector.customobject.CustomVStorageObjectSnapshotInfo;
import com.vmware.ph.phservice.provider.fcd.collector.dataretriever.DataRetrieverFactory;
import com.vmware.ph.phservice.provider.fcd.collector.schema.FcdCustomObjectSchemaGenerator;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FcdDataProviderImpl implements DataProvider {
  private final VcClient _vcClient;
  
  private final QuerySchema _supportedQuerySchema;
  
  public FcdDataProviderImpl(VcClient vcClient) {
    this._vcClient = vcClient;
    this._supportedQuerySchema = getSupportedSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    String resourceModel = query.getResourceModels().iterator().next();
    List<String> queryProperties = query.getProperties();
    List<String> supportedQueryProperties = QuerySchemaUtil.getSupportedQueryPropertyNames(resourceModel, queryProperties, this._supportedQuerySchema);
    List<String> supportedNonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(supportedQueryProperties);
    DataRetriever<?> dataRetriever = DataRetrieverFactory.getDataRetreiver(this._vcClient, query);
    List<Object> fcdData = dataRetriever.retrieveData();
    List<VimResourceItem> fcdResourceItems = VimDataProviderUtil.getVimResourceItems(fcdData, supportedNonQualifiedQueryProperties, dataRetriever);
    ResultSet resultSet = VimDataProviderUtil.convertVimResourceItemsToResultSet(fcdResourceItems, queryProperties, supportedNonQualifiedQueryProperties);
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    return this._supportedQuerySchema;
  }
  
  private QuerySchema getSupportedSchema() {
    Map<String, QuerySchema.ModelInfo> models = new LinkedHashMap<>();
    VmodlTypeMap vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
    VmodlVersion vmodlVersion = this._vcClient.getVmodlVersion();
    List<VmodlType> dataObjectVmodlTypes = VmodlTypeToQuerySchemaModelInfoConverter.getAllDataObjectVmodlTypesInPackage(vmodlTypeMap, "com.vmware.vim.binding.vim.vslm");
    models.putAll(VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToClassNameModelInfos(dataObjectVmodlTypes, vmodlTypeMap, vmodlVersion));
    models.putAll(getCustomObjectsSchema());
    return QuerySchema.forModels(models);
  }
  
  private Map<String, QuerySchema.ModelInfo> getCustomObjectsSchema() {
    Set<Class<?>> customEntities = new HashSet<>();
    customEntities.add(CustomVStorageObjectSnapshotInfo.class);
    FcdCustomObjectSchemaGenerator customObjectSchemaGenerator = new FcdCustomObjectSchemaGenerator(this._vcClient.getVmodlContext().getVmodlTypeMap(), this._vcClient.getVmodlVersion(), customEntities);
    return customObjectSchemaGenerator.getQuerySchemaModel();
  }
}
