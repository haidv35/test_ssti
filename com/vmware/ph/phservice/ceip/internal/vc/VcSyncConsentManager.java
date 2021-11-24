package com.vmware.ph.phservice.ceip.internal.vc;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.ceip.internal.CeipApiException;
import com.vmware.ph.phservice.ceip.internal.CeipUtil;
import com.vmware.vim.binding.phonehome.data.ConsentConfiguration;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VcSyncConsentManager implements SyncConsentManager {
  private static final Logger _log = LoggerFactory.getLogger(VcSyncConsentManager.class);
  
  private final ConsentManager _vcConsentManager;
  
  private final ConsentManager _pscConsentManager;
  
  public VcSyncConsentManager(ConsentManager vcConsentManager, ConsentManager pscConsentManager) {
    this._vcConsentManager = vcConsentManager;
    this._pscConsentManager = pscConsentManager;
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    validateActiveState();
    ConsentConfigurationData ccData = readLocal();
    if (ccData == null)
      ccData = CeipUtil.createConsentConfigurationDataForConsentState(false); 
    return ccData;
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    validateActiveState();
    boolean consentAccepted = CeipUtil.isCeipConsentAccepted(consent);
    _log.debug("Setting consent to: {}", Boolean.valueOf(consentAccepted));
    ConsentConfigurationData localConsent = readLocal();
    long localVersion = getCeipVersion(localConsent);
    ConsentConfiguration[] cc = changeCeipConsentAccepted(
        getConsentConfigurations(localConsent), 
        Boolean.valueOf(consentAccepted));
    localVersion = (localVersion != 0L) ? (localVersion + 1L) : localVersion;
    writeLocal(cc, localVersion + 1L);
    localVersion++;
    writeGlobalSafe(cc, localVersion);
  }
  
  public boolean isActive() {
    return (this._vcConsentManager.isActive() && this._pscConsentManager.isActive());
  }
  
  public void sync() {
    try {
      validateActiveState();
    } catch (ConsentException e) {
      _log.debug("Sync will not be perfromed.", e);
      return;
    } 
    _log.debug("Start syncing the consent...");
    ConsentConfigurationData localConsent = readLocal();
    long localVersion = getCeipVersion(localConsent);
    ConsentConfigurationData globalConsent = readGlobal();
    long globalVersion = getCeipVersion(globalConsent);
    boolean isGlobalCcChanged = !equals(
        getConsentConfigurations(localConsent), 
        getConsentConfigurations(globalConsent));
    boolean isGlobalVersionChanged = (localVersion != globalVersion - 1L && localVersion != globalVersion && localVersion != globalVersion + 1L);
    boolean isGlobalChanged = (isGlobalCcChanged || isGlobalVersionChanged);
    if (isGlobalChanged)
      localVersion++; 
    if (isGlobalChanged)
      if (localVersion <= globalVersion) {
        if (globalVersion > localVersion)
          localVersion = globalVersion + 1L; 
        writeLocal(getConsentConfigurations(globalConsent), localVersion);
      } else {
        writeGlobal(getConsentConfigurations(localConsent), localVersion);
      }  
  }
  
  private void validateActiveState() throws ConsentException {
    if (!isActive())
      throw new ConsentException(
          String.format("The %s is not active. Cannot perform actions on consent state.", new Object[] { VcSyncConsentManager.class.getSimpleName() })); 
  }
  
  private void writeLocal(ConsentConfiguration[] consentAccepted, long version) {
    ConsentConfigurationData ccData = new ConsentConfigurationData(consentAccepted, Long.toString(version));
    _log.debug("Storing consent data locally. Stored data is: {}", ccData);
    try {
      this._vcConsentManager.writeConsent(ccData);
      _log.debug("Consent data has been sucessfully stored locally.");
    } catch (ConsentException e) {
      throw new CeipApiException("Error while locally storing the consent data. As a result consent state cannot be saved. Please check the logs for details.", e);
    } 
  }
  
  private void writeGlobal(ConsentConfiguration[] consentConfiguration, long localVersion) {
    ConsentConfigurationData ccData = new ConsentConfigurationData(consentConfiguration, Long.toString(localVersion + 1L));
    _log.debug("Global consent is successfully changed. Conset data sent: {}", ccData);
    try {
      this._pscConsentManager.writeConsent(ccData);
      _log.debug("Global consent data has been sent successfully.");
    } catch (RuntimeException|ConsentException e) {
      throw new CeipApiException("Error while sending the changes to the global consent. As a result consent state cannot be updated. Please check the logs for details.", e);
    } 
  }
  
  private void writeGlobalSafe(ConsentConfiguration[] consentConfiguration, long currentVersion) {
    try {
      writeGlobal(consentConfiguration, currentVersion);
    } catch (RuntimeException e) {
      _log.debug("Failed to write global consent while setting consent state.", e);
    } 
  }
  
  private ConsentConfigurationData readLocal() {
    ConsentConfigurationData localConsent;
    _log.debug("Reading the locally stored consent.");
    try {
      localConsent = this._vcConsentManager.readConsent();
      _log.debug("Successfully read the locally stored consent data. Consent data is: {}", localConsent);
    } catch (ConsentException e) {
      throw new CeipApiException("Failed to read local consent state.", e);
    } 
    return localConsent;
  }
  
  private ConsentConfigurationData readGlobal() {
    ConsentConfigurationData globalConsent;
    try {
      try {
        globalConsent = this._pscConsentManager.readConsent();
      } catch (ConsentException e) {
        throw new CeipApiException("Failed to read global consent configuration.", e);
      } 
      _log.debug("Loaded consentConfigData from CCS -> {}", globalConsent);
    } catch (RuntimeException re) {
      throw new CeipApiException("Error while reading global consent. As a result consent state is not up to date.", re);
    } 
    return globalConsent;
  }
  
  private static ConsentConfiguration[] getConsentConfigurations(ConsentConfigurationData ccData) {
    return (ccData != null) ? ccData.getConsentConfigurations() : null;
  }
  
  private static long getCeipVersion(ConsentConfigurationData ccData) {
    long version = 0L;
    if (ccData != null)
      version = Long.parseLong(ccData.getVersion()); 
    return version;
  }
  
  private static boolean equals(ConsentConfiguration[] arr1, ConsentConfiguration[] arr2) {
    if (arr1 == arr2)
      return true; 
    if (arr1 == null || arr2 == null)
      return false; 
    Set<String> s = new HashSet<>();
    for (ConsentConfiguration a : arr1)
      s.add(consentConfigToString(a)); 
    for (ConsentConfiguration a : arr2)
      s.remove(consentConfigToString(a)); 
    return (s.size() == 0);
  }
  
  private static String consentConfigToString(ConsentConfiguration cc) {
    StringBuilder sb = new StringBuilder();
    sb.append("consentAccepted=");
    sb.append(cc.isConsentAccepted());
    sb.append("consentId=");
    sb.append(cc.getConsentId());
    String ccOwner = "";
    if (cc.getOwner() != null)
      ccOwner = cc.getOwner(); 
    sb.append("owner=");
    sb.append(ccOwner);
    return sb.toString();
  }
  
  private static ConsentConfiguration[] changeCeipConsentAccepted(ConsentConfiguration[] cc, Boolean consentAccepted) {
    if (cc == null)
      return CeipUtil.createConsentConfigurationsForConsentState(consentAccepted.booleanValue()); 
    cc = changeCeipConsentAccepted(1, cc, consentAccepted);
    cc = changeCeipConsentAccepted(2, cc, consentAccepted);
    return cc;
  }
  
  private static ConsentConfiguration[] changeCeipConsentAccepted(int ceipConsentId, ConsentConfiguration[] cc, Boolean consentAccepted) {
    boolean isCeipConsentAvailable = false;
    cc = deepCopy(cc);
    for (int i = 0; i < cc.length; i++) {
      if (cc[i].getConsentId() == ceipConsentId) {
        cc[i].setConsentAccepted(consentAccepted.booleanValue());
        isCeipConsentAvailable = true;
      } 
    } 
    if (!isCeipConsentAvailable) {
      cc = Arrays.<ConsentConfiguration>copyOf(cc, cc.length + 1);
      cc[cc.length - 1] = new ConsentConfiguration(consentAccepted
          .booleanValue(), ceipConsentId, "");
    } 
    return cc;
  }
  
  private static ConsentConfiguration[] deepCopy(ConsentConfiguration[] cc) {
    ConsentConfiguration[] result = new ConsentConfiguration[cc.length];
    for (int i = 0; i < cc.length; i++)
      result[i] = new ConsentConfiguration(cc[i]
          .isConsentAccepted(), cc[i]
          .getConsentId(), cc[i]
          .getOwner()); 
    return result;
  }
}
