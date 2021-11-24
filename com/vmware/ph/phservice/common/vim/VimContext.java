package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceContext;
import com.vmware.ph.phservice.common.cis.appliance.ApplianceCredentialsProvider;
import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcServiceLocator;
import com.vmware.vim.binding.cis.data.provider.version.internal.version1;
import com.vmware.vim.binding.cis.data.provider.version.internal.version12;
import com.vmware.vim.binding.cis.data.provider.version.internal.version13;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.version.internal.v6_9_1;
import com.vmware.vim.binding.vim.version.internal.v7_0;
import com.vmware.vim.binding.vim.version.internal.v7_0_0_2;
import com.vmware.vim.binding.vim.version.internal.v7_0_1_0;
import com.vmware.vim.binding.vim.version.internal.version10;
import com.vmware.vim.binding.vim.version.internal.version12;
import com.vmware.vim.binding.vim.version.internal.version5;
import com.vmware.vim.binding.vim.version.internal.version6;
import com.vmware.vim.binding.vim.version.internal.version7;
import com.vmware.vim.binding.vim.version.internal.version8;
import com.vmware.vim.binding.vim.version.internal.version9;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VimContext {
  private static final Log _log = LogFactory.getLog(VimContext.class);
  
  public static final Class<?> DEFAULT_VC_VERSION_CLASS = version5.class;
  
  private final KeyStore _vimTrustStore;
  
  private ThumbprintVerifier _vimThumbprintVerifier;
  
  private URI _lookupSdkUri;
  
  private LookupClientBuilder _lookupClientBuilder;
  
  private boolean _shouldUseEnvoySidecar;
  
  private SsoTokenProvider _ssoTokenProvider;
  
  private String _vcInstanceUuid;
  
  private URI _vcSdkUri;
  
  private URI _absoluteVcSdkUri;
  
  private String _vcNodeId;
  
  private String _vcDomainId;
  
  private Class<?> _vcVersionClass;
  
  private boolean _isApplianceLocal;
  
  private ApplianceCredentialsProvider _applianceCredentialsProvider;
  
  private DeploymentNodeTypeReader.DeploymentNodeType _applianceDeploymentNodeType;
  
  private final boolean _isLegacyVim;
  
  private VcServiceLocator _vcServiceLocator;
  
  public VimContext(KeyStore vimTrustStore, URI lookupSdkUri, LookupClientBuilder lookupClientBuilder, VcServiceLocator vcServiceLocator, SsoTokenProvider ssoTokenProvider, String vcInstanceUuid, URI vcSdkUri, String vcNodeId, String vcDomainId) {
    this._vimTrustStore = vimTrustStore;
    this._lookupSdkUri = lookupSdkUri;
    this._lookupClientBuilder = lookupClientBuilder;
    this._vcServiceLocator = vcServiceLocator;
    this._ssoTokenProvider = ssoTokenProvider;
    this._vcInstanceUuid = vcInstanceUuid;
    this._vcSdkUri = vcSdkUri;
    this._absoluteVcSdkUri = null;
    this._vcNodeId = vcNodeId;
    this._vcDomainId = vcDomainId;
    this._isLegacyVim = false;
  }
  
  public VimContext(KeyStore vimTrustStore, URI vcSdkUri, ApplianceCredentialsProvider applianceCredentialsProvider) {
    this._vimTrustStore = vimTrustStore;
    this._vcSdkUri = vcSdkUri;
    this._applianceCredentialsProvider = applianceCredentialsProvider;
    this._isLegacyVim = true;
  }
  
  public void setVimThumbprintVerifier(ThumbprintVerifier vimThumbprintVerifier) {
    this._vimThumbprintVerifier = vimThumbprintVerifier;
  }
  
  public void setShouldUseEnvoySidecar(boolean shouldUseEnvoySidecar) {
    this._shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public void setVcVersionClass(Class<?> vcVersionClass) {
    this._vcVersionClass = vcVersionClass;
  }
  
  public void setApplianceLocal(boolean isApplianceLocal) {
    this._isApplianceLocal = isApplianceLocal;
  }
  
  public void setApplianceCredentialsProvider(ApplianceCredentialsProvider applianceCredentialsProvider) {
    this._applianceCredentialsProvider = applianceCredentialsProvider;
  }
  
  public void setApplianceDeploymentNodeType(DeploymentNodeTypeReader.DeploymentNodeType applianceDeploymentNodeType) {
    this._applianceDeploymentNodeType = applianceDeploymentNodeType;
  }
  
  public SsoTokenProvider getSsoTokenProvider() {
    return this._ssoTokenProvider;
  }
  
  public LookupClientBuilder getLookupClientBuilder(boolean tryToDetermineSuitableVersion) {
    if (this._isLegacyVim)
      return null; 
    if (!tryToDetermineSuitableVersion)
      return this._lookupClientBuilder; 
    LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forAutoLookupVersion(Executors.newSingleThreadExecutor()).withTrust(this._vimTrustStore, this._vimThumbprintVerifier).withLookupSdkUri(this._lookupSdkUri);
    return lookupClientBuilder;
  }
  
  public VcClientBuilder getVcClientBuilder() {
    return getVcClientBuilder(false);
  }
  
  public VcClientBuilder getVcClientBuilder(boolean tryToDetermineSuitableVersion) {
    Class<?> vcVersionClass = this._vcVersionClass;
    if (vcVersionClass == null)
      vcVersionClass = DEFAULT_VC_VERSION_CLASS; 
    VcClientBuilder vcClientBuilder = VcClientBuilder.forVcVersion(vcVersionClass, Executors.newSingleThreadExecutor()).withTrust(this._vimTrustStore, this._vimThumbprintVerifier).withVcSdkUri(getVcSdkUri());
    _log.info("VcClientBuilder created for version " + vcVersionClass);
    if (tryToDetermineSuitableVersion)
      try (VcClient vcClient = vcClientBuilder.build()) {
        Class<?> desiredVersionClass = getDesiredVersionClass(vcClient);
        if (!desiredVersionClass.equals(vcClient.getVmodlVersion().getClass())) {
          vcClientBuilder = VcClientBuilder.forVcVersion(desiredVersionClass, Executors.newSingleThreadExecutor()).withTrust(this._vimTrustStore, this._vimThumbprintVerifier).withVcSdkUri(getVcSdkUri());
          _log.info("VcClientBuilder re-created for version " + desiredVersionClass);
        } 
      }  
    if (this._ssoTokenProvider != null) {
      vcClientBuilder.withSsoTokenProvider(this._ssoTokenProvider);
    } else if (this._applianceCredentialsProvider != null) {
      vcClientBuilder.withVcCredentials(this._applianceCredentialsProvider
          .getUsername(), this._applianceCredentialsProvider
          .getPassword());
    } 
    return vcClientBuilder;
  }
  
  public String getVcInstanceUuid() {
    if (this._vcInstanceUuid == null)
      initLookupVcService(); 
    return this._vcInstanceUuid;
  }
  
  public URI getVcSdkUri() {
    if (this._vcSdkUri == null)
      initLookupVcService(); 
    return this._vcSdkUri;
  }
  
  public URI getAbsoluteVcSdkUri() {
    if (this._absoluteVcSdkUri == null)
      initLookupVcService(); 
    return this._absoluteVcSdkUri;
  }
  
  public String getVcNodeId() {
    return this._vcNodeId;
  }
  
  public URI getLookupSdkUri() {
    return this._lookupSdkUri;
  }
  
  public KeyStore getVimTrustedStore() {
    return this._vimTrustStore;
  }
  
  public ThumbprintVerifier getThumprintVerifier() {
    return this._vimThumbprintVerifier;
  }
  
  public boolean getShouldUseEnvoySidecar() {
    return this._shouldUseEnvoySidecar;
  }
  
  public ApplianceContext getApplianceContext() {
    if (this._isLegacyVim)
      return null; 
    String vcInstanceId = getVcInstanceUuid();
    URI vcSdkUri = getVcSdkUri();
    URI absoluteVcSdkUri = getAbsoluteVcSdkUri();
    ApplianceContext applianceContext = new ApplianceContext(vcInstanceId, vcSdkUri.getHost(), absoluteVcSdkUri.getHost(), this._vcNodeId, this._vcDomainId, this._applianceCredentialsProvider);
    applianceContext.setLocal(this._isApplianceLocal);
    applianceContext.setDeploymentNodeType(this._applianceDeploymentNodeType);
    return applianceContext;
  }
  
  public CisContext getCisContext() {
    if (this._isLegacyVim)
      return null; 
    ApplianceContext applianceContext = getApplianceContext();
    CisContext cisContext = new CisContext(this._vimTrustStore, this._lookupSdkUri, this._ssoTokenProvider, applianceContext);
    cisContext.setThumbprintVerifier(this._vimThumbprintVerifier);
    cisContext.setShouldUseEnvoySidecar(this._shouldUseEnvoySidecar);
    return cisContext;
  }
  
  private void initLookupVcService() {
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Attempt to lookup VC service with the following VC service coordinates: vcSdkUri: %s, vcInstanceUuid: %s, vcNodeId: %s", new Object[] { this._vcSdkUri, this._vcInstanceUuid, this._vcNodeId })); 
    if (this._vcInstanceUuid == null)
      if (this._vcSdkUri != null) {
        try (VcClient vcClient = getVcClientBuilder(false).build()) {
          this
            ._vcInstanceUuid = vcClient.getServiceInstanceContent().getAbout().getInstanceUuid();
        } 
      } else if (!this._isLegacyVim) {
        this._vcInstanceUuid = this._vcServiceLocator.getServiceIdByNodeId(this._vcNodeId);
      }  
    if (this._vcSdkUri == null && this._vcInstanceUuid != null && !this._isLegacyVim)
      this
        ._vcSdkUri = this._vcServiceLocator.getSdkUriByServiceId(this._vcInstanceUuid, this._isApplianceLocal); 
    if (this._absoluteVcSdkUri == null && this._vcInstanceUuid != null && !this._isLegacyVim)
      this
        ._absoluteVcSdkUri = this._vcServiceLocator.getSdkUriByServiceId(this._vcInstanceUuid, false, true); 
    if ((this._vcSdkUri == null || this._vcInstanceUuid == null) && 
      _log.isDebugEnabled())
      _log.debug("VC service was not discovered. VimContext instance is not operational."); 
  }
  
  private static Class<?> getDesiredVersionClass(VcClient vcClient) {
    AboutInfo aboutInfo = vcClient.getServiceInstanceContent().getAbout();
    if (aboutInfo.getVersion().startsWith("4.0"))
      return version5.class; 
    if (aboutInfo.getVersion().startsWith("4.1"))
      return version6.class; 
    if (aboutInfo.getVersion().startsWith("5.0"))
      return version7.class; 
    if (aboutInfo.getVersion().startsWith("5.1"))
      return version8.class; 
    if (aboutInfo.getVersion().startsWith("5.5"))
      return version9.class; 
    if (aboutInfo.getVersion().startsWith("6.0"))
      return version10.class; 
    if (aboutInfo.getVersion().startsWith("6.5"))
      return version1.class; 
    if (aboutInfo.getVersion().startsWith("6.6")) {
      if (aboutInfo.getVersion().startsWith("6.6.0"))
        return version1.class; 
      if (aboutInfo.getVersion().startsWith("6.6.1"))
        return version1.class; 
      if (aboutInfo.getVersion().startsWith("6.6.2"))
        return version1.class; 
      if (aboutInfo.getVersion().startsWith("6.6.3"))
        return version12.class; 
      return version12.class;
    } 
    if (aboutInfo.getVersion().startsWith("6.7")) {
      if (aboutInfo.getApiVersion().startsWith("6.7.0"))
        return version12.class; 
      return version13.class;
    } 
    if (aboutInfo.getVersion().startsWith("6.8.0"))
      return version12.class; 
    if (aboutInfo.getApiVersion().startsWith("6.9.1"))
      return v6_9_1.class; 
    if (aboutInfo.getApiVersion().startsWith("7.0.0.0"))
      return v7_0.class; 
    if (aboutInfo.getApiVersion().startsWith("7.0.0.2"))
      return v7_0_0_2.class; 
    return v7_0_1_0.class;
  }
}
