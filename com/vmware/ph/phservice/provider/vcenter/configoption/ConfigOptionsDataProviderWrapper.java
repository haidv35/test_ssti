package com.vmware.ph.phservice.provider.vcenter.configoption;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResourceItem;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.provider.common.QuerySchemaUtil;
import com.vmware.ph.phservice.provider.common.QueryUtil;
import com.vmware.vim.binding.vim.option.OptionValue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigOptionsDataProviderWrapper implements DataProvider {
  static final String RESOURCE_MODEL_HOST_SYSTEM = "HostSystem";
  
  static final String QUALIFIED_PROPERTY_NAME_CONFIG_OPTION = "HostSystem/config/option";
  
  static final String QUALIFIED_PROPERTY_NAME_CONFIG_OPTION_KEY = "HostSystem/config/option/key";
  
  private final DataProvider _wrappedDataProvider;
  
  public ConfigOptionsDataProviderWrapper(DataProvider wrappedDataProvider) {
    this._wrappedDataProvider = wrappedDataProvider;
  }
  
  public QuerySchema getSchema() {
    return this._wrappedDataProvider.getSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    Query modifiedQuery = QueryUtil.removePredicateFromQueryFilter(query, "HostSystem/config/option/key");
    ResultSet resultSet = this._wrappedDataProvider.executeQuery(modifiedQuery);
    List<String> properties = resultSet.getProperties();
    boolean hasConfigOptionProperty = properties.contains("HostSystem/config/option");
    if (hasConfigOptionProperty) {
      String unqualifiedConfigOptionKey = QuerySchemaUtil.getActualPropertyName("HostSystem/config/option/key");
      List<String> configOptionKeys = QueryUtil.getFilterPropertyComparableValues(query, unqualifiedConfigOptionKey);
      if (!configOptionKeys.isEmpty())
        resultSet = filterConfigOptionsInResultSetByOptionKey(resultSet, configOptionKeys); 
    } 
    return resultSet;
  }
  
  private static ResultSet filterConfigOptionsInResultSetByOptionKey(ResultSet resultSet, List<String> configOptionKeys) {
    List<String> properties = resultSet.getProperties();
    ResultSet.Builder resultSetBuilder = ResultSet.Builder.properties(properties);
    List<ResourceItem> resourceItems = resultSet.getItems();
    for (ResourceItem resourceItem : resourceItems) {
      List<Object> unmodifiablePropertyValues = resourceItem.getPropertyValues();
      Object itemKey = resourceItem.getKey();
      Object configOptions = resourceItem.get("HostSystem/config/option");
      OptionValue[] filteredConfigOptions = filterConfigOptionsByKey(configOptionKeys, configOptions);
      if (filteredConfigOptions != null) {
        List<Object> modifiedPropertyValues = new ArrayList(unmodifiablePropertyValues);
        int indexOfConfigOptions = modifiedPropertyValues.indexOf(configOptions);
        modifiedPropertyValues.set(indexOfConfigOptions, filteredConfigOptions);
        resultSetBuilder.item(itemKey, modifiedPropertyValues);
      } 
    } 
    return resultSetBuilder.build();
  }
  
  private static OptionValue[] filterConfigOptionsByKey(List<String> configOptionKeys, Object configOptions) {
    OptionValue[] filteredConfigOptionsResult = null;
    if (configOptions instanceof OptionValue[]) {
      List<OptionValue> filteredOptions = new ArrayList<>();
      Set<String> configOptionKeysSet = new HashSet<>(configOptionKeys);
      for (OptionValue optionValue : (OptionValue[])configOptions) {
        String optionValueKey = optionValue.getKey();
        if (configOptionKeysSet.contains(optionValueKey)) {
          filteredOptions.add(optionValue);
          if (configOptionKeysSet.size() == filteredOptions.size())
            break; 
        } 
      } 
      filteredConfigOptionsResult = filteredOptions.<OptionValue>toArray(new OptionValue[filteredOptions.size()]);
    } 
    return filteredConfigOptionsResult;
  }
}
