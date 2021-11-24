package com.vmware.ph.phservice.provider.spbm.client.common.context;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.util.Validate;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executor;

public class XServiceClientContextBuilder implements Builder<XServiceClientContext> {
  private final VcClient vcClient;
  
  private Map<String, Class<?>> vmodlPackageNameToPackageClass;
  
  private Executor executor;
  
  private Integer maxConnections;
  
  private ThumbprintVerifier thumbprintVerifier;
  
  private Integer timeoutMs;
  
  private KeyStore trustStore;
  
  private Class<?> xClientVmodlVersion;
  
  private LookupClientBuilder lookupClientBuilder;
  
  private boolean isApplianceLocal;
  
  private boolean shouldUseEnvoySidecar;
  
  private String localNodeId;
  
  private XServiceClientContextBuilder(VcClient vcClient) {
    this.vcClient = vcClient;
  }
  
  public static XServiceClientContextBuilder newInstance(VcClient vcClient) {
    return new XServiceClientContextBuilder(vcClient);
  }
  
  public XServiceClientContextBuilder withMaxConnections(Integer maxConnections) {
    this.maxConnections = maxConnections;
    return this;
  }
  
  public XServiceClientContextBuilder withThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this.thumbprintVerifier = thumbprintVerifier;
    return this;
  }
  
  public XServiceClientContextBuilder withTimeoutMs(Integer timeoutMs) {
    this.timeoutMs = timeoutMs;
    return this;
  }
  
  public XServiceClientContextBuilder withTrustStore(KeyStore trustStore) {
    this.trustStore = trustStore;
    return this;
  }
  
  public XServiceClientContextBuilder withXClientVmodlVersion(Class<?> xClientVmodlVersion) {
    this.xClientVmodlVersion = xClientVmodlVersion;
    return this;
  }
  
  public XServiceClientContextBuilder withVmodlPackageNameToPackageClass(Map<String, Class<?>> vmodlPackageNameToPackageClass) {
    this.vmodlPackageNameToPackageClass = vmodlPackageNameToPackageClass;
    return this;
  }
  
  public XServiceClientContextBuilder withExecutor(Executor executor) {
    this.executor = executor;
    return this;
  }
  
  public XServiceClientContextBuilder withLookupClientBuilder(LookupClientBuilder lookupClientBuilder) {
    this.lookupClientBuilder = lookupClientBuilder;
    return this;
  }
  
  public XServiceClientContextBuilder withLocalAppliance(boolean isApplianceLocal) {
    this.isApplianceLocal = isApplianceLocal;
    return this;
  }
  
  public XServiceClientContextBuilder useEnvoySidecar(boolean shouldUseEnvoySidecar) {
    this.shouldUseEnvoySidecar = shouldUseEnvoySidecar;
    return this;
  }
  
  public XServiceClientContextBuilder withLocalNodeId(String localNodeId) {
    this.localNodeId = localNodeId;
    return this;
  }
  
  public XServiceClientContext build() {
    Validate.notNull("Failed to create Extension due to missing VC context.", this.vcClient);
    XServiceClientContext xServiceClientContext = new XServiceClientContext(this.vcClient);
    xServiceClientContext.setExecutor(this.executor);
    xServiceClientContext.setMaxConnections(this.maxConnections);
    xServiceClientContext.setThumbprintVerifier(this.thumbprintVerifier);
    xServiceClientContext.setTimeoutMs(this.timeoutMs);
    xServiceClientContext.setTrustStore(this.trustStore);
    xServiceClientContext.setVmodlPackages(this.vmodlPackageNameToPackageClass);
    xServiceClientContext.setxClientVmodlVersion(this.xClientVmodlVersion);
    xServiceClientContext.setLookupClientBuilder(this.lookupClientBuilder);
    xServiceClientContext.setApplianceLocal(this.isApplianceLocal);
    xServiceClientContext.setShouldUseEnvoySidecar(this.shouldUseEnvoySidecar);
    xServiceClientContext.setLocalNodeId(this.localNodeId);
    return xServiceClientContext;
  }
}
