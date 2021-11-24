package com.vmware.ph.phservice.provider.spbm.client.sms;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.spbm.client.common.constants.XServiceClientConstants;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.impl.XServiceClientFactoryImpl;
import com.vmware.vim.binding.sms.Task;
import com.vmware.vim.binding.vim.AboutInfo;
import java.util.LinkedHashMap;
import java.util.Map;

public class SmsServiceClientBuilder implements Builder<SmsServiceClient> {
  XServiceClientContext xServiceClientContext;
  
  private SmsServiceClientBuilder(XServiceClientContext xServiceClientContext) {
    this.xServiceClientContext = xServiceClientContext;
    setClientDefaultsIfUnset();
  }
  
  public static SmsServiceClientBuilder newInstance(XServiceClientContext xServiceClientContext) {
    return new SmsServiceClientBuilder(xServiceClientContext);
  }
  
  private void setClientDefaultsIfUnset() {
    if (this.xServiceClientContext.getxClientVmodlVersion() == null)
      this.xServiceClientContext.setxClientVmodlVersion(XServiceClientConstants.DEFAULT_SMS_VERSION_CLASS); 
    if (this.xServiceClientContext.getVmodlPackageNameToPackageClass() == null || this.xServiceClientContext
      .getVmodlPackageNameToPackageClass().size() == 0) {
      Map<String, Class<?>> vmodlPackageNamesToPackageClass = new LinkedHashMap<String, Class<?>>() {
        
        };
      this.xServiceClientContext.setVmodlPackages(vmodlPackageNamesToPackageClass);
    } 
  }
  
  public SmsServiceClient build() {
    SmsServiceClient smsServiceClient = null;
    XServiceClientFactoryImpl xServiceClientFactory = new XServiceClientFactoryImpl();
    smsServiceClient = xServiceClientFactory.getSmsServiceClient(this.xServiceClientContext);
    return smsServiceClient;
  }
}
