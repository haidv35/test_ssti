package com.vmware.ph.phservice.ceip.internal.vc;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.internal.CachedConsentManagerWrapper;
import com.vmware.ph.phservice.ceip.internal.ConsentRefresher;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;

public class CachedSyncConsentManagerWrapper implements SyncConsentManager, ConsentRefresher {
  private final CachedConsentManagerWrapper _cachedConsentManager;
  
  private final SyncConsentManager _wrappedSyncConsentManager;
  
  public CachedSyncConsentManagerWrapper(SyncConsentManager wrappedSyncConsentManager, long cacheExpirationMillis) {
    this._wrappedSyncConsentManager = wrappedSyncConsentManager;
    this._cachedConsentManager = new CachedConsentManagerWrapper(this._wrappedSyncConsentManager, cacheExpirationMillis);
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    return this._cachedConsentManager.readConsent();
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    this._cachedConsentManager.writeConsent(consent);
  }
  
  public void sync() {
    this._wrappedSyncConsentManager.sync();
    this._cachedConsentManager.refreshConsent();
  }
  
  public boolean isActive() {
    return this._wrappedSyncConsentManager.isActive();
  }
  
  public void refreshConsent() {
    this._cachedConsentManager.refreshConsent();
  }
}
