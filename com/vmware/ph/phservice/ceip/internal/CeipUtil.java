package com.vmware.ph.phservice.ceip.internal;

import com.vmware.vim.binding.phonehome.data.ConsentConfiguration;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;

public class CeipUtil {
  public static final int CEIP_CONSENT_ID_OLD = 1;
  
  public static final int CEIP_CONSENT_ID = 2;
  
  public static final long CEIP_VERSION_DEFAULT = 0L;
  
  public static final boolean CEIP_DEFAULT_VALUE = false;
  
  public static final String CEIP_DEFAULT_OWNER = "";
  
  public static boolean isCeipConsentAccepted(ConsentConfigurationData ccData) {
    if (ccData == null)
      return false; 
    ConsentConfiguration[] cc = ccData.getConsentConfigurations();
    for (int i = 0; cc != null && i < cc.length; i++) {
      if (cc[i].getConsentId() == 2)
        return cc[i].isConsentAccepted(); 
    } 
    return false;
  }
  
  public static ConsentConfigurationData createConsentConfigurationDataForConsentState(boolean consentAccepted) {
    ConsentConfiguration[] cc = createConsentConfigurationsForConsentState(consentAccepted);
    ConsentConfigurationData ccData = new ConsentConfigurationData(cc, String.valueOf(0L));
    return ccData;
  }
  
  public static ConsentConfiguration[] createConsentConfigurationsForConsentState(boolean consentAccepted) {
    return new ConsentConfiguration[] { new ConsentConfiguration(consentAccepted, 1, ""), new ConsentConfiguration(consentAccepted, 2, "") };
  }
}
