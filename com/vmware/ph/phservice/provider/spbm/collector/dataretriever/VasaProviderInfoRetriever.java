package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.sms.ServiceInstance;
import com.vmware.vim.binding.sms.StorageManager;
import com.vmware.vim.binding.sms.provider.VasaProvider;
import com.vmware.vim.binding.sms.provider.VasaProviderInfo;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VasaProviderInfoRetriever implements DataRetriever<VasaProviderInfo> {
  private static final Log log = LogFactory.getLog(VasaProviderInfoRetriever.class);
  
  private final SmsServiceClient _smsClient;
  
  public VasaProviderInfoRetriever(SpbmCollectorContext collectorContext) {
    this._smsClient = collectorContext.getSmsServiceClient();
  }
  
  public List<VasaProviderInfo> retrieveData() {
    List<VasaProviderInfo> vasaProviderInfoList = Collections.emptyList();
    StorageManager storageManager = createStorageManagerStub();
    try {
      BlockingFuture blockingFuture = new BlockingFuture();
      storageManager.queryProvider((Future)blockingFuture);
      ManagedObjectReference[] provider = (ManagedObjectReference[])blockingFuture.get();
      if (provider != null) {
        if (log.isDebugEnabled())
          log.debug("Creating VasaProvider stub to retrieve providerInfo"); 
        vasaProviderInfoList = new ArrayList<>(provider.length);
        for (int i = 0; i < provider.length; i++) {
          VasaProvider vasaProvider = this._smsClient.<VasaProvider>createStub(provider[i]);
          VasaProviderInfo providerInfo = (VasaProviderInfo)vasaProvider.queryProviderInfo();
          vasaProviderInfoList.add(providerInfo);
        } 
      } else if (log.isDebugEnabled()) {
        log.debug("No providers present in the environment");
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured while retrieving providers", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return vasaProviderInfoList;
  }
  
  private StorageManager createStorageManagerStub() {
    StorageManager storageManager = null;
    try {
      ServiceInstance smsServiceInstance = this._smsClient.getServiceInstance();
      if (log.isDebugEnabled())
        log.debug("Creating StorageManager stub to retrieve Providers"); 
      BlockingFuture blockingFuture = new BlockingFuture();
      smsServiceInstance.queryStorageManager((Future)blockingFuture);
      ManagedObjectReference storageManagerMor = (ManagedObjectReference)blockingFuture.get();
      storageManager = this._smsClient.<StorageManager>createStub(storageManagerMor);
    } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
      log.warn("Error occured getting StorageManager", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return storageManager;
  }
  
  public String getKey(VasaProviderInfo t) {
    return t.getUid();
  }
}
