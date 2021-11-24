package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.appender.rolling.AbstractRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.GzCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.ZipCompressAction;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name = "CircularRolloverStrategy", category = "Core")
public class CircularRolloverStrategy extends AbstractRolloverStrategy {
  private static final Log _log = LogFactory.getLog(CircularRolloverStrategy.class);
  
  static final String STRATEGY_NAME = "CircularRolloverStrategy";
  
  private final int _maxFiles;
  
  private final int _compressionLevel;
  
  private final List<Action> _customActions;
  
  private volatile int _currentRolloverFileIndex;
  
  private volatile boolean _loadedOnStart;
  
  @PluginFactory
  public static CircularRolloverStrategy createStrategy(@Required @PluginAttribute("max") String max, @PluginElement("Actions") Action[] customActions, @PluginAttribute("compressionLevel") String compressionLevelStr, @PluginConfiguration Configuration config) {
    int compressionLevel = Integers.parseInt(compressionLevelStr, -1);
    List<Action> actions = (customActions != null) ? Arrays.<Action>asList(customActions) : new LinkedList<>();
    return new CircularRolloverStrategy(
        Integer.parseInt(max), compressionLevel, actions, config

        
        .getStrSubstitutor());
  }
  
  protected CircularRolloverStrategy(int maxFiles, int compressionLevel, List<Action> customActions, StrSubstitutor strSubstitutor) {
    super(strSubstitutor);
    this._maxFiles = maxFiles;
    this._compressionLevel = compressionLevel;
    this._customActions = customActions;
  }
  
  public RolloverDescription rollover(RollingFileManager manager) throws SecurityException {
    if (!this._loadedOnStart) {
      this._currentRolloverFileIndex = loadCurrentFileIndex(manager);
      this._loadedOnStart = true;
    } 
    this._currentRolloverFileIndex++;
    if (this._currentRolloverFileIndex > this._maxFiles)
      this._currentRolloverFileIndex = 1; 
    deleteCurrentRolloverFileIfExists(manager);
    StringBuilder buf = new StringBuilder(255);
    manager.getPatternProcessor().formatFileName(this.strSubstitutor, buf, Integer.valueOf(this._currentRolloverFileIndex));
    String currentRolloverFileName = buf.toString();
    Action compressAction = createCompressCurrentActiveFileAction(manager, currentRolloverFileName);
    Action action = merge(compressAction, this._customActions, true);
    return (RolloverDescription)new RolloverDescriptionImpl(currentRolloverFileName, false, action, null);
  }
  
  private void deleteCurrentRolloverFileIfExists(RollingFileManager manager) {
    SortedMap<Integer, Path> eligibleFiles = getEligibleFiles(manager);
    try {
      LOGGER.debug("Eligible files: {}", eligibleFiles);
      Path currentPath = eligibleFiles.get(Integer.valueOf(this._currentRolloverFileIndex));
      if (currentPath != null) {
        LOGGER.debug("Deleting {}", currentPath.toFile().getAbsolutePath());
        Files.delete(currentPath);
        eligibleFiles.remove(Integer.valueOf(this._currentRolloverFileIndex));
      } 
    } catch (IOException ioe) {
      LOGGER.error("Unable to delete {}", eligibleFiles.firstKey(), ioe);
    } 
  }
  
  private Action createCompressCurrentActiveFileAction(RollingFileManager manager, String currentRolloverFileName) {
    Action compressAction = null;
    FileExtension fileExtension = manager.getFileExtension();
    if (fileExtension != null) {
      File compressSource = new File(manager.getFileName());
      File compressTarget = new File(currentRolloverFileName);
      _log.info(
          String.format("Compressing %s to %s", new Object[] { compressSource.getName(), currentRolloverFileName }));
      compressAction = createCompressAction(fileExtension
          .name(), compressSource, compressTarget, true, this._compressionLevel);
    } 
    return compressAction;
  }
  
  private static Action createCompressAction(String fileExtension, File source, File target, boolean deleteSource, int compressionLevel) {
    switch (fileExtension) {
      case "ZIP":
        return (Action)new ZipCompressAction(source, target, deleteSource, compressionLevel);
      case "GZ":
        return (Action)new GzCompressAction(source, target, deleteSource);
    } 
    LOGGER.warn("Cannot create compress action for extension {}", fileExtension);
    return null;
  }
  
  private int loadCurrentFileIndex(RollingFileManager manager) {
    SortedMap<Integer, Path> eligibleFiles = getEligibleFiles(manager);
    long maxLastModified = 0L;
    int currentRolloverFileIndex = 0;
    for (Map.Entry<Integer, Path> eligibleFileEntry : eligibleFiles.entrySet()) {
      int fileIndex = ((Integer)eligibleFileEntry.getKey()).intValue();
      File logFile = ((Path)eligibleFileEntry.getValue()).toFile();
      long lastModified = logFile.lastModified();
      if (lastModified > maxLastModified && fileIndex <= this._maxFiles) {
        currentRolloverFileIndex = fileIndex;
        maxLastModified = lastModified;
      } 
    } 
    return currentRolloverFileIndex;
  }
}
