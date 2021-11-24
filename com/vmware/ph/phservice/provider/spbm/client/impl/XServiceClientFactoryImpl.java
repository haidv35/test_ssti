package com.vmware.ph.phservice.provider.spbm.client.impl;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.vim.VimVmodlUtil;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.provider.spbm.client.XServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.XServiceClientFactory;
import com.vmware.ph.phservice.provider.spbm.client.common.constants.XServiceClientConstants;
import com.vmware.ph.phservice.provider.spbm.client.common.context.XServiceClientContext;
import com.vmware.ph.phservice.provider.spbm.client.pbm.PbmServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.pbm.impl.PbmServiceClientImpl;
import com.vmware.ph.phservice.provider.spbm.client.sms.SmsServiceClient;
import com.vmware.ph.phservice.provider.spbm.client.sms.impl.SmsServiceClientImpl;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.ext.InvocationContext;
import com.vmware.vim.vmomi.client.ext.RequestRetryCallback;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.impl.HttpConfigurationImpl;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XServiceClientFactoryImpl implements XServiceClientFactory {
  private static final Log log = LogFactory.getLog(XServiceClientFactoryImpl.class);
  
  public PbmServiceClient getPbmServiceClient(XServiceClientContext xServiceClientContext) {
    PbmServiceClient pbmServiceClient = null;
    URI pbmServiceUri = findEndpointUri(xServiceClientContext
        .getLookupClientBuilder(), XServiceClientConstants.PBM_ENDPOINT_TYPE, xServiceClientContext
        
        .isApplianceLocal(), xServiceClientContext
        .getLocalNodeId(), xServiceClientContext
        .getShouldUseEnvoySidecar());
    if (log.isTraceEnabled())
      log.trace("Connecting to pbmServiceUri :" + pbmServiceUri.toString()); 
    VmodlContextProvider.getVmodlContextForPacakgeAndClass(xServiceClientContext.getVmodlPackageNameToPackageClass(), false);
    pbmServiceClient = createPbmServiceClient(pbmServiceUri, xServiceClientContext);
    if (log.isTraceEnabled())
      log.trace("Successfully Connected to pbmServiceUri :" + pbmServiceUri.toString()); 
    return pbmServiceClient;
  }
  
  public SmsServiceClient getSmsServiceClient(XServiceClientContext xServiceClientContext) {
    SmsServiceClient smsServiceClient = null;
    URI smsServiceUri = findEndpointUri(xServiceClientContext
        .getLookupClientBuilder(), XServiceClientConstants.SMS_ENDPOINT_TYPE, xServiceClientContext
        
        .isApplianceLocal(), xServiceClientContext
        .getLocalNodeId(), xServiceClientContext
        .getShouldUseEnvoySidecar());
    if (log.isTraceEnabled())
      log.trace("Connecting to smsServiceUri :" + smsServiceUri.toString()); 
    VmodlContextProvider.getVmodlContextForPacakgeAndClass(xServiceClientContext.getVmodlPackageNameToPackageClass(), false);
    smsServiceClient = createSmsServiceClient(smsServiceUri, xServiceClientContext);
    if (log.isTraceEnabled())
      log.trace("Successfully Connected to smsServiceUri :" + smsServiceUri.toString()); 
    return smsServiceClient;
  }
  
  private static Client createVmomiClient(URI xServiceUri, RequestRetryCallback requestRetryCallback, XServiceClientContext xServiceClientContext) {
    HttpConfigurationImpl httpConfigurationImpl = new HttpConfigurationImpl();
    httpConfigurationImpl.setTrustStore(xServiceClientContext.getTrustStore());
    httpConfigurationImpl.setThumbprintVerifier(xServiceClientContext.getThumbprintVerifier());
    httpConfigurationImpl.setCheckStaleConnection(true);
    if (xServiceClientContext.getTimeoutMs() != null)
      httpConfigurationImpl.setTimeoutMs(xServiceClientContext.getTimeoutMs().intValue()); 
    if (xServiceClientContext.getMaxConnections() != null)
      httpConfigurationImpl.setMaxConnections(xServiceClientContext.getMaxConnections().intValue()); 
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    clientConfig.setExecutor(xServiceClientContext.getExecutor());
    clientConfig.setHttpConfiguration((HttpConfiguration)httpConfigurationImpl);
    clientConfig.setRequestRetryCallback(requestRetryCallback);
    Client xVmomiClient = Client.Factory.createClient(xServiceUri, xServiceClientContext.getxClientVmodlVersion(), (ClientConfiguration)clientConfig);
    return xVmomiClient;
  }
  
  private static URI findEndpointUri(LookupClientBuilder lookupClientBuilder, ServiceRegistration.EndpointType endpointType, boolean tryGetLocalUri, String localNodeId, boolean shouldUseEnvoySidecar) {
    Pair<ServiceRegistration.Endpoint, String> endpointToNodeId = null;
    try (LookupClient lookupClient = lookupClientBuilder.build()) {
      endpointToNodeId = ServiceLocatorUtil.getEndpointByEndpointType(lookupClient, endpointType);
    } 
    if (endpointToNodeId == null)
      return null; 
    ServiceRegistration.Endpoint serviceEndpoint = (ServiceRegistration.Endpoint)endpointToNodeId.getFirst();
    String serviceNodeId = (String)endpointToNodeId.getSecond();
    URI endpointUri = ServiceLocatorUtil.convertUriToEnvoySidecarIfNeeded(
        ServiceLocatorUtil.getEndpointUri(serviceEndpoint, tryGetLocalUri), serviceNodeId, localNodeId, shouldUseEnvoySidecar);
    return endpointUri;
  }
  
  private static void setSessionId(Client xVmomiClient, Client vcVmomiClient) {
    if (xVmomiClient != null && vcVmomiClient != null && vcVmomiClient.getBinding().getSession() != null) {
      if (log.isTraceEnabled())
        log.trace("Retrieving a valid session cookie for extension client " + xVmomiClient
            .getBinding().getEndpointUri()); 
      String vcSessionId = vcVmomiClient.getBinding().getSession().getId();
      xVmomiClient.getBinding().setSession(xVmomiClient.getBinding().createSession(vcSessionId));
    } 
  }
  
  private static class XServiceRequestRetryCallbackImpl implements RequestRetryCallback {
    private XServiceRequestRetryCallbackImpl() {}
    
    private static final Log log = LogFactory.getLog(XServiceRequestRetryCallbackImpl.class);
    
    private XServiceClient xServiceClient;
    
    public void setXServiceClient(XServiceClient xServiceClient) {
      this.xServiceClient = xServiceClient;
    }
    
    public boolean retry(Exception exception, InvocationContext context, int count) {
      if (!(exception instanceof com.vmware.vim.binding.vim.fault.NoPermission))
        return false; 
      if (count > 1)
        return false; 
      if (log.isDebugEnabled())
        log.debug("Extension client's session is not authenticated. Re-logging into VC"); 
      callVimApiToInvokeVcRetryCallBack();
      XServiceClientFactoryImpl.setSessionId(this.xServiceClient.getVmomiClient(), this.xServiceClient.getVcClient().getVlsiClient());
      context.getStubRequestContext().put("vcSessionCookie", this.xServiceClient
          .getVmomiClient().getBinding().getSession().getId());
      return true;
    }
    
    private void callVimApiToInvokeVcRetryCallBack() {
      ServiceInstance serviceInstance = (ServiceInstance)this.xServiceClient.getVcClient().createMo(VimVmodlUtil.SERVICE_INSTANCE_MOREF);
      serviceInstance.currentTime();
      if (log.isDebugEnabled())
        log.debug("Successfully logged into VC"); 
    }
  }
  
  private static PbmServiceClient createPbmServiceClient(URI xServiceUri, XServiceClientContext xServiceClientContext) {
    XServiceRequestRetryCallbackImpl requestRetryCallback = new XServiceRequestRetryCallbackImpl();
    Client xVmomiClient = createVmomiClient(xServiceUri, requestRetryCallback, xServiceClientContext);
    PbmServiceClient pbmServiceClient = new PbmServiceClientImpl(xVmomiClient, xServiceClientContext);
    setSessionId(pbmServiceClient.getVmomiClient(), xServiceClientContext.getVcClient().getVlsiClient());
    requestRetryCallback.setXServiceClient(pbmServiceClient);
    return pbmServiceClient;
  }
  
  private static SmsServiceClient createSmsServiceClient(URI xServiceUri, XServiceClientContext xServiceClientContext) {
    XServiceRequestRetryCallbackImpl requestRetryCallback = new XServiceRequestRetryCallbackImpl();
    Client xVmomiClient = createVmomiClient(xServiceUri, requestRetryCallback, xServiceClientContext);
    SmsServiceClientImpl smsServiceClient = new SmsServiceClientImpl(xVmomiClient, xServiceClientContext);
    setSessionId(smsServiceClient.getVmomiClient(), xServiceClientContext.getVcClient().getVlsiClient());
    requestRetryCallback.setXServiceClient(smsServiceClient);
    return smsServiceClient;
  }
}
