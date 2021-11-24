package com.vmware.ph.phservice.push.telemetry.internal.log;

import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogTelemetrySortUtil {
  private static final Log _log = LogFactory.getLog(LogTelemetrySortUtil.class);
  
  private static final int LOG_INDEX_ZERO = 0;
  
  private static final int DEFAULT_ROLL_OVER_START_INDEX = 1;
  
  private static final int NON_INDEXED_LOG_FILE_INDEX = -1;
  
  public static List<Path> sortLogFilePathsByTimeAndRolloverIndex(List<Path> unsortedPaths) {
    List<Path> timeSortedPaths = new ArrayList<>(unsortedPaths);
    Collections.sort(timeSortedPaths, new LastModifiedFileComparator());
    if (areAllFilesModifiedAtDifferentTimes(timeSortedPaths))
      return timeSortedPaths; 
    _log.debug("The telemetry log file order cannot be determined by the last modified timestamps alone.");
    return sortLogFilePathsByTimeAndRolloverIndexInt(timeSortedPaths);
  }
  
  private static boolean areAllFilesModifiedAtDifferentTimes(List<Path> paths) {
    Set<Long> lastModifiedTimestamps = new HashSet<>();
    for (Path path : paths) {
      long lastModified = FileUtil.getLastModified(path);
      boolean isLastModifiedUnique = lastModifiedTimestamps.add(Long.valueOf(lastModified));
      if (!isLastModifiedUnique)
        return false; 
    } 
    return true;
  }
  
  private static List<Path> sortLogFilePathsByTimeAndRolloverIndexInt(List<Path> unsortedPaths) {
    SortedMap<Integer, Path> sortedIndexToPath = getSortedIndexToPaths(unsortedPaths);
    List<Path> nonIndexedPaths = getNonIndexedPaths(sortedIndexToPath);
    if (!sortedIndexToPath.containsKey(Integer.valueOf(1)))
      return new ArrayList<>(sortedIndexToPath.values()); 
    int rollOverStartIndex = 1;
    if (sortedIndexToPath.containsKey(Integer.valueOf(0)))
      rollOverStartIndex = 0; 
    List<List<Integer>> allRolloverSequences = generateAllRolloverSequences(rollOverStartIndex, unsortedPaths
        
        .size() - nonIndexedPaths.size());
    for (List<Integer> rolloverSequence : allRolloverSequences) {
      List<Path> indexSortedPaths = getPathsOrderedByIndex(rolloverSequence, sortedIndexToPath);
      if (areFilePathsTimeStampSorted(indexSortedPaths)) {
        indexSortedPaths.addAll(nonIndexedPaths);
        return indexSortedPaths;
      } 
    } 
    if (_log.isDebugEnabled())
      _log.trace("Cannot sort telemetry log files by rolling file index.Will process them as sorted by timestamp."); 
    return new ArrayList<>(sortedIndexToPath.values());
  }
  
  private static List<Path> getNonIndexedPaths(Map<Integer, Path> indexToPath) {
    Path nonIndexedPath = indexToPath.remove(Integer.valueOf(-1));
    if (nonIndexedPath == null)
      return Collections.emptyList(); 
    return Collections.singletonList(nonIndexedPath);
  }
  
  private static List<Path> getPathsOrderedByIndex(List<Integer> rolloverSequence, Map<Integer, Path> indexToPath) {
    List<Path> pathsOrderedByIndex = new ArrayList<>(rolloverSequence.size());
    for (Integer sequenceIndex : rolloverSequence) {
      Path path = indexToPath.get(sequenceIndex);
      if (path != null)
        pathsOrderedByIndex.add(path); 
    } 
    return pathsOrderedByIndex;
  }
  
  private static SortedMap<Integer, Path> getSortedIndexToPaths(List<Path> logFiles) {
    SortedMap<Integer, Path> indexToPaths = new TreeMap<>();
    for (Path logFile : logFiles) {
      int rolloverFileIndex = LogTelemetryUtil.getLogFileRolloverIndex(logFile, -1);
      indexToPaths.put(Integer.valueOf(rolloverFileIndex), logFile);
    } 
    return indexToPaths;
  }
  
  private static boolean areFilePathsTimeStampSorted(List<Path> paths) {
    long previousLastModified = 0L;
    for (Path path : paths) {
      long lastModified = FileUtil.getLastModified(path);
      if (lastModified < previousLastModified)
        return false; 
      previousLastModified = lastModified;
    } 
    return true;
  }
  
  private static List<List<Integer>> generateAllRolloverSequences(int from, int to) {
    List<List<Integer>> indexSequences = new ArrayList<>(to);
    for (int shift = 0; shift < to; shift++) {
      List<Integer> indexSequence = new ArrayList<>(to);
      int value;
      for (value = to - shift + 1; value <= to; value++)
        indexSequence.add(Integer.valueOf(value)); 
      for (value = from; value <= to - shift; value++)
        indexSequence.add(Integer.valueOf(value)); 
      indexSequences.add(indexSequence);
    } 
    return indexSequences;
  }
  
  private static class LastModifiedFileComparator implements Comparator<Path>, Serializable {
    private static final long serialVersionUID = 1L;
    
    private LastModifiedFileComparator() {}
    
    public int compare(Path o1, Path o2) {
      long o1LastModified = FileUtil.getLastModified(o1);
      long o2LastModified = FileUtil.getLastModified(o2);
      return Long.compare(o1LastModified, o2LastModified);
    }
  }
}
