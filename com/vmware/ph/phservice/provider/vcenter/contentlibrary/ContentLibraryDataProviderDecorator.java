package com.vmware.ph.phservice.provider.vcenter.contentlibrary;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentLibraryDataProviderDecorator implements DataProvider {
  private static final String MODEL_WORD = "Model";
  
  private final DataProvider _clsDataProvider;
  
  private QuerySchema _decoratedQuerySchema;
  
  private QuerySchema _clsQuerySchema;
  
  public ContentLibraryDataProviderDecorator(DataProvider clsDataProvider) {
    this._clsDataProvider = clsDataProvider;
  }
  
  public QuerySchema getSchema() {
    if (this._decoratedQuerySchema == null) {
      this._clsQuerySchema = this._clsDataProvider.getSchema();
      this
        ._decoratedQuerySchema = buildDecoratedQuerySchemaFromClsQuerySchema(this._clsQuerySchema);
    } 
    return this._decoratedQuerySchema;
  }
  
  public ResultSet executeQuery(Query decoratedQuery) {
    decoratedQuery = QueryContextUtil.removeContextFromQueryFilter(decoratedQuery);
    Query clsQuery = buildClsQueryFromDecoratedQuery(decoratedQuery, this._clsQuerySchema);
    ResultSet clsResultSet = this._clsDataProvider.executeQuery(clsQuery);
    ResultSet decoratedResultSet = buildDecoratedResultSetFromClsResultSet(clsResultSet, decoratedQuery);
    return decoratedResultSet;
  }
  
  private static QuerySchema buildDecoratedQuerySchemaFromClsQuerySchema(QuerySchema clsSchema) {
    Map<String, QuerySchema.ModelInfo> clsResourceModels = clsSchema.getModels();
    Map<String, QuerySchema.ModelInfo> modifiedResourceModels = new HashMap<>();
    for (Map.Entry<String, QuerySchema.ModelInfo> clsResourceModel : clsResourceModels
      .entrySet()) {
      String clsResourceModelName = clsResourceModel.getKey();
      QuerySchema.ModelInfo clsResourceModelInfo = clsResourceModel.getValue();
      modifiedResourceModels.put(
          stripModelWordFromResourceName(clsResourceModelName), clsResourceModelInfo);
    } 
    return QuerySchema.forModels(modifiedResourceModels);
  }
  
  private static Query buildClsQueryFromDecoratedQuery(Query decoratedQuery, QuerySchema clsQuerySchema) {
    String decoratedQueryModelName = decoratedQuery.getResourceModels().iterator().next();
    Map<String, QuerySchema.ModelInfo> clsResourceModels = clsQuerySchema.getModels();
    if (clsResourceModels.get(decoratedQueryModelName) != null)
      return decoratedQuery; 
    String clsQueryModelName = decoratedQueryModelName + "Model";
    List<String> clsQueryProperties = changeQueryPropertiesModelName(clsQueryModelName, decoratedQuery.getProperties());
    Query clsQuery = Query.Builder.select(clsQueryProperties).from(new String[] { clsQueryModelName }).offset(decoratedQuery.getOffset()).limit(decoratedQuery.getLimit()).orderBy(decoratedQuery.getSortCriteria()).build();
    return clsQuery;
  }
  
  private static ResultSet buildDecoratedResultSetFromClsResultSet(ResultSet clsResultSet, Query decoratedQuery) {
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(decoratedQuery.getProperties());
    for (ResourceItem clsResourceItem : clsResultSet.getItems())
      resultSetBuilder.item(clsResourceItem
          .getKey(), clsResourceItem
          .getPropertyValues()); 
    return resultSetBuilder.build();
  }
  
  private static String stripModelWordFromResourceName(String resourceName) {
    int modelWordStartIndex = resourceName.indexOf("Model");
    if (modelWordStartIndex > 0)
      resourceName = resourceName.substring(0, modelWordStartIndex); 
    return resourceName;
  }
  
  private static List<String> changeQueryPropertiesModelName(String newModelName, List<String> qualifiedQueryProperties) {
    List<String> modifiedQueryProperties = new ArrayList<>();
    for (String qualifiedQueryProperty : qualifiedQueryProperties) {
      if (qualifiedQueryProperty.startsWith("@")) {
        modifiedQueryProperties.add(qualifiedQueryProperty);
        continue;
      } 
      int nonQualifiedPartStartIndex = qualifiedQueryProperty.indexOf('/');
      String changedQualifierQueryProperty = newModelName + qualifiedQueryProperty.substring(nonQualifiedPartStartIndex);
      modifiedQueryProperties.add(changedQualifierQueryProperty);
    } 
    return modifiedQueryProperties;
  }
}
