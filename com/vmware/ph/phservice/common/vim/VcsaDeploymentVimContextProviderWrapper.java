package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;

public class VcsaDeploymentVimContextProviderWrapper implements VimContextProvider {
  private final VimContextProvider _wrappedVimContextProvider;
  
  public VcsaDeploymentVimContextProviderWrapper(VimContextProvider wrappedVimContextProvider) {
    this._wrappedVimContextProvider = wrappedVimContextProvider;
  }
  
  public VimContext getVimContext() {
    VimContext vimContext = this._wrappedVimContextProvider.getVimContext();
    if (vimContext != null) {
      DeploymentNodeTypeReader.DeploymentNodeType deploymentNodeType = vimContext.getApplianceContext().getDeploymentNodeType();
      if (DeploymentNodeTypeReader.DeploymentNodeType.MANAGEMENT.equals(deploymentNodeType) || DeploymentNodeTypeReader.DeploymentNodeType.EMBEDDED
        .equals(deploymentNodeType))
        return vimContext; 
    } 
    return null;
  }
}
