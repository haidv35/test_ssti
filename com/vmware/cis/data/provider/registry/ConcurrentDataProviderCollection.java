package com.vmware.cis.data.provider.registry;

import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang.Validate;

public final class ConcurrentDataProviderCollection implements DataProviderRegistry, DataProviderLookup {
  private final CopyOnWriteArrayList<DataProvider> _dataProviders = new CopyOnWriteArrayList<>();
  
  public void register(DataProvider dataProvider) {
    Validate.notNull(dataProvider);
    boolean added = this._dataProviders.addIfAbsent(dataProvider);
    if (!added)
      throw new IllegalArgumentException(String.format("Data Provider already registered: '%s'!", new Object[] { dataProvider })); 
  }
  
  public void unregister(DataProvider dataProvider) {
    Validate.notNull(dataProvider);
    this._dataProviders.remove(dataProvider);
  }
  
  public Collection<DataProvider> getProviders() {
    return this._dataProviders;
  }
}
