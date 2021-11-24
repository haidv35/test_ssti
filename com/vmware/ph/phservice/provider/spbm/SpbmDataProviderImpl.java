package com.vmware.ph.phservice.provider.spbm;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vim.internal.VimDataProviderUtil;
import com.vmware.ph.phservice.provider.common.vim.internal.VimResourceItem;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.ph.phservice.provider.spbm.client.XServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.ph.phservice.provider.spbm.collector.customobject.sms.CustomFaultDomainInfo;
import com.vmware.ph.phservice.provider.spbm.collector.dataretriever.DataRetrieverFactory;
import com.vmware.ph.phservice.provider.spbm.collector.schema.SpbmCustomObjectSchemaGenerator;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpbmDataProviderImpl implements DataProvider {
  private final SpbmCollectorContext _spbmCollectorContext;
  
  private final QuerySchema _supportedQuerySchema;
  
  public SpbmDataProviderImpl(SpbmCollectorContext spbmCollectorContext) {
    this._spbmCollectorContext = spbmCollectorContext;
    this._supportedQuerySchema = getSupportedSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    String resourceModel = query.getResourceModels().iterator().next();
    List<String> queryProperties = query.getProperties();
    List<String> supportedQueryProperties = QuerySchemaUtil.getSupportedQueryPropertyNames(resourceModel, queryProperties, this._supportedQuerySchema);
    List<String> supportedNonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(supportedQueryProperties);
    DataRetriever dataRetriever = DataRetrieverFactory.getDataRetreiver(this._spbmCollectorContext, query);
    List<Object> spbmData = dataRetriever.retrieveData();
    List<VimResourceItem> spbmResourceItems = VimDataProviderUtil.getVimResourceItems(spbmData, supportedNonQualifiedQueryProperties, dataRetriever);
    if (spbmResourceItems.size() > query.getLimit())
      spbmResourceItems = PageUtil.pageItems(spbmResourceItems, query.getOffset(), query.getLimit()); 
    ResultSet resultSet = VimDataProviderUtil.convertVimResourceItemsToResultSet(spbmResourceItems, queryProperties, supportedNonQualifiedQueryProperties);
    return resultSet;
  }
  
  public QuerySchema getSchema() {
    return this._supportedQuerySchema;
  }
  
  private QuerySchema getSupportedSchema() {
    Map<String, QuerySchema.ModelInfo> models = new LinkedHashMap<>();
    PbmServiceClient pbmServiceClient = this._spbmCollectorContext.getPbmServiceClient();
    models.putAll(
        getVmodlQuerySchemaModels(pbmServiceClient, "com.vmware.vim.binding.pbm"));
    models.putAll(getPbmCustomObjectsSchema(pbmServiceClient));
    SmsServiceClient smsServiceClient = this._spbmCollectorContext.getSmsServiceClient();
    models.putAll(
        getVmodlQuerySchemaModels(smsServiceClient, "com.vmware.vim.binding.sms"));
    models.putAll(getSmsCustomObjectsSchema(smsServiceClient));
    return QuerySchema.forModels(models);
  }
  
  private Map<String, QuerySchema.ModelInfo> getVmodlQuerySchemaModels(XServiceClient xServiceClient, String packageName) {
    Map<String, QuerySchema.ModelInfo> models = new LinkedHashMap<>();
    VmodlTypeMap vmodlTypeMap = xServiceClient.getVmodlContext().getVmodlTypeMap();
    VmodlVersion vmodlVersion = xServiceClient.getVmodlVersion();
    List<VmodlType> dataObjecVmodlTypes = VmodlTypeToQuerySchemaModelInfoConverter.getAllDataObjectVmodlTypesInPackage(vmodlTypeMap, packageName);
    models.putAll(
        VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(dataObjecVmodlTypes, vmodlTypeMap, vmodlVersion));
    return models;
  }
  
  private Map<String, QuerySchema.ModelInfo> getPbmCustomObjectsSchema(PbmServiceClient pbmServiceClient) {
    Map<String, QuerySchema.ModelInfo> models = new LinkedHashMap<>();
    VmodlTypeMap vmodlTypeMap = pbmServiceClient.getVmodlContext().getVmodlTypeMap();
    VmodlVersion vmodlVersion = pbmServiceClient.getVmodlVersion();
    List<VmodlType> dataObjectVmodlTypesRefferedByClassName = VmodlTypeToQuerySchemaModelInfoConverter.getDataObjectVmodlTypesForVmodlClasses(vmodlTypeMap, DataRetrieverFactory.QUERY_SCHEMA_PBM_CLASSES_REFFERED_BY_CLASS_NAME);
    models.putAll(
        VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToClassNameModelInfos(dataObjectVmodlTypesRefferedByClassName, vmodlTypeMap, vmodlVersion));
    return models;
  }
  
  private Map<String, QuerySchema.ModelInfo> getSmsCustomObjectsSchema(SmsServiceClient smsServiceClient) {
    Set<Class<?>> customEntities = new HashSet<>();
    customEntities.add(CustomFaultDomainInfo.class);
    return getCustomObjectSchema(smsServiceClient, customEntities);
  }
  
  private Map<String, QuerySchema.ModelInfo> getCustomObjectSchema(XServiceClient xServiceClient, Set<Class<?>> schemaClasses) {
    SpbmCustomObjectSchemaGenerator customObjectSchemaGenerator = new SpbmCustomObjectSchemaGenerator(xServiceClient.getVmodlContext().getVmodlTypeMap(), xServiceClient.getVmodlVersion(), schemaClasses);
    return customObjectSchemaGenerator.getQuerySchemaModel();
  }
}
