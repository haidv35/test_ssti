package com.vmware.ph.phservice.common.cdf;

import com.vmware.ph.client.api.commondataformat.PayloadEnvelope;
import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.common.cdf.internal.PayloadFileUtil;
import com.vmware.ph.phservice.common.cdf.internal.jsonld20.JSONObjectUtil;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemPayloadUploader implements PayloadUploader {
  private static final Log _log = LogFactory.getLog(FileSystemPayloadUploader.class);
  
  private final String _outputDirectoryPathName;
  
  private final ExecutorService _executorService;
  
  private final boolean _shouldArchivePayloads;
  
  public FileSystemPayloadUploader(String outputDirectoryPathName, ExecutorService executorService) {
    this(outputDirectoryPathName, executorService, false);
  }
  
  public FileSystemPayloadUploader(String outputDirectoryPathName, ExecutorService executorService, boolean shouldArchivePayloads) {
    this._outputDirectoryPathName = outputDirectoryPathName;
    this._executorService = executorService;
    this._shouldArchivePayloads = shouldArchivePayloads;
  }
  
  public boolean isEnabled() {
    return StringUtils.isNotBlank(this._outputDirectoryPathName);
  }
  
  public Future<?> uploadPayload(final Payload payload, final PayloadEnvelope envelope, final String uploadId) {
    return this._executorService.submit(new Callable<Void>() {
          public Void call() throws PayloadUploader.PayloadUploadException {
            FileSystemPayloadUploader.this.uploadPayloadBlocking(payload, envelope, uploadId);
            return null;
          }
        });
  }
  
  protected void uploadPayloadBlocking(Payload payload, PayloadEnvelope envelope, String uploadId) throws PayloadUploader.PayloadUploadException {
    uploadPayloadBlocking(payload, envelope, uploadId, null);
  }
  
  protected void uploadPayloadBlocking(Payload payload, PayloadEnvelope envelope, String uploadId, Object additionalContext) throws PayloadUploader.PayloadUploadException {
    if (!isEnabled())
      throw new PayloadUploader.PayloadUploadException(FileSystemPayloadUploader.class + " is not configured properly, it is not able to process specified payload."); 
    try {
      if (payload == null) {
        _log.debug("Skipping upload of empty payload.");
        return;
      } 
      if (!payload.getJsons().isEmpty()) {
        String jsonString = JSONObjectUtil.buildJsonArrayStringFromJsonLds(payload.getJsons());
        PayloadFileUtil.writeJsonToFile(this._outputDirectoryPathName, jsonString, this._shouldArchivePayloads);
      } 
    } catch (IOException e) {
      throw new PayloadUploader.PayloadUploadException("An exception occurred while trying to save payload on the local file system.", e);
    } 
  }
}
