package com.vmware.ph.phservice.common.vim.internal;

import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;
import com.vmware.ph.phservice.common.internal.security.TrustStoreHack;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextBuilder;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.AllowAllThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;

public class DefaultVimContextProvider implements VimContextProvider {
  private final URI _lookupServiceUri;
  
  private final URI _vcSdkUri;
  
  private final String _ssoUserName;
  
  private final String _ssoUserPassword;
  
  private final String _osUsername;
  
  private final String _osPassword;
  
  private String _vcNodeId;
  
  private String _vcDomainId;
  
  private final Boolean _shouldUseEnvoySidecar;
  
  public DefaultVimContextProvider(URI lookupServiceUri, URI vcSdkUri, String ssoUserName, String ssoUserPassword, String osUsername, String osPassword, String vcNodeId, String vcDomainId, Boolean shouldUseEnvoySidecar) {
    this._lookupServiceUri = lookupServiceUri;
    this._vcSdkUri = vcSdkUri;
    this._ssoUserName = ssoUserName;
    this._ssoUserPassword = ssoUserPassword;
    this._osUsername = osUsername;
    this._osPassword = osPassword;
    this._vcNodeId = vcNodeId;
    this._vcDomainId = vcDomainId;
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public VimContext getVimContext() {
    KeyStore trustStore = TrustStoreHack.getTrustStore(new URI[] { this._vcSdkUri });
    VimContext vimContext = VimContextBuilder.forVim(this._lookupServiceUri, trustStore).withTrust((ThumbprintVerifier)new AllowAllThumbprintVerifier()).withSsoUser(this._ssoUserName, this._ssoUserPassword.toCharArray()).withOsUser(this._osUsername, this._osPassword.toCharArray()).withVcSdkUri(this._vcSdkUri).withApplianceDeploymentNodeType(DeploymentNodeTypeReader.DeploymentNodeType.MANAGEMENT).withVcNodeId(this._vcNodeId).withVcDomainId(this._vcDomainId).shouldUseEnvoySidecar(this._shouldUseEnvoySidecar).build();
    return vimContext;
  }
}
