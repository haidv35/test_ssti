package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.StsClient;
import com.vmware.ph.phservice.common.cis.internal.sso.StsClientFactory;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.vim.sso.client.SamlToken;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

public class SsoHoKTokenProviderImpl implements SsoTokenProvider {
  private final StsClientFactory _stsClientFactory;
  
  private X509Certificate _solutionCertificate;
  
  private Key _privateKey;
  
  private KeyStore _solutionKeyStore;
  
  private String _solutionKeyAlias;
  
  private char[] _solutionKeyPassword;
  
  private final int _tokenLifetimeSecs = 600;
  
  public SsoHoKTokenProviderImpl(StsClientFactory stsClientFactory, X509Certificate solutionCertificate, Key privateKey) {
    this._stsClientFactory = stsClientFactory;
    this._solutionCertificate = solutionCertificate;
    this._privateKey = privateKey;
  }
  
  public SsoHoKTokenProviderImpl(StsClientFactory stsClientFactory, KeyStore solutionKeyStore, String solutionKeyAlias, char[] solutionKeyPassword) {
    this._stsClientFactory = stsClientFactory;
    this._solutionKeyStore = solutionKeyStore;
    this._solutionKeyAlias = solutionKeyAlias;
    this._solutionKeyPassword = solutionKeyPassword;
  }
  
  public SsoTokenProvider.TokenKeyPair getToken() throws SsoTokenProviderException {
    SsoTokenProvider.TokenKeyPair result = null;
    try {
      X509Certificate solutionCertificate = getSolutionCertificateOrNull();
      Key privateKey = getSolutionPrivateKeyOrNull();
      if (solutionCertificate != null) {
        StsClient stsClient = this._stsClientFactory.createStsClient();
        SamlToken token = stsClient.acquireTokenByCertificate(solutionCertificate, privateKey, 600L);
        result = new SsoTokenProvider.TokenKeyPair();
        result.token = token;
        result.key = (PrivateKey)privateKey;
      } 
    } catch (Exception e) {
      throw new SsoTokenProviderException(e);
    } 
    return result;
  }
  
  private X509Certificate getSolutionCertificateOrNull() {
    X509Certificate solutionCertificate = null;
    if (this._solutionCertificate != null) {
      solutionCertificate = this._solutionCertificate;
    } else if (this._solutionKeyStore != null) {
      try {
        solutionCertificate = (X509Certificate)this._solutionKeyStore.getCertificate(this._solutionKeyAlias);
      } catch (KeyStoreException e) {
        solutionCertificate = null;
      } 
    } 
    return solutionCertificate;
  }
  
  private Key getSolutionPrivateKeyOrNull() {
    Key privateKey = null;
    if (this._privateKey != null) {
      privateKey = this._privateKey;
    } else if (this._solutionKeyStore != null) {
      try {
        privateKey = this._solutionKeyStore.getKey(this._solutionKeyAlias, this._solutionKeyPassword);
      } catch (UnrecoverableKeyException|KeyStoreException|java.security.NoSuchAlgorithmException e) {
        privateKey = null;
      } 
    } 
    return privateKey;
  }
}
