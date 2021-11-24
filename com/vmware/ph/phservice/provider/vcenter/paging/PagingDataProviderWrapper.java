package com.vmware.ph.phservice.provider.vcenter.paging;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.DataProviderQueryPagingIterator;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PagingDataProviderWrapper implements DataProvider {
  private static final Log _log = LogFactory.getLog(PagingDataProviderWrapper.class);
  
  static final String FILTER_PROPERTY_NAME = "filter";
  
  static final String FILTER_PAGE_SIZE_PROPERTY_NAME = "filter/pageSize";
  
  private final DataProvider _wrappedDataProvider;
  
  public PagingDataProviderWrapper(DataProvider wrappedDataProvider) {
    this._wrappedDataProvider = wrappedDataProvider;
  }
  
  public QuerySchema getSchema() {
    QuerySchema.ModelInfo pagingFilterModelInfo = buildPagingFilterModelInfo();
    QuerySchema querySchema = this._wrappedDataProvider.getSchema();
    return buildQuerySchemaWithPagingFilter(querySchema, pagingFilterModelInfo);
  }
  
  public ResultSet executeQuery(Query query) {
    ResultSet resultSet;
    String resourceModel = query.getResourceModels().iterator().next();
    String qualifiedPageSizePropertyName = QuerySchemaUtil.qualifyProperty(resourceModel, "filter/pageSize");
    Query queryWithoutPageSize = QueryUtil.removePredicateFromQueryFilter(query, qualifiedPageSizePropertyName);
    Integer pageSize = getPageSizeFromQuery(query, queryWithoutPageSize, qualifiedPageSizePropertyName);
    if (pageSize != null) {
      resultSet = executeQueryInBatches(queryWithoutPageSize, pageSize.intValue());
    } else {
      resultSet = this._wrappedDataProvider.executeQuery(queryWithoutPageSize);
    } 
    return resultSet;
  }
  
  private ResultSet executeQueryInBatches(Query query, int pageSize) {
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
    DataProviderQueryPagingIterator queryPagingIterator = new DataProviderQueryPagingIterator(this._wrappedDataProvider, query, pageSize);
    while (queryPagingIterator.hasNext()) {
      ResultSet currentResultSet = queryPagingIterator.next();
      List<ResourceItem> resourceItems = currentResultSet.getItems();
      for (ResourceItem resourceItem : resourceItems) {
        Object itemKey = resourceItem.getKey();
        List<Object> propertyValues = resourceItem.getPropertyValues();
        resultSetBuilder.item(itemKey, propertyValues);
      } 
    } 
    return resultSetBuilder.build();
  }
  
  private static Integer getPageSizeFromQuery(Query originalQuery, Query queryWithoutPageSize, String qualifiedPageSizePropertyName) {
    Integer pageSize = null;
    if (originalQuery != queryWithoutPageSize) {
      String unqualifiedPageSizePropertyName = QuerySchemaUtil.getActualPropertyName(qualifiedPageSizePropertyName);
      Object pageSizeComparableValue = QueryUtil.getFilterPropertyComparableValue(originalQuery, unqualifiedPageSizePropertyName);
      if (pageSizeComparableValue != null)
        try {
          pageSize = Integer.valueOf(Integer.parseInt((String)pageSizeComparableValue));
        } catch (NumberFormatException e) {
          String message = String.format("Could not parse the property [%s] with value [%s]. Will skip the batch execution and execute a single query. %s", new Object[] { qualifiedPageSizePropertyName, pageSizeComparableValue, originalQuery });
          _log.warn(message, e);
        }  
    } 
    return pageSize;
  }
  
  private static QuerySchema.ModelInfo buildPagingFilterModelInfo() {
    QuerySchema.PropertyInfo filterPropertyInfo = QuerySchema.PropertyInfo.forNonFilterableProperty();
    QuerySchema.PropertyInfo pageSizePropertyInfo = QuerySchema.PropertyInfo.forFilterableProperty(QuerySchema.PropertyType.INT);
    Map<String, QuerySchema.PropertyInfo> pagingFilterPropertyNameToPropertyInfo = new HashMap<>();
    pagingFilterPropertyNameToPropertyInfo.put("filter", filterPropertyInfo);
    pagingFilterPropertyNameToPropertyInfo.put("filter/pageSize", pageSizePropertyInfo);
    QuerySchema.ModelInfo pagingFilterModelInfo = new QuerySchema.ModelInfo(pagingFilterPropertyNameToPropertyInfo);
    return pagingFilterModelInfo;
  }
  
  private static QuerySchema buildQuerySchemaWithPagingFilter(QuerySchema originalQuerySchema, QuerySchema.ModelInfo pagingFilterModelInfo) {
    Map<String, QuerySchema.ModelInfo> originalQuerySchemaModels = originalQuerySchema.getModels();
    Map<String, QuerySchema.ModelInfo> querySchemaModelsWithPageSize = new HashMap<>(originalQuerySchemaModels.size());
    for (Map.Entry<String, QuerySchema.ModelInfo> querySchemaModel : originalQuerySchemaModels.entrySet()) {
      String modelKey = querySchemaModel.getKey();
      QuerySchema.ModelInfo currentModelInfo = querySchemaModel.getValue();
      QuerySchema.ModelInfo mergedModelInfo = QuerySchema.ModelInfo.merge(
          Arrays.asList(new QuerySchema.ModelInfo[] { currentModelInfo, pagingFilterModelInfo }));
      querySchemaModelsWithPageSize.put(modelKey, mergedModelInfo);
    } 
    return QuerySchema.forModels(querySchemaModelsWithPageSize);
  }
}
