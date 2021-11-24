package com.vmware.ph.phservice.common.vim;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClient;
import com.vmware.ph.phservice.common.vmomi.client.VmomiClientFactory;
import com.vmware.ph.phservice.common.vmomi.client.impl.VmomiClientFactoryImpl;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EsxContext {
  private static final Log _log = LogFactory.getLog(EsxContext.class);
  
  private final KeyStore _vimTrustStore;
  
  private ThumbprintVerifier _vimThumbprintVerifier;
  
  private final URI _sdkUri;
  
  private Class<?> _versionClass;
  
  private final String _username;
  
  private final char[] _password;
  
  public EsxContext(KeyStore vimTrustStore, URI sdkUri, Class<?> versionClass, String username, char[] password) {
    this._vimTrustStore = vimTrustStore;
    this._sdkUri = sdkUri;
    this._versionClass = versionClass;
    this._username = username;
    this._password = password;
  }
  
  public void setVimThumbprintVerifier(ThumbprintVerifier vimThumbprintVerifier) {
    this._vimThumbprintVerifier = vimThumbprintVerifier;
  }
  
  public KeyStore getTrustStore() {
    return this._vimTrustStore;
  }
  
  public Builder<VmomiClient> getEsxClientBuilder() {
    VmodlContext vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass(new HashMap<String, Class<?>>() {
          private static final long serialVersionUID = 1L;
        },  false);
    VmomiClientFactoryImpl factory = new VmomiClientFactoryImpl(this._versionClass, vmodlContext, Executors.newSingleThreadExecutor());
    factory.setTrustStore(this._vimTrustStore);
    factory.setThumbprintVerifier(this._vimThumbprintVerifier);
    EsxClientBuilder esxClientBuilder = new EsxClientBuilder(factory, this._sdkUri, this._username, this._password);
    return esxClientBuilder;
  }
  
  private static class EsxClientBuilder implements Builder<VmomiClient> {
    private final VmomiClientFactory _factory;
    
    private final URI _sdkUri;
    
    private final String _username;
    
    private final char[] _password;
    
    public EsxClientBuilder(VmomiClientFactory factory, URI sdkUri, String username, char[] password) {
      this._factory = factory;
      this._sdkUri = sdkUri;
      this._username = username;
      this._password = password;
    }
    
    public VmomiClient build() {
      VmomiClient vmomiClient = this._factory.create(this._sdkUri);
      try {
        ServiceInstance si = vmomiClient.<ServiceInstance>createStub(VimVmodlUtil.SERVICE_INSTANCE_MOREF);
        SessionManager sessionManager = vmomiClient.<SessionManager>createStub(si.getContent().getSessionManager());
        sessionManager.login(this._username, new String(this._password), null);
      } catch (Exception e) {
        EsxContext._log.warn("Unable to authenticate ESX client: " + e.getMessage());
      } 
      return vmomiClient;
    }
  }
}
