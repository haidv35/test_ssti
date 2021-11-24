package com.vmware.ph.phservice.ceip.internal.vmcgateway;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.ceip.internal.CeipUtil;
import com.vmware.ph.phservice.common.PersistenceService;
import com.vmware.ph.phservice.common.PersistenceServiceException;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmcGatewayConsentManager implements ConsentManager {
  private static final Logger _log = LoggerFactory.getLogger(VmcGatewayConsentManager.class);
  
  private final CisContextProvider _vmcgCisContextProvider;
  
  private final PersistenceService _persistenceService;
  
  private final String _propNameForVmcgCeipStatus;
  
  private CisContext _vmcgCisContext;
  
  public VmcGatewayConsentManager(CisContextProvider vmcgCisContextProvider, PersistenceService persistenceService, String propNameForVmcgCeipStatus) {
    this._vmcgCisContextProvider = vmcgCisContextProvider;
    this._persistenceService = persistenceService;
    this._propNameForVmcgCeipStatus = propNameForVmcgCeipStatus;
  }
  
  public VmcGatewayConsentManager(CisContext vmcgCisContext, PersistenceService persistenceService, String vmcgCeipStatusPropertyKey) {
    this._vmcgCisContext = vmcgCisContext;
    this._vmcgCisContextProvider = null;
    this._persistenceService = persistenceService;
    this._propNameForVmcgCeipStatus = vmcgCeipStatusPropertyKey;
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    initVmcgCisContext();
    validateVmcgCisContext();
    Boolean consentAccepted = null;
    try {
      consentAccepted = this._persistenceService.readBoolean(this._propNameForVmcgCeipStatus);
      if (consentAccepted == null) {
        _log.debug("vmcgCeipStatusProperty not set yet, so defaulting to true.");
        consentAccepted = Boolean.valueOf(true);
      } 
      _log.info("Successfully retrieved CEIP status on VMC Gateway: {}.", consentAccepted);
    } catch (PersistenceServiceException e) {
      throw new ConsentException("Error while retrieving CEIP status on VMC Gateway. Please check the logs for details.", e);
    } 
    return CeipUtil.createConsentConfigurationDataForConsentState(consentAccepted.booleanValue());
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    initVmcgCisContext();
    validateVmcgCisContext();
    _log.debug("Setting VMC Gateway consent from value in: {}", consent);
    boolean consentAccepted = CeipUtil.isCeipConsentAccepted(consent);
    try {
      this._persistenceService.writeBoolean(this._propNameForVmcgCeipStatus, consentAccepted);
      _log.info("Successfully set CEIP status on VMC Gateway: {}.", Boolean.valueOf(consentAccepted));
    } catch (PersistenceServiceException e) {
      throw new ConsentException("Error while setting CEIP status on VMC Gateway. As a result the statuscannot be saved. Please check the logs for details.", e);
    } 
  }
  
  public boolean isActive() {
    initVmcgCisContext();
    boolean isActive = (this._vmcgCisContext != null);
    _log.debug("Active state for {} is {}.", VmcGatewayConsentManager.class.getSimpleName(), Boolean.valueOf(isActive));
    return isActive;
  }
  
  private void initVmcgCisContext() {
    if (this._vmcgCisContext == null)
      this._vmcgCisContext = this._vmcgCisContextProvider.getCisContext(); 
  }
  
  private void validateVmcgCisContext() throws ConsentException {
    if (this._vmcgCisContext == null)
      throw new ConsentException(
          String.format("The %s is not active. Cannot perform actions on consent state.", new Object[] { VmcGatewayConsentManager.class.getSimpleName() })); 
  }
}
