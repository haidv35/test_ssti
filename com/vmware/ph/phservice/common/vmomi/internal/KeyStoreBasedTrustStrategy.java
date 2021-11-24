package com.vmware.ph.phservice.common.vmomi.internal;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.TrustStrategy;

public final class KeyStoreBasedTrustStrategy implements TrustStrategy {
  private static final Log _log = LogFactory.getLog(KeyStoreBasedTrustStrategy.class);
  
  private final TrustManagerFactory _trustManagerFactory;
  
  public KeyStoreBasedTrustStrategy(KeyStore trustStore) {
    this._trustManagerFactory = createTrustManagerFactory(trustStore);
  }
  
  public boolean isTrusted(X509Certificate[] chainToVerify, String authenticationType) throws CertificateException {
    try {
      boolean isTrusted = false;
      for (TrustManager trustManager : this._trustManagerFactory.getTrustManagers()) {
        if (trustManager instanceof X509TrustManager) {
          X509TrustManager x509TrustManager = (X509TrustManager)trustManager;
          x509TrustManager.checkServerTrusted(chainToVerify, authenticationType);
          isTrusted = true;
          break;
        } 
      } 
      return isTrusted;
    } catch (CertificateException e) {
      if (_log.isDebugEnabled())
        _log.debug("Cannot verify that certificate chain " + 
            
            Arrays.toString((Object[])chainToVerify) + " is valid.", e); 
      return false;
    } 
  }
  
  private static TrustManagerFactory createTrustManagerFactory(KeyStore trustStore) {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      return trustManagerFactory;
    } catch (NoSuchAlgorithmException|java.security.KeyStoreException e) {
      String message = "Unable to load trust store " + trustStore;
      if (_log.isDebugEnabled())
        _log.debug(message); 
      throw new IllegalArgumentException(message);
    } 
  }
}
