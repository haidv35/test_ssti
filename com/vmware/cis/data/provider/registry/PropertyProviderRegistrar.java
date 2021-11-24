package com.vmware.cis.data.provider.registry;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyProviderRegistrar {
  private static Logger _logger = LoggerFactory.getLogger(PropertyProviderRegistrar.class);
  
  private final PropertyProviderRegistry _registry;
  
  private final List<Object> _propertyProviders;
  
  public PropertyProviderRegistrar(PropertyProviderRegistry registry, Object propertyProvider) {
    Validate.notNull(registry);
    Validate.notNull(propertyProvider);
    this._registry = registry;
    this._propertyProviders = Collections.singletonList(propertyProvider);
  }
  
  public PropertyProviderRegistrar(PropertyProviderRegistry registry, List<Object> propertyProviders) {
    Validate.notNull(registry);
    Validate.noNullElements(propertyProviders, "Collection of property providers must not contain null elements.");
    this._registry = registry;
    this._propertyProviders = propertyProviders;
  }
  
  public void register() {
    for (Object propertyProvider : this._propertyProviders) {
      this._registry.register(propertyProvider);
      _logger.info("Registered property provider '{}'", propertyProvider
          .getClass().getCanonicalName());
    } 
  }
  
  public void unregister() {
    for (Object propertyProvider : this._propertyProviders) {
      this._registry.unregister(propertyProvider);
      _logger.info("Unregistered property provider '{}'", propertyProvider
          .getClass().getCanonicalName());
    } 
  }
}
