package com.vmware.ph.phservice.common.ph.internal.signature;

import com.vmware.ph.phservice.common.internal.security.CertUtil;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSigner;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSignatureService {
  private static final Log _log = LogFactory.getLog(FileSignatureService.class);
  
  private static final int FILE_IO_BUFFER_SIZE = 4096;
  
  private static final String TRUSTED_CN = "VMware, Inc.";
  
  private static final String CLIENT_METADATA_FILE = "signed_manifest.properties";
  
  private static final String SIGNATURE_OWNER_KEY = "owner";
  
  private static final String DESIRED_OWNER = "svc.manifest-signer@vmware.com";
  
  private final PKIXParameters _certPathValidatorParams;
  
  public FileSignatureService(String keystorePass) {
    this(CertUtil.getJavaTrustedCertificates(keystorePass));
  }
  
  public FileSignatureService(Collection<? extends X509Certificate> trustedCaCerts) {
    Set<TrustAnchor> trustAnchors = new HashSet<>();
    if (trustedCaCerts != null && !trustedCaCerts.isEmpty())
      for (X509Certificate cert : trustedCaCerts) {
        TrustAnchor trustAnchor = new TrustAnchor(cert, null);
        trustAnchors.add(trustAnchor);
      }  
    PKIXParameters certPathValidatorParams = null;
    try {
      certPathValidatorParams = new PKIXParameters(trustAnchors);
    } catch (InvalidAlgorithmParameterException e) {
      _log.error("Couldn't create the PKIX parameters with the passed trust anchors " + trustAnchors, e);
    } 
    if (certPathValidatorParams != null)
      certPathValidatorParams.setRevocationEnabled(false); 
    this._certPathValidatorParams = certPathValidatorParams;
  }
  
  public void verifySignature(JarFile jarFile) throws ManifestSignatureException, ManifestSignatureGeneralException {
    if (this._certPathValidatorParams == null)
      throw new ManifestSignatureGeneralException("Detected an invalid set of trust anchors."); 
    try {
      Manifest manifest;
      Validate.notNull(jarFile);
      ZipEntry ze = jarFile.getEntry("META-INF/MANIFEST.MF");
      if (ze == null)
        throw new ManifestSignatureException("No META-INF/MANIFEST.MF entry found in the plugin zip file."); 
      try(InputStream is = jarFile.getInputStream(ze); 
          BufferedInputStream bis = new BufferedInputStream(is)) {
        manifest = new Manifest(bis);
        Map<String, Attributes> entries = manifest.getEntries();
        _log.debug("Jar file entries: " + entries);
      } catch (SecurityException e) {
        throw new ManifestSignatureException("The signature of the META-INF/MANIFEST.MF entry is corrupted.", e);
      } 
      Set<String> signedEntries = checkForUnsignedOrMissingEntries(jarFile, manifest);
      for (String signedEntry : signedEntries) {
        JarEntry jarEntry = jarFile.getJarEntry(signedEntry);
        verifyJarFileEntry(jarFile, jarEntry);
      } 
      verifyClientMetadata(jarFile);
    } catch (ManifestSignatureException e) {
      Manifest manifest;
      throw manifest;
    } catch (Exception e) {
      Manifest manifest;
      throw new ManifestSignatureGeneralException("The signature verification process failed.", manifest);
    } 
    if (_log.isDebugEnabled())
      _log.debug("The jar file " + jarFile.getName() + " was validated successfully"); 
  }
  
  private void verifyJarFileEntry(JarFile jarFile, JarEntry jarEntry) throws IOException, NoSuchAlgorithmException, ManifestSignatureException, InvalidAlgorithmParameterException {
    try (InputStream is = jarFile.getInputStream(jarEntry)) {
      readFully(is);
    } catch (SecurityException e) {
      throw new ManifestSignatureException("The signature of jar entry " + jarEntry
          .getName() + " in jar file " + jarFile.getName() + " is corrupted.", e);
    } 
    CodeSigner[] codeSigners = jarEntry.getCodeSigners();
    if (codeSigners.length != 1)
      throw new ManifestSignatureException("Expected one code signer, but found " + codeSigners.length); 
    CodeSigner codeSigner = codeSigners[0];
    CertPath certPath = codeSigner.getSignerCertPath();
    if (_log.isDebugEnabled())
      _log.debug(String.format("Certificate path: %s", new Object[] { certPath })); 
    verifyVmwareCommonName(certPath);
    Timestamp codeSignerTimestamp = codeSigner.getTimestamp();
    if (codeSignerTimestamp == null)
      throw new ManifestSignatureException("Missing timestamp in jar entry " + jarEntry
          .getName() + " in file " + jarFile.getName(), ManifestSignatureException.Reason.MISSING_TIMESTAMP); 
    _log.debug("Timestamp: " + codeSignerTimestamp.getTimestamp());
    CertPath timestampCertPath = codeSignerTimestamp.getSignerCertPath();
    if (_log.isDebugEnabled())
      _log.debug(String.format("Timestamp Certificate Path:\n%s", new Object[] { timestampCertPath })); 
    Date signingDate = codeSignerTimestamp.getTimestamp();
    PKIXParameters timestampAwareValidationParams = (PKIXParameters)this._certPathValidatorParams.clone();
    timestampAwareValidationParams.setDate(signingDate);
    verifyCertPath(timestampCertPath, timestampAwareValidationParams);
    _log.debug("Timestamp verified successfully!");
    verifyCertPath(certPath, timestampAwareValidationParams);
  }
  
  private void verifyVmwareCommonName(CertPath certPath) throws ManifestSignatureException {
    List<String> commonNames;
    List<? extends Certificate> certificates = certPath.getCertificates();
    if (certificates.isEmpty())
      throw new ManifestSignatureException("The certificate path of the code signer is empty."); 
    X509Certificate cert = (X509Certificate)certificates.get(0);
    if (cert == null)
      throw new ManifestSignatureException("The VMware certificate should be an X509 certificate"); 
    try {
      commonNames = getRDN(cert, "CN");
    } catch (InvalidNameException e) {
      throw new ManifestSignatureException("An error occurred when extracting the certificate common name.", e);
    } 
    for (String cnVal : commonNames) {
      if ("VMware, Inc.".equals(cnVal))
        return; 
    } 
    throw new ManifestSignatureException("The certificate common names (CN) " + commonNames + " do not contain the expected one.", ManifestSignatureException.Reason.INCORRECT_CN);
  }
  
  private void verifyCertPath(CertPath certPath, PKIXParameters certPathValidatorParams) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, ManifestSignatureException {
    CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
    CertPathValidatorResult result = null;
    try {
      result = certPathValidator.validate(certPath, certPathValidatorParams);
    } catch (CertPathValidatorException e) {
      if (CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED.equals(e.getReason())) {
        _log.debug("The zip was signed with a constrained algorithm", e);
      } else {
        throw new ManifestSignatureException("The certification path couldn't be verified", e, ManifestSignatureException.Reason.CERT_PATH_NOT_VERIFIED);
      } 
    } 
    if (result instanceof PKIXCertPathValidatorResult) {
      PKIXCertPathValidatorResult pkixCertPathValidatorResult = (PKIXCertPathValidatorResult)result;
      TrustAnchor ta = pkixCertPathValidatorResult.getTrustAnchor();
      X509Certificate tsTrustAnchorCert = ta.getTrustedCert();
      if (_log.isDebugEnabled())
        _log.debug(String.format("Trust Anchor: %s", new Object[] { tsTrustAnchorCert })); 
    } 
  }
  
  private void verifyClientMetadata(JarFile jarFile) throws IOException, ManifestSignatureException {
    ZipEntry clientMetadataFile = jarFile.getEntry("signed_manifest.properties");
    if (clientMetadataFile == null)
      throw new ManifestSignatureException("No signed_manifest.properties file found in the zip.", ManifestSignatureException.Reason.CLIENT_METADATA_NOT_VERIFIED); 
    try (InputStream clientMetadataStream = jarFile.getInputStream(clientMetadataFile)) {
      Properties metadata = new Properties();
      metadata.load(clientMetadataStream);
      verifySignatureOwner(metadata);
    } 
  }
  
  private void verifySignatureOwner(Properties metadata) throws ManifestSignatureException {
    String signatureOwner = metadata.getProperty("owner");
    if (signatureOwner == null)
      throw new ManifestSignatureException("The \"owner\" metadata may not be null", ManifestSignatureException.Reason.CLIENT_METADATA_NOT_VERIFIED); 
    if (!signatureOwner.equals("svc.manifest-signer@vmware.com"))
      throw new ManifestSignatureException("The signature owner must be: \"svc.manifest-signer@vmware.com\" found: \"" + signatureOwner + "\" " + ManifestSignatureException.Reason.CLIENT_METADATA_NOT_VERIFIED); 
  }
  
  private static Set<String> checkForUnsignedOrMissingEntries(JarFile jar, Manifest man) throws ManifestSignatureException {
    Set<String> signedEntries = new HashSet<>();
    Map<String, Attributes> manEntries = man.getEntries();
    for (Map.Entry<String, Attributes> manEntry : manEntries.entrySet()) {
      String manEntryName = manEntry.getKey();
      Attributes attrs = manEntry.getValue();
      for (Object attrKey : attrs.keySet()) {
        if (attrKey instanceof Attributes.Name) {
          String attrName = attrKey.toString();
          if (attrName.endsWith("-Digest"))
            signedEntries.add(manEntryName); 
        } 
      } 
    } 
    if (_log.isDebugEnabled())
      _log.debug("Signed entries: " + signedEntries); 
    Set<String> allEntries = new HashSet<>();
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        allEntries.add(entryName);
      } 
    } 
    if (_log.isDebugEnabled())
      _log.debug("All entries: " + allEntries); 
    Set<String> unsignedEntries = new HashSet<>(allEntries);
    unsignedEntries.removeAll(signedEntries);
    if (_log.isDebugEnabled())
      _log.debug("Unsigned entries: " + unsignedEntries); 
    for (String unsignedEntry : unsignedEntries) {
      if (!isAnEntryThatCanBeUnsigned(unsignedEntry))
        throw new ManifestSignatureException("Found an unsigned entry: " + unsignedEntry, ManifestSignatureException.Reason.UNSIGNED_ENTRIES); 
    } 
    Set<String> missingEntries = new HashSet<>(signedEntries);
    missingEntries.removeAll(allEntries);
    if (!missingEntries.isEmpty())
      throw new ManifestSignatureException("Some entries which have a signature in the manifest file but are missing from the archive: " + missingEntries, ManifestSignatureException.Reason.MISSING_ENTRIES); 
    return signedEntries;
  }
  
  private static boolean isAnEntryThatCanBeUnsigned(String entryName) {
    entryName = entryName.toUpperCase(Locale.ENGLISH);
    if (!entryName.startsWith("META-INF/") && 
      !entryName.startsWith("/META-INF/"))
      return false; 
    if ("META-INF/MANIFEST.MF".equals(entryName))
      return true; 
    return (entryName.endsWith(".SF") || entryName
      .endsWith(".DSA") || entryName
      .endsWith(".RSA") || entryName
      .endsWith(".EC"));
  }
  
  private static void readFully(InputStream is) throws IOException {
    byte[] bytes = new byte[4096];
    try {
      while (is.read(bytes, 0, 4096) > 0);
    } finally {
      is.close();
    } 
  }
  
  public List<String> getRDN(X509Certificate cert, String rdnType) throws InvalidNameException {
    List<String> attributeValues = new ArrayList<>();
    X500Principal principal = cert.getSubjectX500Principal();
    LdapName ldapName = new LdapName(principal.getName());
    for (Rdn rdn : ldapName.getRdns()) {
      if (rdnType.equals(rdn.getType()))
        attributeValues.add((String)rdn.getValue()); 
    } 
    return attributeValues;
  }
}
