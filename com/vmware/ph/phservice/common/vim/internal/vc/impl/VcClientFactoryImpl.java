package com.vmware.ph.phservice.common.vim.internal.vc.impl;

import com.vmware.ph.phservice.common.cis.sso.SsoTokenProvider;
import com.vmware.ph.phservice.common.cis.sso.SsoTokenProviderException;
import com.vmware.ph.phservice.common.vim.internal.vc.LoginHelper;
import com.vmware.ph.phservice.common.vim.internal.vc.VcClientFactory;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.vim.binding.cis.data.provider.ResourceModel;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.SessionManager;
import com.vmware.vim.binding.vim.UserSession;
import com.vmware.vim.binding.vim.fault.InvalidLocale;
import com.vmware.vim.binding.vim.fault.InvalidLogin;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.ext.InvocationContext;
import com.vmware.vim.vmomi.client.ext.RequestRetryCallback;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.client.http.ThumbprintVerifier;
import com.vmware.vim.vmomi.client.http.impl.HttpConfigurationImpl;
import com.vmware.vim.vmomi.core.RequestContext;
import com.vmware.vim.vmomi.core.Stub;
import com.vmware.vim.vmomi.core.impl.RequestContextImpl;
import com.vmware.vim.vmomi.core.security.SignInfo;
import com.vmware.vim.vmomi.core.security.impl.SignInfoImpl;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VcClientFactoryImpl implements VcClientFactory {
  private final String _namespace;
  
  private final Class<?> _versionClass;
  
  private final Executor _executor;
  
  private final VmodlContext _vmodlContext;
  
  private KeyStore _trustStore;
  
  private ThumbprintVerifier _thumbprintVerifier;
  
  private Integer _timeoutMs;
  
  private Integer _maxConnections;
  
  public VcClientFactoryImpl(Class<?> versionClass, Executor executor) {
    this(null, versionClass, executor);
  }
  
  public VcClientFactoryImpl(String namespace, Class<?> versionClass, Executor executor) {
    this._namespace = namespace;
    this._versionClass = versionClass;
    this._executor = executor;
    this
      ._vmodlContext = VmodlContextProvider.getVmodlContextForPacakgeAndClass(new HashMap<String, Class<?>>() {
        
        },  false);
  }
  
  public void setTrustStore(KeyStore trustStore) {
    this._trustStore = trustStore;
  }
  
  public void setThumbprintVerifier(ThumbprintVerifier thumbprintVerifier) {
    this._thumbprintVerifier = thumbprintVerifier;
  }
  
  public void setTimeoutMs(Integer timeoutMs) {
    this._timeoutMs = timeoutMs;
  }
  
  public void setMaxConnections(Integer maxConnections) {
    this._maxConnections = maxConnections;
  }
  
  public VcClient connectVcAsUser(URI vcSdkEndpoint, String username, char[] password, String locale) {
    LoginHelper loginHelper = new UsernameLoginHelper(username, password, locale);
    VcClient VcClient = createVcClient(vcSdkEndpoint, loginHelper);
    return VcClient;
  }
  
  public VcClient connectVcWithSamlToken(URI vcSdkEndpoint, SsoTokenProvider ssoTokenProvider, String locale) {
    LoginHelper loginHelper = new SamlTokenLoginHelper(ssoTokenProvider, locale);
    VcClient VcClient = createVcClient(vcSdkEndpoint, loginHelper);
    return VcClient;
  }
  
  public VcClient connectVcAnnonymous(URI vcSdkEndpoint, String locale) {
    VcClient VcClient = createVcClient(vcSdkEndpoint, null);
    return VcClient;
  }
  
  private VcClient createVcClient(URI vcSdkUri, LoginHelper loginHelper) {
    VcRequestRetryCallbackImpl requestRetryCallback = new VcRequestRetryCallbackImpl(loginHelper);
    Client vmomiClient = createVmomiClient(vcSdkUri, requestRetryCallback);
    VcClient vcClient = new VcClientImpl(vmomiClient, this._namespace, this._versionClass, this._vmodlContext, Optional.ofNullable(loginHelper));
    requestRetryCallback.setClient(vcClient);
    return vcClient;
  }
  
  private Client createVmomiClient(URI vcSdkUri, RequestRetryCallback requestRetryCallback) {
    HttpConfigurationImpl httpConfigurationImpl = new HttpConfigurationImpl();
    httpConfigurationImpl.setTrustStore(this._trustStore);
    httpConfigurationImpl.setThumbprintVerifier(this._thumbprintVerifier);
    httpConfigurationImpl.setCheckStaleConnection(true);
    if (this._timeoutMs != null)
      httpConfigurationImpl.setTimeoutMs(this._timeoutMs.intValue()); 
    if (this._maxConnections != null)
      httpConfigurationImpl.setMaxConnections(this._maxConnections.intValue()); 
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    clientConfig.setExecutor(this._executor);
    clientConfig.setHttpConfiguration((HttpConfiguration)httpConfigurationImpl);
    clientConfig.setRequestRetryCallback(requestRetryCallback);
    Client vmomiClient = Client.Factory.createClient(vcSdkUri, this._versionClass, this._vmodlContext, (ClientConfiguration)clientConfig);
    return vmomiClient;
  }
  
  private static class UsernameLoginHelper implements LoginHelper {
    private String _username;
    
    private char[] _password;
    
    private String _locale;
    
    public UsernameLoginHelper(String username, char[] password, String locale) {
      this._username = username;
      this._password = password;
      this._locale = locale;
    }
    
    public UserSession login(SessionManager sessionManager) throws InvalidLogin, InvalidLocale {
      return sessionManager.login(this._username, new String(this._password), this._locale);
    }
  }
  
  private static class SamlTokenLoginHelper implements LoginHelper {
    private SsoTokenProvider _ssoTokenProvider;
    
    private String _locale;
    
    public SamlTokenLoginHelper(SsoTokenProvider ssoTokenProvider, String locale) {
      this._ssoTokenProvider = ssoTokenProvider;
      this._locale = locale;
    }
    
    public UserSession login(SessionManager sessionManager) throws SsoTokenProviderException, InvalidLogin, InvalidLocale {
      SsoTokenProvider.TokenKeyPair tokenKeyPair = this._ssoTokenProvider.getToken();
      RequestContextImpl ctx = new RequestContextImpl();
      SignInfoImpl authInfo = new SignInfoImpl(tokenKeyPair.key, tokenKeyPair.token);
      ctx.setSignInfo((SignInfo)authInfo);
      ((Stub)sessionManager)._setRequestContext((RequestContext)ctx);
      try {
        UserSession userSession = sessionManager.loginByToken(this._locale);
        return userSession;
      } finally {
        ((Stub)sessionManager)._setRequestContext(null);
      } 
    }
  }
  
  private static class VcRequestRetryCallbackImpl implements RequestRetryCallback {
    private static final Log _logger = LogFactory.getLog(VcRequestRetryCallbackImpl.class);
    
    private LoginHelper _loginHelper;
    
    private VcClient _client;
    
    public VcRequestRetryCallbackImpl(LoginHelper loginHelper) {
      this._loginHelper = loginHelper;
    }
    
    public void setClient(VcClient client) {
      this._client = client;
    }
    
    public boolean retry(Exception exception, InvocationContext context, int count) {
      if (!(exception instanceof com.vmware.vim.binding.vim.fault.NotAuthenticated))
        return false; 
      if (context.getMethod().getName().equals("logout"))
        return false; 
      if (count > 1)
        return false; 
      if (this._client != null && this._loginHelper != null)
        try {
          this._loginHelper.login(this._client.getSessionManager());
        } catch (SsoTokenProviderException ssoe) {
          Throwable e = ssoe.getCause();
          _logger.error(e.getMessage(), e);
        } catch (Exception e) {
          _logger.error(e.getMessage(), e);
        }  
      return true;
    }
  }
}
