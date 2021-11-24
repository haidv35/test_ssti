package com.vmware.ph.phservice.proxy;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.aj.MethodLogger;
import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import com.vmware.ph.phservice.proxy.config.TinyProxyConfig;
import com.vmware.ph.phservice.proxy.util.EnvironmentDetector;
import com.vmware.ph.phservice.proxy.util.PopDiscoveryUtil;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(value = 1, prepend = true, trim = false)
public class VmcProxySettingsProvider implements ProxySettingsProvider {
  private static final Logger _logger = LoggerFactory.getLogger(VmcProxySettingsProvider.class);
  
  private static final String PROXY_PROPERTIES_FILE = "/etc/vmware-analytics/proxy.properties";
  
  private static final String PROXY_SCHEME_PROPERTY_NAME = "proxyScheme";
  
  private static final String PROXY_HOST_PROPERTY_NAME = "proxyHost";
  
  private static final String PROXY_PORT_PROPERTY_NAME = "proxyPort";
  
  private File _proxyPropertiesFile;
  
  private EnvironmentDetector _environmentDetector;
  
  private TinyProxyConfig _tinyProxyConfig;
  
  static {
    ajc$preClinit();
  }
  
  public VmcProxySettingsProvider(EnvironmentDetector environmentDetector, TinyProxyConfig tinyProxyConfig) {
    this(environmentDetector, new File("/etc/vmware-analytics/proxy.properties"), tinyProxyConfig);
  }
  
  public VmcProxySettingsProvider(EnvironmentDetector environmentDetector, File proxyPropertiesFile, TinyProxyConfig tinyProxyConfig) {
    this._environmentDetector = environmentDetector;
    this._proxyPropertiesFile = proxyPropertiesFile;
    this._tinyProxyConfig = tinyProxyConfig;
  }
  
  public ProxySettings getProxySettings(HttpConnectionConfig connConfig) {
    HttpConnectionConfig httpConnectionConfig = connConfig;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_0, this, this, httpConnectionConfig);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = this;
      arrayOfObject[1] = httpConnectionConfig;
      arrayOfObject[2] = joinPoint;
      VmcProxySettingsProvider$AjcClosure1 vmcProxySettingsProvider$AjcClosure1;
      return (ProxySettings)MethodLogger.aspectOf().wrapClass((vmcProxySettingsProvider$AjcClosure1 = new VmcProxySettingsProvider$AjcClosure1(arrayOfObject)).linkClosureAndJoinPoint(69648));
    } 
    return getProxySettings_aroundBody0(this, httpConnectionConfig, joinPoint);
  }
  
  private boolean isVmc() {
    return this._environmentDetector.isCloudEnvironment();
  }
  
  private ProxySettings getProxySettingsFromFile() {
    if (this._proxyPropertiesFile == null || !this._proxyPropertiesFile.exists()) {
      _logger.info("Proxy properties file is missing.");
      return ProxySettings.NO_PROXY;
    } 
    Properties properties = readPropertiesFromFile(this._proxyPropertiesFile);
    if (properties.isEmpty()) {
      _logger.info("No proxy settings were found in file {}. Return NO_PROXY.", this._proxyPropertiesFile
          .getAbsolutePath());
      return ProxySettings.NO_PROXY;
    } 
    String proxyHost = properties.getProperty("proxyHost");
    if (StringUtils.isEmpty(proxyHost)) {
      _logger.info("No proxyHost defined in {}. Return NO_PROXY", this._proxyPropertiesFile);
      return ProxySettings.NO_PROXY;
    } 
    String proxyScheme = properties.getProperty("proxyScheme");
    String proxyPort = properties.getProperty("proxyPort");
    int portNumber = 0;
    if (proxyPort != null)
      try {
        portNumber = Integer.valueOf(proxyPort).intValue();
      } catch (NumberFormatException nfe) {
        _logger.error(
            String.format("Failed to parse proxy port %s. Will use default value of %d.", new Object[] { proxyPort, Integer.valueOf(portNumber) }), nfe);
      }  
    return new ProxySettings(
        StringUtils.isEmpty(proxyScheme) ? this._tinyProxyConfig.getScheme() : proxyScheme, proxyHost, portNumber, null, null);
  }
  
  private static Properties readPropertiesFromFile(File systemConfigurationProxyFile) {
    Properties properties = new Properties();
    try (FileReader reader = new FileReader(systemConfigurationProxyFile)) {
      properties.load(reader);
      stripQuotes(properties);
    } catch (IOException e) {
      _logger.warn("Caught exception while trying to read system configuration proxy file {}.", systemConfigurationProxyFile);
    } 
    return properties;
  }
  
  private static void stripQuotes(Properties properties) {
    for (Map.Entry<Object, Object> entry : properties.entrySet())
      entry.setValue(((String)entry
          .getValue())
          .replaceAll("^\"|\"$", "")
          .replaceAll("^'|'$", "")); 
  }
  
  private ProxySettings retrieveTinyProxyAsProxySettings() {
    Optional<String> tinyProxyHost = PopDiscoveryUtil.getPopFqdn();
    if (!tinyProxyHost.isPresent()) {
      _logger.info("TinyProxy host cannot be found");
      return ProxySettings.NO_PROXY;
    } 
    ProxySettings proxySettings = new ProxySettings(this._tinyProxyConfig.getScheme(), tinyProxyHost.get(), this._tinyProxyConfig.getPort(), null, null);
    saveProxySettingsToFile(transformToProperties(proxySettings), this._proxyPropertiesFile);
    return proxySettings;
  }
  
  private static Properties transformToProperties(ProxySettings proxySettings) {
    String scheme = proxySettings.getScheme();
    String hostName = proxySettings.getHostname();
    int port = proxySettings.getPort();
    Properties properties = new Properties();
    if (StringUtils.isNotEmpty(scheme))
      properties.setProperty("proxyScheme", scheme); 
    if (StringUtils.isNotEmpty(hostName))
      properties.setProperty("proxyHost", hostName); 
    if (port > 0)
      properties.setProperty("proxyPort", "" + port); 
    return properties;
  }
  
  protected static synchronized void saveProxySettingsToFile(Properties props, File propertyFile) {
    if (propertyFile == null) {
      _logger.debug("The property file is not defined. The proxy settings won't be persisted.");
      return;
    } 
    try (FileWriter writer = new FileWriter(propertyFile)) {
      props.store(writer, (String)null);
    } catch (IOException e) {
      String message = String.format("Caught exception while trying to write to properties file %s. This exception will be swallowed.", new Object[] { propertyFile });
      _logger.error(message, e);
    } 
  }
}
