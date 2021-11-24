package com.vmware.ph.phservice.proxy;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CachedProxySettingsProvider implements ProxySettingsProvider {
  private static final Log _log = LogFactory.getLog(CachedProxySettingsProvider.class);
  
  private static final String KEY = "PROXY_SETTINGS_CACHE_KEY";
  
  private static final int PROXY_SETTINGS_CACHE_CAPACITY = 1;
  
  private final ProxySettingsProvider _wrappedProxySettingsProvider;
  
  private final Cache<String, ProxySettings> _proxySettingsCache;
  
  CachedProxySettingsProvider(ProxySettingsProvider wrappedProxySettingsProvider, int cacheExpirationIntervalMillis) {
    this._wrappedProxySettingsProvider = wrappedProxySettingsProvider;
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    if (_log.isDebugEnabled())
      cacheBuilder.recordStats(); 
    this


      
      ._proxySettingsCache = cacheBuilder.concurrencyLevel(1).expireAfterWrite(cacheExpirationIntervalMillis, TimeUnit.MILLISECONDS).maximumSize(1L).build();
  }
  
  public ProxySettings getProxySettings(HttpConnectionConfig connConfig) {
    try {
      return (ProxySettings)this._proxySettingsCache.get("PROXY_SETTINGS_CACHE_KEY", () -> {
            ProxySettings proxySettings = this._wrappedProxySettingsProvider.getProxySettings(connConfig);
            if (_log.isInfoEnabled())
              _log.info("Caching proxy settings: " + proxySettings); 
            if (_log.isDebugEnabled())
              _log.debug("Stats: " + this._proxySettingsCache.stats()); 
            return proxySettings;
          });
    } catch (ExecutionException e) {
      _log.error("Error loading settings", e);
      throw new UncheckedExecutionException(e);
    } 
  }
}
