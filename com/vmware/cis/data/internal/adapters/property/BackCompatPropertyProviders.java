package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class BackCompatPropertyProviders {
  public static final BackCompatPropertyProviderRepository VMOMI_PROPERTY_PROVIDER_REPOSITORY;
  
  static {
    List<BackCompatPropertyProvider> propertyProviders = new ArrayList<>();
    propertyProviders.add(new PathToTheRootPropertyProvider());
    propertyProviders.add(new CapacityPropertyProvider());
    propertyProviders.add(new VmFolderSummaryPropertyProvider());
    propertyProviders.add(new VappStoragePropertyProvider());
    propertyProviders.add(new VmVsanFaultDomainNamePropertyProvider());
    propertyProviders.add(new DatastoreIsReadOnlyPropertyProvider());
    VMOMI_PROPERTY_PROVIDER_REPOSITORY = BackCompatPropertyProviderRepository.forProviders(propertyProviders);
  }
  
  public static DataProvider withVmomiBackCompat(DataProvider provider, Client vlsiClient, ExecutorService executor) {
    assert provider != null;
    assert vlsiClient != null;
    assert executor != null;
    return new BackCompatPropertyProviderConnection(provider, VMOMI_PROPERTY_PROVIDER_REPOSITORY, vlsiClient, executor);
  }
}
