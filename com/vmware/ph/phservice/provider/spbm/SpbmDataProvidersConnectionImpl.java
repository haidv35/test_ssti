package com.vmware.ph.phservice.provider.spbm;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContextBuilder;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClientBuilder;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClientBuilder;
import com.vmware.ph.phservice.provider.spbm.collector.SpbmCollectorContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class SpbmDataProvidersConnectionImpl extends VimDataProvidersConnection {
  private final Object _lock = new Object();
  
  private PbmServiceClient _pbmClient;
  
  private SmsServiceClient _smsClient;
  
  public SpbmDataProvidersConnectionImpl(VimContext vimContext) {
    super(vimContext);
  }
  
  public SpbmDataProvidersConnectionImpl(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    synchronized (this._lock) {
      VcClient vcClient = getVcClient();
      XServiceClientContext pbmXServiceClientContext = XServiceClientContextBuilder.newInstance(vcClient).withThumbprintVerifier(this._vimContext.getThumprintVerifier()).withTrustStore(this._vimContext.getVimTrustedStore()).withTimeoutMs(Integer.valueOf(180000)).withExecutor(Executors.newSingleThreadExecutor()).withLookupClientBuilder(this._vimContext.getLookupClientBuilder(true)).withLocalAppliance(this._vimContext.getApplianceContext().isLocal()).useEnvoySidecar(this._vimContext.getShouldUseEnvoySidecar()).withLocalNodeId(this._vimContext.getApplianceContext().getNodeId()).build();
      XServiceClientContext smsXServiceClientContext = XServiceClientContextBuilder.newInstance(vcClient).withThumbprintVerifier(this._vimContext.getThumprintVerifier()).withTrustStore(this._vimContext.getVimTrustedStore()).withTimeoutMs(Integer.valueOf(180000)).withExecutor(Executors.newSingleThreadExecutor()).withLookupClientBuilder(this._vimContext.getLookupClientBuilder(true)).withLocalAppliance(this._vimContext.getApplianceContext().isLocal()).useEnvoySidecar(this._vimContext.getShouldUseEnvoySidecar()).withLocalNodeId(this._vimContext.getVcNodeId()).build();
      this
        ._pbmClient = PbmServiceClientBuilder.newInstance(pbmXServiceClientContext).build();
      this
        ._smsClient = SmsServiceClientBuilder.newInstance(smsXServiceClientContext).build();
      SpbmCollectorContext spbmCollectorContext = new SpbmCollectorContext(this._pbmClient, this._smsClient);
      List<DataProvider> dataProviders = new ArrayList<>();
      dataProviders.add(new SpbmDataProviderImpl(spbmCollectorContext));
      return Collections.unmodifiableList(dataProviders);
    } 
  }
  
  public void close() {
    if (this._pbmClient != null)
      this._pbmClient.close(); 
    if (this._smsClient != null)
      this._smsClient.close(); 
    super.close();
  }
}
