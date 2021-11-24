package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.phservice.cloud.dataapp.collector.CollectionSpec;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.collector.internal.core.UsageDataCollector;
import com.vmware.ph.phservice.collector.internal.core.UsageDataCollectorBuilder;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpecsCollector implements Collector {
  private static final Log log = LogFactory.getLog(SpecsCollector.class);
  
  private final QueryServiceConnection _queryServiceConnection;
  
  private final CollectorAgentProvider _collectorAgentProvider;
  
  private final List<CollectionSpec> _collectionSpec;
  
  private final CollectionSchedule _collectionSchedule;
  
  private final ManifestParser _manifestParser;
  
  public SpecsCollector(QueryServiceConnection queryServiceConnection, CollectorAgentProvider collectorAgentProvider, List<CollectionSpec> collectionSpec, CollectionSchedule collectionSchedule, ManifestParser manifestParser) {
    Validate.notNull(queryServiceConnection);
    this._queryServiceConnection = queryServiceConnection;
    this._collectorAgentProvider = collectorAgentProvider;
    this._collectionSpec = collectionSpec;
    this._collectionSchedule = collectionSchedule;
    this._manifestParser = manifestParser;
  }
  
  public void setContextData(Object contextData) {}
  
  public void run() {
    CollectorOutcome collectorOutcome = collect();
    if (!CollectorOutcome.PASSED.equals(collectorOutcome))
      throw new RuntimeException("Usage data collection failed!"); 
  }
  
  public CollectorOutcome collect() {
    QueryService queryService = this._queryServiceConnection.getQueryService();
    if (queryService == null)
      return CollectorOutcome.LOCAL_ERROR; 
    CollectorOutcome collectorOutcome = null;
    for (CollectionSpec collectionSpec : this._collectionSpec) {
      String objectId = collectionSpec.getObjectId();
      ExceptionsContextManager.setCurrentContextObjectId(objectId);
      try {
        collectorOutcome = collectAndSend(queryService, collectionSpec
            
            .getManifestContentProvider(), collectionSpec
            .getPayloadUploader(), collectionSpec
            .getObfuscator(), collectionSpec
            .getQueryContextData(), collectionSpec
            .getPluginType(), collectionSpec
            .getSignalCollectionCompleted());
      } finally {
        ExceptionsContextManager.removeCurrentObjectId();
      } 
    } 
    return collectorOutcome;
  }
  
  public void close() {}
  
  private CollectorOutcome collectAndSend(QueryService queryService, ManifestContentProvider manifestContentProvider, PayloadUploader payloadUploader, Obfuscator obfuscator, Object contextData, String pluginType, boolean shouldUploadCollectionCompletedPayload) {
    ContextFactory contextFactory = this._queryServiceConnection.getContextFactory();
    UsageDataCollector usageDataCollector = (new UsageDataCollectorBuilder(contextFactory, this._collectorAgentProvider, obfuscator)).shouldUploadCollectionCompletedPayload(shouldUploadCollectionCompletedPayload).build();
    Manifest manifestToExecute = null;
    String manifestString = null;
    try {
      manifestString = usageDataCollector.getManifest(manifestContentProvider);
      manifestToExecute = this._manifestParser.getManifest(manifestString, this._collectionSchedule);
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException|com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException e) {
      log.error("Error fetching manifest.", e);
      return CollectorOutcome.REMOTE_ERROR;
    } 
    if (manifestToExecute == null)
      return getNoManifestCollectorOutcome(manifestString, pluginType); 
    if (contextData != null)
      manifestToExecute = Manifest.Builder.forManifest(manifestToExecute).withContext(contextData).build(); 
    CollectorOutcome collectorOutcome = usageDataCollector.collect(queryService, manifestToExecute, payloadUploader);
    return collectorOutcome;
  }
  
  private CollectorOutcome getNoManifestCollectorOutcome(String manifestString, String pluginType) {
    CollectorOutcome outcome = CollectorOutcome.REMOTE_ERROR;
    if (manifestString != null) {
      log.info(
          String.format("Manifest for plugin [%s] contains no queries for schedule %s. Will report collection as successful.", new Object[] { pluginType, this._collectionSchedule }));
      outcome = CollectorOutcome.PASSED;
    } 
    return outcome;
  }
}
