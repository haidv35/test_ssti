package com.vmware.cis.data.internal.provider.ext.alias;

import com.vmware.cis.data.api.QuerySchema;
import java.util.List;
import java.util.Map;

interface AliasPropertyLookup {
  AliasPropertyDescriptor getAliasPropertyDescriptor(String paramString);
  
  Map<String, AliasPropertyDescriptor> getAliasPropertyDescriptors(List<String> paramList);
  
  QuerySchema calculateAliasPropertySchema(QuerySchema paramQuerySchema);
}
