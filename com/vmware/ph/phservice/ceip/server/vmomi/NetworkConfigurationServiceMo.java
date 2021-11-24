package com.vmware.ph.phservice.ceip.server.vmomi;

import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import com.vmware.vim.binding.phonehome.data.ProxySettings;
import com.vmware.vim.binding.phonehome.service.NetworkConfigurationService;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;

public class NetworkConfigurationServiceMo implements NetworkConfigurationService {
  private static final int MAX_CONNECTIONS_TOTAL = 30;
  
  private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
  
  private static final int CONNECTION_TIMEOUT = 60000;
  
  private static final HttpConnectionConfig HTTP_CONNECTION_CONFIG = new HttpConnectionConfig(10, 30, 60000, null);
  
  private static final String MO_REF_TYPE = "PhonehomeServiceNetworkConfigurationService";
  
  private static final String MO_REF_ID = "ncService";
  
  private final ProxySettingsProvider _proxySettingsProvider;
  
  private final ManagedObjectReference _ref;
  
  public NetworkConfigurationServiceMo(ProxySettingsProvider proxySettingsProvider) {
    this._proxySettingsProvider = proxySettingsProvider;
    this._ref = new ManagedObjectReference("PhonehomeServiceNetworkConfigurationService", "ncService");
  }
  
  public ManagedObjectReference _getRef() {
    return this._ref;
  }
  
  public void get(Future<ProxySettings[]> result) {
    ProxySettings phProxySettings = this._proxySettingsProvider.getProxySettings(HTTP_CONNECTION_CONFIG);
    ProxySettings vmomiProxySettings = toVmomiProxySettings(phProxySettings);
    result.set(new ProxySettings[] { vmomiProxySettings });
  }
  
  public ProxySettings[] get() {
    throw new UnsupportedOperationException("Synchronous calls are not supported.");
  }
  
  public void set(ProxySettings[] proxySettings, Future<Void> result) {
    result.set();
  }
  
  public void set(ProxySettings[] proxySettings) {
    throw new UnsupportedOperationException("Synchronous calls are not supported.");
  }
  
  public static ProxySettings toVmomiProxySettings(ProxySettings phProxySettings) {
    ProxySettings vmomiProxySettings = new ProxySettings();
    vmomiProxySettings.setHostname(phProxySettings.getHostname());
    vmomiProxySettings.setPort(Integer.valueOf(phProxySettings.getPort()));
    vmomiProxySettings.setUsername(phProxySettings.getUsername());
    vmomiProxySettings.setPassword(phProxySettings.getPassword());
    return vmomiProxySettings;
  }
}
