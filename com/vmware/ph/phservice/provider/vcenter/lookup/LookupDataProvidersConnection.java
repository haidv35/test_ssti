package com.vmware.ph.phservice.provider.vcenter.lookup;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.Collections;
import java.util.List;

public class LookupDataProvidersConnection implements DataProvidersConnection {
  private final CisContext _cisContext;
  
  private LookupClient _lookupClient;
  
  public LookupDataProvidersConnection(CisContext cisContext) {
    this._cisContext = cisContext;
  }
  
  public List<DataProvider> getDataProviders() {
    if (this._lookupClient == null)
      this._lookupClient = this._cisContext.getLookupClientBuilder().build(); 
    DataProvider lookupDataProvider = new LookupDataProvider(this._lookupClient);
    return Collections.singletonList(lookupDataProvider);
  }
  
  public void close() {
    if (this._lookupClient != null)
      this._lookupClient.close(); 
  }
}
