package com.vmware.ph.phservice.cloud.dataapp.vsan.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.VsanMassCollectorToJsonLdMapping;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.provider.common.internal.Context;
import java.util.Map;

public class VsanMassCollectorToJsonLdAndPluginDataMapping extends VsanMassCollectorToJsonLdMapping {
  private static final String PLUGIN_TYPE_JSON_KEY = "@plugin_type";
  
  private static final String DATA_TYPE_JSON_KEY = "@type";
  
  protected Map<String, Object> generateJsonProperties(NamedPropertiesResourceItem input, Context context, String objectIdProperty) {
    Map<String, Object> jsonProperties = super.generateJsonProperties(input, context, objectIdProperty);
    PluginTypeContext pluginTypeContext = PluginTypeContext.getContextFromQueryName((String)context
        .get("queryName"));
    if (pluginTypeContext.getDataType() != null)
      jsonProperties.put("@type", pluginTypeContext
          
          .getDataType()); 
    if (pluginTypeContext.getPluginType() != null)
      jsonProperties.put("@plugin_type", pluginTypeContext
          
          .getPluginType()); 
    return jsonProperties;
  }
}
