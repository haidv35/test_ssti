package com.vmware.ph.phservice.common.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vmware.ph.phservice.common.PersistenceServiceException;
import com.vmware.ph.phservice.common.PropertiesFilePersistenceService;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachedPropertiesFilePersistenceService extends PropertiesFilePersistenceService {
  private static final String PROPERTIES_READ_CACHE_KEY = "propertiesReadCacheKey";
  
  private static final int CACHE_SIZE = 1;
  
  private final Cache<String, Properties> _persistenceFileCache;
  
  public CachedPropertiesFilePersistenceService(File persistenceFile, int persistenceFileCacheExpirationInterval) {
    super(persistenceFile);
    this


      
      ._persistenceFileCache = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(1L).expireAfterWrite(persistenceFileCacheExpirationInterval, TimeUnit.MILLISECONDS).build();
  }
  
  protected Properties readPropertiesFromFile() throws PersistenceServiceException {
    try {
      return (Properties)this._persistenceFileCache.get("propertiesReadCacheKey", this::load);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof PersistenceServiceException)
        throw (PersistenceServiceException)e.getCause(); 
      throw new PersistenceServiceException(e.getCause());
    } 
  }
  
  @VisibleForTesting
  Properties load() throws PersistenceServiceException {
    return super.readPropertiesFromFile();
  }
  
  protected void writePropertiesToFile(Properties props) throws PersistenceServiceException {
    this._persistenceFileCache.put("propertiesReadCacheKey", props);
    super.writePropertiesToFile(props);
  }
}
