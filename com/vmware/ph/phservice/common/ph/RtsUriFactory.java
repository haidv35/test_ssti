package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.upload.service.UploadServiceBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;

public class RtsUriFactory {
  private static final URI DEFAULT_RTS_BASE_URL = URI.create("https://vcsa.vmware.com");
  
  private static final String STAGE_BASE_PATH = "/ph-stg/api";
  
  private static final String PROD_BASE_PATH = "/ph/api";
  
  private static final String HYPER_SEND_PATH = "/hyper/send";
  
  private static final String DATAAPP_SEND_PATH = "/dataapp/send";
  
  private static final String OBJECT_QUERY_PATH = "/v1/results";
  
  private final URI _baseUrl;
  
  private final String _basePath;
  
  public RtsUriFactory(PhEnvironmentProvider environmentProvider) {
    this(environmentProvider, DEFAULT_RTS_BASE_URL);
  }
  
  public RtsUriFactory(PhEnvironmentProvider environmentProvider, URI baseUrl) {
    UploadServiceBuilder.Environment environment = (UploadServiceBuilder.Environment)Optional.<PhEnvironmentProvider>ofNullable(environmentProvider).map(PhEnvironmentProvider::getEnvironment).orElseThrow(() -> new NullPointerException("A valid environment provider must be supplied."));
    Objects.requireNonNull(environment, "A valid environment must be provided.");
    if (UploadServiceBuilder.Environment.PRODUCTION.equals(environment)) {
      this._basePath = "/ph/api";
    } else {
      this._basePath = "/ph-stg/api";
    } 
    this._baseUrl = Objects.<URI>requireNonNull(baseUrl, "A valid server URL for the RTS endpoint must be provided.");
  }
  
  public URI createHyperSendUri(String collectorId, String collectorInstanceId, String collectionId, String version) throws URISyntaxException {
    return createSendUri("/hyper/send", collectorId, collectorInstanceId, collectionId, version);
  }
  
  public URI createDataAppSendUri(String collectorId, String collectorInstanceId, String collectionId, String version) throws URISyntaxException {
    return createSendUri("/dataapp/send", collectorId, collectorInstanceId, collectionId, version);
  }
  
  public URI createObjectQueryUri(String collectorId, String collectorInstanceId, String dataType, String objectId, Long sinceTimestamp) throws URISyntaxException {
    URIBuilder uriBuilder = (new URIBuilder(this._baseUrl)).setPath(this._basePath + "/v1/results");
    uriBuilder.setParameter("collectorId", collectorId);
    if (collectorInstanceId != null)
      uriBuilder.setParameter("deploymentId", collectorInstanceId); 
    if (dataType != null)
      uriBuilder.setParameter("type", dataType); 
    if (objectId != null)
      uriBuilder.setParameter("objectId", objectId); 
    if (sinceTimestamp != null)
      uriBuilder.setParameter("since", Long.toString(sinceTimestamp.longValue())); 
    return uriBuilder.build();
  }
  
  private URI createSendUri(String urlPath, String collectorId, String collectorInstanceId, String collectionId, String version) throws URISyntaxException {
    URIBuilder uriBuilder = (new URIBuilder(this._baseUrl)).setPath(this._basePath + urlPath).setParameter("_c", collectorId);
    if (version != null)
      uriBuilder.setParameter("_v", version); 
    if (collectorInstanceId != null)
      uriBuilder.setParameter("_i", collectorInstanceId); 
    if (collectionId != null)
      uriBuilder.setParameter("_n", collectionId); 
    return uriBuilder.build();
  }
}
