package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.ph.phservice.collector.core.DefaultQueryServiceConnectionFactory;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DsDefaultingQueryServiceConnectionFactory extends DefaultQueryServiceConnectionFactory {
  private static final Log _log = LogFactory.getLog(DsDefaultingQueryServiceConnectionFactory.class);
  
  private final Builder<VapiClient> _vapiClientBuilder;
  
  private final ExecutorService _executorService;
  
  public DsDefaultingQueryServiceConnectionFactory(Builder<VapiClient> vapiClientBuilder, ExecutorService executorService) {
    this._vapiClientBuilder = vapiClientBuilder;
    this._executorService = executorService;
  }
  
  public QueryServiceConnection createQueryServiceConnection(Builder<DataProvidersConnection> dataProvidersConnectionBuilder) {
    DataProvidersConnection dataProvidersConnection = getDataProvidersConnection(dataProvidersConnectionBuilder);
    if (dataProvidersConnection == null) {
      _log.debug("Data Providers Connection is null. Will return a null QueryServiceConnection.");
      return null;
    } 
    return new DsDefaultingQueryServiceConnection(dataProvidersConnection, this._vapiClientBuilder, this._executorService);
  }
}
