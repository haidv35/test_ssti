package com.vmware.cis.data.internal.adapters.dsvapi;

import com.vmware.cis.data.ResourceModel;
import com.vmware.cis.data.api.QueryService;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.ph.phservice.collector.core.DefaultQueryServiceConnection;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vapi.client.VapiClient;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DsDefaultingQueryServiceConnection extends DefaultQueryServiceConnection {
  private static final Log _log = LogFactory.getLog(DsDefaultingQueryServiceConnection.class);
  
  private Builder<VapiClient> _vapiClientBuilder;
  
  private VapiClient _vapiClient;
  
  DsDefaultingQueryServiceConnection(DataProvidersConnection dataProvidersConnection, Builder<VapiClient> vapiClientBuilder, ExecutorService executorService) {
    super(dataProvidersConnection, executorService);
    this._vapiClientBuilder = vapiClientBuilder;
  }
  
  protected QueryService buildQueryServiceForDataProviders(List<DataProvider> dataProviders) {
    QueryService queryService = null;
    try {
      this._vapiClient = this._vapiClientBuilder.build();
      ResourceModel resourceModel = this._vapiClient.<ResourceModel>createStub(ResourceModel.class);
      String applianceId = this._vapiClient.getApplianceId();
      queryService = QueryService.Builder.forProvider(new DsVapiQueryDispatcher(resourceModel, dataProviders, this._executor, applianceId)).withQueryLimits(this._queryLimitsSpec).build();
      _log.debug("Successfully created a QueryService defaulting to Data Service.");
    } catch (Exception e) {
      _log.error("Error while creating the QueryService with DsVapiQueryDispatcher. Will fall back to a standard Query Service.", e);
      queryService = QueryService.Builder.forProviders(dataProviders).withExecutor(this._executor).withQueryLimits(this._queryLimitsSpec).build();
    } 
    return queryService;
  }
  
  public void close() {
    if (this._vapiClient != null) {
      try {
        this._vapiClient.close();
      } catch (Exception e) {
        _log.warn("Could not close the VAPI client. This could lead to a resource leaks!", e);
      } 
      this._vapiClient = null;
    } 
    super.close();
  }
}
