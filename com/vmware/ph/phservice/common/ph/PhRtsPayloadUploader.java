package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.client.api.exceptions.AuditFileWritingFailedException;
import com.vmware.ph.phservice.common.cdf.PayloadUploader;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhRtsPayloadUploader implements PayloadUploader {
  private static final Logger _logger = LoggerFactory.getLogger(PhRtsPayloadUploader.class);
  
  private final PhRtsClientFactory _phRtsClientFactory;
  
  private final AuditPersister _auditPersister;
  
  private final ExecutorService _executorService;
  
  public PhRtsPayloadUploader(PhRtsClientFactory phRtsClientFactory, AuditPersister auditPersister, ExecutorService executorService) {
    this._phRtsClientFactory = phRtsClientFactory;
    this._auditPersister = auditPersister;
    this._executorService = executorService;
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public Future<?> uploadPayload(final Payload payload, final PayloadEnvelope payloadEnvelope, final String uploadId) {
    Future<?> result = this._executorService.submit(new Callable<Void>() {
          public Void call() throws PayloadUploader.PayloadUploadException {
            PhRtsPayloadUploader.this.uploadPayloadBlocking(payload, payloadEnvelope, uploadId);
            return null;
          }
        });
    return result;
  }
  
  private void uploadPayloadBlocking(Payload payload, PayloadEnvelope payloadEnvelope, String uploadId) throws PayloadUploader.PayloadUploadException {
    if (payload.getJsons().isEmpty()) {
      _logger.info("Payload is empty. Payload upload will be skipped.");
      return;
    } 
    String jsonArrayString = JSONObjectUtil.buildJsonArrayStringFromJsonLds(payload.getJsons());
    try {
      if (this._auditPersister != null)
        this._auditPersister.persist(jsonArrayString); 
      uploadData(jsonArrayString, payload.getCollectionId(), payloadEnvelope);
    } catch (AuditFileWritingFailedException e) {
      _logger.error("Audit data could not be persisted locally. Metric data upload will not be attempted.");
      throw new PayloadUploader.PayloadUploadException(e);
    } 
  }
  
  private void uploadData(String data, String collectionId, PayloadEnvelope payloadEnvelope) throws PayloadUploader.PayloadUploadException {
    int resultStatusCode;
    try (PhRtsClient phRtsClient = this._phRtsClientFactory.create()) {
      byte[] compressedData = CompressionUtil.getGzippedBytes(data.getBytes(StandardCharsets.UTF_8));
      resultStatusCode = phRtsClient.send(payloadEnvelope
          .getCollector().getCollectorId(), payloadEnvelope
          .getCollector().getCollectorInstanceId(), collectionId, null, compressedData, true);
    } catch (Exception e) {
      throw new PayloadUploader.PayloadUploadException(e);
    } 
    if (resultStatusCode != 201 && resultStatusCode != 202)
      throw new PayloadUploader.PayloadUploadException(
          String.format("Sending payload to VAC failed with status code [%d].", new Object[] { Integer.valueOf(resultStatusCode) })); 
  }
}
