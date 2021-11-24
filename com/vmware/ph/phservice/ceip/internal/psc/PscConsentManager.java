package com.vmware.ph.phservice.ceip.internal.psc;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.ceip.internal.client.ClientConsentManager;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PscConsentManager implements ConsentManager {
  private static final Logger _log = LoggerFactory.getLogger(PscConsentManager.class);
  
  private final ClientConsentManager _pscClientConsentManager;
  
  public PscConsentManager(ClientConsentManager clientConsentManager) {
    this._pscClientConsentManager = Objects.<ClientConsentManager>requireNonNull(clientConsentManager, "The PSC configured ClientConsentManager must be specified.");
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    validateActiveState();
    return this._pscClientConsentManager.readConsent();
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    validateActiveState();
    this._pscClientConsentManager.writeConsent(consent);
  }
  
  public boolean isActive() {
    boolean isActive = this._pscClientConsentManager.isActive();
    _log.debug("Active state for {} is {}.", PscConsentManager.class.getSimpleName(), Boolean.valueOf(isActive));
    return isActive;
  }
  
  private void validateActiveState() throws ConsentException {
    if (!isActive())
      throw new ConsentException(
          String.format("The %s is not active. Cannot perform actions on consent state.", new Object[] { PscConsentManager.class.getSimpleName() })); 
  }
}
