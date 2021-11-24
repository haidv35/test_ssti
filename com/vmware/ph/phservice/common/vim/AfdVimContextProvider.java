package com.vmware.ph.phservice.common.vim;

import com.vmware.af.VmAfClient;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.security.KeyStoreProvider;
import java.net.URI;
import java.security.KeyStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AfdVimContextProvider implements VimContextProvider {
  private static final Log _log = LogFactory.getLog(AfdVimContextProvider.class);
  
  private final Builder<VmAfClient> _vmAfClientBuilder;
  
  private final KeyStoreProvider _trustedKeyStoreProvider;
  
  private final KeyStoreProvider _solutionUserKeyStoreProvider;
  
  private final String _solutionUserEntryAlias;
  
  private DeploymentNodeTypeReader _deploymentNodeTypeReader;
  
  private boolean _isApplianceLocal;
  
  private VmAfClient _vmAfClient;
  
  private Boolean _shouldUseEnvoySidecar;
  
  public AfdVimContextProvider(Builder<VmAfClient> vmAfClientBuilder, KeyStoreProvider trustedKeyStoreProvider, KeyStoreProvider solutionUserKeyStoreProvider, String solutionUserEntryAlias) {
    this._vmAfClientBuilder = vmAfClientBuilder;
    this._trustedKeyStoreProvider = trustedKeyStoreProvider;
    this._solutionUserKeyStoreProvider = solutionUserKeyStoreProvider;
    this._solutionUserEntryAlias = solutionUserEntryAlias;
  }
  
  public void setDeploymentNodeTypeReader(DeploymentNodeTypeReader deploymentNodeTypeReader) {
    this._deploymentNodeTypeReader = deploymentNodeTypeReader;
  }
  
  public void setApplianceLocal(boolean isApplianceLocal) {
    this._isApplianceLocal = isApplianceLocal;
  }
  
  public void setShouldUseEnvoySidecar(Boolean shouldUseEnvoySidecar) {
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public VimContext getVimContext() {
    VmAfClient vmAfClient = getVmAfClient();
    URI lookupServiceSdkUri = ServiceLocatorUtil.getLookupServiceSdkUri(vmAfClient
        .getLSLocation(), this._shouldUseEnvoySidecar
        .booleanValue());
    String nodeId = vmAfClient.getLDU();
    String domainId = vmAfClient.getDomainID();
    KeyStore trustedKeyStore = this._trustedKeyStoreProvider.getKeyStore();
    KeyStore solutionUserKeyStore = this._solutionUserKeyStoreProvider.getKeyStore();
    DeploymentNodeTypeReader.DeploymentNodeType applianceDeploymentNodeType = DeploymentNodeTypeReader.DeploymentNodeType.NONE;
    if (this._deploymentNodeTypeReader != null)
      applianceDeploymentNodeType = this._deploymentNodeTypeReader.getDeploymentNodeType(); 
    VimContext vimContext = VimContextBuilder.forVim(lookupServiceSdkUri, trustedKeyStore).withVcNodeId(nodeId).withVcDomainId(domainId).withSsoSolutionUser(solutionUserKeyStore, this._solutionUserEntryAlias, null).withLocalAppliance(this._isApplianceLocal).withApplianceDeploymentNodeType(applianceDeploymentNodeType).shouldUseEnvoySidecar(this._shouldUseEnvoySidecar).build();
    if (vimContext.getVcInstanceUuid() == null) {
      _log.info("Did not manage to build valid VimContext object. This most probably is caused, because this code runs on PSC node or the vpxd service has not completed its firstboot yet.");
      return null;
    } 
    return vimContext;
  }
  
  private synchronized VmAfClient getVmAfClient() {
    if (this._vmAfClient == null)
      this._vmAfClient = this._vmAfClientBuilder.build(); 
    return this._vmAfClient;
  }
}
