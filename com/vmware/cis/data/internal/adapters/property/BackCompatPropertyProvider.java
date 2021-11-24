package com.vmware.cis.data.internal.adapters.property;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.vim.vmomi.client.Client;
import java.util.Collection;
import java.util.List;

public interface BackCompatPropertyProvider {
  Collection<String> getProperties();
  
  List<Collection<?>> fetchPropertyValues(List<String> paramList, Collection<Object> paramCollection, DataProvider paramDataProvider, Client paramClient);
}
