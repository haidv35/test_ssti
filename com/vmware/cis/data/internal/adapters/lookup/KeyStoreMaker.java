package com.vmware.cis.data.internal.adapters.lookup;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;

public final class KeyStoreMaker {
  public static KeyStore fromTrustedCertificates(String[] base64Certificates) {
    if (base64Certificates == null)
      return null; 
    List<X509Certificate> certs = decodeCertificates(base64Certificates);
    return fromTrustedCertificates(certs);
  }
  
  public static KeyStore fromTrustedCertificates(List<X509Certificate> certs) {
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      for (X509Certificate cert : certs)
        trustStore.setCertificateEntry(cert.getSubjectDN().getName(), cert); 
      return trustStore;
    } catch (GeneralSecurityException|java.io.IOException e) {
      throw new IllegalStateException("Error create in-memory keystore", e);
    } 
  }
  
  private static List<X509Certificate> decodeCertificates(String[] base64Certificates) {
    Validate.notEmpty((Object[])base64Certificates);
    Validate.noNullElements((Object[])base64Certificates);
    List<X509Certificate> result = new ArrayList<>(base64Certificates.length);
    for (String s : base64Certificates) {
      X509Certificate certificate = decodeCertificate(s);
      result.add(certificate);
    } 
    return result;
  }
  
  private static X509Certificate decodeCertificate(String base64Certificate) {
    X509Certificate certificate;
    try {
      CertificateFactory factory = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
      throw new IllegalStateException("Internal error: X.509 CertificateFactory not supported by the runtime.", e);
    } 
    try {
      CertificateFactory factory = CertificateFactory.getInstance("X509");
      certificate = (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(
            Base64.decodeBase64(base64Certificate.getBytes("UTF-8"))));
    } catch (CertificateException|java.io.UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Ignoring invalid base64 encoded certificate", e);
    } 
    return certificate;
  }
}
