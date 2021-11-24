package com.vmware.ph.phservice.cloud.health.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.ph.phservice.cloud.health.dataapp.DataAppMoRefToObjectIdConverter;
import com.vmware.ph.phservice.cloud.health.repository.SilencedTestsRepository;
import com.vmware.ph.phservice.common.internal.file.FileUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemSilencedTestsRepository implements SilencedTestsRepository {
  private static final Logger _logger = LoggerFactory.getLogger(FileSystemSilencedTestsRepository.class);
  
  private static final ObjectMapper _objectMapper = new ObjectMapper();
  
  private final DataAppMoRefToObjectIdConverter _moRefToObjectIdConverter;
  
  private final File _storageFile;
  
  private ConcurrentHashMap<String, HashSet<String>> _cachedObjectIdToSilencedTests;
  
  public FileSystemSilencedTestsRepository(File storageFile, DataAppMoRefToObjectIdConverter moRefToObjectIdConverter) {
    this._storageFile = storageFile;
    this._moRefToObjectIdConverter = moRefToObjectIdConverter;
  }
  
  public synchronized Set<String> getSilencedTests(ManagedObjectReference moRef) {
    Objects.requireNonNull(moRef);
    String objectId = this._moRefToObjectIdConverter.getObjectId(moRef);
    if (this._cachedObjectIdToSilencedTests == null) {
      _logger.debug("Cache is not populated yet, need to read from file storage.");
      if (!this._storageFile.exists()) {
        _logger.debug("No silenced tests file exists.");
        this._cachedObjectIdToSilencedTests = new ConcurrentHashMap<>();
      } else {
        try {
          this._cachedObjectIdToSilencedTests = new ConcurrentHashMap<>(readObjectIdToSilencedTests());
        } catch (IOException e) {
          _logger.debug("Could not read silenced checks from file, returning empty result.");
          this._cachedObjectIdToSilencedTests = null;
        } 
      } 
    } 
    HashSet<String> silencedTestsForObjectId = null;
    if (this._cachedObjectIdToSilencedTests != null)
      silencedTestsForObjectId = this._cachedObjectIdToSilencedTests.get(objectId); 
    _logger.debug("Silenced tests for objectId {}: {}", objectId, silencedTestsForObjectId);
    return Collections.unmodifiableSet(
        Optional.<Set<? extends String>>ofNullable(silencedTestsForObjectId).orElse(new HashSet<>()));
  }
  
  public synchronized void updateSilencedTests(ManagedObjectReference moRef, Set<String> testsToAdd, Set<String> testsToRemove) throws IOException {
    if (CollectionUtils.isEmpty(testsToAdd) && CollectionUtils.isEmpty(testsToRemove)) {
      _logger.debug("No tests to add or remove.");
      return;
    } 
    String objectId = this._moRefToObjectIdConverter.getObjectId(moRef);
    Set<String> originalSilencedTestsForObjectId = getSilencedTests(moRef);
    _logger.debug("Original silenced tests for objectId {}: {}\ntestsToAdd: {}, testsToRemove: {}", new Object[] { objectId, originalSilencedTestsForObjectId, testsToAdd, testsToRemove });
    HashSet<String> updatedSilencedTestsForObjectId = new HashSet<>();
    if (originalSilencedTestsForObjectId != null)
      updatedSilencedTestsForObjectId.addAll(originalSilencedTestsForObjectId); 
    if (!CollectionUtils.isEmpty(testsToAdd))
      updatedSilencedTestsForObjectId.addAll(testsToAdd); 
    if (!CollectionUtils.isEmpty(testsToRemove))
      updatedSilencedTestsForObjectId.removeAll(testsToRemove); 
    if (Objects.equals(originalSilencedTestsForObjectId, updatedSilencedTestsForObjectId)) {
      _logger.debug("Nothing to update. The updated silenced tests are the same as the original.");
      return;
    } 
    try {
      Map<String, HashSet<String>> updatedObjectIdToSilencedTests = cloneMap(_objectMapper, this._cachedObjectIdToSilencedTests);
      if (updatedSilencedTestsForObjectId.isEmpty()) {
        updatedObjectIdToSilencedTests.remove(objectId);
      } else {
        updatedObjectIdToSilencedTests.put(objectId, updatedSilencedTestsForObjectId);
      } 
      storeUpdatesToFileSystem(updatedObjectIdToSilencedTests);
      this._cachedObjectIdToSilencedTests = new ConcurrentHashMap<>(updatedObjectIdToSilencedTests);
    } catch (IOException e) {
      _logger.error("Failed to update silenced health tests to file system.", e);
      throw e;
    } 
  }
  
  public void invalidateCache() {
    this._cachedObjectIdToSilencedTests = null;
  }
  
  private Map<String, HashSet<String>> readObjectIdToSilencedTests() throws IOException {
    try {
      return (Map<String, HashSet<String>>)_objectMapper.readValue(this._storageFile, new TypeReference<Map<String, HashSet<String>>>() {
          
          });
    } catch (IOException e) {
      _logger.warn("Could not read the silenced tests from the storage file: {}", this._storageFile
          
          .toString());
      throw e;
    } 
  }
  
  private static Map<String, HashSet<String>> cloneMap(ObjectMapper objectMapper, Map<String, HashSet<String>> mapToClone) throws JsonProcessingException {
    String mapAsJsonString = objectMapper.writeValueAsString(mapToClone);
    return (Map<String, HashSet<String>>)objectMapper.readValue(mapAsJsonString, new TypeReference<Map<String, HashSet<String>>>() {
        
        });
  }
  
  private void storeUpdatesToFileSystem(Map<String, HashSet<String>> updatedObjectIdToSilencedTests) throws IOException {
    byte[] updatedContent = _objectMapper.writeValueAsBytes(updatedObjectIdToSilencedTests);
    FileUtil.writeToFileAtomicCreateDirs(this._storageFile, updatedContent);
  }
}
