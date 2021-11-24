package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.model.QueryModel;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang.Validate;

public final class PropertyProviderConcurrentCollection implements PropertyProviderRegistry, PropertyProviderLookup {
  private final CopyOnWriteArrayList<Object> _propertyProviders = new CopyOnWriteArrayList();
  
  public void register(Object propertyProvider) {
    Validate.notNull(propertyProvider);
    Validate.isTrue(!isQueryModelAnnotationPresent(propertyProvider), "The " + propertyProvider
        .getClass().getName() + " property provider should not be annotated with @QueryModel.");
    boolean added = this._propertyProviders.addIfAbsent(propertyProvider);
    if (!added)
      throw new IllegalArgumentException("Already registered: " + propertyProvider); 
  }
  
  public void unregister(Object propertyProvider) {
    Validate.notNull(propertyProvider);
    this._propertyProviders.remove(propertyProvider);
  }
  
  public Collection<Object> get() {
    return Collections.unmodifiableCollection(this._propertyProviders);
  }
  
  private static boolean isQueryModelAnnotationPresent(Object object) {
    return object.getClass().isAnnotationPresent((Class)QueryModel.class);
  }
}
