package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.exceptions.StatusCodeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class PhDapClient implements AutoCloseable {
  private static final String STRING_BEGINNING = "\\A";
  
  private static final Log _log = LogFactory.getLog(PhDapClient.class);
  
  private final RtsUriFactory _rtsUriFactory;
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final CloseableHttpClient _httpClient;
  
  public PhDapClient(RtsUriFactory rtsUriFactory, CeipConfigProvider ceipConfigProvider, CloseableHttpClient httpClient) {
    this._ceipConfigProvider = Objects.<CeipConfigProvider>requireNonNull(ceipConfigProvider);
    this._rtsUriFactory = Objects.<RtsUriFactory>requireNonNull(rtsUriFactory);
    this._httpClient = Objects.<CloseableHttpClient>requireNonNull(httpClient);
  }
  
  public void sendPluginData(String collectorId, String collectorInstanceId, String collectionId, String version, String deploymentSecret, PluginData pluginData) {
    if (!this._ceipConfigProvider.isCeipEnabled()) {
      if (_log.isDebugEnabled())
        _log.debug("CEIP is not enabled - not sending collected data to DAP plugin [" + collectorId + ", " + collectorInstanceId + "] "); 
      return;
    } 
    try {
      URI uri = this._rtsUriFactory.createDataAppSendUri(collectorId, collectorInstanceId, collectionId, version);
      HttpPost httpPost = new HttpPost(uri);
      byte[] postData = pluginData.getData();
      if (!pluginData.isCompressed())
        postData = CompressionUtil.getGzippedBytes(postData); 
      httpPost.setEntity((HttpEntity)new ByteArrayEntity(postData));
      httpPost.addHeader("Content-Type", "application/gzip");
      Map<String, String> pluginDataHeaders = getPluginDataHeaders(deploymentSecret, collectionId, pluginData);
      for (Map.Entry<String, String> entry : pluginDataHeaders.entrySet())
        httpPost.addHeader(entry.getKey(), entry.getValue()); 
      try (CloseableHttpResponse response = this._httpClient.execute((HttpUriRequest)httpPost)) {
        int responseStatusCode = getResponseStatusCode((HttpResponse)response);
        switch (responseStatusCode) {
          case 200:
          case 201:
            return;
        } 
        if (_log.isWarnEnabled())
          _log.warn("Received error from VAC when sending collected data to DAP plugin [" + collectorId + ", " + collectorInstanceId + "]: " + 

              
              getResponseReasonPhrase((HttpResponse)response)); 
        throw new StatusCodeException(responseStatusCode, "VAC response contains an error message: " + 

            
            getResponseReasonPhrase(response));
      } catch (ClientProtocolException e) {
        logExceptionFromSend(collectorId, collectorInstanceId, (Exception)e);
        throw new StatusCodeException(500, "There was a problem in he HTTP response from VAC.", e);
      } catch (IOException e) {
        logExceptionFromSend(collectorId, collectorInstanceId, e);
        throw new StatusCodeException(503, "There was a problem in the connection to VAC.", e);
      } 
    } catch (URISyntaxException|IOException e) {
      ExceptionsContextManager.store(e);
      logExceptionFromSend(collectorId, collectorInstanceId, e);
      throw new StatusCodeException(500, "There was a problem in Analytics service.", e);
    } 
  }
  
  public String queryResult(String collectorId, String collectorInstanceId, String deploymentSecret, String dataType, String objectId, Long sinceTimestamp) {
    if (!this._ceipConfigProvider.isCeipEnabled()) {
      if (_log.isDebugEnabled())
        _log.debug("CEIP is not enabled - not invoking data app result service for DAP plugin [" + collectorId + ", " + collectorInstanceId + "] for object '" + objectId + "'."); 
      return null;
    } 
    try {
      URI uri = this._rtsUriFactory.createObjectQueryUri(collectorId, collectorInstanceId, dataType, objectId, sinceTimestamp);
      HttpGet httpGet = new HttpGet(uri);
      httpGet.addHeader("X-Deployment-Secret", deploymentSecret);
      httpGet.addHeader("Content-Type", "application/json");
      if (_log.isDebugEnabled())
        _log.debug("Querying result from DAP with request: " + httpGet.toString()); 
      String responseBody = null;
      try (CloseableHttpResponse response = this._httpClient.execute((HttpUriRequest)httpGet)) {
        int responseStatusCode = getResponseStatusCode((HttpResponse)response);
        if (responseStatusCode == 500)
          throw new StatusCodeException(500, getResponseReasonPhrase(response)); 
        HttpEntity entity = response.getEntity();
        try (InputStream responseStream = entity.getContent()) {
          responseBody = readStream(responseStream);
        } catch (Exception e) {
          logExceptionFromQueryResult(collectorId, collectorInstanceId, objectId, e);
          throw new StatusCodeException(500, "There was a problem in Analytics service.", e);
        } 
      } catch (ClientProtocolException e) {
        logExceptionFromQueryResult(collectorId, collectorInstanceId, objectId, (Exception)e);
        throw new StatusCodeException(500, "There was a problem in he HTTP response from VAC.", e);
      } catch (IOException e) {
        logExceptionFromQueryResult(collectorId, collectorInstanceId, objectId, e);
        throw new StatusCodeException(503, "There was a problem in the connection to VAC.", e);
      } 
      return responseBody;
    } catch (URISyntaxException e) {
      ExceptionsContextManager.store(e);
      logExceptionFromQueryResult(collectorId, collectorInstanceId, objectId, e);
      throw new StatusCodeException(500, "There was a problem in Analytics service.", e);
    } 
  }
  
  public void close() {
    try {
      this._httpClient.close();
    } catch (IOException e) {
      _log.debug("Failed to close HTTP client.", e);
    } 
  }
  
  private static String readStream(InputStream inputStream) {
    Scanner streamScanner = (new Scanner(inputStream, StandardCharsets.UTF_8.name())).useDelimiter("\\A");
    return streamScanner.hasNext() ? streamScanner.next() : "";
  }
  
  private static Map<String, String> getPluginDataHeaders(String deploymentSecret, String transactionId, PluginData pluginData) {
    Map<String, String> headers = new LinkedHashMap<>();
    if (deploymentSecret != null)
      headers.put("X-Deployment-Secret", deploymentSecret); 
    if (transactionId != null)
      headers.put("X-Transaction-Id", transactionId); 
    if (pluginData.getPluginType() != null)
      headers.put("X-Plugin-Type", pluginData.getPluginType()); 
    if (pluginData.getDataType() != null)
      headers.put("X-Data-Type", pluginData.getDataType()); 
    if (pluginData.getObjectId() != null)
      headers.put("X-Object-Id", pluginData.getObjectId()); 
    return headers;
  }
  
  private void logExceptionFromSend(String collectorId, String collectorInstanceId, Exception e) {
    if (_log.isWarnEnabled())
      _log.warn("Exception occurred while sending collected data to DAP plugin [" + collectorId + ", " + collectorInstanceId + "].", e); 
  }
  
  private void logExceptionFromQueryResult(String collectorId, String collectorInstanceId, String objectId, Exception e) {
    if (_log.isWarnEnabled())
      _log.warn("Exception occurred while querying result DAP plugin [" + collectorId + ", " + collectorInstanceId + "] for object '" + objectId + "'.", e); 
  }
  
  private String getResponseReasonPhrase(HttpResponse response) {
    String reasonPhrase;
    StatusLine statusLine = response.getStatusLine();
    if (statusLine != null) {
      reasonPhrase = statusLine.getReasonPhrase();
    } else {
      reasonPhrase = "No reason phrase.";
    } 
    return reasonPhrase;
  }
  
  private int getResponseStatusCode(HttpResponse response) {
    int statusCode;
    StatusLine statusLine = response.getStatusLine();
    if (statusLine != null) {
      statusCode = statusLine.getStatusCode();
    } else {
      statusCode = 500;
    } 
    return statusCode;
  }
}
