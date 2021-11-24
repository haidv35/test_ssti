package com.vmware.ph.phservice.cloud.dataapp.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateSpec;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.collector.CollectorDataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.internal.PropertyControlledDataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.repository.PropertyControlledAgentObfuscationRepositoryWrapper;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.AgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.PropertyControlledAgentPayloadUploadStrategy;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.PersistenceServiceException;
import com.vmware.ph.phservice.common.PropertiesFilePersistenceService;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.internal.dataapp.PropertyControlledManifestSpecBuilder;
import com.vmware.ph.phservice.common.internal.ConfigurationService;
import com.vmware.ph.phservice.common.internal.PropertiesFileConfigurationService;
import com.vmware.ph.phservice.common.internal.manifest.PropertyControlledManifestContentProviderWrapper;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemAgentRepository implements AgentRepository {
  private static final Log _log = LogFactory.getLog(FileSystemAgentRepository.class);
  
  private final File _agentsFolder;
  
  private final Schema _schema;
  
  private final Builder<Pair<String, String>> _instanceIdAndTypeBuilder;
  
  public FileSystemAgentRepository(Path agentsDirPath, Schema schema, Builder<Pair<String, String>> instanceIdAndTypeBuilder) {
    this._agentsFolder = agentsDirPath.toFile();
    this._schema = schema;
    this._instanceIdAndTypeBuilder = instanceIdAndTypeBuilder;
  }
  
  public Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> add(DataAppAgentId agentId, DataAppAgentCreateSpec agentCreateSpec) {
    String agentFileName = getFileName(agentId);
    File agentFile = new File(this._agentsFolder, agentFileName);
    writeAgent(agentFile, agentId, agentCreateSpec);
    return readAgent(agentFile);
  }
  
  public void remove(DataAppAgentId agentId) {
    String agentFileName = getFileName(agentId);
    File agentFile = new File(this._agentsFolder, agentFileName);
    agentFile.delete();
  }
  
  public Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> list() {
    File[] agentFiles = this._agentsFolder.listFiles();
    Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> idToSpec = new LinkedHashMap<>(agentFiles.length);
    for (File agentFile : agentFiles) {
      Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> agentPair = readAgent(agentFile);
      if (agentPair != null)
        idToSpec.put(agentPair.getFirst(), agentPair.getSecond()); 
    } 
    return idToSpec;
  }
  
  public Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> get(DataAppAgentId agentId) {
    Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> agentPair = null;
    String agentFileName = getFileName(agentId);
    File agentFile = new File(this._agentsFolder, agentFileName);
    if (agentFile.exists()) {
      agentPair = readAgent(agentFile);
    } else {
      Map<DataAppAgentIdProvider, DataAppAgentCreateInfo> idToSpec = list();
      for (Map.Entry<DataAppAgentIdProvider, DataAppAgentCreateInfo> entry : idToSpec.entrySet()) {
        DataAppAgentIdProvider agentIdProvider = entry.getKey();
        if (agentId.equals(agentIdProvider.getDataAppAgentId())) {
          agentPair = new Pair<>(agentIdProvider, entry.getValue());
          break;
        } 
      } 
    } 
    return agentPair;
  }
  
  private Pair<DataAppAgentIdProvider, DataAppAgentCreateInfo> readAgent(File agentFile) {
    if (!agentFile.exists())
      return null; 
    ConfigurationService configurationService = new PropertiesFileConfigurationService(agentFile);
    DataAppAgentIdProvider agentIdProvider = new PropertyControlledDataAppAgentIdProvider(configurationService, this._schema.getAgentIdProviderSchema(), this._instanceIdAndTypeBuilder);
    Builder<ManifestSpec> manifestSpecBuilder = new PropertyControlledManifestSpecBuilder(configurationService, this._schema.getManifestSpecSchema());
    String objectType = (String)StringUtils.defaultIfBlank(configurationService
        .getProperty(this._schema.getPropNameForObjectType()), null);
    Boolean collectionTriggerDataNeededPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIsCollectionTriggerDataNeeded());
    boolean collectionTriggerDataNeeded = (collectionTriggerDataNeededPropValue != null && collectionTriggerDataNeededPropValue.booleanValue());
    Boolean deploymentDataNeededPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIsDeploymentDataNeeded());
    boolean deploymentDataNeeded = (deploymentDataNeededPropValue != null && deploymentDataNeededPropValue.booleanValue());
    Boolean includeCollectionMetadataPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIncludeCollectionMetadata());
    boolean includeCollectionMetadata = (includeCollectionMetadataPropValue != null) ? includeCollectionMetadataPropValue.booleanValue() : true;
    Boolean shouldSignalCollectionCompletedPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForShouldSignalCollectionCompleted());
    boolean shouldSignalCollectionCompleted = (shouldSignalCollectionCompletedPropValue != null && shouldSignalCollectionCompletedPropValue.booleanValue());
    ManifestContentProvider additionalManifestContentProvider = new PropertyControlledManifestContentProviderWrapper(configurationService, this._schema.getPropNameForManifestLocation(), null);
    AgentPayloadUploadStrategy additionalPayloadUploadStrategy = new PropertyControlledAgentPayloadUploadStrategy(configurationService, this._schema.getPropNameForPayloadLocation());
    AgentObfuscationRepository agentObfuscationRepository = new PropertyControlledAgentObfuscationRepositoryWrapper(configurationService, this._schema.getPropNameForObfuscationMapLocation());
    Boolean isResultNeededPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIsResultNeeded());
    boolean isResultNeeded = (isResultNeededPropValue != null) ? isResultNeededPropValue.booleanValue() : true;
    Boolean isOnDemandCollectionOnlyPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForOnDemandCollectionOnly());
    boolean isOnDemandCollectionOnly = (isOnDemandCollectionOnlyPropValue != null && isOnDemandCollectionOnlyPropValue.booleanValue());
    Boolean isLocallyLimitedCollectorPropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIsLocallyLimitedCollector());
    boolean isLocallyLimitedCollector = (isLocallyLimitedCollectorPropValue != null) ? isLocallyLimitedCollectorPropValue.booleanValue() : false;
    Boolean isAdditionalUploadStrategyExclusivePropValue = configurationService.getBoolProperty(this._schema
        .getPropNameForIsAdditionalUploadStrategyExclusive());
    boolean isAdditionalUploadStrategyExclusive = (isAdditionalUploadStrategyExclusivePropValue != null) ? isAdditionalUploadStrategyExclusivePropValue.booleanValue() : false;
    DataAppAgentCreateInfo spec = new CollectorDataAppAgentCreateInfo(manifestSpecBuilder, objectType, collectionTriggerDataNeeded, deploymentDataNeeded, includeCollectionMetadata, isResultNeeded, shouldSignalCollectionCompleted, isOnDemandCollectionOnly, isLocallyLimitedCollector, isAdditionalUploadStrategyExclusive, additionalManifestContentProvider, additionalPayloadUploadStrategy, agentObfuscationRepository);
    return new Pair<>(agentIdProvider, spec);
  }
  
  private void writeAgent(File agentFile, DataAppAgentId agentId, DataAppAgentCreateSpec agentCreateSpec) {
    PropertiesFilePersistenceService ps = new PropertiesFilePersistenceService(agentFile);
    Map<String, String> propertyKeyToPropertyValueMap = new LinkedHashMap<>();
    PropertyControlledDataAppAgentIdProvider.Schema agentIdProviderSchema = this._schema.getAgentIdProviderSchema();
    propertyKeyToPropertyValueMap.put(agentIdProviderSchema
        .getPropNameForCollectorId(), agentId
        .getCollectorId());
    propertyKeyToPropertyValueMap.put(agentIdProviderSchema
        .getPropNameForCollectorInstanceId(), agentId
        .getCollectorInstanceId());
    propertyKeyToPropertyValueMap.put(agentIdProviderSchema
        .getPropNameForDeploymentSecret(), agentId
        .getDeploymentSecret());
    propertyKeyToPropertyValueMap.put(agentIdProviderSchema
        .getPropNameForPluginType(), agentId
        .getPluginType());
    ManifestSpec manifestSpec = null;
    if (agentCreateSpec != null)
      manifestSpec = agentCreateSpec.getManifestSpec(); 
    if (manifestSpec != null) {
      PropertyControlledManifestSpecBuilder.Schema manifestSpecSchema = this._schema.getManifestSpecSchema();
      propertyKeyToPropertyValueMap.put(manifestSpecSchema
          .getPropNameForManifestResourceId(), manifestSpec
          .getResourceId());
      propertyKeyToPropertyValueMap.put(manifestSpecSchema
          .getPropNameForManifestObjectId(), manifestSpec
          .getObjectId());
      propertyKeyToPropertyValueMap.put(manifestSpecSchema
          .getPropNameForManifestDataType(), manifestSpec
          .getDataType());
      propertyKeyToPropertyValueMap.put(manifestSpecSchema
          .getPropNameForManifestVersionObjectId(), manifestSpec
          .getVersionObjectId());
      propertyKeyToPropertyValueMap.put(manifestSpecSchema
          .getPropNameForManifestVersionDataType(), manifestSpec
          .getVersionDataType());
    } 
    if (agentCreateSpec != null) {
      if (agentCreateSpec.getObjectType() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForObjectType(), agentCreateSpec
            .getObjectType()); 
      if (agentCreateSpec.isCollectionTriggerDataNeeded() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForIsCollectionTriggerDataNeeded(), agentCreateSpec
            .isCollectionTriggerDataNeeded().toString()); 
      if (agentCreateSpec.isDeploymentDataNeeded() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForIsDeploymentDataNeeded(), agentCreateSpec
            .isDeploymentDataNeeded().toString()); 
      if (agentCreateSpec.isResultNeeded() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForIsResultNeeded(), agentCreateSpec
            .isResultNeeded().toString()); 
      if (agentCreateSpec.shouldSignalCollectionCompleted() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForShouldSignalCollectionCompleted(), agentCreateSpec
            .shouldSignalCollectionCompleted().toString()); 
      if (agentCreateSpec.getLocalManifestPath() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForManifestLocation(), agentCreateSpec
            .getLocalManifestPath()); 
      if (agentCreateSpec.getLocalPayloadPath() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForPayloadLocation(), agentCreateSpec
            .getLocalPayloadPath()); 
      if (agentCreateSpec.getLocalObfuscationMapPath() != null)
        propertyKeyToPropertyValueMap.put(this._schema
            .getPropNameForObfuscationMapLocation(), agentCreateSpec
            .getLocalObfuscationMapPath()); 
    } 
    try {
      ps.writeValues(propertyKeyToPropertyValueMap);
    } catch (PersistenceServiceException e) {
      _log.warn("Unable to persist agent to file: " + e.getMessage());
    } 
  }
  
  private static String getFileName(DataAppAgentId agentId) {
    String pluginType = agentId.getPluginType();
    if (StringUtils.isBlank(pluginType)) {
      pluginType = "";
    } else {
      pluginType = "_" + pluginType;
    } 
    String fileName = agentId.getCollectorId() + pluginType + ".properties";
    return fileName;
  }
  
  public static class Schema {
    private final PropertyControlledDataAppAgentIdProvider.Schema _agentIdProviderSchema;
    
    private final PropertyControlledManifestSpecBuilder.Schema _manifestSpecSchema;
    
    private final String _propNameForManifestLocation;
    
    private final String _propNameForPayloadLocation;
    
    private final String _propNameForObfuscationMapLocation;
    
    private final String _propNameForIsCollectionTriggerTypeNeeded;
    
    private final String _propNameForIsDeploymentDataNeeded;
    
    private final String _propNameForIncludeCollectionMetadata;
    
    private final String _propNameForIsResultNeeded;
    
    private final String _propNameForShouldSignalCollectionCompleted;
    
    private final String _propNameForObjectType;
    
    private final String _propNameForOnDemandCollectionOnly;
    
    private final String _propNameForIsLocallyLimitedCollector;
    
    private final String _propNameForIsAdditionalUploadStrategyExclusive;
    
    public Schema(PropertyControlledDataAppAgentIdProvider.Schema agentIdProviderSchema, PropertyControlledManifestSpecBuilder.Schema manifestSpecSchema, String propNameForManifestLocation, String propNameForPayloadLocation, String propNameForObfuscationMapLocation, String propNameForIsCollectionTriggerTypeNeeded, String propNameForIsDeploymentDataNeeded, String propNameForIncludeCollectionMetadata, String propNameForIsResultNeeded, String propNameForShouldSignalCollectionCompleted, String propNameForObjectType, String propNameForOnDemandCollectionOnly, String propNameForIsLocallyLimitedCollector, String propNameForIsAdditionalUploadStrategyExclusive) {
      this._agentIdProviderSchema = agentIdProviderSchema;
      this._manifestSpecSchema = manifestSpecSchema;
      this._propNameForManifestLocation = propNameForManifestLocation;
      this._propNameForPayloadLocation = propNameForPayloadLocation;
      this._propNameForObfuscationMapLocation = propNameForObfuscationMapLocation;
      this._propNameForIsCollectionTriggerTypeNeeded = propNameForIsCollectionTriggerTypeNeeded;
      this._propNameForIsDeploymentDataNeeded = propNameForIsDeploymentDataNeeded;
      this._propNameForIncludeCollectionMetadata = propNameForIncludeCollectionMetadata;
      this._propNameForIsResultNeeded = propNameForIsResultNeeded;
      this._propNameForShouldSignalCollectionCompleted = propNameForShouldSignalCollectionCompleted;
      this._propNameForObjectType = propNameForObjectType;
      this._propNameForOnDemandCollectionOnly = propNameForOnDemandCollectionOnly;
      this._propNameForIsLocallyLimitedCollector = propNameForIsLocallyLimitedCollector;
      this._propNameForIsAdditionalUploadStrategyExclusive = propNameForIsAdditionalUploadStrategyExclusive;
    }
    
    public PropertyControlledDataAppAgentIdProvider.Schema getAgentIdProviderSchema() {
      return this._agentIdProviderSchema;
    }
    
    public PropertyControlledManifestSpecBuilder.Schema getManifestSpecSchema() {
      return this._manifestSpecSchema;
    }
    
    public String getPropNameForManifestLocation() {
      return this._propNameForManifestLocation;
    }
    
    public String getPropNameForPayloadLocation() {
      return this._propNameForPayloadLocation;
    }
    
    public String getPropNameForObfuscationMapLocation() {
      return this._propNameForObfuscationMapLocation;
    }
    
    public String getPropNameForIsCollectionTriggerDataNeeded() {
      return this._propNameForIsCollectionTriggerTypeNeeded;
    }
    
    public String getPropNameForIsDeploymentDataNeeded() {
      return this._propNameForIsDeploymentDataNeeded;
    }
    
    public String getPropNameForIsResultNeeded() {
      return this._propNameForIsResultNeeded;
    }
    
    public String getPropNameForShouldSignalCollectionCompleted() {
      return this._propNameForShouldSignalCollectionCompleted;
    }
    
    public String getPropNameForObjectType() {
      return this._propNameForObjectType;
    }
    
    public String getPropNameForOnDemandCollectionOnly() {
      return this._propNameForOnDemandCollectionOnly;
    }
    
    public String getPropNameForIncludeCollectionMetadata() {
      return this._propNameForIncludeCollectionMetadata;
    }
    
    public String getPropNameForIsLocallyLimitedCollector() {
      return this._propNameForIsLocallyLimitedCollector;
    }
    
    public String getPropNameForIsAdditionalUploadStrategyExclusive() {
      return this._propNameForIsAdditionalUploadStrategyExclusive;
    }
  }
}
