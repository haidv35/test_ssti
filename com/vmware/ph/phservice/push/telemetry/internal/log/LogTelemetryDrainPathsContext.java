package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class LogTelemetryDrainPathsContext {
  private Set<Path> _drainDirPaths;
  
  private Path _bookmarkDirPath;
  
  public LogTelemetryDrainPathsContext(Set<Path> drainDirPaths, Path bookmarkDirPath) {
    this._drainDirPaths = drainDirPaths;
    this._bookmarkDirPath = bookmarkDirPath;
  }
  
  public LogTelemetryDrainPathsContext(Path drainDirPath, Path bookmarkDirPath) {
    Set<Path> drainDirPaths = new HashSet<>();
    drainDirPaths.add(drainDirPath);
    this._drainDirPaths = drainDirPaths;
    this._bookmarkDirPath = bookmarkDirPath;
  }
  
  public Set<Path> getDrainDirPaths() {
    return this._drainDirPaths;
  }
  
  public Path getBookmarkDirPath() {
    return this._bookmarkDirPath;
  }
}
