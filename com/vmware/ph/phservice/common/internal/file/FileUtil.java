package com.vmware.ph.phservice.common.internal.file;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
  public static final Pattern NOT_ALLOWED_FILENAME_CHARACTERS_PATTERN = Pattern.compile("[^\\w_.-]");
  
  public static final String JSON_GZIP_FILE_EXTENSION = ".json.gz";
  
  private static final Log _log = LogFactory.getLog(FileUtil.class);
  
  public static Path[] listFiles(Path directory, FilenameFilter fileNameFilter) {
    File[] files = directory.toFile().listFiles(fileNameFilter);
    Path[] paths = new Path[0];
    if (files != null)
      paths = toPaths(files); 
    return paths;
  }
  
  public static Path[] toPaths(File[] files) {
    Path[] paths = new Path[files.length];
    int i = 0;
    for (File file : files) {
      paths[i] = file.toPath();
      i++;
    } 
    return paths;
  }
  
  public static boolean isFileModifiedAfterTimestamp(Path logFilePath, Date timestamp) {
    boolean isModified = false;
    if (timestamp == null) {
      isModified = true;
    } else if (logFilePath != null && logFilePath.toFile() != null && 
      timestamp.getTime() < getLastModified(logFilePath)) {
      isModified = true;
    } 
    return isModified;
  }
  
  public static List<String> readLinesSafe(File file) {
    List<String> lines = new ArrayList<>();
    if (file.exists()) {
      try {
        lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        if (_log.isDebugEnabled())
          _log.debug("Could not read lines of file " + file, e); 
      } 
    } else {
      _log.warn(
          String.format("Attempting to read non-existent file: %s. Will report the file as empty.", new Object[] { file }));
    } 
    return lines;
  }
  
  public static void writeLinesSafe(File file, List<String> contentLines) {
    try {
      Files.write(file.toPath(), (Iterable)contentLines, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      if (_log.isDebugEnabled())
        _log.debug("Could not write to file " + file, e); 
    } 
  }
  
  public static long getLastModified(Path path) {
    try {
      FileTime lastModifiedTime = Files.getLastModifiedTime(path, new java.nio.file.LinkOption[0]);
      return lastModifiedTime.toMillis();
    } catch (Exception e) {
      if (_log.isTraceEnabled())
        _log.trace("Could obtain exact last modified time for " + path, e); 
      return path.toFile().lastModified();
    } 
  }
  
  public static void writeStringToFile(File file, String data) throws IOException {
    try (OutputStream out = new FileOutputStream(file)) {
      out.write(data.getBytes("UTF-8"));
    } 
  }
  
  public static String readFileToStringSafe(File file) {
    String result = null;
    try {
      result = readFileToString(file);
    } catch (Exception e) {
      if (_log.isWarnEnabled())
        _log.trace("Failed to read file contents." + file.getName()); 
      if (_log.isDebugEnabled())
        _log.debug("Error when reading file.", e); 
    } 
    return result;
  }
  
  public static String readFileToString(File file) throws IOException {
    try (InputStream in = new FileInputStream(file)) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length = 0;
      while ((length = in.read(buffer)) != -1)
        out.write(buffer, 0, length); 
      return new String(out.toByteArray(), "UTF-8");
    } 
  }
  
  public static String readGzipFileToString(File gzipFile) throws IOException {
    try(FileInputStream fis = new FileInputStream(gzipFile); 
        GZIPInputStream in = new GZIPInputStream(fis); 
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length = 0;
      while ((length = in.read(buffer)) != -1)
        out.write(buffer, 0, length); 
      return new String(out.toByteArray(), "UTF-8");
    } 
  }
  
  public static void writeStringToGzipFile(File gzipFile, String content) throws IOException {
    try(FileOutputStream out = new FileOutputStream(gzipFile); 
        GZIPOutputStream gzip = new GZIPOutputStream(out)) {
      gzip.write(content.getBytes("UTF-8"));
      gzip.flush();
    } 
  }
  
  public static boolean isStringInFile(Path path, String data) throws IOException {
    if (path == null) {
      if (_log.isDebugEnabled())
        _log.debug("Invalid path 'null'."); 
      return false;
    } 
    boolean isStringInFile = false;
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      do {
        line = reader.readLine();
        if (line != null && line.contains(data)) {
          isStringInFile = true;
          break;
        } 
      } while (line != null);
    } 
    return isStringInFile;
  }
  
  public static Path resolvePath(String pathPropertyName) {
    Path path = null;
    if (Paths.get(pathPropertyName, new String[0]).toFile().exists()) {
      path = Paths.get(pathPropertyName, new String[0]);
    } else {
      String pathPropertyValue = System.getProperty(pathPropertyName);
      if (pathPropertyValue == null)
        throw new IllegalStateException("Unable to determine path property value. Missing property: " + pathPropertyName); 
      path = Paths.get(pathPropertyValue, new String[0]);
    } 
    if (_log.isInfoEnabled())
      _log.info("Resolved property: " + pathPropertyName + ", to path: " + path
          
          .toAbsolutePath()); 
    return path;
  }
  
  public static File getOrCreateDirectory(String directoryPathName) {
    File directory = new File(directoryPathName);
    directory.mkdirs();
    return directory;
  }
  
  public static Path createTempFile(String prefix, String suffix, byte[] bytes) throws IOException {
    Path tempFile = Files.createTempFile(prefix, suffix, (FileAttribute<?>[])new FileAttribute[0]);
    Files.write(tempFile, bytes, new java.nio.file.OpenOption[0]);
    return tempFile;
  }
  
  public static boolean deleteFileSafe(Path path) {
    if (path != null)
      try {
        return Files.deleteIfExists(path);
      } catch (Exception e) {
        _log.warn("Failed to delete file", e);
      }  
    return false;
  }
  
  public static void writeUtf8(File file, String contents) throws IOException {
    FileUtils.writeStringToFile(file, contents, StandardCharsets.UTF_8);
  }
  
  public static void writeToFileAtomicCreateDirs(File file, byte[] data) throws IOException {
    Objects.requireNonNull(file, "The destination file must be specified.");
    if (data == null)
      data = new byte[0]; 
    OutputStream out = null;
    try {
      createParentDirs(file);
      out = new TempFileOutputStream(file);
      out.write(data);
    } finally {
      if (out != null)
        out.close(); 
    } 
  }
  
  private static void createParentDirs(File file) throws IOException {
    assert file != null;
    File parent = file.getParentFile();
    if (parent != null && !parent.mkdirs() && !parent.isDirectory())
      throw new IOException("Directory '" + parent + "' could not be created"); 
  }
  
  public static String toUtf8String(File file) throws IOException {
    return toString(file, StandardCharsets.UTF_8);
  }
  
  private static String toString(File file, Charset encoding) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int)file.length());
    try {
      Files.copy(file.toPath(), outputStream);
    } finally {
      IOUtils.closeQuietly(outputStream);
    } 
    return new String(outputStream.toByteArray(), encoding);
  }
}
