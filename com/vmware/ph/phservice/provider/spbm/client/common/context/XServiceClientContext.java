package com.vmware.ph.phservice.provider.spbm.client.common.context;

import com.vmware.ph.phservice.common.cis.lookup.LookupClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.Executor;

public class XServiceClientContext {
  private final VcClient vcClient;
  
  private Map<String, Class<?>> vmodlPackageNameToPackageClass;
  
  private Executor executor;
  
  private Integer maxConnections;
  
  private ThumbprintVerifier thumbprintVerifier;
  
  private Integer timeoutMs;
  
  private KeyStore trustStore;
  
  private Class<?> xClientVmodlVersion;
  
  private boolean shouldUseEnvoySidecar;
  
  private String localNodeId;
  
  private LookupClientBuilder lookupClientBuilder;
  
  private boolean isApplianceLocal;
  
  public VcClient getVcClient() {
    return this.vcClient;
  }
  
  public XServiceClientContext(VcClient vcClient) {
    this.vcClient = vcClient;
  }
  
  public KeyStore getTrustStore() {
    return this.trustStore;
  }
  
  public void setTrustStore(KeyStore trustStore) {
    this.trustStore = trustStore;
  }
  
  public ThumbprintVerifier getThumbprintVerifier() {
    return this.thumbprintVerifier;
  }
  
  public void setThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this.thumbprintVerifier = thumbprintVerifier;
  }
  
  public Integer getTimeoutMs() {
    return this.timeoutMs;
  }
  
  public void setTimeoutMs(Integer timeoutMs) {
    this.timeoutMs = timeoutMs;
  }
  
  public Integer getMaxConnections() {
    return this.maxConnections;
  }
  
  public void setMaxConnections(Integer maxConnections) {
    this.maxConnections = maxConnections;
  }
  
  public Map<String, Class<?>> getVmodlPackageNameToPackageClass() {
    return this.vmodlPackageNameToPackageClass;
  }
  
  public void setVmodlPackages(Map<String, Class<?>> vmodlPackageNameToPackageClass) {
    this.vmodlPackageNameToPackageClass = vmodlPackageNameToPackageClass;
  }
  
  public Executor getExecutor() {
    return this.executor;
  }
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public boolean getShouldUseEnvoySidecar() {
    return this.shouldUseEnvoySidecar;
  }
  
  public void setShouldUseEnvoySidecar(boolean shouldUseEnvoySidecar) {
    this.shouldUseEnvoySidecar = shouldUseEnvoySidecar;
  }
  
  public String getLocalNodeId() {
    return this.localNodeId;
  }
  
  public void setLocalNodeId(String localNodeId) {
    this.localNodeId = localNodeId;
  }
  
  public LookupClientBuilder getLookupClientBuilder() {
    return this.lookupClientBuilder;
  }
  
  public void setLookupClientBuilder(LookupClientBuilder lookupClientBuilder) {
    this.lookupClientBuilder = lookupClientBuilder;
  }
  
  public boolean isApplianceLocal() {
    return this.isApplianceLocal;
  }
  
  public void setApplianceLocal(boolean isApplianceLocal) {
    this.isApplianceLocal = isApplianceLocal;
  }
  
  public Class<?> getxClientVmodlVersion() {
    return this.xClientVmodlVersion;
  }
  
  public void setxClientVmodlVersion(Class<?> xClientVmodlVersion) {
    this.xClientVmodlVersion = xClientVmodlVersion;
  }
}
