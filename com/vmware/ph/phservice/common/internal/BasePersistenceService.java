package com.vmware.ph.phservice.common.internal;

import com.vmware.ph.phservice.common.PersistenceService;
import com.vmware.ph.phservice.common.PersistenceServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BasePersistenceService implements PersistenceService {
  private static final Log _log = LogFactory.getLog(BasePersistenceService.class);
  
  public final String readString(String key) throws PersistenceServiceException {
    return readValue(key);
  }
  
  public final Long readLong(String key) throws PersistenceServiceException {
    Long result = null;
    String rawValue = readValue(key);
    if (rawValue != null)
      try {
        result = Long.valueOf(Long.parseLong(rawValue));
      } catch (NumberFormatException e) {
        _log.warn(
            String.format("Caught exception while trying to convert value for key %s", new Object[] { key }), e);
      }  
    return result;
  }
  
  public Integer readInt(String key) throws PersistenceServiceException {
    Integer result = null;
    String rawValue = readValue(key);
    if (rawValue != null)
      try {
        result = Integer.valueOf(Integer.parseInt(rawValue));
      } catch (NumberFormatException e) {
        _log.warn(
            String.format("Caught exception while trying to convert value for key %s", new Object[] { key }), e);
      }  
    return result;
  }
  
  public final Boolean readBoolean(String key) throws PersistenceServiceException {
    Boolean result = null;
    String rawValue = readValue(key);
    if (rawValue != null)
      result = Boolean.valueOf(Boolean.parseBoolean(rawValue)); 
    return result;
  }
  
  public final void writeString(String key, String value) throws PersistenceServiceException {
    writeValue(key, value);
  }
  
  public final void writeLong(String key, long value) throws PersistenceServiceException {
    writeValue(key, Long.toString(value));
  }
  
  public final void writeInt(String key, int value) throws PersistenceServiceException {
    writeValue(key, Integer.toString(value));
  }
  
  public final void writeBoolean(String key, boolean value) throws PersistenceServiceException {
    writeValue(key, Boolean.toString(value));
  }
  
  protected abstract String readValue(String paramString) throws PersistenceServiceException;
  
  protected abstract void writeValue(String paramString1, String paramString2) throws PersistenceServiceException;
}
