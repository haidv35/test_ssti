package com.vmware.ph.phservice.common.cis;

import com.vmware.af.VmAfClient;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.security.KeyStoreProvider;
import java.net.URI;
import java.security.KeyStore;

public class AfdCisContextProvider implements CisContextProvider {
  private final Builder<VmAfClient> _vmAfClientBuilder;
  
  private final KeyStoreProvider _trustedKeyStoreProvider;
  
  private final KeyStoreProvider _solutionUserKeyStoreProvider;
  
  private final String _solutionUserEntryAlias;
  
  private DeploymentNodeTypeReader _deploymentNodeTypeReader;
  
  private boolean _isApplianceLocal;
  
  private Boolean _shouldUseEnvoySidecar;
  
  private VmAfClient _vmAfClient;
  
  public AfdCisContextProvider(Builder<VmAfClient> vmAfClientBuilder, KeyStoreProvider trustedKeyStoreProvider, KeyStoreProvider solutionUserKeyStoreProvider, String solutionUserEntryAlias) {
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
  
  public CisContext getCisContext() {
    VmAfClient vmAfClient = getVmAfClient();
    URI lookupServiceSdkUri = ServiceLocatorUtil.getLookupServiceSdkUri(vmAfClient
        .getLSLocation(), this._shouldUseEnvoySidecar
        .booleanValue());
    KeyStore trustedKeyStore = this._trustedKeyStoreProvider.getKeyStore();
    KeyStore solutionUserKeyStore = this._solutionUserKeyStoreProvider.getKeyStore();
    String applianceId = vmAfClient.getMachineID();
    String applianceHostName = vmAfClient.getPNID();
    String applianceNodeId = vmAfClient.getLDU();
    String applianceDomainId = vmAfClient.getDomainID();
    ApplianceContext applianceContext = new ApplianceContext(applianceId, applianceHostName, applianceNodeId, applianceDomainId, null);
    applianceContext.setLocal(this._isApplianceLocal);
    if (this._deploymentNodeTypeReader != null) {
      DeploymentNodeTypeReader.DeploymentNodeType deploymentNodeType = this._deploymentNodeTypeReader.getDeploymentNodeType();
      applianceContext.setDeploymentNodeType(deploymentNodeType);
    } 
    CisContext cisContext = CisContextBuilder.forCis(lookupServiceSdkUri, trustedKeyStore).withSsoSolutionUser(solutionUserKeyStore, this._solutionUserEntryAlias, null).withApplianceContext(applianceContext).shouldUseEnvoySidecar(this._shouldUseEnvoySidecar).build();
    return cisContext;
  }
  
  private synchronized VmAfClient getVmAfClient() {
    if (this._vmAfClient == null)
      this._vmAfClient = (VmAfClient)this._vmAfClientBuilder.build(); 
    return this._vmAfClient;
  }
}
