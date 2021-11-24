package com.vmware.ph.phservice.provider.common.vmomi.mo;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vmodl.ManagedObject;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.ManagedObjectType;
import com.vmware.vim.vmomi.core.types.VmodlType;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MoMethodDataProvider implements DataProvider {
  private final MoReader _managedObjectReader;
  
  private final MoTypesProvider _managedObjectTypesProvider;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final VmodlVersion _vmodlVersion;
  
  public MoMethodDataProvider(MoReader managedObjectReader, MoTypesProvider managedObjectTypesProvider, VmodlTypeMap vmodlTypeMap, VmodlVersion targetVmodlVersion) {
    this._managedObjectReader = managedObjectReader;
    this._managedObjectTypesProvider = managedObjectTypesProvider;
    this._vmodlTypeMap = vmodlTypeMap;
    this._vmodlVersion = targetVmodlVersion;
  }
  
  public QuerySchema getSchema() {
    List<ManagedObjectType> managedObjectTypes = this._managedObjectTypesProvider.getManagedObjectTypes();
    return createMethodQuerySchema(managedObjectTypes);
  }
  
  public ResultSet executeQuery(Query query) {
    ManagedObject targetManagedObject = getManagedObject(query.getResourceModels());
    URI modelKey = createModelKey(targetManagedObject);
    LinkedList<Object> queryPropertyValues = getQueryPropertyValues(targetManagedObject, modelKey, query.getProperties());
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    resultSetBuilder.item(modelKey, queryPropertyValues);
    return resultSetBuilder.build();
  }
  
  private QuerySchema createMethodQuerySchema(List<ManagedObjectType> managedObjectTypes) {
    Map<String, QuerySchema.ModelInfo> modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertManagedObjectTypeMethodsToWsdlNameModelInfos(managedObjectTypes, this._vmodlVersion);
    return QuerySchema.forModels(modelInfo);
  }
  
  private ManagedObject getManagedObject(Collection<String> resourceModels) {
    if (resourceModels == null || resourceModels.isEmpty())
      throw new IllegalArgumentException("Query doesn't contain a resource model."); 
    String resourceModel = resourceModels.iterator().next();
    ManagedObjectType managedObjectType = (ManagedObjectType)this._vmodlTypeMap.getVmodlType(resourceModel);
    return this._managedObjectReader.getManagedObject((VmodlType)managedObjectType);
  }
  
  private static URI createModelKey(ManagedObject managedObject) {
    ManagedObjectReference moRef = managedObject._getRef();
    URI modelKey = DataProviderUtil.createModelKey(moRef.getType(), moRef.getServerGuid());
    return modelKey;
  }
  
  private static LinkedList<Object> getQueryPropertyValues(ManagedObject managedObject, URI modelKey, List<String> queryProperties) {
    List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(queryProperties);
    List<String> methodNames = MethodNameToQueryPropertyConverter.toMethodNames(nonQualifiedQueryProperties);
    Map<String, Object> invokedMethodsValues = DataProviderUtil.getMethodInvocationReturnValues(managedObject, methodNames);
    LinkedList<Object> propertyValues = new LinkedList(invokedMethodsValues.values());
    propertyValues.add(0, modelKey);
    return propertyValues;
  }
}
