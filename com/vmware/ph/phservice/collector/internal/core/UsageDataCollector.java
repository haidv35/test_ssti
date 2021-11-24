package com.vmware.ph.phservice.collector.internal.core;

import com.vmware.cis.data.api.QueryService;
import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat.dimensions.Collector;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.CollectorAgentProvider;
import com.vmware.ph.phservice.collector.internal.ContainsErrorInfo;
import com.vmware.ph.phservice.collector.internal.cdf.CollectedPayload;
import com.vmware.ph.phservice.collector.internal.cdf.QueryServicePayloadCollector;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.ph.phservice.provider.common.internal.ContextFactory;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UsageDataCollector {
  private static final Log _log = LogFactory.getLog(UsageDataCollector.class);
  
  public static final String RT_COLLECTION_SUMMARY = "collection_summary";
  
  public static final String RT_COLLECTION = "collection";
  
  static final String RAN_UPLOAD_ATTEMPTS = "upload_attempts";
  
  static final String RAN_SUCCESSFUL_ATTEMPTS = "successful_attempts";
  
  static final String RAN_PAYLOAD_COUNT = "payload_count";
  
  static final String RAN_COLLECTION_TYPE = "collection_type";
  
  static final String RAN_PAGE_SIZE = "page_size";
  
  static final String CT_STRUCTURED_DATA = "structured_data";
  
  static final String COLLECTION_COMPLETED_RESOURCE_TYPE = "collection_completed";
  
  static final String COLLECTION_COMPLETED_TIME = "collection_completed_timestamp";
  
  private final QueryServicePayloadCollector _queryServiceCollector;
  
  private final ContextFactory _contextFactory;
  
  private final CollectorAgentProvider _collectorAgentProvider;
  
  private final boolean _shouldUploadCollectionCompletedPayload;
  
  public UsageDataCollector(QueryServicePayloadCollector queryServiceCollector, ContextFactory contextFactory, CollectorAgentProvider collectorAgentProvider, boolean shouldUploadCollectionCompletedPayload) {
    this._queryServiceCollector = queryServiceCollector;
    this._contextFactory = contextFactory;
    this._collectorAgentProvider = collectorAgentProvider;
    this._shouldUploadCollectionCompletedPayload = shouldUploadCollectionCompletedPayload;
  }
  
  public String getManifest(ManifestContentProvider manifestContentProvider) throws ManifestContentProvider.ManifestException {
    CollectionInfo collectionInfo = buildCollectionInfo();
    String manifestContent = manifestContentProvider.getManifestContent(collectionInfo.collector
        .getCollectorId(), collectionInfo.collector
        .getCollectorInstanceId());
    if (_log.isInfoEnabled())
      _log.info("Retrieved manifest: " + 
          
          StringUtils.abbreviate(manifestContent, 1000)); 
    return manifestContent;
  }
  
  public CollectorOutcome collect(QueryService queryService, Manifest manifest, PayloadUploader payloadUploader) {
    CollectionInfo collectionInfo = buildCollectionInfo();
    if (_log.isInfoEnabled())
      _log.info("Started collection process"); 
    try {
      CollectionStatistics collectionStatistics = collectAndUpload(collectionInfo, queryService, manifest, payloadUploader);
      CollectorOutcome outcome = calculateOutcome(collectionStatistics);
      return outcome;
    } catch (InterruptedException e) {
      _log.error("Upload interrupted.", e);
      Thread.currentThread().interrupt();
      return CollectorOutcome.LOCAL_ERROR;
    } 
  }
  
  private CollectionInfo buildCollectionInfo() {
    CollectorAgentProvider.CollectorAgentId agentId = this._collectorAgentProvider.getCollectorAgentId();
    String collectorId = agentId.getCollectorId();
    String collectorInstanceId = agentId.getCollectorInstanceId();
    Collector collector = new Collector(collectorId, collectorInstanceId);
    CollectionInfo collectionInfo = new CollectionInfo(collector);
    return collectionInfo;
  }
  
  private CollectionStatistics collectAndUpload(CollectionInfo collectionInfo, QueryService queryService, Manifest manifest, PayloadUploader payloadUploader) throws InterruptedException {
    Context context = this._contextFactory.createContext(collectionInfo.collector
        .getCollectorId(), collectionInfo.collector
        .getCollectorInstanceId(), collectionInfo.collectionId);
    CollectionStatistics collectionStatistics = new CollectionStatistics();
    processDataCollectors(collectionInfo, manifest, queryService, context, collectionStatistics, payloadUploader);
    logCollectionStatistics(collectionStatistics, "structured_data");
    return collectionStatistics;
  }
  
  private CollectorOutcome calculateOutcome(CollectionStatistics collectionStatistics) {
    if (collectionStatistics.fatalErrorCount > 0)
      return CollectorOutcome.LOCAL_ERROR; 
    if (collectionStatistics.attemptedUploads > collectionStatistics.successfulUploads)
      return CollectorOutcome.REMOTE_ERROR; 
    return CollectorOutcome.PASSED;
  }
  
  private void processDataCollectors(CollectionInfo collectionInfo, Manifest manifest, QueryService queryService, Context context, CollectionStatistics statistics, PayloadUploader payloadUploader) throws InterruptedException {
    if (_log.isInfoEnabled())
      _log.info(
          String.format("started processing structured data collectors, collection id %s.", new Object[] { collectionInfo.collectionId })); 
    int accumulationSize = manifest.getRecommendedPageSize();
    if (_log.isInfoEnabled())
      _log.info("Page size: " + accumulationSize); 
    Iterable<CollectedPayload> collectedPayloads = this._queryServiceCollector.collect(manifest, queryService, context, accumulationSize);
    Iterator<CollectedPayload> collectedPayloadsIterator = collectedPayloads.iterator();
    try {
      int accumulatedResource = 0;
      Payload.Builder payloadUploadBuilder = new Payload.Builder();
      while (collectedPayloadsIterator.hasNext()) {
        CollectedPayload collectedPayload = collectedPayloadsIterator.next();
        statistics.payloadCount++;
        updateErrorStatistic(statistics, collectedPayload);
        Payload payload = collectedPayload.getPayload();
        accumulatedResource += getResourceCount(payload);
        payloadUploadBuilder.add(payload.getJsons());
        if (accumulatedResource >= accumulationSize || collectedPayload
          .isLastInBatch()) {
          upload(collectionInfo, payloadUploadBuilder
              
              .build(), statistics, payloadUploader);
          accumulatedResource = 0;
          payloadUploadBuilder = new Payload.Builder();
        } 
      } 
      if (accumulatedResource > 0)
        upload(collectionInfo, payloadUploadBuilder
            
            .build(), statistics, payloadUploader); 
    } finally {
      if (collectedPayloadsIterator instanceof Closeable)
        IOUtils.closeQuietly((Closeable)collectedPayloadsIterator); 
    } 
    if (this._shouldUploadCollectionCompletedPayload)
      uploadCollectionCompletedPayload(collectionInfo, 
          
          System.currentTimeMillis(), payloadUploader); 
    uploadCollectionSummaryResource(collectionInfo, statistics, "structured_data", accumulationSize, payloadUploader);
  }
  
  private void uploadCollectionCompletedPayload(CollectionInfo collectionInfo, long collectionCompletedTimeMillis, PayloadUploader payloadUploader) throws InterruptedException {
    Payload collectionCompletedPayload = createCollectionCompletedPayload(collectionCompletedTimeMillis);
    if (collectionCompletedPayload != null)
      upload(collectionInfo, collectionCompletedPayload, null, payloadUploader); 
  }
  
  private void uploadCollectionSummaryResource(CollectionInfo collectionInfo, CollectionStatistics statistics, String collectionType, int pageSize, PayloadUploader payloadUploader) throws InterruptedException {
    if (statistics.payloadCount == 0) {
      if (_log.isInfoEnabled())
        _log.info(
            String.format("There are no payloads for collection type %s, will not send summary resource.", new Object[] { collectionType })); 
      return;
    } 
    Payload summaryPayload = createSummaryPayload(statistics, collectionType, pageSize);
    upload(collectionInfo, summaryPayload, null, payloadUploader);
  }
  
  private static void upload(CollectionInfo collectionInfo, Payload payload, CollectionStatistics statistics, PayloadUploader payloadUploader) throws InterruptedException {
    String uploadId = generateId();
    if (statistics != null)
      statistics.attemptedUploads++; 
    PayloadEnvelope header = new PayloadEnvelope(collectionInfo.collector);
    JsonLd collectionResource = createCollectionResource(collectionInfo.collectionId);
    Payload payloadWithCollection = (new Payload.Builder()).withCollectionId(collectionInfo.collectionId).add(collectionResource).add(payload).build();
    if (_log.isInfoEnabled())
      _log.info(
          String.format("Initiated payload upload (%d jsons)", new Object[] { Integer.valueOf(payloadWithCollection.getJsons().size()) })); 
    Future<?> uploadTask = payloadUploader.uploadPayload(payloadWithCollection, header, uploadId);
    try {
      uploadTask.get();
      if (_log.isInfoEnabled())
        _log.info("Upload completed successfully."); 
      if (statistics != null)
        statistics.successfulUploads++; 
    } catch (ExecutionException e) {
      ExceptionsContextManager.store(e);
      Throwable cause = e.getCause();
      if (cause instanceof InterruptedException)
        throw (InterruptedException)cause; 
      if (cause instanceof Error)
        throw (Error)cause; 
      logUploadError(cause);
    } 
  }
  
  private static JsonLd createCollectionResource(String collectionId) {
    JsonLd collectionResource = null;
    try {
      collectionResource = (new JsonLd.Builder()).withType("collection").withId(collectionId).build();
    } catch (IOException e) {
      _log.error("Failed to create collection resource.", e);
    } 
    return collectionResource;
  }
  
  private static Payload createCollectionCompletedPayload(long collectionCompletedTimeMillis) {
    JsonLd collectionCompletedJsonLd = null;
    try {
      collectionCompletedJsonLd = (new JsonLd.Builder()).withId(generateId()).withType("collection_completed").withProperty("collection_completed_timestamp", Long.valueOf(collectionCompletedTimeMillis)).build();
    } catch (IOException e) {
      _log.error("Failed to create collection completed payload.", e);
    } 
    return (new Payload.Builder()).add(collectionCompletedJsonLd).build();
  }
  
  private static Payload createSummaryPayload(CollectionStatistics statistics, String collectionType, int pageSize) {
    JsonLd summaryJsonLd = null;
    try {
      summaryJsonLd = (new JsonLd.Builder()).withId(generateId()).withType("collection_summary").withProperty("upload_attempts", Integer.valueOf(statistics.attemptedUploads)).withProperty("successful_attempts", Integer.valueOf(statistics.successfulUploads)).withProperty("payload_count", Integer.valueOf(statistics.payloadCount)).withProperty("collection_type", collectionType).withProperty("page_size", Integer.valueOf(pageSize)).build();
    } catch (IOException e) {
      _log.error("Failed to create summary payload.", e);
    } 
    return (new Payload.Builder()).add(summaryJsonLd).build();
  }
  
  private static void updateErrorStatistic(CollectionStatistics statistics, ContainsErrorInfo dataPackage) {
    if (dataPackage.getCollectorError() != null) {
      statistics.errorCount++;
      statistics.fatalErrorCount += dataPackage.hasFatalError() ? 1 : 0;
    } 
  }
  
  private static String generateId() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
  
  private static int getResourceCount(Payload payload) {
    return payload.getJsons().size();
  }
  
  private static void logUploadError(Throwable e) {
    String reason;
    if (e instanceof PayloadUploader.PayloadUploadException) {
      reason = "Payload upload";
    } else {
      reason = "Unexpected";
    } 
    _log.warn(reason + " exception while trying to upload: " + e.getMessage());
  }
  
  private static void logCollectionStatistics(CollectionStatistics statistics, String statisticsLabel) {
    if (_log.isInfoEnabled()) {
      StringBuilder msg = new StringBuilder("Usage data collection of type ");
      msg.append(statisticsLabel);
      msg.append(" completed ");
      if (statistics.errorCount > 0) {
        msg.append(" with ")
          .append(statistics.errorCount)
          .append(" error(s) (")
          .append(statistics.fatalErrorCount)
          .append(" fatal.)");
      } else {
        msg.append(" without errors.");
      } 
      msg.append(" There were ")
        .append(statistics.successfulUploads)
        .append(" successful uploads of total ")
        .append(statistics.attemptedUploads)
        .append(" attempts. Collected payload count is ")
        .append(statistics.payloadCount)
        .append(".");
      _log.info(msg.toString());
    } 
  }
  
  private static final class CollectionInfo {
    public final Collector collector;
    
    public final String collectionId;
    
    public CollectionInfo(Collector collectorInfo) {
      this.collector = collectorInfo;
      this.collectionId = UUID.randomUUID().toString().replace("-", "");
    }
  }
  
  static final class CollectionStatistics {
    public int attemptedUploads = 0;
    
    public int successfulUploads = 0;
    
    public int payloadCount = 0;
    
    public int errorCount = 0;
    
    public int fatalErrorCount = 0;
  }
}
