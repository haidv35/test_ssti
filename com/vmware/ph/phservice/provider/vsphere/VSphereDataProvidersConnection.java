package com.vmware.ph.phservice.provider.vsphere;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.appliance.ApplianceDataProvidersConnection;
import com.vmware.ph.phservice.provider.appliance.healthstatus.HealthStatusDataProvidersConnection;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.CompositeDataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import com.vmware.ph.phservice.provider.common.internal.SafeDataProvidersConnectionWrapper;
import com.vmware.ph.phservice.provider.common.vim.internal.VcContextFactory;
import com.vmware.ph.phservice.provider.esx.VcEsxDataProvidersConnectionImpl;
import com.vmware.ph.phservice.provider.fcd.collector.FcdDataProvidersConnectionImpl;
import com.vmware.ph.phservice.provider.spbm.SpbmDataProvidersConnectionImpl;
import com.vmware.ph.phservice.provider.vcenter.VcVimDataProvidersConnectionImpl;
import com.vmware.ph.phservice.provider.vcenter.contentlibrary.ContentLibraryDataProvidersConnection;
import com.vmware.ph.phservice.provider.vcenter.license.LicenseDataProvidersConnection;
import com.vmware.ph.phservice.provider.vcenter.lookup.LookupDataProvidersConnection;
import com.vmware.ph.phservice.provider.vsan.VsanHealthDataProvidersConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VSphereDataProvidersConnection implements DataProvidersConnection, ContextFactory {
  private final CompositeDataProvidersConnection _compositeDpc;
  
  private final VcContextFactory _vcContextFactory;
  
  public VSphereDataProvidersConnection(VimContext readOnlyVimContext, VimContext nonReadOnlyVimContext) {
    this(readOnlyVimContext, nonReadOnlyVimContext, new DefaultVcClientProviderFactory(), new DefaultVSphereDataProvidersConnectionFactory());
  }
  
  VSphereDataProvidersConnection(VimContext readOnlyVimContext, VimContext nonReadOnlyVimContext, VcClientProviderFactory vcClientProviderFactory, VSphereDataProvidersConnectionFactory dataProvidersFactory) {
    VcClientProvider readOnlyVcClientProvider = vcClientProviderFactory.createVcClientProvider(readOnlyVimContext);
    VcClientProvider nonReadOnlyVcClientProvider = vcClientProviderFactory.createVcClientProvider(nonReadOnlyVimContext);
    this
      
      ._compositeDpc = new CompositeDataProvidersConnection(Collections.unmodifiableList(dataProvidersFactory
          .getDataProvidersConnectionList(readOnlyVimContext, readOnlyVcClientProvider, nonReadOnlyVimContext, nonReadOnlyVcClientProvider)), new AutoCloseable[] { (AutoCloseable)readOnlyVcClientProvider, (AutoCloseable)nonReadOnlyVcClientProvider });
    this._vcContextFactory = new VcContextFactory(readOnlyVcClientProvider);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    return this._compositeDpc.getDataProviders();
  }
  
  public Context createContext(String collectorId, String collectionInstanceId, String collectionId) {
    return this._vcContextFactory.createContext(collectorId, collectionInstanceId, collectionId);
  }
  
  public void close() {
    this._compositeDpc.close();
  }
  
  static interface VSphereDataProvidersConnectionFactory {
    List<DataProvidersConnection> getDataProvidersConnectionList(VimContext param1VimContext1, VcClientProvider param1VcClientProvider1, VimContext param1VimContext2, VcClientProvider param1VcClientProvider2);
  }
  
  static interface VcClientProviderFactory {
    VcClientProvider createVcClientProvider(VimContext param1VimContext);
  }
  
  static class DefaultVcClientProviderFactory implements VcClientProviderFactory {
    public VcClientProvider createVcClientProvider(VimContext vimContext) {
      return (VcClientProvider)new VimContextVcClientProviderImpl(vimContext);
    }
  }
  
  static class DefaultVSphereDataProvidersConnectionFactory implements VSphereDataProvidersConnectionFactory {
    public List<DataProvidersConnection> getDataProvidersConnectionList(VimContext readOnlyVimContext, VcClientProvider readOnlyVcClientProvider, VimContext nonReadOnlyVimContext, VcClientProvider nonReadOnlyVcClientProvider) {
      List<DataProvidersConnection> dpcList = new ArrayList<>();
      dpcList.addAll(getReadOnlyDataProvidersConnectionList(readOnlyVimContext, readOnlyVcClientProvider));
      dpcList.addAll(getNonReadOnlyDataProvidersConnectionList(nonReadOnlyVimContext, nonReadOnlyVcClientProvider));
      dpcList = SafeDataProvidersConnectionWrapper.wrapDataProvidersConnectionsSafe(dpcList);
      return dpcList;
    }
    
    private List<DataProvidersConnection> getReadOnlyDataProvidersConnectionList(VimContext readOnlyVimContext, VcClientProvider readOnlyVcClientProvider) {
      List<DataProvidersConnection> dpcList = new ArrayList<>();
      VcVimDataProvidersConnectionImpl vcVimDataProvidersConnectionImpl = new VcVimDataProvidersConnectionImpl(readOnlyVimContext, readOnlyVcClientProvider);
      dpcList.add(vcVimDataProvidersConnectionImpl);
      LicenseDataProvidersConnection licenseDataProvidersConnection = new LicenseDataProvidersConnection(readOnlyVimContext, readOnlyVcClientProvider);
      dpcList.add(licenseDataProvidersConnection);
      ContentLibraryDataProvidersConnection contentLibraryDataProvidersConnection = new ContentLibraryDataProvidersConnection(readOnlyVimContext);
      dpcList.add(contentLibraryDataProvidersConnection);
      CisContext readOnlyCisContext = readOnlyVimContext.getCisContext();
      ApplianceDataProvidersConnection applianceDataProvidersConnection = new ApplianceDataProvidersConnection(readOnlyCisContext);
      dpcList.add(applianceDataProvidersConnection);
      LookupDataProvidersConnection lookupDataProvidersConnection = new LookupDataProvidersConnection(readOnlyCisContext);
      dpcList.add(lookupDataProvidersConnection);
      HealthStatusDataProvidersConnection healthStatusProvidersConnection = new HealthStatusDataProvidersConnection(readOnlyCisContext);
      dpcList.add(healthStatusProvidersConnection);
      return dpcList;
    }
    
    private List<DataProvidersConnection> getNonReadOnlyDataProvidersConnectionList(VimContext nonReadOnlyVimContext, VcClientProvider nonReadOnlyVcClientProvider) {
      List<DataProvidersConnection> dpcList = new ArrayList<>();
      VcEsxDataProvidersConnectionImpl vcEsxDataProvidersConnectionImpl = new VcEsxDataProvidersConnectionImpl(nonReadOnlyVimContext, nonReadOnlyVcClientProvider);
      dpcList.add(vcEsxDataProvidersConnectionImpl);
      SpbmDataProvidersConnectionImpl spbmDataProvidersConnectionImpl = new SpbmDataProvidersConnectionImpl(nonReadOnlyVimContext, nonReadOnlyVcClientProvider);
      dpcList.add(spbmDataProvidersConnectionImpl);
      FcdDataProvidersConnectionImpl fcdDataProvidersConnectionImpl = new FcdDataProvidersConnectionImpl(nonReadOnlyVimContext, nonReadOnlyVcClientProvider);
      dpcList.add(fcdDataProvidersConnectionImpl);
      VsanHealthDataProvidersConnection vsanHealthDataProvidersConnection = new VsanHealthDataProvidersConnection(nonReadOnlyVimContext);
      dpcList.add(vsanHealthDataProvidersConnection);
      return dpcList;
    }
  }
}
