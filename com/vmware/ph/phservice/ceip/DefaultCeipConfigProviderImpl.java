package com.vmware.ph.phservice.ceip;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.ceip.internal.CeipUtil;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCeipConfigProviderImpl implements CeipConfigProvider {
  private static final Logger _log = LoggerFactory.getLogger(DefaultCeipConfigProviderImpl.class);
  
  private final ConsentManager _consentManager;
  
  public DefaultCeipConfigProviderImpl(ConsentManager consentManager) {
    this._consentManager = consentManager;
  }
  
  public boolean isCeipEnabled() {
    boolean ceipEnabled = false;
    try {
      ConsentConfigurationData ccData = this._consentManager.readConsent();
      ceipEnabled = CeipUtil.isCeipConsentAccepted(ccData);
      _log.debug("Acquired CEIP state: {}", Boolean.valueOf(ceipEnabled));
    } catch (ConsentException e) {
      _log.debug("Failed to read the CEIP consent state. CEIP is assumed to be false.");
    } 
    return ceipEnabled;
  }
}
