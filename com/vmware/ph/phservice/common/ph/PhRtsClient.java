package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.ph.http.execute.RequestExecutor;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

public class PhRtsClient implements AutoCloseable {
  private static final int DEFAULT_ERROR_RESPONSE_CODE = 400;
  
  private static final Log _log = LogFactory.getLog(PhRtsClient.class);
  
  private final RtsUriFactory _rtsUriFactory;
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final RequestExecutor _requestExecutor;
  
  public PhRtsClient(RtsUriFactory rtsUriFactory, CeipConfigProvider ceipConfigProvider, RequestExecutor requestExecutor) {
    this._ceipConfigProvider = Objects.<CeipConfigProvider>requireNonNull(ceipConfigProvider);
    this._rtsUriFactory = Objects.<RtsUriFactory>requireNonNull(rtsUriFactory);
    this._requestExecutor = Objects.<RequestExecutor>requireNonNull(requestExecutor);
  }
  
  public int send(String collectorId, String collectorInstanceId, String collectionId, String version, byte[] data, boolean isCompressed) {
    if (!this._ceipConfigProvider.isCeipEnabled())
      return 202; 
    try {
      URI uri = this._rtsUriFactory.createHyperSendUri(collectorId, collectorInstanceId, collectionId, version);
      HttpPost httpPost = createHttpPostRequest(uri, data, isCompressed);
      if (_log.isDebugEnabled())
        _log.debug("Sending telemetry to: " + httpPost.getURI()); 
      return this._requestExecutor.executeRequest((HttpUriRequest)httpPost);
    } catch (Exception e) {
      _log.error("Failed to execute request.", e);
      return 400;
    } 
  }
  
  public void close() {
    try {
      this._requestExecutor.close();
    } catch (IOException e) {
      _log.debug("Failed to close request executor.", e);
    } 
  }
  
  private static HttpPost createHttpPostRequest(URI uri, byte[] data, boolean isCompressed) {
    HttpPost httpPost = new HttpPost(uri);
    httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
    if (isCompressed)
      httpPost.setHeader("Content-Encoding", "gzip"); 
    httpPost.setEntity((HttpEntity)new ByteArrayEntity(data));
    return httpPost;
  }
}
