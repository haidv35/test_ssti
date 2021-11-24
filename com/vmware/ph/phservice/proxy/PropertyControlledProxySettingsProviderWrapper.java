package com.vmware.ph.phservice.proxy;

import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyControlledProxySettingsProviderWrapper implements ProxySettingsProvider {
  private static final Log _log = LogFactory.getLog(PropertyControlledProxySettingsProviderWrapper.class);
  
  private final ProxySettingsProvider _wrappedProxySettingsProvider;
  
  private boolean _shouldRetrieveProxySettings = true;
  
  public PropertyControlledProxySettingsProviderWrapper(ProxySettingsProvider wrappedProxySettingsProvider, Boolean shouldRetrieveProxySettings) {
    this._wrappedProxySettingsProvider = wrappedProxySettingsProvider;
    if (shouldRetrieveProxySettings != null)
      this._shouldRetrieveProxySettings = shouldRetrieveProxySettings.booleanValue(); 
    if (this._shouldRetrieveProxySettings)
      try {
        if (this._wrappedProxySettingsProvider instanceof com.vmware.ph.client.common.extensions.ps.AutodiscoveredProxySettingsProvider)
          this._wrappedProxySettingsProvider.getProxySettings(null); 
      } catch (RuntimeException e) {
        if (_log.isDebugEnabled())
          _log.debug("Cannot auto-discover proxy settings on startup.", e); 
      }  
  }
  
  public ProxySettings getProxySettings(HttpConnectionConfig connConfig) {
    if (this._shouldRetrieveProxySettings)
      return this._wrappedProxySettingsProvider.getProxySettings(connConfig); 
    return ProxySettings.NO_PROXY;
  }
}
