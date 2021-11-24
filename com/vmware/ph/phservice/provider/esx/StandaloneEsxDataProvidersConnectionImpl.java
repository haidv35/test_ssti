package com.vmware.ph.phservice.provider.esx;

import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vim.EsxContext;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.internal.HttpClientBuilder;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import com.vmware.ph.phservice.provider.esx.cfg.EsxCfgInfoDataProvider;
import com.vmware.ph.phservice.provider.esx.pc.EsxPcDataProvider;
import com.vmware.ph.phservice.provider.esx.telemetry.EsxTelemetryDataProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

public class StandaloneEsxDataProvidersConnectionImpl implements DataProvidersConnection {
  private static final Log _log = LogFactory.getLog(StandaloneEsxDataProvidersConnectionImpl.class);
  
  private final EsxContext _esxContext;
  
  private final HttpClientBuilder _esxHttpClientBuilder;
  
  private final Object _lock = new Object();
  
  private VmomiClient _esxVmomiClient;
  
  private CloseableHttpClient _httpClient;
  
  public StandaloneEsxDataProvidersConnectionImpl(EsxContext esxContext) {
    this._esxContext = esxContext;
    this._esxHttpClientBuilder = new HttpClientBuilder(esxContext.getTrustStore());
  }
  
  public List<DataProvider> getDataProviders() throws Exception {
    synchronized (this._lock) {
      if (this._esxVmomiClient == null) {
        Builder<VmomiClient> esxClientBuilder = this._esxContext.getEsxClientBuilder();
        this._esxVmomiClient = esxClientBuilder.build();
      } 
      if (this._httpClient == null)
        this._httpClient = this._esxHttpClientBuilder.build(); 
    } 
    DataProvider pcProvider = new EsxPcDataProvider(this._esxVmomiClient);
    DataProvider telemetryDataProvider = new EsxTelemetryDataProvider(this._esxVmomiClient);
    DataProvider cfgDataProvider = new EsxCfgInfoDataProvider(this._esxVmomiClient, (HttpClient)this._httpClient);
    return Arrays.asList(new DataProvider[] { pcProvider, telemetryDataProvider, cfgDataProvider });
  }
  
  public void close() {
    synchronized (this._lock) {
      if (this._esxVmomiClient != null)
        this._esxVmomiClient.close(); 
      if (this._httpClient != null)
        try {
          this._httpClient.close();
        } catch (IOException e) {
          if (_log.isWarnEnabled())
            _log.warn("Unable to close HTTP client. There is a risk of memory leaks."); 
        }  
    } 
  }
}
