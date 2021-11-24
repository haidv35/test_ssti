package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.AgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;

public class CollectorDataAppAgentCreateInfo extends DataAppAgentCreateInfo {
  private final String _objectType;
  
  private final boolean _isCollectionTriggerDataNeeded;
  
  private final boolean _isDeploymentDataNeeded;
  
  private final boolean _shouldIncludeCollectionMetadata;
  
  private final boolean _isResultNeeded;
  
  private final boolean _shouldSignalCollectionCompleted;
  
  private final boolean _isOnDemandCollectionOnly;
  
  private final boolean _isLocallyLimitedCollector;
  
  private final boolean _isAdditionalUploadStrategyExclusive;
  
  private final ManifestContentProvider _additionalManifestContentProvider;
  
  private final AgentPayloadUploadStrategy _additionalPayloadUploadStrategy;
  
  private final AgentObfuscationRepository _agentObfuscationRepository;
  
  public CollectorDataAppAgentCreateInfo(Builder<ManifestSpec> manifestSpecBuilder, String objectType, boolean isCollectionTriggerDataNeeded, boolean isDeploymentDataNeeded, boolean shouldIncludeCollectionMetadata, boolean isResultNeeded, boolean shouldSignalCollectionCompleted, boolean isOnDemandCollectionOnly, boolean isLocallyLimitedCollector, boolean isAdditionalUploadStrategyExclusive, ManifestContentProvider additionalManifestContentProvider, AgentPayloadUploadStrategy additionalPayloadUploadStrategy, AgentObfuscationRepository agentObfuscationRepository) {
    super(manifestSpecBuilder);
    this._objectType = objectType;
    this._isCollectionTriggerDataNeeded = isCollectionTriggerDataNeeded;
    this._isDeploymentDataNeeded = isDeploymentDataNeeded;
    this._shouldIncludeCollectionMetadata = shouldIncludeCollectionMetadata;
    this._isResultNeeded = isResultNeeded;
    this._shouldSignalCollectionCompleted = shouldSignalCollectionCompleted;
    this._isOnDemandCollectionOnly = isOnDemandCollectionOnly;
    this._additionalManifestContentProvider = additionalManifestContentProvider;
    this._additionalPayloadUploadStrategy = additionalPayloadUploadStrategy;
    this._agentObfuscationRepository = agentObfuscationRepository;
    this._isLocallyLimitedCollector = isLocallyLimitedCollector;
    this._isAdditionalUploadStrategyExclusive = isAdditionalUploadStrategyExclusive;
  }
  
  public String getObjectType() {
    return this._objectType;
  }
  
  public boolean isCollectionTriggerDataNeeded() {
    return this._isCollectionTriggerDataNeeded;
  }
  
  public boolean isDeploymentDataNeeded() {
    return this._isDeploymentDataNeeded;
  }
  
  public boolean isResultNeeded() {
    return this._isResultNeeded;
  }
  
  public boolean shouldSignalCollectionCompleted() {
    return this._shouldSignalCollectionCompleted;
  }
  
  public boolean isOnDemandCollectionOnly() {
    return this._isOnDemandCollectionOnly;
  }
  
  public ManifestContentProvider getAdditionalManifestContentProvider() {
    return this._additionalManifestContentProvider;
  }
  
  public AgentPayloadUploadStrategy getAdditionalPayloadUploadStrategy() {
    return this._additionalPayloadUploadStrategy;
  }
  
  public AgentObfuscationRepository getAgentObfuscationRepository() {
    return this._agentObfuscationRepository;
  }
  
  public boolean shouldIncludeCollectionMetadata() {
    return this._shouldIncludeCollectionMetadata;
  }
  
  public boolean isLocallyLimitedCollector() {
    return this._isLocallyLimitedCollector;
  }
  
  public boolean isAdditionalUploadStrategyExclusive() {
    return this._isAdditionalUploadStrategyExclusive;
  }
}
