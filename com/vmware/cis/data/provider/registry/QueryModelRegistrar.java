package com.vmware.cis.data.provider.registry;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryModelRegistrar {
  private static Logger _logger = LoggerFactory.getLogger(QueryModelRegistrar.class);
  
  private final QueryModelRegistry _registry;
  
  private final Class<?> _queryModel;
  
  public QueryModelRegistrar(QueryModelRegistry registry, Class<?> queryModel) {
    Validate.notNull(registry);
    Validate.notNull(queryModel);
    this._registry = registry;
    this._queryModel = queryModel;
  }
  
  public void register() {
    this._registry.registerQueryModel(this._queryModel);
    _logger.info("Registered QueryModel {}", this._queryModel.getName());
  }
  
  public void unregister() {
    this._registry.unregisterQueryModel(this._queryModel);
    _logger.info("Unregistered QueryModel {}", this._queryModel.getName());
  }
}
