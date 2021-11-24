package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.ph.http.execute.RequestExecutorFactory;

public class PhRtsClientFactory {
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final RtsUriFactory _rtsUriFactory;
  
  private final RequestExecutorFactory _requestExecutorFactory;
  
  public PhRtsClientFactory(CeipConfigProvider ceipConfigProvider, RtsUriFactory rtsUriFactory, RequestExecutorFactory requestExecutorFactory) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._rtsUriFactory = rtsUriFactory;
    this._requestExecutorFactory = requestExecutorFactory;
  }
  
  public PhRtsClient create() {
    return new PhRtsClient(this._rtsUriFactory, this._ceipConfigProvider, this._requestExecutorFactory.create());
  }
}
