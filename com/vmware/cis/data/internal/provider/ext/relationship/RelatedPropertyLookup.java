package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.api.QuerySchema;
import java.util.List;
import java.util.Map;

interface RelatedPropertyLookup {
  RelatedPropertyDescriptor getRelatedPropertyDescriptor(String paramString);
  
  Map<String, RelatedPropertyDescriptor> getRelatedPropertyDescriptors(List<String> paramList);
  
  QuerySchema addRelatedProps(QuerySchema paramQuerySchema);
}
