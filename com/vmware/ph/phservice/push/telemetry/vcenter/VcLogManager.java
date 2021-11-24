package com.vmware.ph.phservice.push.telemetry.vcenter;

import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.vim.binding.vim.fault.InvalidName;
import com.vmware.vim.binding.vim.option.OptionManager;
import com.vmware.vim.binding.vim.option.OptionValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VcLogManager {
  public enum LogLevel {
    none, quiet, panic, error, warning, info, verbose, trivia;
  }
  
  private static final Log _log = LogFactory.getLog(VcLogManager.class);
  
  private static final String LOGGER_PREFIX = "logger.";
  
  private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.none;
  
  private final VimContextProvider _vimContextProvider;
  
  private VcClientProvider _vcClientProvider;
  
  public VcLogManager(VimContextProvider vimContextProvider) {
    this._vimContextProvider = vimContextProvider;
  }
  
  public Map<String, LogLevel> getVcLogNameToLogLevel(String logGroupPrefix) {
    Map<String, LogLevel> logNameToLogLevel = new LinkedHashMap<>();
    try {
      OptionManager optionManager = getOptionManager();
      if (optionManager == null) {
        _log.info("Option Manager is null - most probably the environment is PSC or VMCG.");
        _log.info("No log level will be propagated.");
        return logNameToLogLevel;
      } 
      OptionValue[] optionValues = getOptionValues(logGroupPrefix, optionManager);
      if (optionValues != null)
        for (OptionValue optionValue : optionValues) {
          String logName = optionValue.getKey();
          String logLevelString = (String)optionValue.getValue();
          LogLevel logLevel = getLogLevel(logName, logLevelString);
          logNameToLogLevel.put(logName, logLevel);
        }  
    } finally {
      closeVcConnection();
    } 
    return logNameToLogLevel;
  }
  
  private OptionValue[] getOptionValues(String logGroupPrefix, OptionManager optionManager) {
    OptionValue[] optionValues = null;
    try {
      optionValues = optionManager.queryView("logger." + logGroupPrefix);
    } catch (InvalidName e) {
      if (_log.isWarnEnabled())
        _log.warn("No option or subtree exists in the advanced settings with the given name: logger." + logGroupPrefix); 
    } 
    return optionValues;
  }
  
  private LogLevel getLogLevel(String logName, String logLevelString) {
    LogLevel logLevel = DEFAULT_LOG_LEVEL;
    try {
      logLevel = LogLevel.valueOf(logLevelString);
    } catch (IllegalArgumentException e) {
      if (_log.isWarnEnabled())
        _log.warn(String.format("Unknown logLevelString: %s found for logName: %s. Will default log level to: " + logLevel, new Object[] { logLevelString, logName })); 
    } 
    return logLevel;
  }
  
  public void setVcLogNameToLogLevel(Map<String, LogLevel> logNameToLogLevel) throws InvalidName {
    try {
      OptionManager optionManager = getOptionManager();
      List<OptionValue> optionValues = new ArrayList<>();
      for (Map.Entry<String, LogLevel> entry : logNameToLogLevel.entrySet()) {
        String logName = entry.getKey();
        LogLevel logLevel = entry.getValue();
        OptionValue optionValue = new OptionValue(logName, logLevel.toString());
        optionValues.add(optionValue);
      } 
      if (optionValues.size() > 0) {
        optionManager.updateValues(optionValues
            .<OptionValue>toArray(new OptionValue[0]));
        if (_log.isDebugEnabled())
          _log.debug("Updating option manager log values with: " + optionValues); 
      } else if (_log.isWarnEnabled()) {
        _log.warn("Trying to update option manager log values with nologNameToLogLevel given. Values for log level will not change.");
      } 
    } finally {
      closeVcConnection();
    } 
  }
  
  private OptionManager getOptionManager() {
    VcClient vcClient = getVcClient();
    if (vcClient == null)
      return null; 
    ManagedObjectReference optionManagerMoRef = vcClient.getServiceInstanceContent().getSetting();
    OptionManager optionManager = (OptionManager)vcClient.createMo(optionManagerMoRef);
    return optionManager;
  }
  
  VcClient getVcClient() {
    VimContext vimContext = this._vimContextProvider.getVimContext();
    if (vimContext == null)
      return null; 
    this._vcClientProvider = (VcClientProvider)new VimContextVcClientProviderImpl(vimContext);
    return this._vcClientProvider.getVcClient();
  }
  
  private void closeVcConnection() {
    if (this._vcClientProvider != null)
      this._vcClientProvider.close(); 
  }
}
