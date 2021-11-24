package com.vmware.ph.phservice.common;

import com.vmware.ph.phservice.common.internal.BasePersistenceService;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class PropertiesFilePersistenceService extends BasePersistenceService {
  private static final String EMPTY_STRING = "";
  
  private final File _persistenceFile;
  
  public PropertiesFilePersistenceService(File persistenceFile) {
    this._persistenceFile = persistenceFile;
  }
  
  public final Map<String, String> readValues(Iterable<String> keys) throws PersistenceServiceException {
    Properties props = readPropertiesFromFile();
    Map<String, String> keyValues = new HashMap<>();
    for (String key : keys)
      keyValues.put(key, props.getProperty(key)); 
    return keyValues;
  }
  
  public final synchronized void writeValues(Map<String, String> propertyKeyToPropertyValueMap) throws PersistenceServiceException {
    Objects.requireNonNull(propertyKeyToPropertyValueMap);
    Properties persistenceProperties = new Properties();
    if (this._persistenceFile.exists())
      persistenceProperties = readPropertiesFromFile(); 
    for (Map.Entry<String, String> keyValuePair : propertyKeyToPropertyValueMap.entrySet()) {
      String key = keyValuePair.getKey();
      Objects.requireNonNull(key);
      Object value = keyValuePair.getValue();
      persistenceProperties.setProperty(key, (value != null) ? value
          
          .toString() : "");
    } 
    writePropertiesToFile(persistenceProperties);
  }
  
  public final Set<String> getAllKeys() throws PersistenceServiceException {
    Properties props = readPropertiesFromFile();
    Set<Object> keyObjectsSet = props.keySet();
    Set<String> keys = new HashSet<>(keyObjectsSet.size());
    for (Object keyObject : keyObjectsSet)
      keys.add((String)keyObject); 
    return keys;
  }
  
  public final synchronized void deleteValues(Set<String> keys) throws PersistenceServiceException {
    Objects.requireNonNull(keys);
    Properties props = readPropertiesFromFile();
    for (String key : keys)
      props.remove(key); 
    writePropertiesToFile(props);
  }
  
  protected final String readValue(String key) throws PersistenceServiceException {
    Objects.requireNonNull(key);
    Properties props = readPropertiesFromFile();
    return props.getProperty(key);
  }
  
  protected final synchronized void writeValue(String key, String value) throws PersistenceServiceException {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    Properties props = new Properties();
    if (this._persistenceFile.exists())
      props = readPropertiesFromFile(); 
    props.setProperty(key, value);
    writePropertiesToFile(props);
  }
  
  protected Properties readPropertiesFromFile() throws PersistenceServiceException {
    Properties props = new Properties();
    try (FileReader reader = new FileReader(this._persistenceFile)) {
      props.load(reader);
    } catch (IOException e) {
      String message = String.format("Caught exception while trying to read persistence file %s.", new Object[] { this._persistenceFile });
      throw new PersistenceServiceException(message, e);
    } 
    return props;
  }
  
  protected void writePropertiesToFile(Properties props) throws PersistenceServiceException {
    try (FileWriter writer = new FileWriter(this._persistenceFile)) {
      props.store(writer, (String)null);
    } catch (IOException e) {
      String message = String.format("Caught exception while trying to write to persistence file %s.", new Object[] { this._persistenceFile });
      throw new PersistenceServiceException(message, e);
    } 
  }
}
