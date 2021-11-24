package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.vcenter.VcenterDataProviderFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang.Validate;

public final class VcenterDataProviderFactoryConcurrentCollection implements VcenterDataProviderFactoryRegistry, VcenterDataProviderFactoryLookup {
  private final CopyOnWriteArrayList<VcenterDataProviderFactory> _providerFactories = new CopyOnWriteArrayList<>();
  
  public void register(VcenterDataProviderFactory providerFactory) {
    Validate.notNull(providerFactory);
    boolean added = this._providerFactories.addIfAbsent(providerFactory);
    if (!added)
      throw new IllegalArgumentException("Already registered: " + providerFactory); 
  }
  
  public void unregister(VcenterDataProviderFactory providerFactory) {
    Validate.notNull(providerFactory);
    this._providerFactories.remove(providerFactory);
  }
  
  public Collection<VcenterDataProviderFactory> get() {
    return Collections.unmodifiableCollection(this._providerFactories);
  }
}
