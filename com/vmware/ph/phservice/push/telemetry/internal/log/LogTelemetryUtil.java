package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import com.vmware.ph.phservice.push.telemetry.CollectorAgent;
import com.vmware.ph.phservice.push.telemetry.TelemetryRequest;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogTelemetryUtil {
  private static final Log _log = LogFactory.getLog(LogTelemetryDrainContextProvider.class);
  
  static final String COLLECTOR_ID_PREFIX = "_c";
  
  static final String COLLECTOR_INSTANCE_ID_PREFIX = "_i";
  
  static final String LOG_FILE_NAME_PATTERN = "_c%1$s_i%2$s";
  
  private static final String DIGIT_FILE_EXTENSION_REGEX = "[.][\\d]+$";
  
  private static final String LOG_COMPRESSED_FILE_EXTENSION_REGEX = "\\.[\\d]+\\.json.gz";
  
  private static final Pattern LOG_ROLLING_FILE_INDEX_PATTERN = Pattern.compile(".+\\.(\\d+).json.gz");
  
  public static String getLogFileNamePattern(String collectorId, String collectorInstanceId) {
    String logFileName = String.format("_c%1$s_i%2$s", new Object[] { collectorId, collectorInstanceId });
    return logFileName;
  }
  
  public static Path[] getTelemetryLogPaths(final Path logDirPath) {
    _log.debug("Executing getTelemetryLogPaths for path: " + logDirPath);
    Path[] paths = FileUtil.listFiles(logDirPath, new FilenameFilter() {
          public boolean accept(File dir, String name) {
            LogTelemetryUtil._log.debug(
                String.format("In accept for %s and %s: %s", new Object[] { dir, name, Boolean.valueOf((name.startsWith("_c") && 
                      LogTelemetryUtil.isCompressed(this.val$logDirPath.resolve(name)))) }));
            return (name.startsWith("_c") && LogTelemetryUtil.isCompressed(logDirPath
                .resolve(name)));
          }
        });
    return paths;
  }
  
  public static CollectorAgent getCollectorAgent(Path logFilePath) {
    _log.debug("Getting collector agent from path: " + logFilePath);
    String logFileName = getLogFileName(logFilePath);
    int instanceIdIndex = logFileName.indexOf("_i");
    String collectorId = logFileName.substring("_c"
        .length(), instanceIdIndex);
    String collectorInstanceId = logFileName.substring(instanceIdIndex + "_i"
        .length());
    CollectorAgent collectorAgent = new CollectorAgent(collectorId, collectorInstanceId);
    _log.debug("Inferred collector agent: " + collectorAgent);
    return collectorAgent;
  }
  
  public static boolean isCompressed(Path logFilePath) {
    boolean isCompressed = logFilePath.toString().endsWith(".gz");
    return isCompressed;
  }
  
  public static TelemetryRequest buildTelemetryRequest(String collectorId, String collectorInstanceId, Path logFilePath) throws IOException {
    boolean isCompressed = isCompressed(logFilePath);
    byte[] data = Files.readAllBytes(logFilePath);
    TelemetryRequest telemetryRequest = new TelemetryRequest("1.0", collectorId, collectorInstanceId, data, isCompressed);
    return telemetryRequest;
  }
  
  public static boolean isLogFileNotProcessed(Path logFilePath, Date lastProcessedTimestamp) {
    if (FileUtil.isFileModifiedAfterTimestamp(logFilePath, lastProcessedTimestamp))
      return true; 
    return false;
  }
  
  public static int getLogFileRolloverIndex(Path path, int defaultIndex) {
    String logFileName = path.toString();
    Matcher matcher = LOG_ROLLING_FILE_INDEX_PATTERN.matcher(logFileName);
    if (matcher.matches())
      return Integer.parseInt(matcher.group(1)); 
    return defaultIndex;
  }
  
  private static String getLogFileName(Path logFilePath) {
    String fileName = logFilePath.getFileName().toString();
    if (fileName.endsWith(".json.gz"))
      return fileName.replaceAll("\\.[\\d]+\\.json.gz", ""); 
    if (fileName.endsWith(".json")) {
      int extensionIndex = fileName.length() - ".json".length();
      return fileName.substring(0, extensionIndex)
        .replaceAll("[.][\\d]+$", "");
    } 
    return fileName;
  }
  
  public static void deleteLogDirectory(Path logDirPath) {
    if (logDirPath == null)
      return; 
    File collectorLogDir = logDirPath.toFile();
    File[] logFiles = collectorLogDir.listFiles();
    if (logFiles != null) {
      for (File file : logFiles)
        file.delete(); 
      collectorLogDir.delete();
    } 
  }
}
