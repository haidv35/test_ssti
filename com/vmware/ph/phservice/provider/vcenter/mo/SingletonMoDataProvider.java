package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPcStartMoRefProvider;
import com.vmware.ph.phservice.common.vim.pc.VimPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcDataProviderImpl;
import com.vmware.ph.phservice.provider.common.vmomi.pc.PcSchemaConverter;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;

public class SingletonMoDataProvider extends PcDataProviderImpl {
  public SingletonMoDataProvider(VcClient vcClient, DataProvider conflictingDataProvider) {
    super(new VimPropertyCollectorReader(vcClient.getVlsiClient()), new VimPcStartMoRefProvider(vcClient
          .getVlsiClient()), vcClient
        .getVmodlContext().getVmodlTypeMap(), vcClient
        .getVmodlVersion(), 
        createPcSchemaConverter(vcClient), new DataProvider[] { conflictingDataProvider });
  }
  
  private static PcSchemaConverter createPcSchemaConverter(VcClient vcClient) {
    VmodlTypeMap vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    return new SingletonMoSchemaConverter(vmodlTypeMap, vcClient);
  }
}
