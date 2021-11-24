package com.vmware.ph.phservice.common.cis.appliance;

public interface ApplianceCredentialsProvider {
  String getUsername();
  
  char[] getPassword();
}
