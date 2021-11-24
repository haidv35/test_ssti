package com.vmware.ph.phservice.cloud.dataapp.internal;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import java.util.Optional;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class PropertyControlledDataAppAgentIdProvider implements DataAppAgentIdProvider {
  private static final Log _log = LogFactory.getLog(PropertyControlledDataAppAgentIdProvider.class);
  
  private final ConfigurationService _configurationService;
  
  private final Schema _schema;
  
  private final Builder<Pair<String, String>> _instanceIdAndTypeBuilder;
  
  public PropertyControlledDataAppAgentIdProvider(ConfigurationService configurationService, Schema schema, Builder<Pair<String, String>> instanceIdAndTypeBuilder) {
    this._configurationService = configurationService;
    this._schema = schema;
    this._instanceIdAndTypeBuilder = instanceIdAndTypeBuilder;
  }
  
  public DataAppAgentId getDataAppAgentId() {
    String collectorId = getCollectorId();
    String pluginType = getPluginType();
    String instanceId = null;
    String instanceType = null;
    Pair<String, String> instanceIdAndTypePair = this._instanceIdAndTypeBuilder.build();
    if (instanceIdAndTypePair != null) {
      instanceId = instanceIdAndTypePair.getFirst();
      instanceType = instanceIdAndTypePair.getSecond();
    } 
    String dataAppNodeType = this._configurationService.getProperty(this._schema._propNameForNodeType);
    boolean isInstanceTypeSupported = isInstanceTypeSupported(dataAppNodeType, instanceType);
    if (!isInstanceTypeSupported) {
      if (_log.isDebugEnabled())
        _log.debug("Instance type not supported: " + instanceType + ", for data app instance type: " + dataAppNodeType); 
      return null;
    } 
    String collectorInstanceId = computeCollectorInstanceId(instanceId);
    if (StringUtils.isBlank(collectorInstanceId)) {
      _log.warn("Unable to retrieve instance UUID, in order to calculate the data app agent instance ID for " + collectorId);
      return null;
    } 
    String deploymentSecret = getDeploymentSecretFromConfiguration();
    if (StringUtils.isBlank(deploymentSecret)) {
      deploymentSecret = computeDeploymentSecret(instanceId);
    } else {
      _log.debug("Using the deployment secret specified in the configuration for the agent.");
    } 
    DataAppAgentId agentId = null;
    try {
      agentId = new DataAppAgentId(collectorId, collectorInstanceId, deploymentSecret, pluginType);
    } catch (com.vmware.ph.phservice.cloud.dataapp.DataAppAgentManager.IncorrectFormat incorrectFormat) {
      _log.warn(
          String.format("Incorrect format for collectorId [%s] or collectorInstanceId [%s].", new Object[] { collectorId, collectorInstanceId }));
      return null;
    } 
    return agentId;
  }
  
  public String getCollectorId() {
    String collectorId = this._configurationService.getProperty(this._schema._propNameForCollectorId);
    return collectorId;
  }
  
  public String getPluginType() {
    String pluginType = this._configurationService.getProperty(this._schema._propNameForPluginType);
    if (StringUtils.isBlank(pluginType))
      pluginType = null; 
    return pluginType;
  }
  
  private boolean isInstanceTypeSupported(String dataAppNodeType, String instanceType) {
    if (StringUtils.isBlank(dataAppNodeType))
      return true; 
    if (instanceType == null)
      return true; 
    return dataAppNodeType.toLowerCase().contains(instanceType.toLowerCase());
  }
  
  private String computeCollectorInstanceId(String instanceId) {
    String collectorInstanceId = this._configurationService.getProperty(this._schema._propNameForCollectorInstanceId);
    String collectorInstanceIdPrefix = this._configurationService.getProperty(this._schema
        ._propNameForCollectorInstanceIdPrefix);
    if (StringUtils.isBlank(collectorInstanceId) && instanceId != null) {
      if (StringUtils.isBlank(collectorInstanceIdPrefix))
        collectorInstanceIdPrefix = ""; 
      collectorInstanceId = collectorInstanceIdPrefix + instanceId;
    } 
    return collectorInstanceId;
  }
  
  private String computeDeploymentSecret(String instanceId) {
    String deploymentSecret;
    if (instanceId != null) {
      String prefix = getDeploymentSecretPrefixFromConfiguration();
      deploymentSecret = prefix + instanceId;
      boolean shouldHashSecret = getShouldHashSecretFromConfiguration();
      if (shouldHashSecret)
        deploymentSecret = DigestUtils.sha256Hex(deploymentSecret); 
    } else {
      _log.warn("Deployment secret will be null since it is not specified in the configuration and the given instance ID is null.");
      deploymentSecret = null;
    } 
    return deploymentSecret;
  }
  
  private String getDeploymentSecretFromConfiguration() {
    return this._configurationService.getProperty(this._schema.getPropNameForDeploymentSecret());
  }
  
  private String getDeploymentSecretPrefixFromConfiguration() {
    String prefix = this._configurationService.getProperty(this._schema.getPropNameForDeploymentSecretPrefix());
    return Optional.<String>ofNullable(prefix).orElse("");
  }
  
  private boolean getShouldHashSecretFromConfiguration() {
    Boolean hashSecret = this._configurationService.getBoolProperty(this._schema.getPropNameForHashSecret());
    return Boolean.TRUE.equals(hashSecret);
  }
  
  public static class Schema {
    private final String _propNameForCollectorId;
    
    private final String _propNameForCollectorInstanceId;
    
    private final String _propNameForDeploymentSecret;
    
    private final String _propNameForCollectorInstanceIdPrefix;
    
    private final String _propNameForDeploymentSecretPrefix;
    
    private final String _propNameForHashSecret;
    
    private final String _propNameForPluginType;
    
    private final String _propNameForNodeType;
    
    public Schema(String propNameForCollectorId, String propNameForCollectorInstanceId, String propNameForDeploymentSecret, String propNameForCollectorInstanceIdPrefix, String propNameForDeploymentSecretPrefix, String propNameForHashSecret, String propNameForPluginType, String propNameForNodeType) {
      this._propNameForCollectorId = propNameForCollectorId;
      this._propNameForCollectorInstanceId = propNameForCollectorInstanceId;
      this._propNameForDeploymentSecret = propNameForDeploymentSecret;
      this._propNameForCollectorInstanceIdPrefix = propNameForCollectorInstanceIdPrefix;
      this._propNameForDeploymentSecretPrefix = propNameForDeploymentSecretPrefix;
      this._propNameForHashSecret = propNameForHashSecret;
      this._propNameForPluginType = propNameForPluginType;
      this._propNameForNodeType = propNameForNodeType;
    }
    
    public String getPropNameForCollectorId() {
      return this._propNameForCollectorId;
    }
    
    public String getPropNameForCollectorInstanceId() {
      return this._propNameForCollectorInstanceId;
    }
    
    public String getPropNameForDeploymentSecret() {
      return this._propNameForDeploymentSecret;
    }
    
    public String getPropNameForCollectorInstanceIdPrefix() {
      return this._propNameForCollectorInstanceIdPrefix;
    }
    
    public String getPropNameForDeploymentSecretPrefix() {
      return this._propNameForDeploymentSecretPrefix;
    }
    
    public String getPropNameForHashSecret() {
      return this._propNameForHashSecret;
    }
    
    public String getPropNameForPluginType() {
      return this._propNameForPluginType;
    }
    
    public String getPropNameForNodeType() {
      return this._propNameForNodeType;
    }
  }
}
