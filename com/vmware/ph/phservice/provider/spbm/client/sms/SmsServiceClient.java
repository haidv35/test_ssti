package com.vmware.ph.phservice.provider.spbm.client.sms;

import com.vmware.ph.phservice.provider.spbm.client.XServiceClient;
import com.vmware.vim.binding.sms.ServiceInstance;

public interface SmsServiceClient extends XServiceClient {
  ServiceInstance getServiceInstance();
}
