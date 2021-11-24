package com.vmware.ph.phservice.common.cis.internal.sso;

import java.net.URI;

public class SsoEndpoint {
  private final URI _url;
  
  private final String[] _sslTrust;
  
  public SsoEndpoint(URI url, String[] sslTrust) {
    this._url = url;
    this._sslTrust = sslTrust;
  }
  
  public URI getUrl() {
    return this._url;
  }
  
  public String[] getSslTrust() {
    return this._sslTrust;
  }
}
