package com.vmware.ph.phservice.ceip.server.vmomi;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import com.vmware.vim.binding.phonehome.service.ConsentConfigurationService;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsentConfigurationServiceMo implements ConsentConfigurationService {
  private static final String MO_REF_TYPE = "PhonehomeServiceConsentConfigurationService";
  
  private static final String MO_REF_ID = "ccService";
  
  private static final Log _log = LogFactory.getLog(ConsentConfigurationServiceMo.class);
  
  private final ConsentManager _consentManager;
  
  private final ManagedObjectReference _ref;
  
  public ConsentConfigurationServiceMo(ConsentManager consentManager) {
    this._consentManager = consentManager;
    this._ref = new ManagedObjectReference("PhonehomeServiceConsentConfigurationService", "ccService");
  }
  
  public ManagedObjectReference _getRef() {
    return this._ref;
  }
  
  public void get(Future<ConsentConfigurationData> result) {
    try {
      ConsentConfigurationData ccData = this._consentManager.readConsent();
      result.set(ccData);
    } catch (ConsentException e) {
      if (_log.isDebugEnabled())
        _log.debug("Failed to read consent data:", (Throwable)e); 
      result.setException((Exception)e);
    } 
  }
  
  public ConsentConfigurationData get() {
    throw new UnsupportedOperationException("Synchronous calls are not supported.");
  }
  
  public void set(ConsentConfigurationData consentConfigs, Future<Void> result) {
    try {
      this._consentManager.writeConsent(consentConfigs);
      result.set();
    } catch (ConsentException e) {
      if (_log.isDebugEnabled())
        _log.debug("Failed to write consent data:", (Throwable)e); 
      result.setException((Exception)e);
    } 
  }
  
  public void set(ConsentConfigurationData consentConfigs) {
    throw new UnsupportedOperationException("Synchronous calls are not supported.");
  }
  
  public void validatePrivilegeForSet(Future<Void> serverFuture) {
    serverFuture.set();
  }
  
  public void validatePrivilegeForSet() {
    throw new UnsupportedOperationException("Synchronous calls are not supported.");
  }
}
