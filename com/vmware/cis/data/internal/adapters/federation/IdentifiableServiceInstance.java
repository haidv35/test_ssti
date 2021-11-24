package com.vmware.cis.data.internal.adapters.federation;

public interface IdentifiableServiceInstance {
  String getServiceInstanceUuid();
  
  String getVersion();
  
  String getBuild();
}
