package com.vmware.ph.phservice.provider.appliance.domain;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DomainDeploymentDataProvider implements DataProvider {
  static final String DEPLOYMENT_DOMAIN_RESOURCE_MODEL_NAME = "DeploymentDomain";
  
  static final String DEPLOYMENT_PSC_NODE_RESOURCE_MODEL_NAME = "DeploymentPscNode";
  
  static final String DEPLOYMENT_SITE_RESOURCE_MODEL_NAME = "DeploymentSite";
  
  private DomainDeploymentReader _domainDeploymentReader;
  
  public DomainDeploymentDataProvider(String domainId, String hostName, LookupClient lookupClient) {
    this(domainId, new DeploymentInfo(hostName, lookupClient));
  }
  
  DomainDeploymentDataProvider(String domainId, DeploymentInfo deploymentInfo) {
    this._domainDeploymentReader = new DomainDeploymentReader(domainId, deploymentInfo);
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema();
  }
  
  public ResultSet executeQuery(Query query) {
    ResultSet resultSet;
    Map<String, Object> objectIdToQueryResultObject = getObjectIdToQueryResultObject(query, this._domainDeploymentReader);
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
    try {
      for (Map.Entry<String, Object> entry : objectIdToQueryResultObject.entrySet()) {
        String objectId = entry.getKey();
        Object queryResultObject = entry.getValue();
        URI modelKey = DataProviderUtil.createModelKey(queryResultObject.getClass(), objectId);
        List<Object> propertyValues = buildPropertyValues(modelKey, queryResultObject, nonQualifiedQueryProperties);
        resultSetBuilder.item(entry.getKey(), propertyValues);
      } 
      resultSet = resultSetBuilder.build();
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Could not build result set: ", e);
    } 
    return resultSet;
  }
  
  private static QuerySchema createQuerySchema() {
    Map<String, QuerySchema.ModelInfo> modelNameToModelInfo = new HashMap<>();
    modelNameToModelInfo.put("DeploymentDomain", 
        
        createQueryModelInfo(DeploymentDomain.class));
    modelNameToModelInfo.put("DeploymentPscNode", 
        
        createQueryModelInfo(DeploymentPscNode.class));
    modelNameToModelInfo.put("DeploymentSite", 
        
        createQueryModelInfo(DeploymentSite.class));
    return QuerySchema.forModels(modelNameToModelInfo);
  }
  
  private static QuerySchema.ModelInfo createQueryModelInfo(Class<?> objectClass) {
    List<String> supportedProperties = DataProviderUtil.getPropertyNames(objectClass, "get", true);
    return VmodlTypeToQuerySchemaModelInfoConverter.convertPropertyNamesToModelInfo(supportedProperties);
  }
  
  private static Map<String, Object> getObjectIdToQueryResultObject(Query query, DomainDeploymentReader domainDeploymentReader) {
    DeploymentDomain domain;
    String resourceModelName = query.getResourceModels().iterator().next();
    Map<String, Object> objectIdToQueryResultObject = new LinkedHashMap<>();
    switch (resourceModelName) {
      case "DeploymentDomain":
        domain = domainDeploymentReader.getDeploymentDomain();
        objectIdToQueryResultObject.put(domain.getId(), domain);
        break;
      case "DeploymentPscNode":
        objectIdToQueryResultObject.putAll(domainDeploymentReader
            .getDeploymentPscNodes());
        break;
      case "DeploymentSite":
        objectIdToQueryResultObject.putAll(domainDeploymentReader
            .getDeploymentSites());
        break;
    } 
    return objectIdToQueryResultObject;
  }
  
  private static List<Object> buildPropertyValues(URI modelKey, Object queryResultObject, List<String> nonQualifiedQueryProperties) {
    List<Object> propertyValues = new ArrayList();
    propertyValues.add(0, modelKey);
    for (String queryProperty : nonQualifiedQueryProperties) {
      if (!queryProperty.startsWith("@modelKey"))
        propertyValues.add(DataProviderUtil.getPropertyValue(queryResultObject, queryProperty)); 
    } 
    return propertyValues;
  }
}
