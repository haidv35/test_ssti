package com.vmware.ph.phservice.provider.vcenter.contentlibrary;

import com.vmware.cis.data.internal.adapters.lookup.ServiceEndpointInfo;
import com.vmware.cis.data.internal.adapters.util.vapi.DefaultVapiApiProviderPool;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiApiProviderPool;
import com.vmware.cis.data.internal.adapters.vapi.VapiDataProviderConfig;
import com.vmware.cis.data.internal.adapters.vapi.VapiPropertyValueConverter;
import com.vmware.cis.data.internal.adapters.vapi.impl.VapiDataProviderConnection;
import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.provider.DataProvider;
import com.vmware.content.util.StructTypeUtil;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.ph.phservice.common.vapi.MapBasedVapiTypeProvider;
import com.vmware.ph.phservice.common.vapi.VapiTypeProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.provider.common.vim.VimDataProvidersConnection;
import com.vmware.vapi.bindings.type.StructType;
import com.vmware.vapi.core.ApiProvider;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentLibraryDataProvidersConnection extends VimDataProvidersConnection {
  private static final String PRODUCT_CIS = "com.vmware.cis";
  
  private static final String SERVICE_TYPE_CLS = "cis.cls";
  
  private static final String ENDPOINT_CLS = "com.vmware.cis.cls.vapi.https";
  
  private static final String PROTOCOL_VAPI = "vapi.json.https";
  
  private static final Log _log = LogFactory.getLog(ContentLibraryDataProvidersConnection.class);
  
  private DataProvider _clsDataProvider;
  
  private VapiApiProviderPool _clsApiProivderPool;
  
  public ContentLibraryDataProvidersConnection(VimContext vimContext) {
    super(vimContext);
  }
  
  public synchronized List<DataProvider> getDataProviders() throws Exception {
    if (this._vimContext.getVimTrustedStore() == null) {
      if (_log.isWarnEnabled())
        _log.warn(String.format("%s is not provided with trusted roots key store. Without this keystore the collection process is not possible. Returning empty DataProvider list.", new Object[] { getClass().getSimpleName() })); 
      return Collections.emptyList();
    } 
    if (this._clsDataProvider == null) {
      ServiceEndpointInfo clsServiceEndpointInfo = discoverContentLibraryServiceEndpointInfo(this._vimContext);
      if (clsServiceEndpointInfo != null) {
        this
          ._clsApiProivderPool = createContentLibraryApiProviderPool(this._vimContext, clsServiceEndpointInfo);
        this._clsDataProvider = createContentLibraryDataProvider(this._clsApiProivderPool, clsServiceEndpointInfo, this._vimContext

            
            .getVcInstanceUuid());
      } 
    } 
    List<DataProvider> dataProviders = Collections.emptyList();
    if (this._clsDataProvider != null)
      dataProviders = Collections.singletonList(new ContentLibraryDataProviderDecorator(this._clsDataProvider)); 
    return dataProviders;
  }
  
  public synchronized void close() {
    super.close();
    if (this._clsApiProivderPool != null)
      try {
        this._clsApiProivderPool.close();
      } catch (Exception e) {
        if (_log.isDebugEnabled())
          _log.debug("Could not close the Content Library API provider pool."); 
      } finally {
        this._clsApiProivderPool = null;
      }  
    this._clsDataProvider = null;
  }
  
  private static VapiApiProviderPool createContentLibraryApiProviderPool(VimContext vimContext, ServiceEndpointInfo clsServiceEndpointInfo) {
    AuthenticationTokenSource authTokenSource = new SsoTokenProviderAuthenticationTokenSource(vimContext.getSsoTokenProvider());
    VapiApiProviderPool clsApiProviderPool = DefaultVapiApiProviderPool.Builder.forEndpoints(Collections.singletonList(clsServiceEndpointInfo)).connect(authTokenSource);
    return clsApiProviderPool;
  }
  
  private static DataProvider createContentLibraryDataProvider(VapiApiProviderPool clsApiProivderPool, ServiceEndpointInfo clsServiceEndpointInfo, String vcInstanceId) {
    VapiDataProviderConfig clsDataProviderConfig = createContentLibraryDataProviderConfig(clsApiProivderPool, clsServiceEndpointInfo);
    ApiProvider clsApiProvider = clsDataProviderConfig.getContentLibraryApiProviderPool().getApiProvider(clsServiceEndpointInfo.getNodeId());
    return (DataProvider)new VapiDataProviderConnection(clsApiProvider, clsDataProviderConfig
        
        .getPropertyValueConverter(), clsServiceEndpointInfo
        .getUrl(), vcInstanceId, false);
  }
  
  private static ServiceEndpointInfo discoverContentLibraryServiceEndpointInfo(VimContext vimContext) {
    LookupClient lookupClient = vimContext.getLookupClientBuilder(true).build();
    ServiceRegistration.Info[] clsServiceInfos = null;
    try {
      String nodeId = vimContext.getVcNodeId();
      clsServiceInfos = ServiceLocatorUtil.findServiceByServiceAndEndpointType(lookupClient, new ServiceRegistration.ServiceType("com.vmware.cis", "cis.cls"), new ServiceRegistration.EndpointType("vapi.json.https", "com.vmware.cis.cls.vapi.https"), nodeId);
    } finally {
      lookupClient.close();
    } 
    ServiceEndpointInfo clsServiceEndpointInfo = null;
    if (clsServiceInfos != null)
      clsServiceEndpointInfo = createContentLibraryServiceEndpointInfo(clsServiceInfos[0], vimContext
          
          .getVimTrustedStore(), vimContext
          .getApplianceContext().isLocal(), vimContext
          .getVcNodeId(), vimContext
          .getShouldUseEnvoySidecar()); 
    return clsServiceEndpointInfo;
  }
  
  private static VapiDataProviderConfig createContentLibraryDataProviderConfig(VapiApiProviderPool clsApiProviderPool, ServiceEndpointInfo clsServiceEndpointInfo) {
    VapiPropertyValueConverter clsPropertyValueConverter = getContentLibraryPropertyValueConverter();
    VapiDataProviderConfig clsVapiDataProviderConfig = VapiDataProviderConfig.Builder.create(clsApiProviderPool, new NoOpApiProviderPool(), new NoOpApiProviderPool()).withPropertyValueConverter(clsPropertyValueConverter).build();
    return clsVapiDataProviderConfig;
  }
  
  private static VapiPropertyValueConverter getContentLibraryPropertyValueConverter() {
    Map<String, StructType> clsCanonicalNameToStructMap = new HashMap<>();
    StructTypeUtil.populateCanonicalNameToStructTypeMap(clsCanonicalNameToStructMap);
    MapBasedVapiTypeProvider mapBasedVapiTypeProvider = new MapBasedVapiTypeProvider(clsCanonicalNameToStructMap);
    VapiPropertyValueConverter clsPropertyValueConverter = new VapiPropertyValueConverterImpl((VapiTypeProvider)mapBasedVapiTypeProvider);
    return clsPropertyValueConverter;
  }
  
  private static ServiceEndpointInfo createContentLibraryServiceEndpointInfo(ServiceRegistration.Info clsServiceInfo, KeyStore trustStore, boolean withLocalClsEndpoint, String localNodeId, boolean shouldUseEnvoySidecar) {
    ServiceRegistration.Endpoint[] clsServiceEndpoints = clsServiceInfo.getServiceEndpoints();
    if (clsServiceEndpoints == null)
      return null; 
    ServiceRegistration.Endpoint clsEndpoint = clsServiceEndpoints[0];
    URI clsUri = ServiceLocatorUtil.convertUriToEnvoySidecarIfNeeded(
        ServiceLocatorUtil.getEndpointUri(clsEndpoint, withLocalClsEndpoint), clsServiceInfo
        .getNodeId(), localNodeId, shouldUseEnvoySidecar);
    String serviceTypeId = clsServiceInfo.serviceType.product + ":" + clsServiceInfo.serviceType.type;
    ServiceEndpointInfo clsServiceEndpointInfo = new ServiceEndpointInfo(serviceTypeId, clsUri, clsServiceInfo.nodeId, trustStore, clsEndpoint.endpointType.type, clsEndpoint.endpointType.protocol, clsServiceInfo.serviceId, null, null);
    return clsServiceEndpointInfo;
  }
  
  private static class NoOpApiProviderPool implements VapiApiProviderPool {
    private NoOpApiProviderPool() {}
    
    public void close() throws Exception {}
    
    public ApiProvider getApiProvider(String nodeId) {
      return null;
    }
  }
}
