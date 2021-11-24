package com.vmware.ph.phservice.cloud.dataapp.internal;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class PluginTypeContext {
  private static final char PLUGIN_TYPE_STRING_SEPARATOR = ';';
  
  private static final char QUERY_NAME_SEPARATOR = '#';
  
  private final String _pluginType;
  
  private final String _dataType;
  
  private final boolean _isUserTriggerSupported;
  
  public PluginTypeContext(String pluginType, String dataType, boolean isUserTriggerSupported) {
    this._pluginType = pluginType;
    this._dataType = dataType;
    this._isUserTriggerSupported = isUserTriggerSupported;
  }
  
  public String getPluginType() {
    return this._pluginType;
  }
  
  public String getDataType() {
    return this._dataType;
  }
  
  public boolean isUserTriggerSupported() {
    return this._isUserTriggerSupported;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof PluginTypeContext))
      return false; 
    PluginTypeContext other = (PluginTypeContext)obj;
    if (this._pluginType == null && other._pluginType != null)
      return false; 
    if (this._pluginType != null && !this._pluginType.equals(other._pluginType))
      return false; 
    if (this._dataType == null && other._dataType != null)
      return false; 
    if (this._dataType != null && !this._dataType.equals(other._dataType))
      return false; 
    if (this._isUserTriggerSupported != other._isUserTriggerSupported)
      return false; 
    return true;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this._pluginType, this._dataType, Boolean.valueOf(this._isUserTriggerSupported) });
  }
  
  public static String encode(PluginTypeContext pluginTypeContext, char separator) {
    StringBuilder builder = new StringBuilder();
    builder
      .append((pluginTypeContext._pluginType != null) ? pluginTypeContext._pluginType : "")
      
      .append(separator)
      .append(pluginTypeContext._isUserTriggerSupported)
      .append(separator)
      .append((pluginTypeContext._dataType != null) ? pluginTypeContext._dataType : "");
    return builder.toString();
  }
  
  public static PluginTypeContext decode(String pluginTypeContextStr, char separator) {
    String[] parts = pluginTypeContextStr.split(String.valueOf(separator));
    String pluginType = parts[0];
    pluginType = StringUtils.isEmpty(pluginType) ? null : pluginType;
    boolean userTriggerSupported = Boolean.parseBoolean(parts[1]);
    String dataType = null;
    if (parts.length > 2)
      dataType = parts[2]; 
    dataType = StringUtils.isEmpty(dataType) ? null : dataType;
    PluginTypeContext pluginTypeContext = new PluginTypeContext(pluginType, dataType, userTriggerSupported);
    return pluginTypeContext;
  }
  
  public static String createQueryName(String queryPrefix, PluginTypeContext pluginTypeContext) {
    if (pluginTypeContext == null)
      return queryPrefix; 
    return queryPrefix + '#' + 
      encode(pluginTypeContext, ';');
  }
  
  public static PluginTypeContext getContextFromQueryName(String queryName) {
    String[] parts = queryName.split(String.valueOf('#'));
    if (parts.length <= 1)
      return null; 
    String pluginTypeContextStr = parts[1];
    return decode(pluginTypeContextStr, ';');
  }
}
