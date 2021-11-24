package com.vmware.ph.phservice.common.ph.internal;

import com.google.common.annotations.VisibleForTesting;
import com.vmware.ph.phservice.common.internal.file.FileUtil;
import com.vmware.ph.phservice.common.ph.internal.signature.FileSignatureService;
import com.vmware.ph.phservice.common.ph.internal.signature.ManifestSignatureException;
import com.vmware.ph.phservice.common.ph.internal.signature.ManifestSignatureGeneralException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManifestSignatureValidator {
  private static final Log _log = LogFactory.getLog(ManifestSignatureValidator.class);
  
  private static final String JAR_FILE_EXTENSION = ".jar";
  
  private static final String TEMP_JAR_FILENAME = "tempManifest";
  
  private static final String MANIFEST_FILE_NAME = "manifest";
  
  private final FileSignatureService _fileSignatureService;
  
  public ManifestSignatureValidator(String keystorePass) {
    this(new FileSignatureService(keystorePass));
  }
  
  public ManifestSignatureValidator(Collection<? extends X509Certificate> trustedCaCerts) {
    this(new FileSignatureService(trustedCaCerts));
  }
  
  private ManifestSignatureValidator(FileSignatureService fileSignatureService) {
    this._fileSignatureService = fileSignatureService;
  }
  
  public String verifyManifest(String encodedZip) throws ManifestSignatureGeneralException, ManifestSignatureException {
    byte[] decodedManifestZip = decodeManifestZip(encodedZip);
    Path tempFilePath = null;
    try {
      tempFilePath = createTempManifestJarFile(decodedManifestZip);
    } catch (IOException e) {
      throw new ManifestSignatureGeneralException(e);
    } finally {
      FileUtil.deleteFileSafe(tempFilePath);
    } 
  }
  
  private byte[] decodeManifestZip(String encodedManifestZip) throws ManifestSignatureException {
    if (encodedManifestZip == null || encodedManifestZip.isEmpty())
      throw new ManifestSignatureException("Base64-encoded manifest ZIP is empty or null", ManifestSignatureException.Reason.IMPROPER_FORMAT); 
    try {
      return Base64.getMimeDecoder().decode(encodedManifestZip.trim());
    } catch (IllegalArgumentException e) {
      throw new ManifestSignatureException("Failed to Base64-decode manifest ZIP", e, ManifestSignatureException.Reason.IMPROPER_FORMAT);
    } 
  }
  
  @VisibleForTesting
  Path createTempManifestJarFile(byte[] bytes) throws IOException {
    return FileUtil.createTempFile("tempManifest", ".jar", bytes);
  }
  
  private String getValidatedManifestContent(JarFile jarFile) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile
            .getInputStream(jarFile.getEntry("manifest"))))) {
      String line;
      while ((line = reader.readLine()) != null)
        sb.append(line + "\n"); 
    } catch (IOException e) {
      _log.error("Could not read manifest file from jar", e);
    } 
    return sb.toString().trim();
  }
}
