package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogTelemetryDrainContextProvider {
  private static final Log _log = LogFactory.getLog(LogTelemetryDrainContextProvider.class);
  
  private final ProcessedLogFilesBookmarker _processedLogFilesBookmarker;
  
  private final LogTelemetryDrainPathsContext _drainContext;
  
  public LogTelemetryDrainContextProvider(LogTelemetryDrainPathsContext drainContext) {
    this._drainContext = drainContext;
    this
      ._processedLogFilesBookmarker = new ProcessedLogFilesBookmarker(drainContext.getBookmarkDirPath());
  }
  
  LogTelemetryDrainContext getContext(String collectorId, String collectorInstanceId) {
    List<Path> logFilePaths = getCollectorLogFilePathsFromDirs(this._drainContext
        .getDrainDirPaths(), collectorId, collectorInstanceId);
    Map<Path, Date> logFilePathToLastProcessedDate = this._processedLogFilesBookmarker.loadBookmarks(collectorId, collectorInstanceId);
    LogTelemetryDrainContext drainContext = new LogTelemetryDrainContext(collectorId, collectorInstanceId, logFilePaths, logFilePathToLastProcessedDate);
    return drainContext;
  }
  
  void updateContext(LogTelemetryDrainContext context) {
    Map<Path, Date> logFilePathToLastProcessedDate = context.getLogFilePathToLastProcessedDate();
    this._processedLogFilesBookmarker.saveBookmarks(context
        .getCollectorId(), context
        .getCollectorInstanceId(), logFilePathToLastProcessedDate);
  }
  
  private List<Path> getCollectorLogFilePathsFromDirs(Collection<Path> directoryPaths, String collectorId, String collectorInstanceId) {
    final String logFileNamePattern = LogTelemetryUtil.getLogFileNamePattern(collectorId, collectorInstanceId);
    Set<Path> unsortedPaths = new TreeSet<>();
    for (Path directoryPath : directoryPaths) {
      Path[] paths = FileUtil.listFiles(directoryPath, new FilenameFilter() {
            public boolean accept(File dir, String name) {
              return name.startsWith(logFileNamePattern);
            }
          });
      unsortedPaths.addAll(Arrays.asList(paths));
    } 
    _log.debug("Unsorted collector log file paths: " + unsortedPaths);
    List<Path> timeAndIndexSortedPaths = LogTelemetrySortUtil.sortLogFilePathsByTimeAndRolloverIndex(new ArrayList<>(unsortedPaths));
    _log.debug("Sorted collector log file paths: " + timeAndIndexSortedPaths);
    return timeAndIndexSortedPaths;
  }
  
  public List<CollectorAgent> getAvailableCollectors() {
    Set<CollectorAgent> collectors = new LinkedHashSet<>();
    Path[] paths = getLogPaths();
    _log.debug("Inferred log paths for getAvailableCollectors: " + 
        Arrays.toString((Object[])paths));
    for (Path path : paths) {
      CollectorAgent collectorAgent = LogTelemetryUtil.getCollectorAgent(path);
      collectors.add(collectorAgent);
    } 
    return new ArrayList<>(collectors);
  }
  
  private Path[] getLogPaths() {
    Set<Path> logPaths = new HashSet<>();
    Set<Path> drainDirPaths = this._drainContext.getDrainDirPaths();
    _log.debug("Looking through the following drain dir paths for available collectors: " + drainDirPaths);
    for (Path drainPath : drainDirPaths)
      logPaths.addAll(
          Arrays.asList(LogTelemetryUtil.getTelemetryLogPaths(drainPath))); 
    return logPaths.<Path>toArray(new Path[logPaths.size()]);
  }
}
