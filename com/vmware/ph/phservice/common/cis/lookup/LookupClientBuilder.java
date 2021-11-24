package com.vmware.ph.phservice.common.cis.lookup;

import com.vmware.ph.phservice.common.cis.internal.lookup.LookupClientFactory;
import com.vmware.ph.phservice.common.cis.internal.lookup.impl.LookupClientFactoryImpl;
import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.lookup.version.versions;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import java.net.URI;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class LookupClientBuilder {
  private final boolean _autoDetermineVersion;
  
  private final Class<?> _versionClass;
  
  private final ExecutorService _threadPool;
  
  private URI _lookupSdkUri;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  public static LookupClientBuilder forLookup(Class<?> versionClass, LookupClientBuilder lookupClientBuilder, ExecutorService threadPool) {
    LookupClientBuilder builder = (new LookupClientBuilder(false, versionClass, threadPool)).withLookupSdkUri(lookupClientBuilder._lookupSdkUri).withTrust(lookupClientBuilder._trustStore, lookupClientBuilder._thumbprintVerifier);
    return builder;
  }
  
  public static LookupClientBuilder forAutoLookupVersion(ExecutorService threadPool) {
    LookupClientBuilder builder = new LookupClientBuilder(true, null, threadPool);
    return builder;
  }
  
  private LookupClientBuilder(boolean autoDetermineVersion, Class<?> versionClass, ExecutorService threadPool) {
    this._autoDetermineVersion = autoDetermineVersion;
    if (autoDetermineVersion) {
      this._versionClass = versionClass;
    } else {
      this
        ._versionClass = Objects.<Class<?>>requireNonNull(versionClass, "The version class must be specified when the version is not automatically looked up.");
    } 
    this._threadPool = threadPool;
  }
  
  public LookupClientBuilder withTrust(KeyStore trustStore, ThumbprintVerifier thumbprintVerifier) {
    this._trustStore = trustStore;
    this._thumbprintVerifier = thumbprintVerifier;
    return this;
  }
  
  public LookupClientBuilder withLookupSdkUri(URI lookupSdkUri) {
    this._lookupSdkUri = lookupSdkUri;
    return this;
  }
  
  public LookupClient build() {
    LookupClient lookupClient = null;
    if (this._autoDetermineVersion) {
      lookupClient = buildLookupClientForAutoLookupVersion();
    } else {
      LookupClientFactory lookupClientFactory = buildLookupClientFactory(this._versionClass, this._threadPool);
      lookupClient = lookupClientFactory.connectLookup(this._lookupSdkUri);
    } 
    return lookupClient;
  }
  
  LookupClientFactory buildLookupClientFactory(Class<?> versionClass, ExecutorService threadPool) {
    LookupClientFactoryImpl lookupClientFactory = new LookupClientFactoryImpl(versionClass, threadPool);
    lookupClientFactory.setTimeoutMs(180000);
    lookupClientFactory.setTrustStore(this._trustStore);
    lookupClientFactory.setThumbprintVerifier(this._thumbprintVerifier);
    return lookupClientFactory;
  }
  
  private LookupClient buildLookupClientForAutoLookupVersion() {
    LookupClient lookupClient = null;
    for (Class<?> versionClass : (Iterable<Class<?>>)versions.LOOKUP_VERSIONS_LIST) {
      LookupClientFactory lookupClientFactory = buildLookupClientFactory(versionClass, this._threadPool);
      lookupClient = lookupClientFactory.connectLookup(this._lookupSdkUri);
      if (isLookupVersionSupported(lookupClient))
        break; 
      lookupClient.close();
      lookupClient = null;
    } 
    return lookupClient;
  }
  
  private boolean isLookupVersionSupported(LookupClient lookupClient) {
    boolean isLookupVersionSupported;
    try {
      ServiceRegistration sr = lookupClient.getServiceRegistration();
      isLookupVersionSupported = (sr != null);
    } catch (Exception e) {
      isLookupVersionSupported = false;
    } 
    return isLookupVersionSupported;
  }
}
