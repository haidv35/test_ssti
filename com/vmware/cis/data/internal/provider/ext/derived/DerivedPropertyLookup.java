package com.vmware.cis.data.internal.provider.ext.derived;

import com.vmware.cis.data.api.QuerySchema;

interface DerivedPropertyLookup {
  DerivedPropertyDescriptor getDerivedPropertyDescriptor(String paramString);
  
  QuerySchema addDerivedProps(QuerySchema paramQuerySchema);
}
