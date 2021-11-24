package com.vmware.ph.phservice.provider.vcenter.mo;

import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoMethodDataProvider;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoReader;
import com.vmware.ph.phservice.provider.common.vmomi.mo.MoTypesProvider;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import com.vmware.vim.vmomi.core.types.VmodlVersion;

public class SingletonMoMethodDataProviderFactory {
  public static MoMethodDataProvider creatInternalMoMethodDataProvider(VcClient vcClient) {
    SingletonMoReader internalMoReader = SingletonMoReader.createInternalSingletonMoReader(vcClient);
    MoTypesProvider internalMoTypesProvider = InternalSingletonMoTypesProvider.forVcClient(vcClient);
    return createMoMethodDataProvider(vcClient, internalMoReader, internalMoTypesProvider);
  }
  
  public static MoMethodDataProvider createPublicMoMethodDataProvider(VcClient vcClient) {
    SingletonMoReader publicMoReader = SingletonMoReader.createPublicSingletonMoReader(vcClient);
    SingletonMoTypesProvider publicMoTypesProvider = new SingletonMoTypesProvider(vcClient);
    return createMoMethodDataProvider(vcClient, publicMoReader, publicMoTypesProvider);
  }
  
  private static MoMethodDataProvider createMoMethodDataProvider(VcClient vcClient, MoReader moReader, MoTypesProvider moTypesProvider) {
    VmodlTypeMap vmodlTypeMap = vcClient.getVmodlContext().getVmodlTypeMap();
    VmodlVersion vmodlVersion = vcClient.getVmodlVersion();
    return new MoMethodDataProvider(moReader, moTypesProvider, vmodlTypeMap, vmodlVersion);
  }
}
