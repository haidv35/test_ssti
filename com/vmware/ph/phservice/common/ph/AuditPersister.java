package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.exceptions.AuditFileWritingFailedException;
import com.vmware.ph.client.common.audit.LoggerWriter;
import com.vmware.ph.common.audit.AuditFileConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditPersister {
  private static final Logger _logger = LoggerFactory.getLogger(AuditPersister.class);
  
  private static final String AUDIT_TARGET_FILENAME = "analytics.audit";
  
  private static final int AUDIT_FILE_MAX_SIZE_BYTES = 10485760;
  
  private static final int AUDIT_FILE_MAX_HISTORY = 5;
  
  private static final String CEIP_DIRECTORY = "ceip";
  
  private static final String AUDIT_DIRECTORY = "audit";
  
  private final Path _auditParentDirectoriesPath;
  
  private final AuditFileConfig _auditFileConfig;
  
  public AuditPersister(Path storageDirectoryPath) {
    this

      
      ._auditParentDirectoriesPath = storageDirectoryPath.resolve("ceip").resolve("audit");
    Path auditFilePath = this._auditParentDirectoriesPath.resolve("analytics.audit");
    this
      ._auditFileConfig = new AuditFileConfig(auditFilePath.toAbsolutePath().toString(), true, 10485760, 5);
  }
  
  public synchronized void persist(String auditData) {
    try {
      if (Files.notExists(this._auditParentDirectoriesPath, new java.nio.file.LinkOption[0]))
        Files.createDirectories(this._auditParentDirectoriesPath, (FileAttribute<?>[])new FileAttribute[0]); 
      _logger.debug("Storing audit data.");
      try (LoggerWriter auditFileWriter = new LoggerWriter(this._auditFileConfig)) {
        auditFileWriter.write(auditData);
      } 
    } catch (IOException|SecurityException|UnsupportedOperationException e) {
      _logger.error("Audit data was not persisted.", e);
      throw new AuditFileWritingFailedException(e);
    } 
  }
}
