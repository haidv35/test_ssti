package com.vmware.ph.phservice.collector.core.impl;

import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.core.QueryServiceConnection;
import com.vmware.ph.phservice.collector.internal.core.UsageDataCollector;
import com.vmware.ph.phservice.collector.internal.core.UsageDataCollectorBuilder;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.XmlManifestParser;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.internal.obfuscation.DefaultObfuscator;
import com.vmware.ph.phservice.common.internal.obfuscation.Obfuscator;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CollectorImpl implements Collector {
  private static final Log log = LogFactory.getLog(CollectorImpl.class);
  
  private final QueryServiceConnection _queryServiceConnection;
  
  private final UsageDataCollector _usageDataCollector;
  
  private final ManifestContentProvider _manifeContentProvider;
  
  private final PayloadUploader _payloadUploader;
  
  private final ManifestParser _manifestParser;
  
  private Object _contextData;
  
  private CollectionSchedule _collectionSchedule = CollectionSchedule.WEEKLY;
  
  @Deprecated
  public CollectorImpl(QueryServiceConnection queryServiceConnection, CollectorAgentProvider collectorAgentProvider, ManifestContentProvider manifestContentProvider, PayloadUploader resultPayloadUploader) {
    this(queryServiceConnection, collectorAgentProvider, manifestContentProvider, resultPayloadUploader, (Obfuscator)new DefaultObfuscator(), CollectionSchedule.WEEKLY, new XmlManifestParser(), false);
  }
  
  @Deprecated
  public CollectorImpl(QueryServiceConnection queryServiceConnection, CollectorAgentProvider collectorAgentProvider, ManifestContentProvider manifestContentProvider, PayloadUploader resultPayloadUploader, Obfuscator obfuscator, CollectionSchedule collectionSchedule, ManifestParser manifestParser, boolean shouldUploadCollectionCompletedPayload) {
    Validate.notNull(queryServiceConnection);
    this._queryServiceConnection = queryServiceConnection;
    ContextFactory contextFactory = this._queryServiceConnection.getContextFactory();
    this





      
      ._usageDataCollector = (new UsageDataCollectorBuilder(contextFactory, collectorAgentProvider, obfuscator)).shouldUploadCollectionCompletedPayload(shouldUploadCollectionCompletedPayload).build();
    this._manifeContentProvider = manifestContentProvider;
    this._payloadUploader = resultPayloadUploader;
    this._manifestParser = manifestParser;
    this._collectionSchedule = collectionSchedule;
  }
  
  public void setContextData(Object contextData) {
    this._contextData = contextData;
  }
  
  public void run() {
    CollectorOutcome collectorOutcome = collect();
    if (!CollectorOutcome.PASSED.equals(collectorOutcome))
      throw new RuntimeException("Usage data collection failed!"); 
  }
  
  public CollectorOutcome collect() {
    QueryService queryService = this._queryServiceConnection.getQueryService();
    if (queryService == null)
      return CollectorOutcome.LOCAL_ERROR; 
    Manifest manifestToExecute = null;
    try {
      manifestToExecute = getManifest(this._usageDataCollector
          .getManifest(this._manifeContentProvider));
    } catch (com.vmware.ph.phservice.common.manifest.ManifestContentProvider.ManifestException e) {
      log.error("Error fetching manifest.", (Throwable)e);
    } 
    if (manifestToExecute == null)
      return CollectorOutcome.REMOTE_ERROR; 
    if (this._contextData != null)
      manifestToExecute = Manifest.Builder.forManifest(manifestToExecute).withContext(this._contextData).build(); 
    CollectorOutcome collectorOutcome = this._usageDataCollector.collect(queryService, manifestToExecute, this._payloadUploader);
    return collectorOutcome;
  }
  
  public void close() {}
  
  Manifest getManifest(String manifest) {
    return this._manifestParser.getManifest(manifest, this._collectionSchedule);
  }
}
