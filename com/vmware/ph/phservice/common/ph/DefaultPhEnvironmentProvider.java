package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.phservice.common.internal.ConfigurationService;
import com.vmware.ph.upload.service.UploadServiceBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultPhEnvironmentProvider implements PhEnvironmentProvider {
  private static final Log logger = LogFactory.getLog(DefaultPhEnvironmentProvider.class);
  
  private final ConfigurationService _configurationService;
  
  private final String _targetEnvironmentPropertyName;
  
  private final String _targetEnvironment;
  
  public DefaultPhEnvironmentProvider(ConfigurationService configurationService, String targetEnvironmentPropertyName) {
    this(configurationService, targetEnvironmentPropertyName, null);
  }
  
  public DefaultPhEnvironmentProvider(String targetEnvironment) {
    this(null, null, targetEnvironment);
  }
  
  DefaultPhEnvironmentProvider(ConfigurationService configurationService, String targetEnvironmentPropertyName, String targetEnvironment) {
    this._configurationService = configurationService;
    this._targetEnvironmentPropertyName = targetEnvironmentPropertyName;
    this._targetEnvironment = targetEnvironment;
  }
  
  public UploadServiceBuilder.Environment getEnvironment() {
    UploadServiceBuilder.Environment result = UploadServiceBuilder.Environment.PRODUCTION;
    String targetEnvironment = getTargetEnvironment();
    if (StringUtils.isNotBlank(targetEnvironment))
      try {
        result = UploadServiceBuilder.Environment.valueOf(targetEnvironment.toUpperCase());
      } catch (IllegalArgumentException e) {
        logger.warn(
            String.format("Cannot convert string value '%s' to Environment enum, will default to %s.", new Object[] { targetEnvironment, result }));
      }  
    if (logger.isDebugEnabled())
      logger.debug("environment to be used: " + result); 
    return result;
  }
  
  private String getTargetEnvironment() {
    String targetEnvironment = this._targetEnvironment;
    if (StringUtils.isEmpty(targetEnvironment) && 
      this._configurationService != null)
      targetEnvironment = this._configurationService.getProperty(this._targetEnvironmentPropertyName); 
    return targetEnvironment;
  }
}
