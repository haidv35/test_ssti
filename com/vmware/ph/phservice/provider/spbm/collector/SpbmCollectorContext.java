package com.vmware.ph.phservice.provider.spbm.collector;

import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;

public class SpbmCollectorContext {
  private final PbmServiceClient _pbmServiceClient;
  
  private final SmsServiceClient _smsServiceClient;
  
  public SpbmCollectorContext(PbmServiceClient pbmServiceClient, SmsServiceClient smsServiceClient) {
    this._pbmServiceClient = pbmServiceClient;
    this._smsServiceClient = smsServiceClient;
  }
  
  public PbmServiceClient getPbmServiceClient() {
    return this._pbmServiceClient;
  }
  
  public SmsServiceClient getSmsServiceClient() {
    return this._smsServiceClient;
  }
}
