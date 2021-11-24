package com.vmware.ph.phservice.provider.vsan;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vsan.VsanHealthClientBuilder;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsanHealthDataProvidersConnection extends VimDataProvidersConnection {
  private static final Log _log = LogFactory.getLog(VsanHealthDataProvidersConnection.class);
  
  private static final int VSAN_HEALTH_CLIENT_TIMEOUT_MS = 3600000;
  
  private VmomiClient _vsanHealthVmomiClient;
  
  public VsanHealthDataProvidersConnection(VimContext vimContext) {
    super(vimContext);
  }
  
  public VsanHealthDataProvidersConnection(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    if (this._vsanHealthVmomiClient == null)
      this._vsanHealthVmomiClient = createVsanHealthClient(getVcClient(), this._vimContext); 
    VmodlToJsonLdSerializer serializer = new VmodlToJsonLdSerializer(VsanHealthClientBuilder.VMODL_CONTEXT.getVmodlTypeMap(), VsanHealthClientBuilder.VMODL_PACKAGES);
    serializer.setSerializeCalendarAsObject(true);
    serializer.setSerializeCalendarInSeconds(true);
    serializer.setSerializeNullValues(true);
    DataProvider vsanMassCollectorDataProvider = new VsanMassCollectorDataProvider(this._vsanHealthVmomiClient, getVcClient(), serializer);
    DataProvider vsanPerfDiagnosticsDataProvider = new VsanPerfDiagnosticsDataProvider(this._vsanHealthVmomiClient, getVcClient(), serializer, Executors.newCachedThreadPool());
    DataProvider vsanPerfMetricsDataProvider = new VsanPerfMetricsDataProvider(this._vsanHealthVmomiClient, getVcClient(), serializer, Executors.newCachedThreadPool());
    DataProvider vsanVcClusterHealthSystemDataProvider = new VsanVcClusterHealthSystemDataProvider(getVcClient(), this._vsanHealthVmomiClient);
    return Arrays.asList(new DataProvider[] { vsanMassCollectorDataProvider, vsanPerfDiagnosticsDataProvider, vsanPerfMetricsDataProvider, vsanVcClusterHealthSystemDataProvider });
  }
  
  public void close() {
    if (this._vsanHealthVmomiClient != null) {
      this._vsanHealthVmomiClient.close();
      this._vsanHealthVmomiClient = null;
    } 
    super.close();
  }
  
  private static VmomiClient createVsanHealthClient(VcClient vcClient, VimContext vimContext) {
    KeyStore trustStore = vimContext.getVimTrustedStore();
    ThumbprintVerifier thumbprintVerifier = vimContext.getThumprintVerifier();
    VmomiClient vsanHealthVmomiClient = VsanHealthClientBuilder.forVc(vimContext, vcClient).withTrust(trustStore, thumbprintVerifier).withTimeoutMs(3600000).build();
    _log.info(String.format("Created vsanHealthVmomiClient with version %s and ULR %s: %s", new Object[] { vsanHealthVmomiClient
            .getServiceUri(), vsanHealthVmomiClient
            .getVmodlVersion(), vsanHealthVmomiClient }));
    return vsanHealthVmomiClient;
  }
}
