package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.collector.CollectionSpec;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.AgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.InMemoryAgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.cloud.dataapp.service.DefaultDataApp;
import com.vmware.ph.phservice.cloud.dataapp.service.ManifestCachingDataAppWrapper;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfo;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestInfoId;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginResult;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionData;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContext;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.DefaultReversibleObfuscator;
import com.vmware.ph.phservice.common.internal.obfuscation.ReversibleObfuscator;
import com.vmware.ph.phservice.common.manifest.InMemoryManifestContentProvider;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultCollectorDataAppAgent implements CollectorDataAppAgent {
  private static final Log _log = LogFactory.getLog(DefaultCollectorDataAppAgent.class);
  
  private static final int PERCENT_DONE_STARTING_AGENT_EXECUTE = 30;
  
  private static final int PERCENT_DONE_AFTER_COLLECTION_SPEC_CREATED = 40;
  
  private static final int PERCENT_DONE_AFTER_COLLECTOR_CREATED = 50;
  
  private static final int PERCENT_DONE_AFTER_COLLECTION = 80;
  
  private static final int PERCENT_DONE_AFTER_PLUGIN_RESULT_RETRIEVED = 90;
  
  private final DataApp _dataApp;
  
  private final DataAppAgentIdProvider _agentIdProvider;
  
  private final Builder<ManifestSpec> _agentManifestSpecBuilder;
  
  private final AgentCollectorFactory _agentCollectorFactory;
  
  private final AgentResultRetriever _agentResultRetriever;
  
  private DataAppAgentId _agentId;
  
  private AgentObfuscationRepository _agentObfuscationRepository;
  
  private boolean _isCollectionTriggerDataNeeded = false;
  
  private boolean _isDeploymentDataNeeded = false;
  
  private boolean _shouldIncludeCollectionMetadata = true;
  
  private boolean _isResultNeeded = true;
  
  private boolean _shouldSignalCollectionCompleted = false;
  
  private boolean _isAdditionalUploadStrategyExclusive = false;
  
  private ManifestContentProvider _additionalManifestContentProvider;
  
  private AgentPayloadUploadStrategy _additionalPayloadUploadStrategy;
  
  private final ReadWriteLock _agentStatusReadWriteLock = new ReentrantReadWriteLock();
  
  private Map<String, ManifestInfo> _lastCollectionObjectManifests = Collections.emptyMap();
  
  private Set<String> _lastCollectionObjects = Collections.emptySet();
  
  private long _lastUploadTime = 0L;
  
  public DefaultCollectorDataAppAgent(DataApp dataApp, DataAppAgentIdProvider agentIdProvider, Builder<ManifestSpec> agentManifestSpecBuilder, AgentCollectorFactory agentCollectorFactory, AgentResultRetriever agentResultRetriever) {
    this._dataApp = dataApp;
    this._agentIdProvider = agentIdProvider;
    this._agentManifestSpecBuilder = agentManifestSpecBuilder;
    this._agentCollectorFactory = agentCollectorFactory;
    this._agentResultRetriever = agentResultRetriever;
  }
  
  public void setCollectionTriggerDataNeeded(boolean isCollectionTriggerDataNeeded) {
    this._isCollectionTriggerDataNeeded = isCollectionTriggerDataNeeded;
  }
  
  public void setDeploymentDataNeeded(boolean isDeploymentDataNeeded) {
    this._isDeploymentDataNeeded = isDeploymentDataNeeded;
  }
  
  public void setShouldIncludeCollectionMetadata(boolean shouldIncludeCollectionMetadata) {
    this._shouldIncludeCollectionMetadata = shouldIncludeCollectionMetadata;
  }
  
  public void setResultNeeded(boolean isResultNeeded) {
    this._isResultNeeded = isResultNeeded;
  }
  
  public void setShouldSignalCollectionCompleted(boolean shouldSignalCollectionCompleted) {
    this._shouldSignalCollectionCompleted = shouldSignalCollectionCompleted;
  }
  
  public void setAdditionalManifestContentProvider(ManifestContentProvider additionalManifestContentProvider) {
    this._additionalManifestContentProvider = additionalManifestContentProvider;
  }
  
  public void setAdditionalPayloadUploadStrategy(AgentPayloadUploadStrategy additionalPayloadUploadStrategy) {
    this._additionalPayloadUploadStrategy = additionalPayloadUploadStrategy;
  }
  
  public void setAgentObfuscationRepository(AgentObfuscationRepository agentObfuscationRepository) {
    this._agentObfuscationRepository = agentObfuscationRepository;
  }
  
  public void setAdditionalUploadStrategyExclusive(boolean additionalUploadStrategyExclusive) {
    this._isAdditionalUploadStrategyExclusive = additionalUploadStrategyExclusive;
  }
  
  public synchronized DataAppAgentId getAgentId() {
    if (this._agentId == null) {
      try {
        this._agentId = this._agentIdProvider.getDataAppAgentId();
      } catch (IllegalStateException e) {
        _log.warn("Failed to create data app agent ID.", e);
      } 
      if (this._agentId == null)
        _log.warn("Cannot create data app agent ID. Will skip data app collection cycle execution."); 
    } 
    return this._agentId;
  }
  
  public void close() {}
  
  public PluginResult execute(String objectId, Object contextData, boolean useCache) {
    return execute(objectId, contextData, (ProgressReporter)null);
  }
  
  public PluginResult execute(String objectId, Object contextData, ProgressReporter progressReporter) {
    Map<String, Object> objectIdToContextData = null;
    if (objectId != null)
      objectIdToContextData = Collections.singletonMap(objectId, contextData); 
    Iterable<PluginResult> pluginResults = execute(objectIdToContextData, this._agentResultRetriever, null, progressReporter);
    if (pluginResults == null || !pluginResults.iterator().hasNext())
      return null; 
    return pluginResults.iterator().next();
  }
  
  private Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever resultRetriever, CollectionSchedule schedule, ProgressReporter progressReporter) {
    if (progressReporter != null)
      progressReporter.reportProgress(30); 
    if (objectIdToContextData == null) {
      objectIdToContextData = new HashMap<>();
      objectIdToContextData.put(null, null);
    } 
    if (resultRetriever == null)
      resultRetriever = this._agentResultRetriever; 
    CollectionTriggerType collectionTriggerType = (schedule == null) ? CollectionTriggerType.ON_DEMAND : CollectionTriggerType.SCHEDULED;
    DataApp dataApp = (this._dataApp instanceof DefaultDataApp) ? new ManifestCachingDataAppWrapper((DefaultDataApp)this._dataApp) : this._dataApp;
    List<CollectionSpec> collectionSpecs = createCollectionSpecs(dataApp, objectIdToContextData, collectionTriggerType, null, null, this._isAdditionalUploadStrategyExclusive);
    updateAgentStatus(dataApp, objectIdToContextData);
    if (progressReporter != null)
      progressReporter.reportProgress(40); 
    CollectorOutcome outcome = null;
    long collectionStartTimestamp = System.currentTimeMillis();
    try {
      this._lastUploadTime = 0L;
      Collector collector = createCollector(collectionSpecs, schedule, collectionTriggerType);
      if (progressReporter != null)
        progressReporter.reportProgress(50); 
      if (collector != null) {
        try {
          outcome = collector.collect();
          if (outcome == CollectorOutcome.PASSED)
            updateAgentStatusLastUploadTime(); 
        } finally {
          collector.close();
        } 
      } else {
        _log.warn("Skipping collection due to not being able to create the collector!");
      } 
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      _log.warn("Data app collection failed", e);
    } 
    if (progressReporter != null)
      progressReporter.reportProgress(80); 
    if (outcome == CollectorOutcome.PASSED)
      storeObfuscationMap(collectionSpecs); 
    List<PluginResult> pluginResults = null;
    if (outcome == CollectorOutcome.PASSED || collectionTriggerType == CollectionTriggerType.ON_DEMAND)
      if (collectionSpecs != null && resultRetriever != null)
        pluginResults = getResults(collectionSpecs, resultRetriever, collectionStartTimestamp);  
    if (progressReporter != null)
      progressReporter.reportProgress(90); 
    return pluginResults;
  }
  
  public AgentStatus getAgentStatus() {
    this._agentStatusReadWriteLock.readLock().lock();
    AgentStatus agentStatus = new AgentStatus();
    try {
      agentStatus.setLastUploadTime(this._lastUploadTime);
      agentStatus.setManifestSpec(this._agentManifestSpecBuilder.build());
      agentStatus.setObjectManifests(this._lastCollectionObjectManifests);
      agentStatus.setObjects(this._lastCollectionObjects);
      agentStatus.setExceptions(getExceptionsContextForAgent(getAgentId()));
    } finally {
      this._agentStatusReadWriteLock.readLock().unlock();
    } 
    return agentStatus;
  }
  
  private ExceptionsContext getExceptionsContextForAgent(DataAppAgentId agentId) {
    ExceptionsContext exceptionsContext = ExceptionsContextManager.getContextForId(getAgentId());
    if (exceptionsContext != null && exceptionsContext.containsKey(null)) {
      String globalObjectId = agentId.getCollectorInstanceId();
      List<ExceptionData> environmentRelatedExceptionData = exceptionsContext.get(null);
      exceptionsContext.remove(null);
      exceptionsContext.put(globalObjectId, environmentRelatedExceptionData);
    } 
    return exceptionsContext;
  }
  
  public String collect(String manifestContent, String objectId, Object contextData) {
    Map<String, Object> objectIdToContextData = new HashMap<>();
    objectIdToContextData.put(objectId, contextData);
    ManifestContentProvider inMemoryManifestContentProvider = new InMemoryManifestContentProvider(manifestContent);
    InMemoryAgentPayloadUploadStrategy inMemoryUploadStrategy = new InMemoryAgentPayloadUploadStrategy();
    List<CollectionSpec> collectionSpecs = createCollectionSpecs(this._dataApp, objectIdToContextData, CollectionTriggerType.ON_DEMAND, inMemoryManifestContentProvider, inMemoryUploadStrategy, true);
    CollectorOutcome outcome = null;
    try {
      Collector collector = createCollector(collectionSpecs, null, CollectionTriggerType.ON_DEMAND);
      if (collector != null)
        try {
          outcome = collector.collect();
        } finally {
          collector.close();
        }  
    } catch (Exception e) {
      ExceptionsContextManager.store(e);
      _log.warn("Data app collection failed", e);
    } 
    if (outcome != CollectorOutcome.PASSED)
      return null; 
    List<String> jsonPayloads = inMemoryUploadStrategy.getUploadedJsons();
    String collectedData = JSONObjectUtil.getFirstJsonObjectFromJsonString(jsonPayloads);
    return collectedData;
  }
  
  public AgentCollectionScheduleSpec getCollectionScheduleSpec(Set<String> objectIds) {
    DataAppAgentId agentId = getAgentId();
    if (agentId == null)
      return AgentCollectionScheduleSpec.EMPTY_SPEC; 
    ManifestSpec agentManifestSpec = this._agentManifestSpecBuilder.build();
    return this._agentCollectorFactory.getCollectionScheduleSpec(this._dataApp, agentId, agentManifestSpec, objectIds, this._additionalManifestContentProvider);
  }
  
  public Map<String, String> exportObfuscationMap(String objectId) {
    if (this._agentObfuscationRepository != null) {
      DataAppAgentId agentId = getAgentId();
      return this._agentObfuscationRepository.readObfuscationMap(agentId, objectId);
    } 
    return Collections.emptyMap();
  }
  
  Collector createCollector(List<CollectionSpec> collectionSpecs, CollectionSchedule collectionSchedule, CollectionTriggerType collectionTriggerType) {
    DataAppAgentId agentId = getAgentId();
    if (agentId == null || collectionSpecs == null)
      return null; 
    Collector collector = this._agentCollectorFactory.createCollector(agentId, collectionSpecs, collectionSchedule, collectionTriggerType);
    return collector;
  }
  
  List<CollectionSpec> createCollectionSpecs(DataApp dataApp, Map<String, Object> objectIdToContextData, CollectionTriggerType collectionTriggerType, ManifestContentProvider specificManifestContentProvider, AgentPayloadUploadStrategy specificPayloadUploadStrategy, boolean isAdditionalUploadStrategyExclusive) {
    DataAppAgentId agentId = getAgentId();
    if (agentId == null)
      return null; 
    ManifestSpec agentManifestSpec = this._agentManifestSpecBuilder.build();
    ManifestContentProvider manifestContentProvider = (specificManifestContentProvider != null) ? specificManifestContentProvider : this._additionalManifestContentProvider;
    AgentPayloadUploadStrategy payloadUploadStrategy = (specificPayloadUploadStrategy != null) ? specificPayloadUploadStrategy : this._additionalPayloadUploadStrategy;
    List<CollectionSpec> collectionSpecs = this._agentCollectorFactory.getCollectionSpecs(dataApp, agentId, agentManifestSpec, objectIdToContextData, this._isCollectionTriggerDataNeeded ? collectionTriggerType : null, this._isDeploymentDataNeeded, this._shouldIncludeCollectionMetadata, manifestContentProvider, payloadUploadStrategy, isAdditionalUploadStrategyExclusive, this._shouldSignalCollectionCompleted);
    return collectionSpecs;
  }
  
  public Iterable<PluginResult> execute(Map<String, Object> objectIdToContextData, AgentResultRetriever resultRetriever, CollectionSchedule schedule) {
    return execute(objectIdToContextData, resultRetriever, schedule, null);
  }
  
  private void storeObfuscationMap(List<CollectionSpec> collectionSpecs) {
    if (this._agentObfuscationRepository == null)
      return; 
    for (CollectionSpec collectionSpec : collectionSpecs) {
      if (collectionSpec.getObfuscator() instanceof DefaultReversibleObfuscator) {
        DefaultReversibleObfuscator defaultReversibleObfuscator = (DefaultReversibleObfuscator)collectionSpec.getObfuscator();
        Map<String, String> obfuscationMap = defaultReversibleObfuscator.exportObfuscationMap();
        DataAppAgentId agentId = getAgentId();
        this._agentObfuscationRepository.writeObfuscationMap(agentId, collectionSpec
            
            .getObjectId(), obfuscationMap);
      } 
    } 
  }
  
  private List<PluginResult> getResults(List<CollectionSpec> collectionSpecs, AgentResultRetriever agentResultRetriever, long sinceTimestamp) {
    List<PluginResult> pluginResults = null;
    Set<String> objectIdsWithPluginResults = new HashSet<>();
    for (CollectionSpec collectionSpec : collectionSpecs) {
      String objectId = collectionSpec.getObjectId();
      if (objectIdsWithPluginResults.contains(objectId))
        continue; 
      List<PluginResult> objectPluginResults = null;
      if (collectionSpec.getObfuscator() instanceof ReversibleObfuscator) {
        ReversibleObfuscator reversibleObfuscator = (ReversibleObfuscator)collectionSpec.getObfuscator();
        objectPluginResults = getResult(objectId, reversibleObfuscator, agentResultRetriever, sinceTimestamp);
      } 
      if (objectPluginResults != null) {
        if (pluginResults == null)
          pluginResults = new ArrayList<>(); 
        pluginResults.addAll(objectPluginResults);
        objectIdsWithPluginResults.add(objectId);
      } 
    } 
    ExceptionsContextManager.removeCurrentObjectId();
    return pluginResults;
  }
  
  private List<PluginResult> getResult(String objectId, ReversibleObfuscator reversibleObfuscator, AgentResultRetriever agentResultRetriever, long sinceTimestamp) {
    ExceptionsContextManager.setCurrentContextObjectId(objectId);
    if (!this._isResultNeeded) {
      if (_log.isDebugEnabled())
        _log.debug("The agent does not need to retrieve results, so skipping result retrieval."); 
      return null;
    } 
    DataAppAgentId agentId = getAgentId();
    if (agentId == null)
      return null; 
    String dataType = agentId.getPluginType();
    List<PluginResult> pluginResults = agentResultRetriever.getPluginResults(this._dataApp, agentId, objectId, dataType, 



        
        Long.valueOf(sinceTimestamp));
    if (pluginResults != null)
      pluginResults = deobfuscateResult(pluginResults, reversibleObfuscator); 
    return pluginResults;
  }
  
  private static List<PluginResult> deobfuscateResult(Iterable<PluginResult> rawPluginResults, ReversibleObfuscator reversibleObfuscator) {
    List<PluginResult> pluginResults = new ArrayList<>();
    for (PluginResult rawPluginResult : rawPluginResults) {
      Object deobfuscatedContent = reversibleObfuscator.deobfuscate(rawPluginResult
          .getContent());
      PluginResult deobfuscatedResult = new PluginResult(rawPluginResult.getObjectId(), deobfuscatedContent, rawPluginResult.getTimestamp());
      pluginResults.add(deobfuscatedResult);
    } 
    return pluginResults;
  }
  
  private void updateAgentStatusLastUploadTime() {
    this._agentStatusReadWriteLock.writeLock().lock();
    try {
      this._lastUploadTime = System.currentTimeMillis();
    } finally {
      this._agentStatusReadWriteLock.writeLock().unlock();
    } 
  }
  
  private void updateAgentStatus(DataApp dataApp, Map<String, Object> objectIdToContextData) {
    this._agentStatusReadWriteLock.writeLock().lock();
    try {
      this._lastCollectionObjects = getObjectIds(objectIdToContextData);
      _log.info("Executing the data app agent for the following objects: " + this._lastCollectionObjects);
      this._lastCollectionObjectManifests = getObjectManifests(dataApp);
    } finally {
      this._agentStatusReadWriteLock.writeLock().unlock();
    } 
  }
  
  private static Set<String> getObjectIds(Map<String, Object> objectIdToContextData) {
    Set<String> objectIds = new HashSet<>(objectIdToContextData.size());
    for (String objectId : objectIdToContextData.keySet()) {
      if (StringUtils.isNotBlank(objectId))
        objectIds.add(objectId); 
    } 
    return objectIds;
  }
  
  private static Map<String, ManifestInfo> getObjectManifests(DataApp dataApp) {
    if (!(dataApp instanceof ManifestCachingDataAppWrapper))
      return Collections.emptyMap(); 
    Map<ManifestInfoId, ManifestInfo> manifestCache = ((ManifestCachingDataAppWrapper)dataApp).exportManifestCache();
    Map<String, ManifestInfo> manifests = new HashMap<>(manifestCache.size());
    for (Map.Entry<ManifestInfoId, ManifestInfo> manifestCacheEntry : manifestCache.entrySet()) {
      ManifestInfo manifest = manifestCacheEntry.getValue();
      if (manifest != null)
        manifests.put(((ManifestInfoId)manifestCacheEntry
            .getKey()).getContentObjectId(), manifest); 
    } 
    return manifests;
  }
}
