package com.vmware.cis.data.internal.provider.ext.predicate;

import com.vmware.cis.data.api.QuerySchema;

interface PredicatePropertyLookup {
  PredicatePropertyDescriptor getPredicatePropertyDescriptor(String paramString);
  
  QuerySchema addPredicateProps(QuerySchema paramQuerySchema);
}
