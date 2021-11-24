package com.vmware.ph.phservice.proxy;

import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import java.util.List;

public class CompositeProxySettingsProvider implements ProxySettingsProvider {
  private final List<ProxySettingsProvider> _proxySettingsProviders;
  
  public CompositeProxySettingsProvider(List<ProxySettingsProvider> proxySettingsProviders) {
    this._proxySettingsProviders = proxySettingsProviders;
  }
  
  public ProxySettings getProxySettings(HttpConnectionConfig connConfig) {
    ProxySettings proxySettings = ProxySettings.NO_PROXY;
    for (ProxySettingsProvider proxySettingsProvider : this._proxySettingsProviders) {
      proxySettings = proxySettingsProvider.getProxySettings(connConfig);
      if (proxySettings == null)
        proxySettings = ProxySettings.NO_PROXY; 
      if (!proxySettings.equals(ProxySettings.NO_PROXY))
        break; 
    } 
    return proxySettings;
  }
}
