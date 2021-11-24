package com.vmware.ph.phservice.provider.spbm.client;

import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;

public interface XServiceClientFactory {
  PbmServiceClient getPbmServiceClient(XServiceClientContext paramXServiceClientContext);
  
  SmsServiceClient getSmsServiceClient(XServiceClientContext paramXServiceClientContext);
}
