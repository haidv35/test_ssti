package com.vmware.ph.phservice.proxy.config;

public class TinyProxyConfig {
  private String _scheme;
  
  private int _port;
  
  public TinyProxyConfig(String scheme, int port) {
    this._scheme = scheme;
    this._port = port;
  }
  
  public String getScheme() {
    return this._scheme;
  }
  
  public int getPort() {
    return this._port;
  }
}
