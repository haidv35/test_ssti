package com.vmware.ph.phservice.common.internal.security;

import com.vmware.ph.phservice.common.internal.CodecUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CertUtil {
  private static final String CERT_TYPE_X509 = "X.509";
  
  private static final String KEYSTORE_TYPE_JKS = "JKS";
  
  private static final String HEX = "0123456789ABCDEF";
  
  private static final String JAVA_HOME_PROPERTY = "java.home";
  
  private static final String PATH_TO_JAVA_CA_CERTS = "/lib/security/cacerts";
  
  private static final Log _log = LogFactory.getLog(CertUtil.class);
  
  public static X509Certificate[] decodeCerts(String[] base64EncodedCerts) throws CertificateException {
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    List<X509Certificate> certificates = new ArrayList<>();
    for (String base64CertStr : base64EncodedCerts) {
      byte[] certBytes = CodecUtil.decodeBase64(base64CertStr);
      X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
      certificates.add(cert);
    } 
    return certificates.<X509Certificate>toArray(new X509Certificate[0]);
  }
  
  public static String[] getCertsThumbprints(X509Certificate[] certs) throws CertificateEncodingException, NoSuchAlgorithmException {
    List<String> thumbprints = new ArrayList<>();
    for (X509Certificate cert : certs) {
      String thumbprint = computeCertificateThumbprint(cert);
      thumbprints.add(thumbprint);
    } 
    return thumbprints.<String>toArray(new String[0]);
  }
  
  private static String computeCertificateThumbprint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] digest = md.digest(cert.getEncoded());
    StringBuilder thumbprint = new StringBuilder();
    for (int i = 0, len = digest.length; i < len; i++) {
      if (i > 0)
        thumbprint.append(':'); 
      byte b = digest[i];
      thumbprint.append("0123456789ABCDEF".charAt((b & 0xF0) >> 4));
      thumbprint.append("0123456789ABCDEF".charAt(b & 0xF));
    } 
    return thumbprint.toString();
  }
  
  public static KeyStore createKeyStoreFromCerts(String[] base64EncodedCerts) {
    if (base64EncodedCerts == null || base64EncodedCerts.length == 0) {
      _log.warn("No SSL certificates provided. No KeyStore will be created.");
      return null;
    } 
    KeyStore keyStore = null;
    try {
      X509Certificate[] certificates = decodeCerts(base64EncodedCerts);
      keyStore = createKeyStoreFromCerts(certificates);
    } catch (CertificateException e) {
      _log.error("The SSL certificates could not be decoded.", e);
    } 
    return keyStore;
  }
  
  static KeyStore createKeyStoreFromCerts(X509Certificate[] certificates) {
    KeyStore keyStore = null;
    try {
      String keyStoreType = KeyStore.getDefaultType();
      _log.debug(String.format("Using default KeyStore type: %s", new Object[] { keyStoreType }));
      keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      for (X509Certificate x509Certificate : certificates)
        keyStore.setCertificateEntry(x509Certificate.getSubjectX500Principal().getName(), x509Certificate); 
    } catch (GeneralSecurityException|IOException e) {
      _log.error("The KeyStore could not be initialized.", e);
    } 
    return keyStore;
  }
  
  public static Collection<? extends X509Certificate> getJavaTrustedCertificates(String keystorePass) {
    KeyStore cacertsKeyStore;
    String javaHome = System.getProperty("java.home");
    try {
      cacertsKeyStore = loadKeyStore(
          Paths.get(javaHome + "/lib/security/cacerts", new String[0]), keystorePass.toCharArray());
    } catch (NoSuchAlgorithmException|CertificateException|KeyStoreException|IOException e) {
      _log.error("Couldn't load the java cacerts store", e);
      return new HashSet<>();
    } 
    return extractCertificatesFromKeystore(cacertsKeyStore);
  }
  
  public static Set<X509Certificate> extractCertificatesFromKeystore(KeyStore keyStore) {
    Set<X509Certificate> trustedCaCerts = new HashSet<>();
    Validate.notNull(keyStore);
    try {
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        if (keyStore.isCertificateEntry(alias)) {
          Certificate cert = keyStore.getCertificate(alias);
          if (cert instanceof X509Certificate)
            trustedCaCerts.add((X509Certificate)cert); 
        } 
      } 
    } catch (KeyStoreException e) {
      _log.error("Error while getting the aliases from the java keystore", e);
    } 
    return trustedCaCerts;
  }
  
  public static KeyStore loadKeyStore(Path keyStorePath, char[] password) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
    Validate.notNull(keyStorePath);
    KeyStore ks = KeyStore.getInstance("JKS");
    try(InputStream is = Files.newInputStream(keyStorePath, new OpenOption[] { StandardOpenOption.READ }); BufferedInputStream bis = new BufferedInputStream(is)) {
      ks.load(bis, password);
    } 
    return ks;
  }
}
