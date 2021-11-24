package com.vmware.cis.data.internal.provider;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.provider.DataProvider;
import java.util.Collection;

public interface ProviderBySchemaLookup {
  DataProvider getProviderForProperty(String paramString);
  
  DataProvider getProviderForProperties(Collection<String> paramCollection);
  
  DataProvider getProviderForModel(String paramString);
  
  DataProvider getProviderForModels(Collection<String> paramCollection);
  
  QuerySchema getSchema();
}
