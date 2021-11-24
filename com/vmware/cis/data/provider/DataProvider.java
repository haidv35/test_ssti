package com.vmware.cis.data.provider;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;

public interface DataProvider {
  ResultSet executeQuery(Query paramQuery);
  
  QuerySchema getSchema();
}
