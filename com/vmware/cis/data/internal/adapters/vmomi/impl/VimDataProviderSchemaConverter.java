package com.vmware.cis.data.internal.adapters.vmomi.impl;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.vim.binding.vim.dp.ResourceModelInfo;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VimDataProviderSchemaConverter {
  private static final Logger _logger = LoggerFactory.getLogger(VimDataProviderSchemaConverter.class);
  
  public static QuerySchema convertSchema(ResourceModelInfo[] vmomiModels) {
    Validate.notEmpty((Object[])vmomiModels);
    Map<String, QuerySchema.ModelInfo> models = new HashMap<>(vmomiModels.length);
    for (ResourceModelInfo vmomiModel : vmomiModels)
      models.put(vmomiModel.getName(), convertModel(vmomiModel)); 
    return QuerySchema.forModels(models);
  }
  
  private static QuerySchema.ModelInfo convertModel(ResourceModelInfo vmomiModel) {
    Map<String, QuerySchema.PropertyInfo> properties = new HashMap<>((vmomiModel.getProperties()).length);
    for (String propertyName : vmomiModel.getProperties()) {
      String modelName = vmomiModel.getName();
      try {
        properties.put(propertyName, QuerySchema.PropertyInfo.forNonFilterableProperty());
      } catch (Exception e) {
        String msg = String.format("There is an error in the registration of %s/%s.", new Object[] { modelName, propertyName });
        _logger.error(msg, e);
      } 
    } 
    return new QuerySchema.ModelInfo(properties);
  }
}
