package com.vmware.ph.phservice.common.cis.appliance;

public interface DeploymentNodeTypeReader {
  DeploymentNodeType getDeploymentNodeType();
  
  public enum DeploymentNodeType {
    INFRASTRUCTURE, MANAGEMENT, EMBEDDED, VMC_GATEWAY, NONE;
  }
}
