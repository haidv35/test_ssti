package com.vmware.ph.phservice.ceip.internal.client;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.common.cis.CisContext;
import com.vmware.ph.phservice.common.cis.CisContextProvider;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import com.vmware.vim.binding.phonehome.service.ConsentConfigurationService;
import com.vmware.vim.binding.vmodl.fault.SecurityError;
import com.vmware.vim.sso.client.SamlToken;
import java.net.URI;
import java.security.PrivateKey;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConsentManager implements ConsentManager {
  private static final Logger _log = LoggerFactory.getLogger(ClientConsentManager.class);
  
  private final CisContextProvider _cisContextProvider;
  
  private final CcsLocator _ccsLocator;
  
  private CisContext _cisContext;
  
  private URI _ccsSdkUri;
  
  public ClientConsentManager(CisContextProvider cisContextProvider, CcsLocator ccsLocator) {
    this._cisContextProvider = Objects.<CisContextProvider>requireNonNull(cisContextProvider, "The CisContextProvider must be specified.");
    this._ccsLocator = Objects.<CcsLocator>requireNonNull(ccsLocator);
  }
  
  public ClientConsentManager(CisContext cisContext, CcsLocator ccsLocator) {
    this._cisContextProvider = null;
    this._cisContext = Objects.<CisContext>requireNonNull(cisContext, "The CisContext must be specified.");
    this._ccsLocator = Objects.<CcsLocator>requireNonNull(ccsLocator);
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    ConsentConfigurationData ccData;
    validateActiveState();
    try (VmomiClient ccsVmomiClient = createCcsVmomiClient()) {
      ConsentConfigurationService ccs = CcsClientUtil.getConsentConfigurationServiceMo(ccsVmomiClient);
      ccData = ccs.get();
      if (ccData == null)
        throw new ConsentException("Failed to read consent data from Consent Configuration Service."); 
      _log.debug("Consent data read from the Consent Configuration Service is {}", ccData);
    } catch (Exception e) {
      throw new ConsentException("Failed to create Consent Configuration Serivce client stub.", e);
    } 
    return ccData;
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    validateActiveState();
    try (VmomiClient ccsVmomiClient = createCcsVmomiClient()) {
      ConsentConfigurationService ccs = CcsClientUtil.getConsentConfigurationServiceMo(ccsVmomiClient);
      ccs.set(consent);
      _log.debug("Consent data stored in the Consent Configuration Service is {}", consent);
    } catch (Exception e) {
      throw new ConsentException("Failed to write consent data via the Consent Configuration Serivce", e);
    } 
  }
  
  public boolean isActive() {
    initCisContext();
    if (this._cisContext != null && this._ccsSdkUri == null)
      this._ccsSdkUri = this._ccsLocator.getSdkUri(this._cisContext); 
    boolean isActive = (this._cisContext != null && this._ccsSdkUri != null);
    _log.debug("Active state for {} is {}.", ClientConsentManager.class.getSimpleName(), Boolean.valueOf(isActive));
    return isActive;
  }
  
  public boolean isConsentChangeAllowed(SamlToken samlToken, PrivateKey privateKey) {
    try (VmomiClient ccsVmomiClient = createCcsVmomiClient(samlToken, privateKey)) {
      ConsentConfigurationService ccs = CcsClientUtil.getConsentConfigurationServiceMo(ccsVmomiClient);
      ccs.validatePrivilegeForSet();
      return true;
    } catch (SecurityError e) {
      _log.info("Current user does not have permissions to change CEIP. Exception message is: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      _log.info("Failed to obtain a ConsentConfigurationService client stub. {}", e.getMessage());
      return false;
    } 
  }
  
  private VmomiClient createCcsVmomiClient(SamlToken samlToken, PrivateKey privateKey) {
    return CcsClientUtil.createCcsVmomiClient(this._cisContext, this._ccsLocator, samlToken, privateKey);
  }
  
  VmomiClient createCcsVmomiClient() {
    initCisContext();
    return CcsClientUtil.createCcsVmomiClient(this._cisContext, this._ccsLocator);
  }
  
  private void initCisContext() {
    if (this._cisContext == null)
      this._cisContext = this._cisContextProvider.getCisContext(); 
  }
  
  private void validateActiveState() throws ConsentException {
    if (!isActive())
      throw new ConsentException(
          String.format("The %s is not active. Cannot perform actions with CCS MO.", new Object[] { ClientConsentManager.class.getSimpleName() })); 
  }
}
