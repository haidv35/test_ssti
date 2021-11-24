package com.vmware.ph.phservice.provider.spbm.client.pbm;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.provider.spbm.client.common.constants.XServiceClientConstants;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.impl.XServiceClientFactoryImpl;
import com.vmware.vim.binding.cis.data.provider.ResourceModel;
import com.vmware.vim.binding.pbm.AboutInfo;
import com.vmware.vim.binding.vim.AboutInfo;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PbmServiceClientBuilder implements Builder<PbmServiceClient> {
  private static final Log log = LogFactory.getLog(PbmServiceClientBuilder.class);
  
  private final XServiceClientContext xServiceClientContext;
  
  private PbmServiceClientBuilder(XServiceClientContext xServiceClientContext) {
    this.xServiceClientContext = xServiceClientContext;
    setClientDefaultsIfUnset();
  }
  
  public static PbmServiceClientBuilder newInstance(XServiceClientContext xServiceClientContext) {
    return new PbmServiceClientBuilder(xServiceClientContext);
  }
  
  private void setClientDefaultsIfUnset() {
    if (this.xServiceClientContext.getxClientVmodlVersion() == null) {
      if (log.isTraceEnabled())
        log.trace("Connecting with default pbm version" + XServiceClientConstants.DEFAULT_PBM_VERSION_CLASS); 
      this.xServiceClientContext.setxClientVmodlVersion(XServiceClientConstants.DEFAULT_PBM_VERSION_CLASS);
    } 
    if (this.xServiceClientContext.getVmodlPackageNameToPackageClass() == null || this.xServiceClientContext
      .getVmodlPackageNameToPackageClass().size() == 0) {
      Map<String, Class<?>> vmodlPackageNamesToPackageClass = new LinkedHashMap<String, Class<?>>() {
        
        };
      this.xServiceClientContext.setVmodlPackages(vmodlPackageNamesToPackageClass);
    } 
  }
  
  public PbmServiceClient build() {
    PbmServiceClient pbmServiceClient = null;
    XServiceClientFactoryImpl xServiceClientFactory = new XServiceClientFactoryImpl();
    pbmServiceClient = xServiceClientFactory.getPbmServiceClient(this.xServiceClientContext);
    return pbmServiceClient;
  }
}
