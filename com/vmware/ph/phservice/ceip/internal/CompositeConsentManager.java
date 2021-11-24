package com.vmware.ph.phservice.ceip.internal;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeConsentManager implements ConsentManager, ConsentRefresher {
  private static final Logger _log = LoggerFactory.getLogger(CompositeConsentManager.class);
  
  private final List<ConsentManager> _consentManagers;
  
  public CompositeConsentManager(List<ConsentManager> consentManagers) {
    this._consentManagers = consentManagers;
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    ConsentManager activeConsentManager = getActiveConsentManager();
    ConsentConfigurationData ccData = null;
    try {
      validateConsentManager(activeConsentManager);
      ccData = activeConsentManager.readConsent();
    } catch (CeipApiException e) {
      _log.warn("Consent data could not be read.", e);
    } 
    return ccData;
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    ConsentManager activeConsentManager = getActiveConsentManager();
    try {
      validateConsentManager(activeConsentManager);
      activeConsentManager.writeConsent(consent);
    } catch (CeipApiException e) {
      _log.warn("Consent data could not be stored.", e);
    } 
  }
  
  public boolean isActive() {
    ConsentManager activeConsentManager = getActiveConsentManager();
    return (activeConsentManager != null);
  }
  
  public void refreshConsent() {
    ConsentManager activeConsentManager = getActiveConsentManager();
    if (activeConsentManager == null)
      _log.info("No available active ConsentManager. Consent cache will not be refreshed."); 
    if (activeConsentManager instanceof ConsentRefresher) {
      ((ConsentRefresher)activeConsentManager).refreshConsent();
    } else {
      _log.info("The active consent manager is not recognized as having a consent cache.");
    } 
  }
  
  ConsentManager getActiveConsentManager() {
    ConsentManager ceipConsentManager = null;
    for (ConsentManager consentManager : this._consentManagers) {
      if (consentManager.isActive()) {
        ceipConsentManager = consentManager;
        break;
      } 
    } 
    return ceipConsentManager;
  }
  
  private void validateConsentManager(ConsentManager consentManager) throws ConsentException {
    if (consentManager == null)
      throw new ConsentException(
          String.format("The %s is not active. Cannot perform actions on consent state.", new Object[] { CompositeConsentManager.class.getSimpleName() })); 
  }
}
