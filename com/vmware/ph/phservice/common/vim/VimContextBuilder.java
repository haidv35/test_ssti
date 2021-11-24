package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.cis.appliance.ApplianceCredentialsProvider;
import com.vmware.ph.phservice.common.cis.appliance.DefaultApplianceCredentialsProvider;
import com.vmware.ph.phservice.common.cis.appliance.DeploymentNodeTypeReader;
import com.vmware.ph.phservice.common.cis.internal.sso.impl.LookupSsoEndpointProviderImpl;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcServiceLocator;
import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;

public class VimContextBuilder {
  private final KeyStore _vimTrustStore;
  
  private ThumbprintVerifier _vimThumbprintVerifier;
  
  private boolean _isLegacyVim = false;
  
  private URI _vcSdkUri;
  
  private String _vcInstanceUuid;
  
  private String _vcNodeId;
  
  private String _vcDomainId;
  
  private Class<?> _vcVersionClass;
  
  private String _osUsername;
  
  private char[] _osPassword;
  
  private URI _lookupSdkUri;
  
  private Boolean _shouldUseEnvoySidecar = Boolean.valueOf(true);
  
  private String _ssoUsername;
  
  private char[] _ssoPassword;
  
  private X509Certificate _solutionCertificate;
  
  private Key _solutionPrivateKey;
  
  private SsoTokenProvider.TokenKeyPair _tokenKeyPair;
  
  private KeyStore _solutionKeyStore;
  
  private String _solutionKeyAlias;
  
  private char[] _solutionKeyPassword;
  
  private boolean _isApplianceLocal;
  
  private DeploymentNodeTypeReader.DeploymentNodeType _applianceDeploymentNodeType;
  
  private VcServiceLocator _vcServiceLocator;
  
  private VimContextBuilder(URI lookupSdkUri, KeyStore vimTrustStore) {
    this._lookupSdkUri = lookupSdkUri;
    this._vimTrustStore = vimTrustStore;
    this._isLegacyVim = false;
  }
  
  private VimContextBuilder(URI vcSdkUri, KeyStore vimTrustStore, boolean isLegacyVim) {
    this._vcSdkUri = vcSdkUri;
    this._vimTrustStore = vimTrustStore;
    this._isLegacyVim = isLegacyVim;
  }
  
  public static VimContextBuilder forVim(URI lookupSdkUri, KeyStore vimTrustStore) {
    return new VimContextBuilder(lookupSdkUri, vimTrustStore);
  }
  
  public static VimContextBuilder forLegacyVim(URI vcSdkUri, KeyStore vimTrustStore) {
    return new VimContextBuilder(vcSdkUri, vimTrustStore, true);
  }
  
  public VimContextBuilder withTrust(ThumbprintVerifier vimThumbprintVerifier) {
    this._vimThumbprintVerifier = vimThumbprintVerifier;
    return this;
  }
  
  public VimContextBuilder shouldUseEnvoySidecar(Boolean shouldUseEnvoySidecar) {
    if (shouldUseEnvoySidecar != null)
      this._shouldUseEnvoySidecar = shouldUseEnvoySidecar; 
    return this;
  }
  
  public VimContextBuilder withVcServiceLocator(VcServiceLocator vcServiceLocator) {
    this._vcServiceLocator = vcServiceLocator;
    return this;
  }
  
  public VimContextBuilder withVcInstanceUuid(String vcInstanceUuid) {
    this._vcInstanceUuid = vcInstanceUuid;
    return this;
  }
  
  public VimContextBuilder withVcSdkUri(URI vcSdkUri) {
    this._vcSdkUri = vcSdkUri;
    return this;
  }
  
  public VimContextBuilder withVcNodeId(String vcNodeId) {
    this._vcNodeId = vcNodeId;
    return this;
  }
  
  public VimContextBuilder withVcDomainId(String vcDomainId) {
    this._vcDomainId = vcDomainId;
    return this;
  }
  
  public VimContextBuilder withVcVersion(Class<?> vcVersionClass) {
    this._vcVersionClass = vcVersionClass;
    return this;
  }
  
  public VimContextBuilder withSsoUser(String ssoUsername, char[] ssoPassword) {
    this._ssoUsername = ssoUsername;
    this._ssoPassword = ssoPassword;
    return this;
  }
  
  public VimContextBuilder withSamlTokenAndPrivateKey(SamlToken samlToken, PrivateKey privateKey) {
    this._tokenKeyPair = new SsoTokenProvider.TokenKeyPair();
    this._tokenKeyPair.token = samlToken;
    this._tokenKeyPair.key = privateKey;
    return this;
  }
  
  public VimContextBuilder withSsoSolutionUser(X509Certificate solutionCertificate, Key solutionPrivateKey) {
    this._solutionCertificate = solutionCertificate;
    this._solutionPrivateKey = solutionPrivateKey;
    return this;
  }
  
  public VimContextBuilder withSsoSolutionUser(KeyStore solutionKeyStore, String solutionKeyAlias, char[] solutionKeyPassword) {
    this._solutionKeyStore = solutionKeyStore;
    this._solutionKeyAlias = solutionKeyAlias;
    this._solutionKeyPassword = solutionKeyPassword;
    return this;
  }
  
  public VimContextBuilder withOsUser(String osUsername, char[] osPassword) {
    this._osUsername = osUsername;
    this._osPassword = osPassword;
    return this;
  }
  
  public VimContextBuilder withLocalAppliance(boolean isApplianceLocal) {
    this._isApplianceLocal = isApplianceLocal;
    return this;
  }
  
  public VimContextBuilder withApplianceDeploymentNodeType(DeploymentNodeTypeReader.DeploymentNodeType applianceDeploymentNodeType) {
    this._applianceDeploymentNodeType = applianceDeploymentNodeType;
    return this;
  }
  
  public VimContext build() {
    VimContext vimContext = null;
    ApplianceCredentialsProvider osCredentialsProvider = null;
    if (this._osUsername != null)
      osCredentialsProvider = new DefaultApplianceCredentialsProvider(this._osUsername, this._osPassword); 
    if (!this._isLegacyVim) {
      LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forAutoLookupVersion(Executors.newSingleThreadExecutor()).withTrust(this._vimTrustStore, this._vimThumbprintVerifier).withLookupSdkUri(this._lookupSdkUri);
      if (this._vcServiceLocator == null)
        this._vcServiceLocator = new VcServiceLocator(lookupClientBuilder, this._shouldUseEnvoySidecar.booleanValue()); 
      SsoTokenProvider ssoTokenProvider = createSsoTokenProvider(lookupClientBuilder);
      if (this._vcInstanceUuid == null && this._vcSdkUri == null && this._vcNodeId == null)
        throw new IllegalArgumentException("Missing VC context"); 
      vimContext = new VimContext(this._vimTrustStore, this._lookupSdkUri, lookupClientBuilder, this._vcServiceLocator, ssoTokenProvider, this._vcInstanceUuid, this._vcSdkUri, this._vcNodeId, this._vcDomainId);
    } else {
      if (this._vcSdkUri == null)
        throw new IllegalArgumentException("Missing VC context - VC SDK URI"); 
      vimContext = new VimContext(this._vimTrustStore, this._vcSdkUri, osCredentialsProvider);
    } 
    vimContext.setApplianceCredentialsProvider(osCredentialsProvider);
    vimContext.setApplianceLocal(this._isApplianceLocal);
    vimContext.setApplianceDeploymentNodeType(this._applianceDeploymentNodeType);
    vimContext.setVimThumbprintVerifier(this._vimThumbprintVerifier);
    vimContext.setShouldUseEnvoySidecar(this._shouldUseEnvoySidecar.booleanValue());
    vimContext.setVcVersionClass(this._vcVersionClass);
    return vimContext;
  }
  
  private SsoTokenProvider createSsoTokenProvider(LookupClientBuilder lookupClientBuilder) {
    if (this._isLegacyVim)
      return null; 
    LookupSsoEndpointProviderImpl ssoEndpointProvider = new LookupSsoEndpointProviderImpl(lookupClientBuilder, this._shouldUseEnvoySidecar.booleanValue());
    SsoTokenProviderBuilder ssoTokenProviderBuilder = SsoTokenProviderBuilder.forSsoService(ssoEndpointProvider).withTrust(this._vimTrustStore, this._vimThumbprintVerifier);
    if (this._ssoUsername != null) {
      ssoTokenProviderBuilder.withSsoUser(this._ssoUsername, this._ssoPassword);
    } else if (this._tokenKeyPair != null) {
      ssoTokenProviderBuilder.withTokenKeyPair(this._tokenKeyPair);
    } else if (this._solutionCertificate != null) {
      ssoTokenProviderBuilder.withSsoSolutionUser(this._solutionCertificate, this._solutionPrivateKey);
    } else if (this._solutionKeyStore != null) {
      ssoTokenProviderBuilder.withSsoSolutionUser(this._solutionKeyStore, this._solutionKeyAlias, this._solutionKeyPassword);
    } else {
      throw new IllegalArgumentException("Missing SSO user");
    } 
    SsoTokenProvider ssoTokenProvider = ssoTokenProviderBuilder.build();
    return ssoTokenProvider;
  }
}
