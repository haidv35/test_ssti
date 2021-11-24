package com.vmware.ph.phservice.provider.vcenter.resourcemodel;

import com.vmware.cis.data.api.Query;
import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPcStartMoRefProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.QueryContextUtil;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcDataProviderImpl;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcSchemaConverter;

public class PcResourceModelDataProviderImpl implements DataProvider {
  private final PcDataProviderImpl _pcDataProvider;
  
  public PcResourceModelDataProviderImpl(VcClient vcClient) {
    PcSchemaConverter pcSchemaConverter = new PcResourceModelSchemaConverter(vcClient.getVmodlContext().getVmodlTypeMap());
    this



      
      ._pcDataProvider = new PcDataProviderImpl(new VimPropertyCollectorReader(vcClient.getVlsiClient()), new VimPcStartMoRefProvider(vcClient.getVlsiClient()), vcClient.getVmodlContext().getVmodlTypeMap(), vcClient.getVmodlVersion(), pcSchemaConverter);
  }
  
  public ResultSet executeQuery(Query query) {
    query = QueryContextUtil.removeContextFromQueryFilter(query);
    return this._pcDataProvider.executeQuery(query);
  }
  
  public QuerySchema getSchema() {
    return this._pcDataProvider.getSchema();
  }
}
