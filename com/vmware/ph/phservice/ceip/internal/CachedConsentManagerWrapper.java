package com.vmware.ph.phservice.ceip.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedConsentManagerWrapper implements ConsentManager, ConsentRefresher {
  private static final Logger _log = LoggerFactory.getLogger(CachedConsentManagerWrapper.class);
  
  private static final String KEY = "CEIP_STATE_KEY";
  
  private static final int CONSENT_CACHE_CAPACITY = 1;
  
  private final ConsentManager _wrappedConsentManager;
  
  private final LoadingCache<String, ConsentConfigurationData> _consentStateCache;
  
  public CachedConsentManagerWrapper(ConsentManager wrappedConsentManager, long cacheExpirationMillis) {
    this._wrappedConsentManager = wrappedConsentManager;
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    if (_log.isDebugEnabled())
      cacheBuilder.recordStats(); 
    this


      
      ._consentStateCache = cacheBuilder.concurrencyLevel(1).maximumSize(1L).expireAfterWrite(cacheExpirationMillis, TimeUnit.MILLISECONDS).build(new CacheLoader<String, ConsentConfigurationData>() {
          public ConsentConfigurationData load(@Nonnull String key) throws ConsentException {
            return CachedConsentManagerWrapper.this.doReadConsent();
          }
        });
  }
  
  public ConsentConfigurationData readConsent() {
    try {
      return (ConsentConfigurationData)this._consentStateCache.get("CEIP_STATE_KEY");
    } catch (ExecutionException e) {
      _log.warn("Failed to obtain the consent state. The consent state is considered to be 'null'.", e.getCause());
      return null;
    } 
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    this._wrappedConsentManager.writeConsent(consent);
    refreshConsent();
  }
  
  public boolean isActive() {
    return this._wrappedConsentManager.isActive();
  }
  
  public void refreshConsent() {
    if (!isActive()) {
      _log.debug("The consent manager is not active. The consent state will not be refreshed.");
      return;
    } 
    this._consentStateCache.refresh("CEIP_STATE_KEY");
  }
  
  private ConsentConfigurationData doReadConsent() throws ConsentException {
    _log.debug("Refreshing consent cache. Calling wrapped consent manager {}", this._wrappedConsentManager.getClass().getSimpleName());
    ConsentConfigurationData newCcData = this._wrappedConsentManager.readConsent();
    _log.debug("Consent cache refreshed with value {}", newCcData);
    _log.debug("Stats: {}", this._consentStateCache.stats());
    return newCcData;
  }
}
