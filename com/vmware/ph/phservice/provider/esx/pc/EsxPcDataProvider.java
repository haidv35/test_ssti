package com.vmware.ph.phservice.provider.esx.pc;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPcStartMoRefProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.provider.common.vim.pc.VimPcSchemaConverter;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcDataProviderImpl;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;

public class EsxPcDataProvider implements DataProvider {
  private final PcDataProviderImpl _innerPcDataProvider;
  
  public EsxPcDataProvider(VmomiClient esxClient) {
    VmodlTypeMap vmodlTypeMap = esxClient.getVmodlContext().getVmodlTypeMap();
    this



      
      ._innerPcDataProvider = new PcDataProviderImpl(new VimPropertyCollectorReader(esxClient.getVlsiClient()), new VimPcStartMoRefProvider(esxClient.getVlsiClient()), vmodlTypeMap, esxClient.getVmodlVersion(), new VimPcSchemaConverter(vmodlTypeMap));
  }
  
  public QuerySchema getSchema() {
    return this._innerPcDataProvider.getSchema();
  }
  
  public ResultSet executeQuery(Query query) {
    return this._innerPcDataProvider.executeQuery(query);
  }
}
