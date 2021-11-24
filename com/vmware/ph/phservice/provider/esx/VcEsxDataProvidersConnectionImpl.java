package com.vmware.ph.phservice.provider.esx;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.internal.HttpClientBuilder;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import com.vmware.ph.phservice.provider.esx.cfg.EsxCfgInfoDataProvider;
import com.vmware.ph.phservice.provider.esx.telemetry.EsxTelemetryDataProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

public final class VcEsxDataProvidersConnectionImpl extends VimDataProvidersConnection {
  private static final Log _log = LogFactory.getLog(VcEsxDataProvidersConnectionImpl.class);
  
  private static final int VLSI_CLIENT_ESX_TELEMETRY_TIMEOUT_MS = 4000;
  
  private final HttpClientBuilder _esxHttpClientBuilder;
  
  private final VimContextVcClientProviderImpl _esxTelemetryVcClientProvider;
  
  private final Object _lock = new Object();
  
  private CloseableHttpClient _httpClient;
  
  public VcEsxDataProvidersConnectionImpl(VimContext vimContext) {
    super(vimContext);
    this._esxHttpClientBuilder = new HttpClientBuilder(vimContext.getVimTrustedStore());
    this._esxTelemetryVcClientProvider = getEsxTelemetryVcClientProvider(vimContext);
  }
  
  public VcEsxDataProvidersConnectionImpl(VimContext vimContext, VcClientProvider vcClientProvider) {
    super(vimContext, vcClientProvider);
    this._esxHttpClientBuilder = new HttpClientBuilder(vimContext.getVimTrustedStore());
    this._esxTelemetryVcClientProvider = getEsxTelemetryVcClientProvider(vimContext);
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    synchronized (this._lock) {
      if (this._httpClient == null)
        this._httpClient = this._esxHttpClientBuilder.build(); 
    } 
    VcClient vcClient = getVcClient();
    DataProvider esxCfgInfoDataProvider = new EsxCfgInfoDataProvider(vcClient, (HttpClient)this._httpClient);
    DataProvider esxTelemetryDataProvider = new EsxTelemetryDataProvider(this._esxTelemetryVcClientProvider.getVcClient());
    return Arrays.asList(new DataProvider[] { esxCfgInfoDataProvider, esxTelemetryDataProvider });
  }
  
  public void close() {
    super.close();
    synchronized (this._lock) {
      if (this._httpClient != null)
        try {
          this._httpClient.close();
        } catch (IOException e) {
          if (_log.isWarnEnabled())
            _log.warn("Unable to close HTTP client. There is a risk of memory leaks."); 
        }  
    } 
    this._esxTelemetryVcClientProvider.close();
  }
  
  private static VimContextVcClientProviderImpl getEsxTelemetryVcClientProvider(VimContext vimContext) {
    VimContextVcClientProviderImpl esxTelemetryVcClientProvider = new VimContextVcClientProviderImpl(vimContext);
    esxTelemetryVcClientProvider.setTimeoutMs(4000);
    return esxTelemetryVcClientProvider;
  }
}
