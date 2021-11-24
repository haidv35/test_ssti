package com.vmware.ph.phservice.ceip.internal;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.internal.ConfigurationService;

public class PropertyControlledCeipConfigProviderWrapper implements CeipConfigProvider {
  private final ConfigurationService _configurationService;
  
  private final String _propNameForOverrideCeipStatus;
  
  private final CeipConfigProvider _wrapperdCeipConfigProvider;
  
  public PropertyControlledCeipConfigProviderWrapper(ConfigurationService configurationService, String propNameForOverrideCeipStatus, CeipConfigProvider ceipConfigProvider) {
    this._configurationService = configurationService;
    this._propNameForOverrideCeipStatus = propNameForOverrideCeipStatus;
    this._wrapperdCeipConfigProvider = ceipConfigProvider;
  }
  
  public boolean isCeipEnabled() {
    boolean result = false;
    Boolean isCeipEnabledOverride = this._configurationService.getBoolProperty(this._propNameForOverrideCeipStatus);
    if (isCeipEnabledOverride != null) {
      result = isCeipEnabledOverride.booleanValue();
    } else {
      result = this._wrapperdCeipConfigProvider.isCeipEnabled();
    } 
    return result;
  }
}
