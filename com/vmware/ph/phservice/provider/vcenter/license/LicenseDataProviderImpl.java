package com.vmware.ph.phservice.provider.vcenter.license;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.common.vim.internal.VimDataProviderUtil;
import com.vmware.ph.phservice.provider.common.vim.internal.VimResourceItem;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.ph.phservice.provider.vcenter.license.collector.dataretriever.AssetsSpecilizedDataRetreiverImpl;
import com.vmware.ph.phservice.provider.vcenter.license.collector.dataretriever.LicenseServiceDataRetrieverImpl;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.List;
import java.util.Map;

public class LicenseDataProviderImpl implements DataProvider {
  private static final String ASSET_QUERY_RESOURCE_MODEL = "Asset";
  
  private final LicenseClient _licenseClient;
  
  private final VcClient _vcClient;
  
  private final QuerySchema _querySchema;
  
  public LicenseDataProviderImpl(LicenseClient licenseClient, VcClient vcClient, VmodlContext vmodlContext, Class<?> vmodlClassVersion) {
    this._querySchema = createQuerySchema(vmodlContext, vmodlClassVersion);
    this._licenseClient = licenseClient;
    this._vcClient = vcClient;
  }
  
  public QuerySchema getSchema() {
    return this._querySchema;
  }
  
  public ResultSet executeQuery(Query query) {
    String queryResourceModel = query.getResourceModels().iterator().next();
    DataRetriever<? extends Object> dataRetriever = null;
    if (queryResourceModel.equals("Asset")) {
      dataRetriever = new AssetsSpecilizedDataRetreiverImpl(this._licenseClient, this._vcClient);
    } else {
      dataRetriever = new LicenseServiceDataRetrieverImpl(this._licenseClient, queryResourceModel);
    } 
    List<VimResourceItem> licenseResourceItems = VimDataProviderUtil.getVimResourceItems(query.getProperties(), dataRetriever);
    licenseResourceItems = PageUtil.pageItems(licenseResourceItems, query.getOffset(), query.getLimit());
    ResultSet resultSet = VimDataProviderUtil.convertVimResourceItemsToResultSet(licenseResourceItems, query
        .getProperties());
    return resultSet;
  }
  
  private static QuerySchema createQuerySchema(VmodlContext vmodlContext, Class<?> vmodlClassVersion) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    VmodlVersion vmodlVersion = vmodlContext.getVmodlVersionMap().getVersion(vmodlClassVersion);
    List<VmodlType> dataObjectVmodlTypes = VmodlTypeToQuerySchemaModelInfoConverter.getAllDataObjectVmodlTypesInPackage(vmodlTypeMap, "com.vmware.vim.binding.cis.license");
    Map<String, QuerySchema.ModelInfo> typeNameToModelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToClassNameModelInfos(dataObjectVmodlTypes, vmodlTypeMap, vmodlVersion);
    QuerySchema querySchema = QuerySchema.forModels(typeNameToModelInfo);
    return querySchema;
  }
}
