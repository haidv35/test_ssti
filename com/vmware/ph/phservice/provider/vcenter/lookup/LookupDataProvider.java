package com.vmware.ph.phservice.provider.vcenter.lookup;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QueryFilterConverter;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class LookupDataProvider implements DataProvider {
  static final String FILTER_PROPERTIES_PREFIX = ServiceRegistration.Filter.class
    .getSimpleName().toLowerCase();
  
  private final LookupClient _lookupClient;
  
  private final String _resourceModelName;
  
  public LookupDataProvider(LookupClient lookupClient) {
    this._lookupClient = lookupClient;
    this._resourceModelName = getResourceModelName(lookupClient.getVmodlContext());
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._lookupClient
        .getVmodlContext(), this._lookupClient
        .getVmodlVersion(), this._resourceModelName);
  }
  
  public ResultSet executeQuery(Query query) {
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    ServiceRegistration.Filter serviceRegistartionFilter = QueryFilterConverter.<ServiceRegistration.Filter>convertQueryFilter(query
        .getFilter(), ServiceRegistration.Filter.class);
    ServiceRegistration.Info[] serviceInfos = getServiceInfos(this._lookupClient, serviceRegistartionFilter);
    ResultSet lookupResultSet = createResultSetFromServiceInfos(query, serviceInfos, this._resourceModelName);
    return lookupResultSet;
  }
  
  private static ServiceRegistration.Info[] getServiceInfos(LookupClient lookupClient, ServiceRegistration.Filter serviceRegistrationFilter) {
    ServiceRegistration serviceRegistration = lookupClient.getServiceRegistration();
    ServiceRegistration.Info[] serviceInfos = new ServiceRegistration.Info[0];
    if (serviceRegistration != null)
      serviceInfos = serviceRegistration.list(serviceRegistrationFilter); 
    return serviceInfos;
  }
  
  private static ResultSet createResultSetFromServiceInfos(Query query, ServiceRegistration.Info[] serviceInfos, String resourceModelName) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    for (ServiceRegistration.Info serviceInfo : serviceInfos) {
      URI modelKey = DataProviderUtil.createModelKey(resourceModelName, 
          
          String.valueOf(serviceInfo.getServiceId()));
      List<Object> propertyValues = DataProviderUtil.getPropertyValuesFromObjectAndValueMap(serviceInfo, modelKey, nonQualifiedQueryProperties, 


          
          QueryUtil.getNonQualifiedFilterPropertyToComparableValue(query
            .getFilter()));
      resultSetBuilder.item(modelKey, propertyValues);
    } 
    return resultSetBuilder.build();
  }
  
  private static QuerySchema createQuerySchema(VmodlContext vmodlContext, VmodlVersion vmodlVersion, String resourceModelName) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    QuerySchema.ModelInfo modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlClassesPropertiesToModelInfo(
        Arrays.asList((Class<? extends DataObject>[])new Class[] { ServiceRegistration.Info.class, ServiceRegistration.Filter.class }, ), vmodlTypeMap, vmodlVersion);
    QuerySchema querySchema = QuerySchemaUtil.buildQuerySchemaFromModelInfo(resourceModelName, modelInfo);
    return querySchema;
  }
  
  private static String getResourceModelName(VmodlContext vmodlContext) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    String serviceRegistartionInfoWsdlName = vmodlTypeMap.getVmodlType(ServiceRegistration.Info.class).getWsdlName();
    return serviceRegistartionInfoWsdlName;
  }
}
