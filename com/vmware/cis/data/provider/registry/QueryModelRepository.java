package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.model.QueryModel;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.Validate;

public final class QueryModelRepository implements QueryModelRegistry, QueryModelLookup {
  private final ConcurrentMap<Class<?>, String> _modelClasses = new ConcurrentHashMap<>();
  
  public static QueryModelRepository createQueryModelRepository() {
    return new QueryModelRepository();
  }
  
  public void registerQueryModel(Class<?> modelClass) {
    Validate.notNull(modelClass);
    this._modelClasses.putIfAbsent(modelClass, getQueryModelName(modelClass));
  }
  
  public void unregisterQueryModel(Class<?> modelClass) {
    Validate.notNull(modelClass);
    this._modelClasses.remove(modelClass);
  }
  
  public Set<Class<?>> getRegisteredQueryModels() {
    return Collections.unmodifiableSet(this._modelClasses.keySet());
  }
  
  private static String getQueryModelName(Class<?> modelClass) {
    QueryModel modelDefinition = modelClass.<QueryModel>getAnnotation(QueryModel.class);
    Validate.notNull(modelDefinition, "The provided class is not annotated with @QueryModel annotation.");
    return modelDefinition.value();
  }
}
