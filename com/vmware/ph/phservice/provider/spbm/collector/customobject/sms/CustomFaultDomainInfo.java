package com.vmware.ph.phservice.provider.spbm.collector.customobject.sms;

import com.vmware.vim.binding.sms.storage.replication.FaultDomainInfo;

public class CustomFaultDomainInfo extends FaultDomainInfo {
  private static final long serialVersionUID = 1L;
  
  public String providerUid;
  
  public String getProviderUid() {
    return this.providerUid;
  }
  
  public void setProviderUid(String providerUid) {
    this.providerUid = providerUid;
  }
}
