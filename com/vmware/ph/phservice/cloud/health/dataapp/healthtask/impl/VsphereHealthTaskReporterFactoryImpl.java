package com.vmware.ph.phservice.cloud.health.dataapp.healthtask.impl;

import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporter;
import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporterFactory;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import java.util.Objects;

public class VsphereHealthTaskReporterFactoryImpl implements VsphereHealthTaskReporterFactory {
  private final VimContextProvider _vimContextProvider;
  
  public VsphereHealthTaskReporterFactoryImpl(VimContextProvider vimContextProvider) {
    this._vimContextProvider = Objects.<VimContextProvider>requireNonNull(vimContextProvider);
  }
  
  public VsphereHealthTaskReporter createVsphereHealthTaskReporter(LocalizedMessageProvider localizedMessageProvider) {
    VimContextVcClientProviderImpl vimContextVcClientProviderImpl = new VimContextVcClientProviderImpl(this._vimContextProvider.getVimContext());
    return new VsphereHealthTaskReporterImpl(vimContextVcClientProviderImpl
        .getVcClient(), localizedMessageProvider);
  }
}
