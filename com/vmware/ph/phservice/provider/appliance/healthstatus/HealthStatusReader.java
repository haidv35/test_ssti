package com.vmware.ph.phservice.provider.appliance.healthstatus;

import com.vmware.ph.phservice.common.vmomi.internal.HttpClientBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public class HealthStatusReader {
  private static final Log _log = LogFactory.getLog(HealthStatusReader.class);
  
  public static String readHealthStatus(URI healthStatusUrl, KeyStore trustStore) {
    HttpGet httpGet = new HttpGet(healthStatusUrl);
    String healthStatus = null;
    try(CloseableHttpClient httpClient = (new HttpClientBuilder(trustStore)).build(); 
        CloseableHttpResponse healthStatusResponse = httpClient.execute((HttpUriRequest)httpGet); 
        
        InputStream healthStatusInputStream = healthStatusResponse.getEntity().getContent()) {
      healthStatus = IOUtils.toString(healthStatusInputStream, StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      _log.warn("Failed to create an HTTP connection to the health status URI. No health status will be read.", e);
    } 
    return healthStatus;
  }
}
