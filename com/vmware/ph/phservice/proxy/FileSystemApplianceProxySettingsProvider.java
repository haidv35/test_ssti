package com.vmware.ph.phservice.proxy;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.aj.MethodLogger;
import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(value = 1, prepend = true, trim = false)
public class FileSystemApplianceProxySettingsProvider implements ProxySettingsProvider {
  private static final Logger _logger = LoggerFactory.getLogger(FileSystemApplianceProxySettingsProvider.class);
  
  private static final String PROXY_SCHEME = "https";
  
  private static final String SYSTEM_CONFIG_PROXY_FILE = "/etc/sysconfig/proxy";
  
  private static final String PROXY_ENABLED = "PROXY_ENABLED";
  
  private static final String PROXY_ENABLED_VALUE = "yes";
  
  private static final String PROXY_NOT_ENABLED_VALUE = "no";
  
  private static final String PROXY_SCHEME_PROPERTY_KEY = "HTTPS_PROXY";
  
  private final File _systemConfigurationProxyFile;
  
  static {
    ajc$preClinit();
  }
  
  public FileSystemApplianceProxySettingsProvider() {
    this(new File("/etc/sysconfig/proxy"));
  }
  
  FileSystemApplianceProxySettingsProvider(File systemConfigurationProxyFile) {
    this._systemConfigurationProxyFile = systemConfigurationProxyFile;
  }
  
  public ProxySettings getProxySettings(HttpConnectionConfig connConfig) {
    HttpConnectionConfig httpConnectionConfig = connConfig;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_0, this, this, httpConnectionConfig);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = this;
      arrayOfObject[1] = httpConnectionConfig;
      arrayOfObject[2] = joinPoint;
      FileSystemApplianceProxySettingsProvider$AjcClosure1 fileSystemApplianceProxySettingsProvider$AjcClosure1;
      return (ProxySettings)MethodLogger.aspectOf().wrapClass((fileSystemApplianceProxySettingsProvider$AjcClosure1 = new FileSystemApplianceProxySettingsProvider$AjcClosure1(arrayOfObject)).linkClosureAndJoinPoint(69648));
    } 
    return getProxySettings_aroundBody0(this, httpConnectionConfig, joinPoint);
  }
  
  private static ProxySettings readProxySettingsFromSystemFile(File systemConfigurationProxyFile) {
    Properties properties = readPropertiesFromFile(systemConfigurationProxyFile);
    if (properties.isEmpty()) {
      _logger.info("No properties retrieved, returning null as proxy settings.");
      return ProxySettings.NO_PROXY;
    } 
    String proxyEnabledPropertyValue = properties.getProperty("PROXY_ENABLED", "yes");
    boolean isProxyEnabled = !proxyEnabledPropertyValue.equalsIgnoreCase("no");
    if (isProxyEnabled)
      return parseProxySettings(properties.getProperty("HTTPS_PROXY")); 
    _logger.info("Proxy is not enabled, PROXY_ENABLED value: {}.", proxyEnabledPropertyValue);
    return ProxySettings.NO_PROXY;
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
  
  private static ProxySettings parseProxySettings(String proxySettingsString) {
    ProxySettings proxySettings = ProxySettings.NO_PROXY;
    if (StringUtils.isNotBlank(proxySettingsString)) {
      try {
        URL url = new URL(proxySettingsString);
        String userInfo = url.getUserInfo();
        String userName = null;
        String password = null;
        if (StringUtils.isNotBlank(userInfo)) {
          String[] userInfoParts = userInfo.split(":");
          userName = decodeUrlEscaped(userInfoParts[0]);
          if (userInfoParts.length > 1)
            password = decodeUrlEscaped(userInfoParts[1]); 
        } 
        proxySettings = new ProxySettings(url.getProtocol(), decodeUrlEscaped(url.getHost()), url.getPort(), userName, password);
      } catch (MalformedURLException|UnsupportedEncodingException e) {
        _logger.warn("The specified proxy settings string is not a correct url: {}. Will return NO_PROXY", proxySettingsString);
      } 
    } else {
      _logger.info("No proxy configuration is enabled for scheme {}.", "https");
    } 
    return proxySettings;
  }
  
  private static String decodeUrlEscaped(String urlEscapedString) throws UnsupportedEncodingException {
    return URLDecoder.decode(urlEscapedString, StandardCharsets.UTF_8.toString());
  }
}
