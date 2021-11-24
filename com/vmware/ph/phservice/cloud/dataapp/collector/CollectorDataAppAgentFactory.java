package com.vmware.ph.phservice.cloud.dataapp.collector;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentCreateInfo;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentFactory;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.AgentCollectorFactory;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.AsyncCollectorDataAppAgentImpl;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CachedCollectorDataAppAgentWrapper;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CeipCollectorDataAppAgentWrapper;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CollectorDataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.CollectorLoopExecutionCoordinatorFactory;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.DefaultCollectorDataAppAgent;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.LoopCollectorDataAppAgentWrapper;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.ObjectIdToContextDataFactory;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.PermitControlledCollectorDataAppAgentWrapper;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.Permits;
import com.vmware.ph.phservice.cloud.dataapp.internal.collector.PropertyControlledCollectorLoopExecutionConfigProviderFactory;
import com.vmware.ph.phservice.cloud.dataapp.internal.repository.MnemonicsAgentObfuscationRepositoryWrapper;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.collector.core.QueryServiceConnectionFactory;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.internal.scheduler.CollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoopExecutionConfigProvider;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.internal.cache.Cache;
import com.vmware.ph.phservice.common.internal.cache.SimpleTimeBasedCacheImpl;
import com.vmware.ph.phservice.common.threadstate.ThreadActiveStateManager;
import java.util.List;
import java.util.Map;

public class CollectorDataAppAgentFactory implements DataAppAgentFactory {
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final DataProvidersConnectionFactory _dataProvidersConnectionFactory;
  
  private final PropertyControlledCollectorLoopExecutionConfigProviderFactory _loopExecutionConfigProviderFactory;
  
  private final CollectorLoopExecutionCoordinatorFactory _loopExecutionCoordinatorFactory;
  
  private ManifestParser _manifestParser;
  
  private QueryServiceConnectionFactory _queryServiceConnectionFactory;
  
  private Integer _cacheCapacity;
  
  private Long _resultRetryIntervalMillis;
  
  private Long _resultWaitTimeoutMillis;
  
  private Long _loopResultRetryIntervalMillis;
  
  private Long _loopResultWaitTimeoutMillis;
  
  private Permits _permits;
  
  private Permits _onDemandPermits;
  
  private Builder<List<String>> _mnemonicWordsBuilder;
  
  private ObjectIdToContextDataFactory _objectIdToContextDataFactory;
  
  private Long _objectIdToContextDataCacheExpirationIntervalMillis;
  
  private ThreadActiveStateManager _threadActiveStateManager;
  
  public CollectorDataAppAgentFactory(CeipConfigProvider ceipConfigProvider, DataProvidersConnectionFactory dataProvidersConnectionFactory) {
    this(ceipConfigProvider, dataProvidersConnectionFactory, null, null, null);
  }
  
  public CollectorDataAppAgentFactory(CeipConfigProvider ceipConfigProvider, DataProvidersConnectionFactory dataProvidersConnectionFactory, PropertyControlledCollectorLoopExecutionConfigProviderFactory loopExecutionConfigProviderFactory, CollectorLoopExecutionCoordinatorFactory loopExecutionCoordinatorFactory, ThreadActiveStateManager threadActiveStateManager) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._dataProvidersConnectionFactory = dataProvidersConnectionFactory;
    this._loopExecutionConfigProviderFactory = loopExecutionConfigProviderFactory;
    this._loopExecutionCoordinatorFactory = loopExecutionCoordinatorFactory;
    this._threadActiveStateManager = threadActiveStateManager;
  }
  
  public void setManifestParser(ManifestParser manifestParser) {
    this._manifestParser = manifestParser;
  }
  
  public void setQueryServiceConnectionFactory(QueryServiceConnectionFactory queryServiceConnectionFactory) {
    this._queryServiceConnectionFactory = queryServiceConnectionFactory;
  }
  
  public void setCacheCapacity(Integer cacheCapacity) {
    this._cacheCapacity = cacheCapacity;
  }
  
  public void setResultRetryIntervalMillis(long resultRetryIntervalMillis) {
    this._resultRetryIntervalMillis = Long.valueOf(resultRetryIntervalMillis);
  }
  
  public void setResultWaitTimeoutMillis(long resultWaitTimeoutMillis) {
    this._resultWaitTimeoutMillis = Long.valueOf(resultWaitTimeoutMillis);
  }
  
  public void setLoopResultRetryIntervalMillis(long loopResultRetryIntervalMillis) {
    this._loopResultRetryIntervalMillis = Long.valueOf(loopResultRetryIntervalMillis);
  }
  
  public void setLoopResultWaitTimeoutMillis(long loopResultWaitTimeoutMillis) {
    this._loopResultWaitTimeoutMillis = Long.valueOf(loopResultWaitTimeoutMillis);
  }
  
  public void setPermits(Permits permits) {
    this._permits = permits;
  }
  
  public void setOnDemandPermits(Permits permits) {
    this._onDemandPermits = permits;
  }
  
  public void setMnemonicWordsBuilder(Builder<List<String>> mnemonicWordsBuilder) {
    this._mnemonicWordsBuilder = mnemonicWordsBuilder;
  }
  
  public void setObjectIdToContextDataFactory(ObjectIdToContextDataFactory objectIdToContextDataFactory) {
    this._objectIdToContextDataFactory = objectIdToContextDataFactory;
  }
  
  public void setObjectIdToContextDataCacheExpirationIntervalMillis(Long objectIdToContextDataCacheExpirationIntervalMillis) {
    this._objectIdToContextDataCacheExpirationIntervalMillis = objectIdToContextDataCacheExpirationIntervalMillis;
  }
  
  public DataAppAgent createAgent(DataApp dataApp, DataAppAgentIdProvider agentIdProvider, DataAppAgentCreateInfo createInfo) {
    DataAppAgent result;
    if (shouldCreateLocalAgent(createInfo)) {
      validateLocalAgentCreateInfo(createInfo);
      CollectorDataAppAgent collectorDataAppAgent = createLocalDefaultAgentCollector(dataApp, agentIdProvider, createInfo);
      collectorDataAppAgent = wrapInPermitControlledAgent(createInfo, collectorDataAppAgent);
      result = collectorDataAppAgent;
    } else {
      CollectorDataAppAgent collectorDataAppAgent = createDefaultAgent(dataApp, agentIdProvider, createInfo);
      collectorDataAppAgent = wrapInPermitControlledAgent(createInfo, collectorDataAppAgent);
      if (this._cacheCapacity != null)
        collectorDataAppAgent = new CachedCollectorDataAppAgentWrapper(collectorDataAppAgent, this._cacheCapacity.intValue()); 
      collectorDataAppAgent = new CeipCollectorDataAppAgentWrapper(collectorDataAppAgent, this._ceipConfigProvider);
      if (shouldCreateLoopAgentWrapper(createInfo))
        collectorDataAppAgent = createLoopAgentWrapper(collectorDataAppAgent, agentIdProvider, createInfo); 
      result = collectorDataAppAgent;
    } 
    return result;
  }
  
  protected CollectorDataAppAgent wrapInPermitControlledAgent(DataAppAgentCreateInfo createInfo, CollectorDataAppAgent collectorDataAppAgent) {
    Permits permits = isOnDemand(createInfo) ? this._onDemandPermits : this._permits;
    if (permits != null)
      collectorDataAppAgent = new PermitControlledCollectorDataAppAgentWrapper(collectorDataAppAgent, permits); 
    return collectorDataAppAgent;
  }
  
  private boolean shouldCreateLocalAgent(DataAppAgentCreateInfo createInfo) {
    return (createInfo instanceof CollectorDataAppAgentCreateInfo && ((CollectorDataAppAgentCreateInfo)createInfo)
      .isLocallyLimitedCollector());
  }
  
  private DefaultCollectorDataAppAgent createDefaultAgent(DataApp dataApp, DataAppAgentIdProvider agentIdProvider, DataAppAgentCreateInfo createInfo) {
    AgentCollectorFactory collectorFactory = new AgentCollectorFactory(this._dataProvidersConnectionFactory, this._queryServiceConnectionFactory, this._manifestParser);
    AgentResultRetriever resultRetriever = new AgentResultRetriever();
    if (this._resultRetryIntervalMillis != null && this._resultWaitTimeoutMillis != null)
      resultRetriever = new AgentResultRetriever(this._resultRetryIntervalMillis.longValue(), this._resultWaitTimeoutMillis.longValue()); 
    DefaultCollectorDataAppAgent dataAppAgent = new DefaultCollectorDataAppAgent(dataApp, agentIdProvider, createInfo.getManifestSpecBuilder(), collectorFactory, resultRetriever);
    configureDefaultDataAppAgent(createInfo, dataAppAgent);
    return dataAppAgent;
  }
  
  private CollectorDataAppAgent createLocalDefaultAgentCollector(DataApp dataApp, DataAppAgentIdProvider agentIdProvider, DataAppAgentCreateInfo createInfo) {
    AgentCollectorFactory collectorFactory = new AgentCollectorFactory(this._dataProvidersConnectionFactory, this._queryServiceConnectionFactory, this._manifestParser);
    DefaultCollectorDataAppAgent dataAppAgent = new DefaultCollectorDataAppAgent(dataApp, agentIdProvider, createInfo.getManifestSpecBuilder(), collectorFactory, null);
    configureDefaultDataAppAgent(createInfo, dataAppAgent);
    CollectorDataAppAgent agent = new AsyncCollectorDataAppAgentImpl(dataAppAgent);
    return agent;
  }
  
  private void configureDefaultDataAppAgent(DataAppAgentCreateInfo createInfo, DefaultCollectorDataAppAgent dataAppAgent) {
    if (createInfo instanceof CollectorDataAppAgentCreateInfo) {
      CollectorDataAppAgentCreateInfo collectorCreateInfo = (CollectorDataAppAgentCreateInfo)createInfo;
      dataAppAgent.setCollectionTriggerDataNeeded(collectorCreateInfo
          .isCollectionTriggerDataNeeded());
      dataAppAgent.setDeploymentDataNeeded(collectorCreateInfo.isDeploymentDataNeeded());
      dataAppAgent.setShouldIncludeCollectionMetadata(collectorCreateInfo.shouldIncludeCollectionMetadata());
      dataAppAgent.setResultNeeded(collectorCreateInfo.isResultNeeded());
      dataAppAgent.setShouldSignalCollectionCompleted(collectorCreateInfo
          .shouldSignalCollectionCompleted());
      dataAppAgent.setAdditionalManifestContentProvider(collectorCreateInfo
          .getAdditionalManifestContentProvider());
      dataAppAgent.setAdditionalPayloadUploadStrategy(collectorCreateInfo
          .getAdditionalPayloadUploadStrategy());
      dataAppAgent.setAdditionalUploadStrategyExclusive(collectorCreateInfo
          .isAdditionalUploadStrategyExclusive());
      AgentObfuscationRepository agentObfuscationRepository = collectorCreateInfo.getAgentObfuscationRepository();
      if (this._mnemonicWordsBuilder != null)
        agentObfuscationRepository = new MnemonicsAgentObfuscationRepositoryWrapper(agentObfuscationRepository, this._mnemonicWordsBuilder); 
      dataAppAgent.setAgentObfuscationRepository(agentObfuscationRepository);
    } 
  }
  
  private boolean shouldCreateLoopAgentWrapper(DataAppAgentCreateInfo createInfo) {
    if (this._loopExecutionConfigProviderFactory == null || this._loopExecutionCoordinatorFactory == null)
      return false; 
    return !isOnDemand(createInfo);
  }
  
  private void validateLocalAgentCreateInfo(DataAppAgentCreateInfo createInfo) {
    CollectorDataAppAgentCreateInfo collectorCreateInfo = (CollectorDataAppAgentCreateInfo)createInfo;
    if (collectorCreateInfo.isLocallyLimitedCollector() && 
      !collectorCreateInfo.isAdditionalUploadStrategyExclusive())
      throw new IllegalArgumentException("A locally limited collector should only upload locally exclusively. Please configure the isAdditionalUploadStrategyExclusive property of the local agent to true."); 
  }
  
  private boolean isOnDemand(DataAppAgentCreateInfo createInfo) {
    if (createInfo instanceof CollectorDataAppAgentCreateInfo) {
      CollectorDataAppAgentCreateInfo collectorDataAppAgentCreateInfo = (CollectorDataAppAgentCreateInfo)createInfo;
      return collectorDataAppAgentCreateInfo.isOnDemandCollectionOnly();
    } 
    return false;
  }
  
  private CollectorDataAppAgent createLoopAgentWrapper(CollectorDataAppAgent dataAppAgent, DataAppAgentIdProvider agentIdProvider, DataAppAgentCreateInfo createInfo) {
    CollectorLoopExecutionCoordinator loopExecutionCoordinator = this._loopExecutionCoordinatorFactory.create(agentIdProvider);
    CollectorLoopExecutionConfigProvider loopExecutionConfigProvider = this._loopExecutionConfigProviderFactory.create(agentIdProvider);
    AgentResultRetriever loopResultRetriever = new AgentResultRetriever();
    if (this._loopResultRetryIntervalMillis != null && this._loopResultWaitTimeoutMillis != null)
      loopResultRetriever = new AgentResultRetriever(this._loopResultRetryIntervalMillis.longValue(), this._loopResultWaitTimeoutMillis.longValue()); 
    dataAppAgent = new LoopCollectorDataAppAgentWrapper(dataAppAgent, agentIdProvider, loopExecutionConfigProvider, loopExecutionCoordinator, loopResultRetriever, buildObjectIdToContextBuilder(createInfo), this._threadActiveStateManager);
    return dataAppAgent;
  }
  
  private Builder<Map<String, Object>> buildObjectIdToContextBuilder(DataAppAgentCreateInfo createInfo) {
    Builder<Map<String, Object>> objectIdToContextBuilder = null;
    if (this._objectIdToContextDataFactory != null && createInfo instanceof CollectorDataAppAgentCreateInfo) {
      String type = ((CollectorDataAppAgentCreateInfo)createInfo).getObjectType();
      if (type != null)
        objectIdToContextBuilder = new ObjectIdToContextDataBuilder(this._objectIdToContextDataFactory, type); 
    } 
    if (objectIdToContextBuilder == null) {
      objectIdToContextBuilder = (() -> null);
    } else if (this._objectIdToContextDataCacheExpirationIntervalMillis != null) {
      objectIdToContextBuilder = new CachedObjectIdToContextDataBuilderWrapper(objectIdToContextBuilder, this._objectIdToContextDataCacheExpirationIntervalMillis.longValue());
    } 
    return objectIdToContextBuilder;
  }
  
  private static class ObjectIdToContextDataBuilder implements Builder<Map<String, Object>> {
    private final ObjectIdToContextDataFactory _objectIdAndContextFactory;
    
    private final String _objectType;
    
    public ObjectIdToContextDataBuilder(ObjectIdToContextDataFactory objectIdAndContextFactory, String type) {
      this._objectIdAndContextFactory = objectIdAndContextFactory;
      this._objectType = type;
    }
    
    public Map<String, Object> build() {
      return this._objectIdAndContextFactory.getObjectIdToContextData(this._objectType);
    }
  }
  
  private static class CachedObjectIdToContextDataBuilderWrapper implements Builder<Map<String, Object>> {
    private static final String CACHE_KEY = "OBJECT_ID_TO_CONTEXT_CACHE";
    
    private static final int CACHE_CAPACITY = 1;
    
    private final Builder<Map<String, Object>> _wrappedObjectIdToContextDataBuilder;
    
    private final Cache<String, Map<String, Object>> _objectIdToContextDataCache;
    
    public CachedObjectIdToContextDataBuilderWrapper(Builder<Map<String, Object>> wrappedObjectIdToContextDataBuilder, long cacheExpirationIntervalMillis) {
      this._wrappedObjectIdToContextDataBuilder = wrappedObjectIdToContextDataBuilder;
      this._objectIdToContextDataCache = new SimpleTimeBasedCacheImpl<>(cacheExpirationIntervalMillis, 1);
    }
    
    public Map<String, Object> build() {
      Map<String, Object> objectIdToContextData = this._objectIdToContextDataCache.get("OBJECT_ID_TO_CONTEXT_CACHE");
      if (objectIdToContextData == null) {
        objectIdToContextData = this._wrappedObjectIdToContextDataBuilder.build();
        this._objectIdToContextDataCache.put("OBJECT_ID_TO_CONTEXT_CACHE", objectIdToContextData);
      } 
      return objectIdToContextData;
    }
  }
}
