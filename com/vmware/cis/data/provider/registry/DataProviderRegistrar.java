package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.DataProvider;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataProviderRegistrar {
  private static Logger _logger = LoggerFactory.getLogger(DataProviderRegistrar.class);
  
  private final DataProviderRegistry _registry;
  
  private final DataProvider _provider;
  
  public DataProviderRegistrar(DataProviderRegistry registry, DataProvider provider) {
    Validate.notNull(registry);
    Validate.notNull(provider);
    this._registry = registry;
    this._provider = provider;
  }
  
  public void register() {
    this._registry.register(this._provider);
    _logger.info("Registered data provider {}", this._provider);
  }
  
  public void unregister() {
    this._registry.unregister(this._provider);
    _logger.info("Unregistered data provider {}", this._provider);
  }
}
