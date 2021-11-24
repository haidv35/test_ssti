package com.vmware.ph.phservice.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PageUtil {
  public static final int NO_LIMIT = -1;
  
  public static List<Integer> getPagesOffsets(int itemsSize, int pageSize) {
    List<Integer> pageOffsets = new ArrayList<>();
    int offset = 0;
    while (offset < itemsSize) {
      pageOffsets.add(Integer.valueOf(offset));
      offset += pageSize;
    } 
    return pageOffsets;
  }
  
  public static <T> List<T> pageItems(List<T> items, int offset, int limit) {
    return pageWindowItems(items, 0, offset, limit);
  }
  
  public static <T> List<T> pageWindowItems(List<T> windowItems, int windowAbsoluteOffset, int offset, int limit) {
    Pair<Integer, Integer> windowIndexes = pageWindow(windowAbsoluteOffset, windowItems.size(), offset, limit);
    int startIndexInWindow = ((Integer)windowIndexes.getFirst()).intValue();
    int endIndexInWindow = ((Integer)windowIndexes.getSecond()).intValue();
    if (endIndexInWindow > 0)
      return windowItems.subList(startIndexInWindow, endIndexInWindow); 
    return Collections.emptyList();
  }
  
  public static Pair<Integer, Integer> pageWindow(int windowAbsoluteOffset, int windowLength, int offset, int limit) {
    int startIndexInWindow = 0;
    int endIndexInWindow = 0;
    Pair<Integer, Integer> result = intersectIntervals(windowAbsoluteOffset, windowLength, offset, limit);
    int resultOffset = ((Integer)result.getFirst()).intValue();
    int resultSize = ((Integer)result.getSecond()).intValue();
    if (resultSize > 0) {
      startIndexInWindow = resultOffset - windowAbsoluteOffset;
      endIndexInWindow = startIndexInWindow + resultSize;
    } 
    return new Pair<>(Integer.valueOf(startIndexInWindow), Integer.valueOf(endIndexInWindow));
  }
  
  public static <T> List<T> pageItems(ItemsStream<T> itemsStream, int offset, int limit) {
    Objects.requireNonNull(itemsStream, "The ItemsStream must not be null.");
    if (offset < 0)
      throw new IllegalArgumentException("Offset cannot be a negative number."); 
    if (offset > 0)
      readItems(itemsStream, offset, true); 
    List<T> items = null;
    if (limit != -1) {
      items = readItems(itemsStream, limit, false);
    } else {
      items = readItems(itemsStream);
    } 
    return items;
  }
  
  static Pair<Integer, Integer> intersectIntervals(int intervalAStart, int intervalALength, int intervalBStart, int intervalBLength) {
    int intervalCStart = 0;
    int intervalCLength = 0;
    int intervalAEnd = intervalAStart + intervalALength - 1;
    int intervalBEnd = intervalBStart + intervalBLength - 1;
    if (intervalAEnd >= intervalBStart && (0 >= intervalBLength || intervalBEnd >= intervalAStart)) {
      intervalCStart = Math.max(intervalAStart, intervalBStart);
      int intervalCEnd = 0;
      if (intervalBLength > 0) {
        intervalCEnd = Math.min(intervalAEnd, intervalBEnd);
      } else {
        intervalCEnd = intervalAEnd;
      } 
      intervalCLength = intervalCEnd - intervalCStart + 1;
    } 
    return new Pair<>(Integer.valueOf(intervalCStart), Integer.valueOf(intervalCLength));
  }
  
  static <T> List<T> readItems(ItemsStream<T> itemsStream, int numItemsToRead, boolean shouldSkip) {
    List<T> allItemsRead = Collections.emptyList();
    if (!shouldSkip)
      allItemsRead = new ArrayList<>(numItemsToRead); 
    int streamPageSize = itemsStream.getLimit();
    boolean isStreamEmpty = false;
    while (numItemsToRead > 0 && !isStreamEmpty) {
      List<T> items = itemsStream.read(Math.min(streamPageSize, numItemsToRead));
      if (items.isEmpty()) {
        isStreamEmpty = true;
        continue;
      } 
      if (!shouldSkip)
        allItemsRead.addAll(items); 
      numItemsToRead -= items.size();
    } 
    return allItemsRead;
  }
  
  static <T> List<T> readItems(ItemsStream<T> itemsStream) {
    List<T> allItemsRead = new ArrayList<>();
    boolean isStreamEmpty = false;
    while (!isStreamEmpty) {
      List<T> items = itemsStream.read(itemsStream.getLimit());
      if (items.isEmpty()) {
        isStreamEmpty = true;
        continue;
      } 
      allItemsRead.addAll(items);
    } 
    return allItemsRead;
  }
}
