package com.vmware.ph.phservice.common.cis.internal.sso.impl;

import com.vmware.ph.phservice.common.cis.internal.sso.StsClient;
import com.vmware.vim.sso.client.DefaultSecurityTokenServiceFactory;
import com.vmware.vim.sso.client.SamlToken;
import com.vmware.vim.sso.client.SecurityTokenService;
import com.vmware.vim.sso.client.SecurityTokenServiceConfig;
import com.vmware.vim.sso.client.TokenSpec;
import com.vmware.vim.sso.client.exception.CertificateValidationException;
import com.vmware.vim.sso.client.exception.InvalidTokenException;
import com.vmware.vim.sso.client.exception.TokenRequestRejectedException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class StsClientImpl implements StsClient {
  private final URL _stsUrl;
  
  private final KeyStore _trustStore;
  
  private final String _trustedCertThumbprint;
  
  private final X509Certificate[] _signingCertificates;
  
  private final SecurityTokenServiceConfig.ConnectionConfig _stsConnConfig;
  
  private ExecutorService _executorService = null;
  
  public StsClientImpl(URL stsUrl, KeyStore trustStore, String trustedCertThumbprint, X509Certificate[] signingCerts) {
    this._stsUrl = stsUrl;
    this._trustStore = trustStore;
    this._trustedCertThumbprint = trustedCertThumbprint;
    this._signingCertificates = signingCerts;
    this

      
      ._stsConnConfig = new SecurityTokenServiceConfig.ConnectionConfig(this._stsUrl, extractTrustedCeritifcates(this._trustStore), this._trustedCertThumbprint);
  }
  
  public void setExecutorService(ExecutorService executorService) {
    this._executorService = executorService;
  }
  
  private static TokenSpec createTokenSpec(long tokenLifetimeSecs) {
    TokenSpec.Builder builder = new TokenSpec.Builder(tokenLifetimeSecs);
    builder.renewable(true).delegationSpec(new TokenSpec.DelegationSpec(true));
    return builder.createTokenSpec();
  }
  
  public SamlToken acquireBearerToken(String userName, char[] password, long tokenLifetimeSecs) throws InvalidTokenException, TokenRequestRejectedException, CertificateValidationException {
    SecurityTokenServiceConfig stsConfig = new SecurityTokenServiceConfig(this._stsConnConfig, this._signingCertificates, this._executorService);
    SecurityTokenService stsStub = DefaultSecurityTokenServiceFactory.getSecurityTokenService(stsConfig);
    TokenSpec tokenSpec = createTokenSpec(tokenLifetimeSecs);
    SamlToken token = stsStub.acquireToken(userName, new String(password), tokenSpec);
    return token;
  }
  
  public SamlToken acquireTokenByCertificate(X509Certificate certificate, Key privateKey, long tokenLifetimeSecs) throws InvalidTokenException, TokenRequestRejectedException, CertificateValidationException {
    SecurityTokenServiceConfig.HolderOfKeyConfig hokConfig = new SecurityTokenServiceConfig.HolderOfKeyConfig(privateKey, certificate, null);
    SecurityTokenServiceConfig stsConfig = new SecurityTokenServiceConfig(this._stsConnConfig, this._signingCertificates, this._executorService, hokConfig);
    SecurityTokenService stsStub = DefaultSecurityTokenServiceFactory.getSecurityTokenService(stsConfig);
    TokenSpec tokenSpec = createTokenSpec(tokenLifetimeSecs);
    SamlToken token = stsStub.acquireTokenByCertificate(tokenSpec);
    return token;
  }
  
  private static X509Certificate[] extractTrustedCeritifcates(KeyStore keyStore) {
    if (keyStore == null)
      return new X509Certificate[0]; 
    List<X509Certificate> trustedCerts = new ArrayList<>();
    try {
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        try {
          KeyStore.Entry entry = keyStore.getEntry(alias, null);
          if (entry instanceof KeyStore.TrustedCertificateEntry) {
            Certificate trustedCert = ((KeyStore.TrustedCertificateEntry)entry).getTrustedCertificate();
            trustedCerts.add((X509Certificate)trustedCert);
          } 
        } catch (UnrecoverableEntryException unrecoverableEntryException) {
        
        } catch (ClassCastException classCastException) {}
      } 
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to extract the trusted certificates from the trust store: " + e
          .getMessage(), e);
    } 
    return trustedCerts.<X509Certificate>toArray(new X509Certificate[trustedCerts.size()]);
  }
}
