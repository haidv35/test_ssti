package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.vim.binding.sms.ServiceInstance;
import com.vmware.vim.binding.sms.StorageManager;
import com.vmware.vim.binding.sms.storage.StorageContainer;
import com.vmware.vim.binding.sms.storage.StorageContainerResult;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StorageContainerRetriever implements DataRetriever<StorageContainer> {
  private static final Log log = LogFactory.getLog(StorageContainerRetriever.class);
  
  private final SmsServiceClient _smsClient;
  
  public StorageContainerRetriever(SpbmCollectorContext collectorContext) {
    this._smsClient = collectorContext.getSmsServiceClient();
  }
  
  public List<StorageContainer> retrieveData() {
    List<StorageContainer> storageContainerList = Collections.emptyList();
    try {
      ServiceInstance smsServiceInstance = this._smsClient.getServiceInstance();
      BlockingFuture blockingFuture = new BlockingFuture();
      smsServiceInstance.queryStorageManager((Future)blockingFuture);
      ManagedObjectReference storageManagerMor = (ManagedObjectReference)blockingFuture.get();
      StorageManager storageManager = this._smsClient.<StorageManager>createStub(storageManagerMor);
      StorageContainerResult storageContainerResult = storageManager.queryStorageContainer(null);
      if (storageContainerResult != null) {
        StorageContainer[] storageContainers = storageContainerResult.getStorageContainer();
        if (storageContainers != null)
          storageContainerList = Arrays.asList(storageContainers); 
      } else if (log.isDebugEnabled()) {
        log.debug("No storage containers in the environment.");
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException|com.vmware.vim.binding.sms.fault.QueryExecutionFault|com.vmware.vim.binding.vim.fault.NotFound e) {
      log.warn("Error occurred while retrieving StorageContainer", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return storageContainerList;
  }
  
  public String getKey(StorageContainer t) {
    return t.getUuid();
  }
}
