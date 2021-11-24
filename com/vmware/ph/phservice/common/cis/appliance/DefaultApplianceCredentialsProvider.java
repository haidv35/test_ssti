package com.vmware.ph.phservice.common.cis.appliance;

public class DefaultApplianceCredentialsProvider implements ApplianceCredentialsProvider {
  private final String _username;
  
  private final char[] _password;
  
  public DefaultApplianceCredentialsProvider(String username, char[] password) {
    this._username = username;
    this._password = password;
  }
  
  public String getUsername() {
    return this._username;
  }
  
  public char[] getPassword() {
    return this._password;
  }
}
