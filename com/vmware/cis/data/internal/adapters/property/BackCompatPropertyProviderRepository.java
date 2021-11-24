package com.vmware.cis.data.internal.adapters.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class BackCompatPropertyProviderRepository {
  private final Map<String, BackCompatPropertyProvider> _providersByProperty;
  
  private final Collection<String> _properties;
  
  private BackCompatPropertyProviderRepository(Map<String, BackCompatPropertyProvider> providersByProperty) {
    assert providersByProperty != null;
    this._providersByProperty = providersByProperty;
    this._properties = Collections.unmodifiableCollection(providersByProperty.keySet());
  }
  
  public static BackCompatPropertyProviderRepository forProviders(Collection<BackCompatPropertyProvider> providers) {
    assert providers != null;
    assert !providers.isEmpty();
    Map<String, BackCompatPropertyProvider> providersByProperty = new HashMap<>();
    for (BackCompatPropertyProvider provider : providers)
      registerPropertyProvider(provider, providersByProperty); 
    return new BackCompatPropertyProviderRepository(providersByProperty);
  }
  
  public Collection<String> getProperties() {
    return this._properties;
  }
  
  public Map<BackCompatPropertyProvider, List<String>> getPropertiesByProvider(Collection<String> properties) {
    assert properties != null;
    assert !properties.isEmpty();
    Map<BackCompatPropertyProvider, List<String>> propertiesByProvider = new HashMap<>();
    for (String property : properties) {
      BackCompatPropertyProvider provider = getProviderForProperty(property);
      List<String> providerProperties = propertiesByProvider.get(provider);
      if (providerProperties == null) {
        providerProperties = new ArrayList<>();
        propertiesByProvider.put(provider, providerProperties);
      } 
      providerProperties.add(property);
    } 
    return propertiesByProvider;
  }
  
  private BackCompatPropertyProvider getProviderForProperty(String property) {
    assert property != null;
    BackCompatPropertyProvider provider = this._providersByProperty.get(property);
    if (provider == null)
      throw new IllegalArgumentException("No back-compat provider for property " + property); 
    return provider;
  }
  
  private static void registerPropertyProvider(BackCompatPropertyProvider provider, Map<String, BackCompatPropertyProvider> providersByProperty) {
    Collection<String> properties = provider.getProperties();
    for (String property : properties) {
      BackCompatPropertyProvider old = providersByProperty.put(property, provider);
      if (old != null) {
        String msg = String.format("Back-compat provider %s overrides property '%s' defined by back-compat provider %s", new Object[] { provider, property, old });
        throw new IllegalArgumentException(msg);
      } 
    } 
  }
}
