package com.vmware.ph.phservice.cloud.health.vmomi;

import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientBuilder;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.ph.phservice.common.vmomi.internal.server.VmomiUtil;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vmomi.server.Activation;
import com.vmware.vim.vmomi.server.ServerFuture;
import java.util.Locale;

public class VcHealthSystemMo extends HealthSystemMo {
  private final VimContextProvider _vimContextProvider;
  
  private final Object _lock = new Object();
  
  private String _vcGuid;
  
  public VcHealthSystemMo(HealthSystem healthSystem, VimContextProvider vimContextProvider) {
    super(healthSystem);
    this._vimContextProvider = vimContextProvider;
  }
  
  protected ManagedObjectReference addVcGuidToMoRef(ManagedObjectReference moRef) {
    String vcGuid = getVcGuid();
    ManagedObjectReference newMoRef = new ManagedObjectReference(moRef.getType(), moRef.getValue(), vcGuid);
    return newMoRef;
  }
  
  protected Locale getCallerLocale(Future<?> serverFuture) {
    Locale resultLocale = Locale.ENGLISH;
    if (serverFuture instanceof ServerFuture) {
      Activation serverActivation = ((ServerFuture)serverFuture).getActivation();
      String callerSessionCookie = VmomiUtil.getCookieFromRequest("Cookie", serverActivation);
      VcClientProvider vcClientProvider = VcVmomiUtil.getVcClientProvider(this._vimContextProvider);
      String messageLocale = VcVmomiUtil.getMessageLocaleForVcSession(callerSessionCookie, vcClientProvider);
      if (messageLocale != null)
        resultLocale = Locale.forLanguageTag(messageLocale); 
    } 
    return resultLocale;
  }
  
  protected String getSessionUser(Future<?> serverFuture) {
    return VcVmomiUtil.getSessionUser(serverFuture, this._vimContextProvider);
  }
  
  private String getVcGuid() {
    if (this._vcGuid != null)
      return this._vcGuid; 
    synchronized (this._lock) {
      if (this._vcGuid == null) {
        VimContext vimContext = this._vimContextProvider.getVimContext();
        if (vimContext != null) {
          VcClientBuilder vcClientBuilder = this._vimContextProvider.getVimContext().getVcClientBuilder();
          try (VcClient vcClient = vcClientBuilder.build()) {
            this
              ._vcGuid = vcClient.getServiceInstanceContent().getAbout().getInstanceUuid();
          } 
        } 
      } 
    } 
    return this._vcGuid;
  }
}
