package com.vmware.ph.phservice.provider.common.vapi;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.common.vapi.util.VapiResultUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vapi.bindings.Service;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseVapiDataProvider implements DataProvider {
  public static final String ID_PROPERTY = "id";
  
  public static final String VALUE_PROPERTY = "value";
  
  public static final String FILTER_PROPERTY = "filter";
  
  public static final String GET_VALUE_METHOD_NAME = "get";
  
  public static final String LIST_VALUE_METHOD_NAME = "list";
  
  private final String _serverGuid;
  
  protected final VapiClient _vapiClient;
  
  private final Map<String, Class<? extends Service>> _resourceNameToServiceClazz;
  
  private final Map<String, List<String>> _resourceNameToFilterProperties;
  
  public BaseVapiDataProvider(String serverGuid, VapiClient vapiClient, Map<String, Class<? extends Service>> resourceNameToServiceClazz) {
    this(serverGuid, vapiClient, resourceNameToServiceClazz, null);
  }
  
  public BaseVapiDataProvider(String serverGuid, VapiClient vapiClient, Map<String, Class<? extends Service>> resourceNameToServiceClazz, Map<String, List<String>> resourceNameToFilterProperties) {
    this._serverGuid = serverGuid;
    this._vapiClient = vapiClient;
    this._resourceNameToServiceClazz = resourceNameToServiceClazz;
    this._resourceNameToFilterProperties = resourceNameToFilterProperties;
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.ModelInfo> modelNameToModelInfo = new HashMap<>();
    for (String resourceModel : this._resourceNameToServiceClazz.keySet()) {
      List<String> properties = new ArrayList<>();
      properties.add("id");
      properties.add("value");
      if (this._resourceNameToFilterProperties != null) {
        List<String> filterProperties = this._resourceNameToFilterProperties.get(resourceModel);
        if (filterProperties != null)
          properties.addAll(filterProperties); 
      } 
      QuerySchema.ModelInfo queryModelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(properties);
      modelNameToModelInfo.put(resourceModel, queryModelInfo);
    } 
    QuerySchema schema = QuerySchema.forModels(modelNameToModelInfo);
    return schema;
  }
  
  public final ResultSet executeQuery(Query query) {
    String resourceModelName = query.getResourceModels().iterator().next();
    List<String> queryProperties = query.getProperties();
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    Class<? extends Service> vapiServiceClazz = this._resourceNameToServiceClazz.get(resourceModelName);
    Service vapiService = this._vapiClient.createStub((Class)vapiServiceClazz);
    Object vapiFilter = convertQueryFilterToServiceFilter(query);
    Map<? extends Object, ? extends Object> vapiResourceEntityIdToResourceEntityValue = executeService(vapiService, vapiFilter);
    List<List<Object>> queryResultItemsPropertyValues = convertResourceEntitiesToResultItems(resourceModelName, vapiResourceEntityIdToResourceEntityValue, nonQualifiedQueryProperties, query);
    queryResultItemsPropertyValues = PageUtil.pageItems(queryResultItemsPropertyValues, query
        
        .getOffset(), query
        .getLimit());
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(queryProperties);
    for (List<Object> queryResultItemPropertyValues : queryResultItemsPropertyValues)
      resultSetBuilder.item(queryResultItemPropertyValues
          .get(0), queryResultItemPropertyValues); 
    ResultSet resultSet = resultSetBuilder.build();
    return resultSet;
  }
  
  protected abstract Map<? extends Object, ? extends Object> executeService(Service paramService, Object paramObject);
  
  protected Object convertQueryFilterToServiceFilter(Query query) {
    return null;
  }
  
  protected String convertResourceEntityIdToString(String resourceName, Object resourceEntityId) {
    return (resourceEntityId != null) ? resourceEntityId.toString() : "";
  }
  
  private List<List<Object>> convertResourceEntitiesToResultItems(String resourceModelName, Map<? extends Object, ? extends Object> resourceEntityIdToResourceEntityValue, List<String> nonQualifiedQueryProperties, Query query) {
    List<List<Object>> itemsQueryPropertyValues = new ArrayList<>();
    for (Map.Entry<? extends Object, ? extends Object> entry : resourceEntityIdToResourceEntityValue.entrySet()) {
      URI modelKey = VapiResultUtil.createModelKey(resourceModelName, entry
          
          .getKey(), this._serverGuid);
      List<Object> itemPropertyValues = convertResourceEntityValueToQueryPropertyValues(resourceModelName, modelKey, entry

          
          .getValue(), nonQualifiedQueryProperties, query);
      itemsQueryPropertyValues.add(itemPropertyValues);
    } 
    return itemsQueryPropertyValues;
  }
  
  private List<Object> convertResourceEntityValueToQueryPropertyValues(String resourceName, URI resourceEntityId, Object resourceEntityValue, List<String> nonQualifiedQueryProperties, Query query) {
    List<Object> propertyValues = new ArrayList(nonQualifiedQueryProperties.size());
    propertyValues.add(0, resourceEntityId);
    for (String propertyName : nonQualifiedQueryProperties) {
      if (propertyName.equals("id")) {
        propertyValues.add(resourceEntityId);
        continue;
      } 
      if (propertyName.equals("value")) {
        propertyValues.add(resourceEntityValue);
        continue;
      } 
      if (propertyName.startsWith("filter")) {
        Object propertyValue = QueryUtil.getFilterPropertyComparableValue(query, propertyName);
        propertyValues.add(propertyValue);
      } 
    } 
    return propertyValues;
  }
}
