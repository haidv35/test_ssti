package com.vmware.ph.phservice.ceip;

import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;

public interface ConsentManager {
  ConsentConfigurationData readConsent() throws ConsentException;
  
  void writeConsent(ConsentConfigurationData paramConsentConfigurationData) throws ConsentException;
  
  boolean isActive();
}
