package com.vmware.ph.phservice.provider.vcenter;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import com.vmware.ph.phservice.provider.common.vim.internal.VcContextFactory;
import com.vmware.ph.phservice.provider.vcenter.alarms.AlarmsDataProviderImpl;
import com.vmware.ph.phservice.provider.vcenter.configoption.ConfigOptionsDataProviderWrapper;
import com.vmware.ph.phservice.provider.vcenter.event.EventsDataProvider;
import com.vmware.ph.phservice.provider.vcenter.extensions.ExtensionsDataProvider;
import com.vmware.ph.phservice.provider.vcenter.mo.InternalSingletonMoDataProvider;
import com.vmware.ph.phservice.provider.vcenter.mo.SingletonMoDataProvider;
import com.vmware.ph.phservice.provider.vcenter.mo.SingletonMoMethodDataProviderFactory;
import com.vmware.ph.phservice.provider.vcenter.paging.PagingDataProviderWrapper;
import com.vmware.ph.phservice.provider.vcenter.performance.PerfMetricsDataProvider;
import com.vmware.ph.phservice.provider.vcenter.resourcemodel.PcResourceModelDataProviderImpl;
import com.vmware.ph.phservice.provider.vcenter.resourcemodel.RiseResourceModelDataProviderImpl;
import com.vmware.ph.phservice.provider.vcenter.resourcemodel.VimRiseResourceModelDataProviderImpl;
import com.vmware.ph.phservice.provider.vcenter.task.TasksDataProvider;
import com.vmware.vim.binding.cis.data.provider.version.version1;
import com.vmware.vim.binding.vim.version.version12;
import com.vmware.vim.vmomi.core.types.VmodlVersion;
import java.util.Arrays;
import java.util.List;

public class VcDataProvidersConnectionImpl implements DataProvidersConnection, ContextFactory {
  private Builder<VcClient> _vcClientBuilder;
  
  private VcClient _vcClient;
  
  private final boolean _shouldDisposeClient;
  
  private final VcContextFactory _vcContextFactory;
  
  public VcDataProvidersConnectionImpl(Builder<VcClient> vcClientBuilder) {
    this._vcClientBuilder = vcClientBuilder;
    this._shouldDisposeClient = true;
    this._vcContextFactory = new VcContextFactory(vcClientBuilder);
  }
  
  VcDataProvidersConnectionImpl(VcClient vcClient) {
    this._vcClient = vcClient;
    this._shouldDisposeClient = false;
    this._vcContextFactory = null;
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    if (this._vcClient == null && this._vcClientBuilder != null)
      this._vcClient = this._vcClientBuilder.build(); 
    DataProvider resourceModelDataProvider = new PagingDataProviderWrapper(new ConfigOptionsDataProviderWrapper(createResourceModelDataProvider(this._vcClient)));
    DataProvider alarmsDataProvider = new AlarmsDataProviderImpl(this._vcClient);
    DataProvider singletonMoDataProvider = new SingletonMoDataProvider(this._vcClient, resourceModelDataProvider);
    DataProvider internalSingletonMoDataProvider = new InternalSingletonMoDataProvider(this._vcClient, new DataProvider[] { singletonMoDataProvider });
    DataProvider singletonMoMethodDataProvider = SingletonMoMethodDataProviderFactory.createPublicMoMethodDataProvider(this._vcClient);
    DataProvider internalSingletonMoMethodDataProvider = SingletonMoMethodDataProviderFactory.creatInternalMoMethodDataProvider(this._vcClient);
    DataProvider extensionsDataProvider = new ExtensionsDataProvider(this._vcClient);
    DataProvider eventsDataProvider = new EventsDataProvider(this._vcClient);
    DataProvider tasksDataProvider = new TasksDataProvider(this._vcClient);
    PerfMetricsDataProvider perfDataProvider = new PerfMetricsDataProvider(this._vcClient);
    return Arrays.asList(new DataProvider[] { resourceModelDataProvider, singletonMoDataProvider, singletonMoMethodDataProvider, internalSingletonMoDataProvider, internalSingletonMoMethodDataProvider, extensionsDataProvider, eventsDataProvider, tasksDataProvider, alarmsDataProvider, perfDataProvider });
  }
  
  public Context createContext(String collectorId, String collectorInstanceId, String collectionId) {
    Context context = null;
    if (this._vcContextFactory != null)
      context = this._vcContextFactory.createContext(collectorId, collectorInstanceId, collectionId); 
    return context;
  }
  
  public void close() {
    if (this._vcClient != null && this._shouldDisposeClient)
      this._vcClient.close(); 
  }
  
  public static DataProvider createResourceModelDataProvider(VcClient vcClient) {
    DataProvider dataProvider = null;
    VmodlVersion riseVersion1 = vcClient.getVmodlContext().getVmodlVersionMap().getVersion(version1.class);
    VmodlVersion riseVersion12 = vcClient.getVmodlContext().getVmodlVersionMap().getVersion(version12.class);
    if (riseVersion1 != null && vcClient
      .getVmodlVersion().isCompatible(riseVersion1)) {
      dataProvider = new RiseResourceModelDataProviderImpl(vcClient);
    } else if (riseVersion12 != null && vcClient
      .getVmodlVersion().isCompatible(riseVersion12)) {
      dataProvider = new VimRiseResourceModelDataProviderImpl(vcClient);
    } else {
      dataProvider = new PcResourceModelDataProviderImpl(vcClient);
    } 
    return dataProvider;
  }
}
