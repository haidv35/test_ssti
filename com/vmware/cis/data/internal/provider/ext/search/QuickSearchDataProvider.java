package com.vmware.cis.data.internal.provider.ext.search;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.provider.profiler.QueryIdLogConfigurator;
import com.vmware.cis.data.internal.provider.util.ResultSetUtil;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuickSearchDataProvider implements DataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(QuickSearchDataProvider.class);
  
  public static final String QUICK_SEARCH = "QuickSearch";
  
  public static final String SEARCH_TERM = "searchTerm";
  
  private static final String VIM_SEARCH = "VimSearch";
  
  private static final String CATEGORY = "inventoryservice:InventoryServiceCategory";
  
  private static final String TAG = "inventoryservice:InventoryServiceTag";
  
  private static final String ITEM = "com.vmware.content.library.ItemModel";
  
  private static final String LIBRARY = "com.vmware.content.LibraryModel";
  
  private static final String CONFIG_MANAGED_BY = "config/managedBy";
  
  private static final String CONFIG_FTINFO_PRIMARY_VM = "config/ftInfo/primaryVM";
  
  private static final String CONFIG_TEMPLATE = "config/template";
  
  private static final String NAME = "name";
  
  private static final SearchModelDescriptor _searchModel = new SearchModelDescriptor("QuickSearch", 

      
      Arrays.asList(new SearchChildModelDescriptor[] { SearchChildModelDescriptor.childModel("VimSearch")
          .exactMatchIgnoreCase("searchTerm")
          .selectable("name")
          .selectable("config/managedBy")
          .selectable("config/template")
          .selectable("config/ftInfo/primaryVM")
          .build(), 
          SearchChildModelDescriptor.childModel("inventoryservice:InventoryServiceCategory", "name"), 
          SearchChildModelDescriptor.childModel("inventoryservice:InventoryServiceTag", "name"), 
          SearchChildModelDescriptor.childModel("com.vmware.content.library.ItemModel", "name"), 
          SearchChildModelDescriptor.childModel("com.vmware.content.LibraryModel", "name") }));
  
  private static final Map<String, String> _displayNameByModel;
  
  private final DataProvider _dataProvider;
  
  private final TaskExecutor _taskExecutor;
  
  static {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("inventoryservice:InventoryServiceCategory", "category");
    m.put("inventoryservice:InventoryServiceTag", "tag");
    m.put("com.vmware.content.library.ItemModel", "libitem");
    m.put("com.vmware.content.LibraryModel", "library");
    m.put("VimSearch", "vim");
    _displayNameByModel = Collections.unmodifiableMap(m);
  }
  
  public QuickSearchDataProvider(DataProvider dataProvider, ExecutorService executor) {
    assert dataProvider != null;
    assert executor != null;
    this._dataProvider = dataProvider;
    this._taskExecutor = new TaskExecutor(executor, TaskExecutor.ErrorHandlingPolicy.STRICT);
  }
  
  public ResultSet executeQuery(Query query) {
    List<ResultSet> childResults;
    assert query != null;
    if (!_searchModel.isSearchQuery(query))
      return this._dataProvider.executeQuery(query); 
    Collection<Query> childQueries = _searchModel.toChildQueries(query);
    List<Callable<ResultSet>> tasks = toTasks(childQueries);
    try {
      childResults = this._taskExecutor.invokeTasks(tasks);
    } catch (RuntimeException ex) {
      throw new RuntimeException("Error in quick search", ex);
    } 
    ResultSet result = _searchModel.toAggregatedResult(childResults, query);
    return result;
  }
  
  public QuerySchema getSchema() {
    QuerySchema baseSchema = this._dataProvider.getSchema();
    QuerySchema extSchema = _searchModel.addModel(baseSchema);
    return extSchema;
  }
  
  public String toString() {
    return this._dataProvider.toString();
  }
  
  private List<Callable<ResultSet>> toTasks(Collection<Query> queries) {
    assert queries != null;
    List<Callable<ResultSet>> tasks = new ArrayList<>(queries.size());
    for (Query query : queries) {
      boolean throwOnError = query.getResourceModels().contains("VimSearch");
      Callable<ResultSet> task = new QuickSearchTask(this._dataProvider, query, throwOnError);
      tasks.add(task);
    } 
    return tasks;
  }
  
  private static final class QuickSearchTask implements Callable<ResultSet> {
    private final DataProvider _dataProvider;
    
    private final Query _query;
    
    private final boolean _throwOnError;
    
    QuickSearchTask(DataProvider dataProvider, Query query, boolean throwOnError) {
      assert dataProvider != null;
      assert query != null;
      String displayName = getDisplayName(query);
      this._dataProvider = QueryIdLogConfigurator.withQueryCounter(dataProvider, displayName);
      this._query = query;
      this._throwOnError = throwOnError;
    }
    
    public ResultSet call() {
      try {
        return this._dataProvider.executeQuery(this._query);
      } catch (RuntimeException ex) {
        if (this._throwOnError)
          throw ex; 
        QuickSearchDataProvider._logger.error("Error in quick search query for model {}", this._query
            .getResourceModels(), ex);
        return ResultSetUtil.emptyResult(this._query);
      } 
    }
    
    public String toString() {
      return "Quick search query for " + this._query.getResourceModels();
    }
    
    private static String getDisplayName(Query query) {
      assert query != null;
      assert query.getResourceModels().size() == 1;
      String model = query.getResourceModels().iterator().next();
      String displayName = (String)QuickSearchDataProvider._displayNameByModel.get(model);
      if (displayName == null)
        return model; 
      return displayName;
    }
  }
}
