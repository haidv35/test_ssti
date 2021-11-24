package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.collector.AgentCollectorBuilder;
import com.vmware.ph.phservice.cloud.dataapp.collector.CollectionSpec;
import com.vmware.ph.phservice.cloud.dataapp.collector.DataProvidersConnectionFactory;
import com.vmware.ph.phservice.cloud.dataapp.internal.PluginTypeContext;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.AgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.AgentPayloadUploader;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.DataAppAgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.internal.upload.MultiplexingAgentPayloadUploadStrategy;
import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.QueryServiceConnectionFactory;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.cdf.dataapp.ManifestSpec;
import com.vmware.ph.phservice.common.internal.manifest.CompositeManifestContentProvider;
import com.vmware.ph.phservice.common.internal.obfuscation.DefaultReversibleObfuscator;
import com.vmware.ph.phservice.common.internal.obfuscation.ReversibleObfuscator;
import com.vmware.ph.phservice.common.manifest.InMemoryManifestContentProvider;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.DataProvidersConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AgentCollectorFactory {
  private static final Log _log = LogFactory.getLog(AgentCollectorFactory.class);
  
  private final DataProvidersConnectionFactory _dataProvidersConnectionFactory;
  
  private final QueryServiceConnectionFactory _queryServiceConnectionFactory;
  
  private final ManifestParser _manifestParser;
  
  public AgentCollectorFactory(DataProvidersConnectionFactory dataProvidersConnectionFactory) {
    this(dataProvidersConnectionFactory, null, null);
  }
  
  public AgentCollectorFactory(DataProvidersConnectionFactory dataProvidersConnectionFactory, QueryServiceConnectionFactory queryServiceConnectionFactory, ManifestParser manifestParser) {
    this._dataProvidersConnectionFactory = dataProvidersConnectionFactory;
    this._queryServiceConnectionFactory = queryServiceConnectionFactory;
    this._manifestParser = manifestParser;
  }
  
  public Collector createCollector(DataAppAgentId agentId, List<CollectionSpec> collectionSpecs, CollectionSchedule collectionSchedule, CollectionTriggerType collectionTriggerType) {
    AgentCollectorAgentProvider collectorAgentProvider = new AgentCollectorAgentProvider(agentId);
    Builder<DataProvidersConnection> dataProvidersConnectionBuilder = new Builder<DataProvidersConnection>() {
        public DataProvidersConnection build() {
          return AgentCollectorFactory.this._dataProvidersConnectionFactory.createDataProvidersConnection(null);
        }
      };
    AgentCollectorBuilder collectorBuilder = (new AgentCollectorBuilder(collectorAgentProvider, dataProvidersConnectionBuilder, collectionSpecs)).withCollectionSchedule(collectionSchedule);
    if (this._manifestParser != null)
      collectorBuilder.withManifestParser(new CollectionTriggerTypeManifestParser(this._manifestParser, collectionTriggerType)); 
    if (this._queryServiceConnectionFactory != null)
      collectorBuilder.withQueryServiceConnectionFactory(this._queryServiceConnectionFactory); 
    Collector collector = collectorBuilder.build();
    return collector;
  }
  
  public AgentCollectionScheduleSpec getCollectionScheduleSpec(DataApp dataApp, DataAppAgentId agentId, ManifestSpec agentManifestSpec, Set<String> objectIds, ManifestContentProvider additionalManifestContentProvider) {
    AgentCollectorAgentProvider collectorAgentProvider = new AgentCollectorAgentProvider(agentId);
    AgentCollectionScheduleSpec.Builder agentCollectionScheduleSpecBuilder = new AgentCollectionScheduleSpec.Builder();
    for (String objectId : objectIds) {
      Set<CollectionSchedule> collectionSchedulesForObjectId = getCollectionSchedules(dataApp, agentManifestSpec, objectId, this._manifestParser, collectorAgentProvider, additionalManifestContentProvider);
      agentCollectionScheduleSpecBuilder.add(objectId, collectionSchedulesForObjectId);
    } 
    return agentCollectionScheduleSpecBuilder.build();
  }
  
  public List<CollectionSpec> getCollectionSpecs(DataApp dataApp, DataAppAgentId agentId, ManifestSpec agentManifestSpec, Map<String, Object> objectIdToContextData, CollectionTriggerType collectionTriggerType, boolean isDeploymentDataNeeded, boolean shouldIncludeCollectionMetadata, ManifestContentProvider additionalManifestContentProvider, AgentPayloadUploadStrategy additionalPayloadUploadStrategy, boolean isAdditionalUploadStrategyExclusive, boolean shouldSignalCollectionCompleted) {
    List<CollectionSpec> collectionSpecs = new ArrayList<>();
    for (Map.Entry<String, Object> entry : objectIdToContextData.entrySet()) {
      String objectId = entry.getKey();
      Object queryContextData = entry.getValue();
      Map<PluginTypeContext, ManifestContentProvider> pluginManifestContentProviders = createPluginManifestContentProviders(dataApp, agentId, agentManifestSpec, objectId, additionalManifestContentProvider);
      ReversibleObfuscator reversibleObfuscator = new DefaultReversibleObfuscator();
      for (PluginTypeContext pluginContext : pluginManifestContentProviders.keySet()) {
        ManifestContentProvider manifestContentProvider = pluginManifestContentProviders.get(pluginContext);
        PayloadUploader payloadUploader = createPayloadUploader(dataApp, agentId, objectId, pluginContext, collectionTriggerType, isDeploymentDataNeeded, shouldIncludeCollectionMetadata, additionalPayloadUploadStrategy, isAdditionalUploadStrategyExclusive);
        CollectionSpec collectionSpec = new CollectionSpec(manifestContentProvider, payloadUploader, reversibleObfuscator, queryContextData, objectId, pluginContext.getPluginType(), pluginContext.getDataType(), shouldSignalCollectionCompleted);
        collectionSpecs.add(collectionSpec);
      } 
    } 
    return collectionSpecs;
  }
  
  private Map<PluginTypeContext, ManifestContentProvider> createPluginManifestContentProviders(DataApp dataApp, DataAppAgentId agentId, ManifestSpec agentManifestSpec, String objectId, ManifestContentProvider additionalManifestContentProvider) {
    ManifestContentProvider manifestContentProvider = createManifestContentProvider(dataApp, agentManifestSpec, objectId, additionalManifestContentProvider);
    Map<PluginTypeContext, ManifestContentProvider> pluginManifestContentProviders = Collections.emptyMap();
    if (this._manifestParser instanceof PluginManifestParser) {
      String manifestContent = getManifestContent(manifestContentProvider, agentId);
      pluginManifestContentProviders = getPluginManifestContentProviders((PluginManifestParser)this._manifestParser, manifestContent);
    } 
    if (pluginManifestContentProviders.isEmpty()) {
      PluginTypeContext pluginTypeContext = new PluginTypeContext(agentId.getPluginType(), null, true);
      pluginManifestContentProviders = Collections.singletonMap(pluginTypeContext, manifestContentProvider);
    } 
    return pluginManifestContentProviders;
  }
  
  private static Map<PluginTypeContext, ManifestContentProvider> getPluginManifestContentProviders(PluginManifestParser pluginManifestParser, String manifestContent) {
    if (StringUtils.isBlank(manifestContent))
      return Collections.emptyMap(); 
    Map<PluginTypeContext, String> manifestContents = pluginManifestParser.getPluginManifests(manifestContent);
    if (manifestContents == null || manifestContents.isEmpty())
      return Collections.emptyMap(); 
    Map<PluginTypeContext, ManifestContentProvider> pluginManifestContentProviders = new LinkedHashMap<>(manifestContents.size());
    for (Map.Entry<PluginTypeContext, String> manifestContentEntry : manifestContents.entrySet()) {
      PluginTypeContext pluginTypeContext = manifestContentEntry.getKey();
      String manifestContentForPlugin = manifestContentEntry.getValue();
      InMemoryManifestContentProvider manifestContentProviderForPlugin = new InMemoryManifestContentProvider(manifestContentForPlugin);
      pluginManifestContentProviders.put(pluginTypeContext, manifestContentProviderForPlugin);
    } 
    return pluginManifestContentProviders;
  }
  
  private static ManifestContentProvider createManifestContentProvider(DataApp dataApp, ManifestSpec manifestSpec, String objectId, ManifestContentProvider additionalManifestContentProvider) {
    ManifestContentProvider manifestContentProvider = new AgentManifestContentProvider(dataApp, manifestSpec, objectId);
    if (additionalManifestContentProvider != null)
      manifestContentProvider = new CompositeManifestContentProvider(Arrays.asList(new ManifestContentProvider[] { additionalManifestContentProvider, manifestContentProvider })); 
    return manifestContentProvider;
  }
  
  private static String getManifestContent(ManifestContentProvider manifestContentProvider, DataAppAgentId agentId) {
    String manifestContent = null;
    try {
      manifestContent = manifestContentProvider.getManifestContent(agentId
          .getCollectorId(), agentId
          .getCollectorInstanceId());
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException e) {
      _log.error(
          String.format("Cannot obtain the content of the collector manifest for collector %s and instance %s.", new Object[] { agentId.getCollectorId(), agentId
              .getCollectorInstanceId() }));
    } 
    return manifestContent;
  }
  
  private static PayloadUploader createPayloadUploader(DataApp dataApp, DataAppAgentId agentId, String objectId, PluginTypeContext pluginContext, CollectionTriggerType collectionTriggerType, boolean isDeploymentDataNeeded, boolean shouldIncludeCollectionMetadata, AgentPayloadUploadStrategy additionalPayloadUploadStrategy, boolean isAdditionalUploadStrategyExclusive) {
    AgentPayloadUploadStrategy uploadStrategy = createUploadStrategy(dataApp, additionalPayloadUploadStrategy, isAdditionalUploadStrategyExclusive);
    PayloadUploader payloadUploader = new AgentPayloadUploader(uploadStrategy, objectId, pluginContext, agentId.getDeploymentSecret(), collectionTriggerType, isDeploymentDataNeeded, shouldIncludeCollectionMetadata, Executors.newSingleThreadExecutor());
    return payloadUploader;
  }
  
  private static AgentPayloadUploadStrategy createUploadStrategy(DataApp dataApp, AgentPayloadUploadStrategy additionalPayloadUploadStrategy, boolean isAdditionalUploadStrategyExclusive) {
    AgentPayloadUploadStrategy uploadStrategy = new DataAppAgentPayloadUploadStrategy(dataApp);
    if (additionalPayloadUploadStrategy != null)
      if (isAdditionalUploadStrategyExclusive) {
        uploadStrategy = additionalPayloadUploadStrategy;
      } else {
        uploadStrategy = new MultiplexingAgentPayloadUploadStrategy(Arrays.asList(new AgentPayloadUploadStrategy[] { additionalPayloadUploadStrategy, uploadStrategy }));
      }  
    return uploadStrategy;
  }
  
  private static Set<CollectionSchedule> getCollectionSchedules(DataApp dataApp, ManifestSpec agentManifestSpec, String objectId, ManifestParser manifestParser, AgentCollectorAgentProvider collectorAgentProvider, ManifestContentProvider additionalManifestContentProvider) {
    ManifestContentProvider manifestContentProvider = createManifestContentProvider(dataApp, agentManifestSpec, objectId, additionalManifestContentProvider);
    AgentCollectorBuilder collectorBuilder = new AgentCollectorBuilder(collectorAgentProvider, manifestContentProvider);
    if (manifestParser != null)
      collectorBuilder.withManifestParser(manifestParser); 
    return collectorBuilder.getCollectorSchedules();
  }
}
