package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessedLogFilesBookmarker {
  private static final Log _log = LogFactory.getLog(ProcessedLogFilesBookmarker.class);
  
  private static final String PROCESSED_LOGS_BOOKMARK_FILE_PATTERN = "%1$s_%2$s_processed_logs";
  
  private static final String BOOKMARK_ENTRY_PATTERN = "%s=%s%n";
  
  private static final String BOOKMARK_ENTRY_REGEX = "[^=]+=[\\d]+";
  
  private static final String BOOKMARK_ENTRY_DELIMITER = "=";
  
  private final Path _bookmarkDirectoryPath;
  
  public ProcessedLogFilesBookmarker(Path bookmarkDirectoryPath) {
    this._bookmarkDirectoryPath = bookmarkDirectoryPath;
  }
  
  public Map<Path, Date> loadBookmarks(String collectorId, String collectorInstanceId) {
    File bookmarkFile = getBookmarkFile(collectorId, collectorInstanceId);
    List<String> bookmarkFileLines = FileUtil.readLinesSafe(bookmarkFile);
    Map<Path, Date> bookmarks = buildBookmarks(bookmarkFileLines, bookmarkFile);
    return getExistingLogFilePathToLastProcessedDate(bookmarks);
  }
  
  public void saveBookmarks(String collectorId, String collectorInstanceId, Map<Path, Date> logFilePathToLastProcessedDate) {
    if (logFilePathToLastProcessedDate.isEmpty())
      return; 
    Map<Path, Date> existingFilesBookmarks = getExistingLogFilePathToLastProcessedDate(logFilePathToLastProcessedDate);
    List<String> bookmarkFileContent = convertToBookmarkFileContent(existingFilesBookmarks);
    createBookmarkDirectoryIfNotExist();
    File bookmarkFile = getBookmarkFile(collectorId, collectorInstanceId);
    FileUtil.writeLinesSafe(bookmarkFile, bookmarkFileContent);
  }
  
  File getBookmarkFile(String collectorId, String collectorInstanceId) {
    String processedLogsFileName = String.format("%1$s_%2$s_processed_logs", new Object[] { collectorId, collectorInstanceId });
    Path processedLogsFilePath = this._bookmarkDirectoryPath.resolve(processedLogsFileName);
    return processedLogsFilePath.toFile();
  }
  
  private void createBookmarkDirectoryIfNotExist() {
    if (!this._bookmarkDirectoryPath.toFile().exists())
      try {
        boolean hasCreatedSuccessfully = this._bookmarkDirectoryPath.toFile().mkdirs();
        if (!hasCreatedSuccessfully)
          logErrorForBookmarkDirectoryPathCreation(); 
      } catch (Exception e) {
        logErrorForBookmarkDirectoryPathCreation();
      }  
  }
  
  private void logErrorForBookmarkDirectoryPathCreation() {
    if (_log.isErrorEnabled())
      _log.error("Cannot create directories on path: " + this._bookmarkDirectoryPath
          
          .toFile().getAbsolutePath()); 
  }
  
  private static Map<Path, Date> buildBookmarks(List<String> bookmarkFileLines, File bookmarkFile) {
    Map<Path, Date> logFilePathToLastProcessedDate = new HashMap<>();
    for (String bookmarkLine : bookmarkFileLines) {
      if (!bookmarkLine.matches("[^=]+=[\\d]+")) {
        if (_log.isDebugEnabled())
          _log.debug(
              String.format("Bad entry: \"%s\" found in bookmark file \"%s\".", new Object[] { bookmarkLine, bookmarkFile.getAbsolutePath() })); 
        continue;
      } 
      String[] lineParts = bookmarkLine.split("=");
      Path logFilePath = Paths.get(lineParts[0], new String[0]);
      long lastProcessedTime = Long.parseLong(lineParts[1]);
      Date lastProcessedDate = new Date(lastProcessedTime);
      logFilePathToLastProcessedDate.put(logFilePath, lastProcessedDate);
    } 
    return logFilePathToLastProcessedDate;
  }
  
  private static Map<Path, Date> getExistingLogFilePathToLastProcessedDate(Map<Path, Date> bookmarks) {
    Map<Path, Date> existingFilesBookmarks = new HashMap<>();
    for (Map.Entry<Path, Date> bookmark : bookmarks.entrySet()) {
      if (((Path)bookmark.getKey()).toFile().exists())
        existingFilesBookmarks.put(bookmark.getKey(), bookmark.getValue()); 
    } 
    return existingFilesBookmarks;
  }
  
  private static List<String> convertToBookmarkFileContent(Map<Path, Date> logFilePathToLastProcessedDate) {
    List<String> bookmarkFileContent = new ArrayList<>(logFilePathToLastProcessedDate.size());
    for (Map.Entry<Path, Date> processedLogEntry : logFilePathToLastProcessedDate.entrySet()) {
      Date lastProcessedDate = processedLogEntry.getValue();
      String bookmarkEntry = String.format("%s=%s%n", new Object[] { processedLogEntry.getKey(), 
            Long.valueOf(lastProcessedDate.getTime()) });
      bookmarkFileContent.add(bookmarkEntry);
    } 
    return bookmarkFileContent;
  }
}
