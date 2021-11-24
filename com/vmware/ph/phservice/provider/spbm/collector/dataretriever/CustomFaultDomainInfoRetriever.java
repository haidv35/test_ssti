package com.vmware.ph.phservice.provider.spbm.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.CopyUtil;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import com.vmware.ph.phservice.provider.spbm.collector.customobject.sms.CustomFaultDomainInfo;
import com.vmware.vim.binding.sms.ServiceInstance;
import com.vmware.vim.binding.sms.StorageManager;
import com.vmware.vim.binding.sms.provider.VasaProvider;
import com.vmware.vim.binding.sms.provider.VasaProviderInfo;
import com.vmware.vim.binding.sms.storage.replication.FaultDomainInfo;
import com.vmware.vim.binding.vim.vm.replication.FaultDomainId;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.core.impl.BlockingFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomFaultDomainInfoRetriever implements DataRetriever<CustomFaultDomainInfo> {
  private static final Log log = LogFactory.getLog(CustomFaultDomainInfoRetriever.class);
  
  private final SmsServiceClient _smsClient;
  
  public CustomFaultDomainInfoRetriever(SpbmCollectorContext collectorContext) {
    this._smsClient = collectorContext.getSmsServiceClient();
  }
  
  public List<CustomFaultDomainInfo> retrieveData() {
    List<CustomFaultDomainInfo> faultDomainInfoList = Collections.emptyList();
    try {
      ServiceInstance smsServiceInstance = this._smsClient.getServiceInstance();
      BlockingFuture blockingFuture = new BlockingFuture();
      smsServiceInstance.queryStorageManager((Future)blockingFuture);
      ManagedObjectReference storageManagerMor = (ManagedObjectReference)blockingFuture.get();
      StorageManager storageManager = this._smsClient.<StorageManager>createStub(storageManagerMor);
      FaultDomainId[] faulDomainIdArray = storageManager.queryFaultDomain(null);
      if (faulDomainIdArray != null) {
        faultDomainInfoList = new ArrayList<>(faulDomainIdArray.length);
        for (FaultDomainId fdId : faulDomainIdArray) {
          if (fdId instanceof FaultDomainInfo) {
            FaultDomainInfo smsFdInfo = (FaultDomainInfo)fdId;
            CustomFaultDomainInfo fdInfo = new CustomFaultDomainInfo();
            CopyUtil.copyPublicFields(smsFdInfo, fdInfo);
            VasaProvider vasaProvider = this._smsClient.<VasaProvider>createStub(smsFdInfo.getProvider());
            VasaProviderInfo providerInfo = (VasaProviderInfo)vasaProvider.queryProviderInfo();
            fdInfo.setProviderUid(providerInfo.getUid());
            faultDomainInfoList.add(fdInfo);
          } 
        } 
      } else if (log.isDebugEnabled()) {
        log.debug("No FaultDomains found in the environment.");
      } 
    } catch (InterruptedException|java.util.concurrent.ExecutionException|com.vmware.vim.binding.sms.fault.QueryExecutionFault|com.vmware.vim.binding.vim.fault.NotFound e) {
      log.warn("Error occurred while retrieving FaultDomainInfo", e);
      if (e instanceof InterruptedException)
        Thread.currentThread().interrupt(); 
    } 
    return faultDomainInfoList;
  }
  
  public String getKey(CustomFaultDomainInfo t) {
    return t.getId();
  }
}
