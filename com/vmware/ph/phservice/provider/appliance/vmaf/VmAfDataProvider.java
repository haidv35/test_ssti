package com.vmware.ph.phservice.provider.appliance.vmaf;

import com.vmware.af.VmAfClient;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VmAfDataProvider implements DataProvider {
  static final String RESOURCE_MODEL_NAME = "VmwareAuthenticationFramework";
  
  private VmAfClient _vmAfClient;
  
  private String _applianceId;
  
  public VmAfDataProvider(VmAfClient vmAfClient, String applianceId) {
    this._vmAfClient = vmAfClient;
    this._applianceId = applianceId;
  }
  
  public QuerySchema getSchema() {
    List<String> supportedProperties = DataProviderUtil.getPropertyNames(VmAfClient.class, "get", false);
    QuerySchema.ModelInfo queryModelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(supportedProperties);
    Map<String, QuerySchema.ModelInfo> modelNameToModelInfo = new HashMap<>();
    modelNameToModelInfo.put("VmwareAuthenticationFramework", queryModelInfo);
    return QuerySchema.forModels(modelNameToModelInfo);
  }
  
  public ResultSet executeQuery(Query query) {
    String resourceModelName = query.getResourceModels().iterator().next();
    List<String> queryProperties = query.getProperties();
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    Object modelKey = DataProviderUtil.createModelKey(resourceModelName, this._applianceId);
    List<Object> propertyValues = new LinkedList();
    propertyValues.add(0, modelKey);
    for (String queryProperty : nonQualifiedQueryProperties) {
      if (!queryProperty.startsWith("@modelKey"))
        propertyValues.add(DataProviderUtil.getPropertyValue(this._vmAfClient, queryProperty)); 
    } 
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    resultSetBuilder.item(modelKey, propertyValues);
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
}
