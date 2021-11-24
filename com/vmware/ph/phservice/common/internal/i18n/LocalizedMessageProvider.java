package com.vmware.ph.phservice.common.internal.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalizedMessageProvider {
  private static final Log _log = LogFactory.getLog(LocalizedMessageProvider.class);
  
  private final String _resourceBundleBaseName;
  
  public LocalizedMessageProvider(String resourceBundleBaseName) {
    this._resourceBundleBaseName = resourceBundleBaseName;
  }
  
  public String getMessage(String messageId, Locale messageLocale) {
    ResourceBundle resourceBundle = null;
    Locale providedLocale = messageLocale;
    if (providedLocale == null)
      providedLocale = Locale.ENGLISH; 
    resourceBundle = ResourceBundle.getBundle(this._resourceBundleBaseName, providedLocale);
    if (!resourceBundle.containsKey(messageId)) {
      _log.warn("No localized message was found for key " + messageId + ". Message will be shown as it is.");
      return messageId;
    } 
    return resourceBundle.getString(messageId);
  }
}
