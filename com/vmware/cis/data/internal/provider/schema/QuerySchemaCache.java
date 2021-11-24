package com.vmware.cis.data.internal.provider.schema;

import com.vmware.cis.data.api.QuerySchema;
import java.util.concurrent.Callable;

public interface QuerySchemaCache {
  QuerySchema get(String paramString, Callable<QuerySchema> paramCallable);
}
