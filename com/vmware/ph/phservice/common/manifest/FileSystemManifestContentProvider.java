package com.vmware.ph.phservice.common.manifest;

import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemManifestContentProvider implements ManifestContentProvider {
  private static final Log _log = LogFactory.getLog(FileSystemManifestContentProvider.class);
  
  private final String _manifestPathName;
  
  public FileSystemManifestContentProvider(String manifestFilePathName) {
    this._manifestPathName = manifestFilePathName;
  }
  
  public boolean isEnabled() {
    return (this._manifestPathName != null && getManifestFile().isFile());
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    if (!isEnabled())
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(FileSystemManifestContentProvider.class
            .getName() + " is not enabled either because the manifest filename (" + this._manifestPathName + ") does not represent an existing filename.")); 
    try {
      return getManifestContentFromFile(getManifestFile());
    } catch (IOException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException("An IOException occurred while trying to read local file system manifest.", ManifestContentProvider.ManifestExceptionType.GENERAL_ERROR, e));
    } 
  }
  
  private String getManifestContentFromFile(File mfFile) throws IOException {
    if (_log.isDebugEnabled())
      _log.debug("Reading manifest from file " + mfFile.getAbsolutePath()); 
    return FileUtil.readFileToString(getManifestFile());
  }
  
  File getManifestFile() {
    return new File(this._manifestPathName);
  }
}
