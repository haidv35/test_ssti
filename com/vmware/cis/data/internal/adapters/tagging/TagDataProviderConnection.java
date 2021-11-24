package com.vmware.cis.data.internal.adapters.tagging;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vapi.core.ApiProvider;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class TagDataProviderConnection implements DataProvider {
  private final LenientTaggingFacade _tagging;
  
  private final URI _taggingVapiUri;
  
  public TagDataProviderConnection(ApiProvider apiProvider, URI taggingVapiUri) {
    Validate.notNull(apiProvider);
    Validate.notNull(taggingVapiUri);
    this._tagging = new TaggingFacadePerfLogging(new TaggingFacadeImpl(apiProvider));
    this._taggingVapiUri = taggingVapiUri;
  }
  
  public ResultSet executeQuery(Query query) {
    Validate.notNull(query);
    String model = getModel(query.getResourceModels());
    LenientTaggingFacade tagging = new TaggingFacadeCache(this._tagging);
    PropertyProviderBasedQueryExecutor queryExecutor = createQueryExecutorForModel(model, tagging);
    return queryExecutor.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    Map<String, QuerySchema.PropertyInfo> propertyInfoByQualifiedName = new HashMap<>();
    propertyInfoByQualifiedName.putAll(TagModelPropertyProvider.getProperties());
    propertyInfoByQualifiedName.putAll(CategoryModelPropertyProvider.getProperties());
    propertyInfoByQualifiedName.putAll(InvSvcTagModelPropertyProvider.getProperties());
    propertyInfoByQualifiedName.putAll(InvSvcCategoryModelPropertyProvider.getProperties());
    return QuerySchema.forProperties(propertyInfoByQualifiedName);
  }
  
  private static String getModel(Collection<String> resourceModels) {
    assert resourceModels != null;
    assert !resourceModels.isEmpty();
    if (resourceModels.size() > 1)
      throw new IllegalArgumentException("Multiple models in tagging query: " + resourceModels); 
    return resourceModels.iterator().next();
  }
  
  private static PropertyProviderBasedQueryExecutor createQueryExecutorForModel(String model, LenientTaggingFacade tagging) {
    assert model != null;
    assert tagging != null;
    switch (model) {
      case "com.vmware.cis.tagging.TagModel":
        return new PropertyProviderBasedQueryExecutor(new TagModelPropertyProvider(tagging), new TagModelFilteringPropertyProvider(tagging));
      case "com.vmware.cis.tagging.CategoryModel":
        return new PropertyProviderBasedQueryExecutor(new CategoryModelPropertyProvider(tagging));
      case "inventoryservice:InventoryServiceTag":
        return new PropertyProviderBasedQueryExecutor(new InvSvcTagModelPropertyProvider(tagging), new TagModelFilteringPropertyProvider(tagging));
      case "inventoryservice:InventoryServiceCategory":
        return new PropertyProviderBasedQueryExecutor(new InvSvcCategoryModelPropertyProvider(tagging), new InvSvcCategoryModelFilteringPropertyProvider(tagging));
    } 
    throw new IllegalArgumentException("Unknown model: " + model);
  }
  
  public String toString() {
    return "TagDataProviderConnection(url=" + this._taggingVapiUri + ")";
  }
}
