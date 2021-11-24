package com.vmware.ph.phservice.common.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PropertiesFileConfigurationService extends BaseConfigurationService {
  private final File _propertiesFile;
  
  public PropertiesFileConfigurationService(File propertiesFile) {
    this._propertiesFile = propertiesFile;
  }
  
  public String getProperty(String propertyName) {
    return (String)getAllProperties().get(propertyName);
  }
  
  private Map<Object, Object> getAllProperties() {
    Properties props = new Properties();
    try (FileReader fileReader = new FileReader(this._propertiesFile)) {
      props.load(fileReader);
    } catch (IOException iOException) {}
    return props;
  }
}
