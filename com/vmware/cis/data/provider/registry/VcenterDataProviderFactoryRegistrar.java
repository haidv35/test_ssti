package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.vcenter.VcenterDataProviderFactory;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VcenterDataProviderFactoryRegistrar {
  private static Logger _logger = LoggerFactory.getLogger(VcenterDataProviderFactoryRegistrar.class);
  
  private final VcenterDataProviderFactoryRegistry _registry;
  
  private final VcenterDataProviderFactory _providerFactory;
  
  public VcenterDataProviderFactoryRegistrar(VcenterDataProviderFactoryRegistry registry, VcenterDataProviderFactory providerFactory) {
    Validate.notNull(registry);
    Validate.notNull(providerFactory);
    this._registry = registry;
    this._providerFactory = providerFactory;
  }
  
  public void register() {
    this._registry.register(this._providerFactory);
    _logger.info("Registered VcenterDataProviderFactory {}", this._providerFactory);
  }
  
  public void unregister() {
    this._registry.unregister(this._providerFactory);
    _logger.info("Unregistered VcenterDataProviderFactory {}", this._providerFactory);
  }
}
