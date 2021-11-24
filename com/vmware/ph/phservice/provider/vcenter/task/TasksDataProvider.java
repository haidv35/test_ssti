package com.vmware.ph.phservice.provider.vcenter.task;

import com.vmware.cis.data.api.Filter;
import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.QueryFilterConverter;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.ph.phservice.provider.common.vmomi.VmodlTypeToQuerySchemaModelInfoConverter;
import com.vmware.vim.binding.vim.TaskFilterSpec;
import com.vmware.vim.binding.vim.TaskInfo;
import com.vmware.vim.binding.vmodl.DataObject;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TasksDataProvider implements DataProvider {
  private static final int TASKS_READER_PAGE_LIMIT = 1000;
  
  private static final Log _log = LogFactory.getLog(TasksDataProvider.class);
  
  private final VcClient _vcClient;
  
  private final String _resourceModelName;
  
  public TasksDataProvider(VcClient vcClient) {
    this._vcClient = vcClient;
    this._resourceModelName = getResourceModelName(vcClient.getVmodlContext());
  }
  
  public QuerySchema getSchema() {
    return createQuerySchema(this._vcClient
        .getVmodlContext(), this._vcClient
        .getVmodlVersion(), this._resourceModelName);
  }
  
  public ResultSet executeQuery(Query query) {
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    TaskFilterSpec taskFilterSpec = QueryFilterConverter.<TaskFilterSpec>convertQueryFilter(query
        .getFilter(), TaskFilterSpec.class);
    ResultSet result = null;
    try (ItemsStream<TaskInfo> taskInfosReader = new TaskInfosReader(this._vcClient, taskFilterSpec, 1000)) {
      List<String> nonQualifiedQueryProperties = QuerySchemaUtil.getNonQualifiedPropertyNames(query.getProperties());
      List<TaskInfo> tasksInfo = PageUtil.pageItems(taskInfosReader, query.getOffset(), query.getLimit());
      ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(query.getProperties());
      for (TaskInfo taksInfo : tasksInfo)
        addTaskInfoToResultSet(query
            .getFilter(), nonQualifiedQueryProperties, resultSetBuilder, this._resourceModelName, taksInfo); 
      result = resultSetBuilder.build();
    } catch (IOException e) {
      _log.debug("Failed to close the Items Stream.", e);
    } 
    return result;
  }
  
  private static ResultSet addTaskInfoToResultSet(Filter queryFilter, List<String> nonQualifiedQueryProperties, ResultSet.Builder resultSetBuilder, String resourceModelName, TaskInfo taskInfo) {
    URI modelKey = DataProviderUtil.createModelKey(resourceModelName, 
        
        String.valueOf(taskInfo.getKey()));
    List<Object> propertyValues = DataProviderUtil.getPropertyValuesFromObjectAndValueMap(taskInfo, modelKey, nonQualifiedQueryProperties, 


        
        QueryUtil.getNonQualifiedFilterPropertyToComparableValue(queryFilter));
    resultSetBuilder.item(modelKey, propertyValues);
    return resultSetBuilder.build();
  }
  
  private static QuerySchema createQuerySchema(VmodlContext vmodlContext, VmodlVersion vmodlVersion, String resourceModelName) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    QuerySchema.ModelInfo modelInfo = VmodlTypeToQuerySchemaModelInfoConverter.convertVmodlClassesPropertiesToModelInfo(
        Arrays.asList((Class<? extends DataObject>[])new Class[] { TaskInfo.class, TaskFilterSpec.class }, ), vmodlTypeMap, vmodlVersion);
    QuerySchema querySchema = QuerySchemaUtil.buildQuerySchemaFromModelInfo(resourceModelName, modelInfo);
    return querySchema;
  }
  
  private static String getResourceModelName(VmodlContext vmodlContext) {
    VmodlTypeMap vmodlTypeMap = vmodlContext.getVmodlTypeMap();
    String taskInfoWsdlName = vmodlTypeMap.getVmodlType(TaskInfo.class).getWsdlName();
    return taskInfoWsdlName;
  }
}
