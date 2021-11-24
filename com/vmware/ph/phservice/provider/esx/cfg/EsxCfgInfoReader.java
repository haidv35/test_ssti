package com.vmware.ph.phservice.provider.esx.cfg;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.vim.binding.vim.SessionManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;

public class EsxCfgInfoReader {
  private static final String ESX_CFG_INFO_LOCATION = "/cgi-bin/esxcfg-info.cgi?format=%s&subtree=%s";
  
  private static final String VMWARE_CGI_TICKET_HEADER = "vmware_cgi_ticket=";
  
  private static final Log _log = LogFactory.getLog(EsxCfgInfoReader.class);
  
  private final HttpClient _httpClient;
  
  private final Builder<SessionManager> _sessionManagerBuilder;
  
  public EsxCfgInfoReader(HttpClient httpClient, Builder<SessionManager> sessionManagerBuilder) {
    this._sessionManagerBuilder = sessionManagerBuilder;
    this._httpClient = httpClient;
  }
  
  public String getEsxCfgInfo(List<String> hostManagementIps, List<String> esxCfgInfoFilters, String esxCfgInfoFormat, String esxCfgInfoSubtree) {
    Objects.requireNonNull(hostManagementIps);
    if (esxCfgInfoFilters == null || esxCfgInfoFilters.isEmpty())
      return null; 
    if (StringUtils.isBlank(esxCfgInfoFormat) || 
      StringUtils.isBlank(esxCfgInfoSubtree))
      return null; 
    String esxCfgInfo = null;
    for (String hostManagementIp : hostManagementIps) {
      try {
        String hostConfigUrl = buildEsxCfgInfoUrl(hostManagementIp, esxCfgInfoFilters, esxCfgInfoFormat, esxCfgInfoSubtree);
        esxCfgInfo = downloadEsxCfgInfo(hostConfigUrl);
        break;
      } catch (Exception e) {
        if (_log.isDebugEnabled())
          _log.debug(
              String.format("Could not get collect configuration data from IP '%s'", new Object[] { hostManagementIp }), e); 
      } 
    } 
    return esxCfgInfo;
  }
  
  private String buildEsxCfgInfoUrl(String hostManagementIp, List<String> esxCfgInfoFilters, String esxCfgInfoFormat, String esxCfgInfoSubtree) {
    String esxCfgInfoUrlPath = String.format("/cgi-bin/esxcfg-info.cgi?format=%s&subtree=%s", new Object[] { esxCfgInfoFormat, esxCfgInfoSubtree });
    StringBuilder urlSuffix = new StringBuilder(esxCfgInfoUrlPath);
    for (String esxCfgInfoFilter : esxCfgInfoFilters)
      urlSuffix.append("&filter=" + esxCfgInfoFilter); 
    try {
      URL esxCfgInfoUrl = new URL("https", hostManagementIp, urlSuffix.toString());
      return esxCfgInfoUrl.toString();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } 
  }
  
  private String downloadEsxCfgInfo(String hostFileUrl) throws IOException {
    SessionManager.GenericServiceTicket serviceTicket = obtainGenericServiceTicket(hostFileUrl);
    if (_log.isDebugEnabled())
      _log.debug("Requesting to download data. Request address is " + hostFileUrl); 
    HttpResponse response = get(hostFileUrl, serviceTicket);
    try {
      if (_log.isDebugEnabled())
        _log.debug("Request to download ESX CFG info data received response: " + response
            
            .getStatusLine()); 
      String esxCfgInfo = IOUtils.toString(response
          .getEntity().getContent(), StandardCharsets.UTF_8);
      return esxCfgInfo;
    } finally {
      if (response != null)
        HttpClientUtils.closeQuietly(response); 
    } 
  }
  
  private SessionManager.GenericServiceTicket obtainGenericServiceTicket(String url) {
    SessionManager.HttpServiceRequestSpec spec = new SessionManager.HttpServiceRequestSpec();
    spec.setUrl(url);
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Requesting service ticket for URL '%s' ...", new Object[] { url })); 
    SessionManager sessionManager = this._sessionManagerBuilder.build();
    SessionManager.GenericServiceTicket serviceTicket = sessionManager.acquireGenericServiceTicket((SessionManager.ServiceRequestSpec)spec);
    if (_log.isDebugEnabled())
      _log.debug(
          String.format("Successfully cquired generic service ticket for URL '%s'", new Object[] { spec.getUrl() })); 
    return serviceTicket;
  }
  
  private HttpResponse get(String accessUrl, SessionManager.GenericServiceTicket accessTicket) throws IOException {
    try {
      HttpGet httpGet = new HttpGet(accessUrl);
      httpGet.setHeader("Cookie", "vmware_cgi_ticket=" + accessTicket.getId());
      HttpResponse response = this._httpClient.execute((HttpUriRequest)httpGet);
      return response;
    } catch (Exception e) {
      String message = "FAILED connecting to URL " + accessUrl + ". The configuration data for this host will not be downloaded.";
      if (_log.isDebugEnabled())
        _log.debug(message, e); 
      throw new IOException(message, e);
    } 
  }
}
