package com.vmware.ph.phservice.common;

import java.util.Map;
import java.util.Set;

public interface PersistenceService {
  Map<String, String> readValues(Iterable<String> paramIterable) throws PersistenceServiceException;
  
  String readString(String paramString) throws PersistenceServiceException;
  
  Long readLong(String paramString) throws PersistenceServiceException;
  
  Integer readInt(String paramString) throws PersistenceServiceException;
  
  void writeValues(Map<String, String> paramMap) throws PersistenceServiceException;
  
  Boolean readBoolean(String paramString) throws PersistenceServiceException;
  
  void writeString(String paramString1, String paramString2) throws PersistenceServiceException;
  
  void writeLong(String paramString, long paramLong) throws PersistenceServiceException;
  
  void writeInt(String paramString, int paramInt) throws PersistenceServiceException;
  
  void writeBoolean(String paramString, boolean paramBoolean) throws PersistenceServiceException;
  
  Set<String> getAllKeys() throws PersistenceServiceException;
  
  void deleteValues(Set<String> paramSet) throws PersistenceServiceException;
}
