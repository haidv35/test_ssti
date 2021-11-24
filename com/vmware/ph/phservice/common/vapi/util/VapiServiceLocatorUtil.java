package com.vmware.ph.phservice.common.vapi.util;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.cis.lookup.LookupClient;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.cis.lookup.ServiceLocatorUtil;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.fault.EntryNotFoundFault;
import java.net.URI;

public final class VapiServiceLocatorUtil {
  public static final String VAPI_PUBLIC_ENDPOINT_PROTOCOL_ID = "vapi.json.https.public";
  
  public static final String VAPI_PUBLIC_ENDPOINT_TYPE_ID = "com.vmware.vapi.endpoint";
  
  public static final ServiceRegistration.EndpointType VAPI_PUBLIC_ENDPOINT = new ServiceRegistration.EndpointType("vapi.json.https.public", "com.vmware.vapi.endpoint");
  
  public static URI discoverVapiServiceUri(LookupClientBuilder lookupClientBuilder, ServiceRegistration.EndpointType endpointType, String nodeId, boolean tryGetLocalVapiUri, boolean shouldUseEnvoySidecar) throws EntryNotFoundFault {
    try (LookupClient lookupClient = lookupClientBuilder.build()) {
      Pair<ServiceRegistration.Endpoint, String> endpointToNodeId = ServiceLocatorUtil.getEndpointByEndpointTypeAndNodeId(lookupClient, endpointType, nodeId);
      if (endpointToNodeId == null)
        return null; 
      URI uri = ServiceLocatorUtil.convertUriToLocalEnvoySidecarIfNeeded(
          ServiceLocatorUtil.getEndpointUri((ServiceRegistration.Endpoint)endpointToNodeId.getFirst(), tryGetLocalVapiUri), shouldUseEnvoySidecar);
      return uri;
    } 
  }
}
