package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.ph.http.HttpClientFactory;

public class PhDapClientFactory {
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final RtsUriFactory _rtsUriFactory;
  
  private final HttpClientFactory _httpClientFactory;
  
  public PhDapClientFactory(CeipConfigProvider ceipConfigProvider, RtsUriFactory rtsUriFactory, HttpClientFactory httpClientFactory) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._rtsUriFactory = rtsUriFactory;
    this._httpClientFactory = httpClientFactory;
  }
  
  public PhDapClient create() {
    return new PhDapClient(this._rtsUriFactory, this._ceipConfigProvider, this._httpClientFactory.create());
  }
}
