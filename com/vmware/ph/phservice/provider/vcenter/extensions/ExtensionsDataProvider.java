package com.vmware.ph.phservice.provider.vcenter.extensions;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vim.Extension;
import com.vmware.vim.binding.vim.ExtensionManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExtensionsDataProvider implements DataProvider {
  private static final String EXTENSION_VMODL_TYPE = "Extension";
  
  private final VcClient _vcClient;
  
  private ExtensionManager _extensionManager;
  
  public ExtensionsDataProvider(VcClient vcClient) {
    this._vcClient = vcClient;
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._vcClient);
  }
  
  public ResultSet executeQuery(Query query) {
    List<String> queryProperties = query.getProperties();
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    List<Extension> extensions = getExtensions(query.getOffset(), query.getLimit());
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    for (Extension extension : extensions) {
      URI extensionModelKey = DataProviderUtil.createModelKey(Extension.class, extension.getKey());
      List<Object> propertyValues = DataProviderUtil.getPropertyValues(extension, extensionModelKey, nonQualifiedQueryProperties);
      resultSetBuilder.item(extensionModelKey, propertyValues);
    } 
    return resultSetBuilder.build();
  }
  
  private List<Extension> getExtensions(int offset, int limit) {
    ExtensionManager extensionManager = getExtensionManager();
    if (extensionManager == null)
      return Collections.emptyList(); 
    List<Extension> extensionList = retrieveExtensionList(extensionManager);
    return PageUtil.pageItems(extensionList, offset, limit);
  }
  
  private ExtensionManager getExtensionManager() {
    if (this._extensionManager == null) {
      ManagedObjectReference extensionManagerMoRef = this._vcClient.getServiceInstanceContent().getExtensionManager();
      this._extensionManager = this._vcClient.<ExtensionManager>createMo(extensionManagerMoRef);
    } 
    return this._extensionManager;
  }
  
  private static List<Extension> retrieveExtensionList(ExtensionManager extensionManager) {
    Extension[] extensions = extensionManager.getExtensionList();
    return (extensions != null) ? Arrays.<Extension>asList(extensions) : new ArrayList<>();
  }
  
  private static QuerySchema createQuerySchema(VcClient vcClient) {
    VmodlTypeMap vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    VmodlType extensionVmodlType = vmodlTypeMap.getVmodlType("Extension");
    VmodlVersion vmodlVersion = vcClient.getVmodlVersion();
    Map<String, QuerySchema.ModelInfo> extensionModels = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlTypesToWsdlNameModelInfos(
        Collections.singletonList(extensionVmodlType), vmodlTypeMap, vmodlVersion);
    return QuerySchema.forModels(extensionModels);
  }
}
