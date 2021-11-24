package com.vmware.ph.phservice.common.internal.security;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class TrustStoreHack {
  private static final int CONNECTION_TIMEOUT_MS = 60000;
  
  public static KeyStore getTrustStore(URI... endpoints) {
    try {
      KeyStore trustStore = KeyStore.getInstance("JKS");
      trustStore.load(null, null);
      for (URI endpoint : endpoints) {
        List<X509Certificate> certChain = getCertificates(endpoint);
        for (X509Certificate cert : certChain)
          trustStore.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert); 
      } 
      return trustStore;
    } catch (GeneralSecurityException|java.io.IOException e) {
      throw new IllegalStateException("Failed to create in-memory key store", e);
    } 
  }
  
  public static List<X509Certificate> getCertificates(URI endpoint) {
    NullTrustManager trustMgr = new NullTrustManager();
    try {
      URL address = endpoint.toURL();
      SSLContext ctx = SSLContext.getInstance("SSL");
      ctx.init(null, (TrustManager[])new X509TrustManager[] { trustMgr }, null);
      SSLSocketFactory factory = ctx.getSocketFactory();
      int port = (address.getPort() == -1) ? 443 : address.getPort();
      SSLSocket socket = (SSLSocket)factory.createSocket();
      try {
        InetSocketAddress inetAddress = new InetSocketAddress(address.getHost(), port);
        socket.connect(inetAddress, 60000);
        socket.setSoTimeout(60000);
        socket.startHandshake();
        Certificate[] certs = socket.getSession().getPeerCertificates();
        X509Certificate[] x509certs = new X509Certificate[certs.length];
        for (int i = 0; i < certs.length; i++)
          x509certs[i] = (X509Certificate)certs[i]; 
        return Arrays.asList(x509certs);
      } finally {
        socket.close();
      } 
    } catch (Exception e) {
      throw new IllegalArgumentException("Error getting certificates for endpoint " + endpoint, e);
    } 
  }
  
  private static final class NullTrustManager implements X509TrustManager {
    private NullTrustManager() {}
    
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }
}
