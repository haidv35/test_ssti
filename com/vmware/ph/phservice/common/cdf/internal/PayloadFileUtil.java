package com.vmware.ph.phservice.common.cdf.internal;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;

public final class PayloadFileUtil {
  public static final int MAX_NUMBER_OF_LOCALLY_STORED_PAYLOADS = 32;
  
  public static final String DEFAULT_PAYLOAD_FILENAME_PREFIX = "payload";
  
  public static final String DEFAULT_PAYLOAD_FILENAME_NUMBER_PATTERN = "%04d";
  
  public static final String ARCHIVE_EXTENSION = ".gz";
  
  public static final String JSON_FILE_EXTENSION = ".json";
  
  private static final Object LOCK = new Object();
  
  public static void writeJsonToFile(String directoryPathName, String jsonString, boolean shouldArchivePayloads) throws IOException {
    writeJsonToFile(directoryPathName, jsonString, null, shouldArchivePayloads);
  }
  
  public static void writeJsonToFile(String directoryPathName, String jsonString, String jsonFileName, boolean shouldArchivePayloads) throws IOException {
    synchronized (LOCK) {
      File outputDirectory = FileUtil.getOrCreateDirectory(directoryPathName);
      if (StringUtils.isBlank(jsonFileName)) {
        jsonFileName = computeFileName(shouldArchivePayloads);
        rotatePayloadFiles(outputDirectory);
      } 
      File jsonFile = new File(outputDirectory, jsonFileName);
      if (shouldArchivePayloads) {
        FileUtil.writeStringToGzipFile(jsonFile, jsonString);
      } else {
        FileUtil.writeStringToFile(jsonFile, jsonString);
      } 
    } 
  }
  
  private static String computeFileName(boolean isArchived) {
    StringBuilder fileName = new StringBuilder("payload");
    fileName.append(String.format("%04d", new Object[] { Integer.valueOf(0) }));
    fileName.append(".json");
    if (isArchived)
      fileName.append(".gz"); 
    return fileName.toString();
  }
  
  private static void rotatePayloadFiles(File outputDirectory) throws IOException {
    File[] payloadFiles = outputDirectory.listFiles((dir, name) -> 
        (name.startsWith("payload") && name.contains(".")));
    if (payloadFiles != null && payloadFiles.length > 0) {
      Arrays.sort(payloadFiles, Collections.reverseOrder());
      int startTraversingFrom = 0;
      if (payloadFiles.length == 32) {
        payloadFiles[0].delete();
        startTraversingFrom = 1;
      } 
      for (int i = startTraversingFrom; i < payloadFiles.length; i++) {
        File oldFile = payloadFiles[i];
        String oldFileName = oldFile.getName();
        int extensionSeparatorIndex = oldFileName.indexOf('.');
        String oldFileNumber = oldFileName.substring("payload"
            .length(), extensionSeparatorIndex);
        String extensions = oldFileName.substring(extensionSeparatorIndex);
        int newFileNumber = Integer.parseInt(oldFileNumber) + 1;
        String newFileName = "payload" + String.format("%04d", new Object[] { Integer.valueOf(newFileNumber) }) + extensions;
        Files.move(oldFile.toPath(), oldFile.toPath().resolveSibling(newFileName), new java.nio.file.CopyOption[0]);
      } 
    } 
  }
}
